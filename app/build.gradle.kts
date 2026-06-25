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
// JRE同梱の配布物生成（jlink でフルJRE → jpackage で exe インストーラ）
//   Java 未インストールのPCでも、生成された exe をダブルクリックでインストールできる。
//   インストール後はスタートメニューから起動でき、アンインストーラも付く。
//   実行: ./gradlew :app:jpackageInstaller
//   ※ ビルドするPCに WiX Toolset v3.14 が必要（candle.exe / light.exe に PATH を通す）。
//      配布先の利用者には WiX は不要。
// =====================================================================
val distAppName = "DocSearch"
val distAppVersion = "1.0.0" // jpackage は数値形式（major.minor.patch）が必要
val distVendor = "com.example"
// 上書き更新を識別するための固定UUID。一度決めたら以後は絶対に変更しないこと。
val distUpgradeUuid = "d9830b14-acc5-47b6-a6be-3d836478a35c"

val javaToolchainService = extensions.getByType<JavaToolchainService>()
val jdkLauncher = javaToolchainService.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(21))
}
fun jdkHome() = jdkLauncher.get().metadata.installationPath.asFile
fun jdkTool(tool: String) = jdkHome().resolve("bin").resolve(tool).absolutePath

val runtimeImageDir = layout.buildDirectory.dir("jpackage/runtime")
val jpackageInputDir = layout.buildDirectory.dir("jpackage/input")
val jpackageInstallerDir = layout.buildDirectory.dir("dist")
val jpackageAppImageDir = layout.buildDirectory.dir("jpackage/app-image")

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

val jpackageInstaller by tasks.registering(Exec::class) {
    group = "distribution"
    description = "JRE同梱の exe インストーラを jpackage で生成する"
    dependsOn(jlinkRuntime, prepareJpackageInput)
    val out = jpackageInstallerDir.get().asFile
    val mainJar = tasks.named<BootJar>("bootJar").get().archiveFileName.get()
    doFirst { out.mkdirs() }
    commandLine(
        jdkTool("jpackage.exe"),
        "--type", "exe",
        "--name", distAppName,
        "--app-version", distAppVersion,
        "--vendor", distVendor,
        "--input", jpackageInputDir.get().asFile.absolutePath,
        "--main-jar", mainJar,
        "--main-class", "org.springframework.boot.loader.launch.JarLauncher",
        "--runtime-image", runtimeImageDir.get().asFile.absolutePath,
        "--dest", out.absolutePath,
        // ユーザー単位インストール（管理者権限不要）
        "--win-per-user-install",
        // スタートメニューに登録
        "--win-menu",
        "--win-menu-group", distAppName,
        // 上書き更新（再インストールで旧版を置換）を可能にする
        "--win-upgrade-uuid", distUpgradeUuid,
        // コンソール窓は表示しない（ログはファイル出力で追う。application.yml 参照）
        "--java-options", "-Dfile.encoding=UTF-8"
        // アイコンは後日追加: "--icon", "<path-to>.ico"
    )
}

// ---------------------------------------------------------------------
// ポータブル版（インストール不要・ZIP配布）
//   jpackage の app-image を生成し、zip にまとめる。
//   利用者は zip を解凍して DocSearch\DocSearch.exe を実行するだけ。
//   インストール不要・管理者権限不要・WiX 不要。
//   実行: ./gradlew :app:packagePortableZip
// ---------------------------------------------------------------------
val jpackageAppImage by tasks.registering(Exec::class) {
    group = "distribution"
    description = "JRE同梱のポータブルアプリ（app-image）を jpackage で生成する"
    dependsOn(jlinkRuntime, prepareJpackageInput)
    val out = jpackageAppImageDir.get().asFile
    val mainJar = tasks.named<BootJar>("bootJar").get().archiveFileName.get()
    // jpackage は出力先に同名フォルダがあるとエラーになるため作り直す
    doFirst { out.deleteRecursively(); out.mkdirs() }
    commandLine(
        jdkTool("jpackage.exe"),
        "--type", "app-image",
        "--name", distAppName,
        "--app-version", distAppVersion,
        "--vendor", distVendor,
        "--input", jpackageInputDir.get().asFile.absolutePath,
        "--main-jar", mainJar,
        "--main-class", "org.springframework.boot.loader.launch.JarLauncher",
        "--runtime-image", runtimeImageDir.get().asFile.absolutePath,
        "--dest", out.absolutePath,
        // コンソール窓は表示しない（ログはファイル出力で追う。application.yml 参照）
        "--java-options", "-Dfile.encoding=UTF-8"
        // アイコンは後日追加: "--icon", "<path-to>.ico"
    )
}

val packagePortableZip by tasks.registering(Zip::class) {
    group = "distribution"
    description = "ポータブル版（app-image）を配布用 zip にまとめる"
    dependsOn(jpackageAppImage)
    // app-image は <dest>/<name>/ 配下に生成される。解凍時に DocSearch フォルダごと展開されるよう含める。
    from(jpackageAppImageDir.get().dir(distAppName))
    into(distAppName)
    archiveFileName.set("$distAppName-$distAppVersion-portable-win-x64.zip")
    destinationDirectory.set(jpackageInstallerDir)
}
