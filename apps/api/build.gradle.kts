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

val skipWeb = providers.gradleProperty("skipWeb").map(String::toBoolean).orElse(false)
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
