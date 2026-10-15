plugins {
    alias(libs.plugins.node)
}

node {
    version.set("24.21.0")
    download.set(false)
    nodeProjectDir.set(layout.projectDirectory)
}

val skipWeb = providers.gradleProperty("skipWeb").map { it.isBlank() || it.toBoolean() }.orElse(false)

val npmCi =
    tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmCi") {
        onlyIf { !skipWeb.get() }
        args.set(listOf("ci"))
        inputs.files(layout.projectDirectory.file("package.json"), layout.projectDirectory.file("package-lock.json"))
        outputs.dir(layout.projectDirectory.dir("node_modules"))
    }

val npmBuild =
    tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmBuild") {
        onlyIf { !skipWeb.get() }
        dependsOn(npmCi)
        args.set(listOf("run", "build"))
        inputs.dir(layout.projectDirectory.dir("src"))
        inputs.files(
            layout.projectDirectory.file("package.json"),
            layout.projectDirectory.file("package-lock.json"),
            layout.projectDirectory.file("vite.config.ts"),
            layout.projectDirectory.file("tsconfig.json"),
            layout.projectDirectory.file("index.html"),
        )
        outputs.dir(layout.projectDirectory.dir("dist"))
    }

tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmTest") {
    onlyIf { !skipWeb.get() }
    dependsOn(npmCi)
    args.set(listOf("run", "test"))
}

tasks.register("assemble") {
    dependsOn(npmBuild)
}

tasks.register("check") {
    dependsOn("npmTest")
}

tasks.register("test") {
    dependsOn("npmTest")
}
