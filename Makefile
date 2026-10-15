COMPOSE := docker compose -f infra/docker/compose.yaml --env-file .env
TF_LIVE := infra/terraform/live
TF_BOOT := infra/terraform/bootstrap
ENV ?= dev

.PHONY: init jars up up-core down reset logs test verify e2e image lint tf-fmt tf-validate tf-plan screenshots bundle

init:
	@test -f .env || cp .env.example .env
	@chmod +x infra/docker/minio-init.sh

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
	./gradlew test

verify:
	./gradlew verify

e2e:
	cd e2e && npm ci && npx playwright test

image: jars
	docker build -f infra/docker/Dockerfile.app -t brief3/app:$${IMAGE_TAG:-local} .

lint:
	./gradlew spotlessCheck
	cd apps/web && npm ci && npm run lint
	terraform -chdir=$(TF_LIVE) fmt -check -recursive
	terraform -chdir=$(TF_BOOT) fmt -check -recursive

tf-fmt:
	terraform -chdir=$(TF_LIVE) fmt -recursive
	terraform -chdir=$(TF_BOOT) fmt -recursive

tf-validate:
	terraform -chdir=$(TF_BOOT) init -backend=false
	terraform -chdir=$(TF_BOOT) validate
	terraform -chdir=$(TF_LIVE) init -backend=false
	terraform -chdir=$(TF_LIVE) validate

tf-plan:
	terraform -chdir=$(TF_LIVE) init -input=false
	terraform -chdir=$(TF_LIVE) workspace select $(ENV)
	terraform -chdir=$(TF_LIVE) plan -var-file=env/$(ENV).tfvars -out=$(ENV).tfplan

screenshots:
	cd e2e && npm ci && npx playwright test --project=screenshots

bundle:
	git bundle create memorial-ops.bundle --all
