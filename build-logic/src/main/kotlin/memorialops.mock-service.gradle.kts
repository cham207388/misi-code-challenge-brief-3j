plugins {
    id("memorialops.spring-app")
}

dependencies {
    implementation(project(":mocks:mock-common"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
