plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:4.1.1")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    implementation("com.github.node-gradle:gradle-node-plugin:7.1.0")
}
