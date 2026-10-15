plugins {
    alias(libs.plugins.spotless)
}

allprojects {
    group = "com.wsllclabs.memorialops"
    version = "0.1.0"
}

spotless {
    java {
        target("apps/*/src/**/*.java", "mocks/*/src/**/*.java")
        palantirJavaFormat(libs.versions.palantir.java.format.get())
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts", "gradle/*.gradle.kts", "build-logic/**/*.gradle.kts", "apps/**/*.gradle.kts", "mocks/**/*.gradle.kts")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.register("verify") {
    group = "verification"
    description = "Format check, unit tests, integration tests and JaCoCo gates (make verify)."
    dependsOn(
        "spotlessCheck",
        "test",
        ":apps:api:integrationTest",
        ":apps:api:jacocoTestCoverageVerification",
    )
}
