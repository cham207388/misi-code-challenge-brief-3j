pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "memorial-ops"

include("apps:api")
include("apps:web")
include("mocks:mock-common")
include("mocks:applicant-registry")
include("mocks:regional-calendar")
include("mocks:notification-gateway")
include("mocks:weather-advisory")
include("mocks:geocoding")
include("mocks:partner-feed")
