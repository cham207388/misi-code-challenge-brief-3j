plugins {
    id("memorialops.java-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// Boot 4.1.2 is not out yet; pin the Trivy-fixed Tomcat and Jackson lines.
extra["tomcat.version"] = "11.0.25"
extra["jackson-bom.version"] = "3.1.7"
extra["jackson-2-bom.version"] = "2.21.7"

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    layered {
        enabled = true
    }
}
