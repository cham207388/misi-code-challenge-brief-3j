plugins {
    id("memorialops.spring-app")
}

dependencies {
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.session.jdbc)
    implementation(libs.flyway.postgres)
    implementation(libs.postgres)
    implementation(libs.jspecify)
    implementation(libs.modulith.starter.jdbc)
    implementation(libs.modulith.actuator)
    implementation(libs.springdoc.ui)
    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.security.test)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
    testImplementation(libs.spring.boot.starter.flyway.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.modulith.test)
    testImplementation(libs.archunit.junit6)
}

val skipWeb =
    providers.gradleProperty("skipWeb").map { it.isBlank() || it.toBoolean() }.orElse(false)
val webDist = project(":apps:web").layout.projectDirectory.dir("dist")

tasks.named<ProcessResources>("processResources") {
    if (!skipWeb.get()) {
        dependsOn(":apps:web:npmBuild")
    }
    from(webDist) {
        into("static")
    }
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    args("--spring.profiles.active=local")
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

val integrationTest =
    tasks.register<Test>("integrationTest") {
        description = "Runs Testcontainers integration tests against a reusable Postgres."
        group = "verification"
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        shouldRunAfter(tasks.test)
        useJUnitPlatform {
            includeTags("integration")
        }
    }

tasks.named("check") {
    dependsOn(integrationTest)
}

val jacocoExec =
    files(
        layout.buildDirectory.file("jacoco/test.exec"),
        layout.buildDirectory.file("jacoco/integrationTest.exec"),
    )

tasks.jacocoTestReport {
    dependsOn(tasks.test, integrationTest)
    executionData.setFrom(jacocoExec)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    executionData.setFrom(jacocoExec)
    violationRules {
        rule {
            element = "PACKAGE"
            includes =
                listOf(
                    "com.wsllclabs.memorialops.identity.service",
                    "com.wsllclabs.memorialops.identity.controller",
                    "com.wsllclabs.memorialops.identity.dto",
                    "com.wsllclabs.memorialops.exception",
                    "com.wsllclabs.memorialops.shared.health",
                    "com.wsllclabs.memorialops.application",
                    "com.wsllclabs.memorialops.workflow",
                    "com.wsllclabs.memorialops.audit",
                    "com.wsllclabs.memorialops.shared.reference",
                )
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
