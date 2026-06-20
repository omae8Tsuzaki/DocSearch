import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("java")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.example.docsearch"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))

    // Spring Boot
    implementation(libs.spring.boot.starter.web)

    testImplementation(libs.spring.boot.starter.test)
}

tasks.test {
    useJUnitPlatform()
}

// =====================================================================
// JRE同梱の配布物生成（jlink でフルJRE → jpackage でポータブル app-image）
//   Java 未インストールのPCでも、生成フォルダ内の DocSearch.exe で起動できる。
//   実行: ./gradlew :app:packageZip
// =====================================================================
val distAppName = "DocSearch"
val distAppVersion = "1.0.0" // jpackage は数値形式（major.minor.patch）が必要

val javaToolchainService = extensions.getByType<JavaToolchainService>()
val jdkLauncher = javaToolchainService.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(21))
}
fun jdkHome() = jdkLauncher.get().metadata.installationPath.asFile
fun jdkTool(tool: String) = jdkHome().resolve("bin").resolve(tool).absolutePath

val runtimeImageDir = layout.buildDirectory.dir("jpackage/runtime")
val jpackageInputDir = layout.buildDirectory.dir("jpackage/input")
val jpackageDistDir = layout.buildDirectory.dir("jpackage/dist")

val jlinkRuntime by tasks.registering(Exec::class) {
    group = "distribution"
    description = "全モジュールを含むフルJREを jlink で生成する"
    val out = runtimeImageDir.get().asFile
    // jlink は出力先が存在するとエラーになるため事前に削除
    doFirst { out.deleteRecursively() }
    commandLine(
        jdkTool("jlink.exe"),
        "--module-path", jdkHome().resolve("jmods").absolutePath,
        "--add-modules", "ALL-MODULE-PATH",
        "--strip-debug", "--no-header-files", "--no-man-pages",
        "--output", out.absolutePath
    )
}

val prepareJpackageInput by tasks.registering(Copy::class) {
    group = "distribution"
    description = "jpackage 入力（実行可能JARのみ）を用意する"
    dependsOn("bootJar")
    val input = jpackageInputDir.get().asFile
    doFirst { input.deleteRecursively() }
    from(tasks.named<BootJar>("bootJar").flatMap { it.archiveFile })
    into(jpackageInputDir)
}

val jpackageImage by tasks.registering(Exec::class) {
    group = "distribution"
    description = "JRE同梱のポータブルアプリ（app-image）を jpackage で生成する"
    dependsOn(jlinkRuntime, prepareJpackageInput)
    val out = jpackageDistDir.get().asFile
    val mainJar = tasks.named<BootJar>("bootJar").get().archiveFileName.get()
    // jpackage は出力先に同名フォルダがあるとエラーになるため作り直す
    doFirst { out.deleteRecursively(); out.mkdirs() }
    commandLine(
        jdkTool("jpackage.exe"),
        "--type", "app-image",
        "--name", distAppName,
        "--app-version", distAppVersion,
        "--input", jpackageInputDir.get().asFile.absolutePath,
        "--main-jar", mainJar,
        "--main-class", "org.springframework.boot.loader.launch.JarLauncher",
        "--runtime-image", runtimeImageDir.get().asFile.absolutePath,
        "--dest", out.absolutePath,
        "--win-console",
        "--java-options", "-Dfile.encoding=UTF-8"
    )
}

val packageZip by tasks.registering(Zip::class) {
    group = "distribution"
    description = "app-image を配布用 zip にまとめる"
    dependsOn(jpackageImage)
    from(jpackageDistDir.get().dir(distAppName))
    into(distAppName)
    archiveFileName.set("$distAppName-$distAppVersion-win-x64.zip")
    destinationDirectory.set(layout.buildDirectory.dir("dist"))
}
