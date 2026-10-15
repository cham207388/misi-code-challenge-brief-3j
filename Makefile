COMPOSE := docker compose -f infra/docker/compose.yaml --env-file .env
TF_LIVE := infra/terraform/live
TF_BOOT := infra/terraform/bootstrap
ENV ?= dev
IMAGE_TAG ?= local
AWS_REGION ?= us-east-1
ACCOUNT_ID ?= $(shell aws sts get-caller-identity --query Account --output text 2>/dev/null)
ECR_REGISTRY ?= $(ACCOUNT_ID).dkr.ecr.$(AWS_REGION).amazonaws.com

.PHONY: init jars up up-core down reset logs test verify e2e deploy-smoke image ecr-login image-push trivy lint tf-fmt tf-validate tf-plan tf-apply screenshots bundle

init:
	@test -f .env || cp .env.example .env
	@chmod +x infra/docker/rustfs-init.sh

jars:
	./gradlew bootJar

up: init jars
	$(COMPOSE) --profile full up -d --build

up-core: init jars
	$(COMPOSE) --profile core up -d --build

down:
	$(COMPOSE) --profile full --profile core --profile mocks down

reset: down
	$(COMPOSE) --profile full down -v

logs:
	$(COMPOSE) logs -f $(SERVICE)

test:
	./gradlew test integrationTest

verify:
	./gradlew verify

e2e:
	cd e2e && npm ci && npx playwright install --with-deps chromium && npx playwright test --project=chromium

deploy-smoke:
	cd e2e && npm ci && npx playwright install --with-deps chromium && npx playwright test --project=chromium deploy-smoke.spec.ts

image:
	docker build -f infra/docker/Dockerfile.app -t brief3/app:$(IMAGE_TAG) .

ecr-login:
	aws ecr get-login-password --region $(AWS_REGION) | docker login --username AWS --password-stdin $(ECR_REGISTRY)

image-push: ecr-login
	docker tag brief3/app:$(IMAGE_TAG) $(ECR_REGISTRY)/brief3/app:$(IMAGE_TAG)
	docker push $(ECR_REGISTRY)/brief3/app:$(IMAGE_TAG)

trivy:
	trivy image --exit-code 1 --severity HIGH,CRITICAL brief3/app:$(IMAGE_TAG)

lint:
	./gradlew spotlessCheck
	cd apps/web && npm ci && npm run lint
	terraform fmt -check -recursive infra/terraform

tf-fmt:
	terraform fmt -recursive infra/terraform

tf-validate:
	terraform -chdir=$(TF_BOOT) init -backend=false
	terraform -chdir=$(TF_BOOT) validate
	terraform -chdir=$(TF_LIVE) init -backend=false
	terraform -chdir=$(TF_LIVE) validate

tf-plan:
	terraform -chdir=$(TF_LIVE) init -input=false
	terraform -chdir=$(TF_LIVE) workspace select -or-create $(ENV)
	terraform -chdir=$(TF_LIVE) plan -var-file=env/$(ENV).tfvars -var image_tag=$(IMAGE_TAG) -out=$(ENV).tfplan

tf-apply:
	terraform -chdir=$(TF_LIVE) apply $(ENV).tfplan

screenshots:
	cd e2e && npm ci && npx playwright install --with-deps chromium && npx playwright test --project=screenshots

bundle:
	git bundle create memorial-ops.bundle --all
