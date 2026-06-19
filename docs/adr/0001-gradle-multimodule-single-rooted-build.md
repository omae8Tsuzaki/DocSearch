# ADR 0001: Gradle マルチモジュールをルート単一ビルドに統合する

- ステータス: Accepted
- 日付: 2026-06-20

## コンテキスト

`DocSearch` をマルチモジュール構成にするにあたり、`core`（Spring 非依存の最基底）と `domain`（ビジネス層）を作成した。当初、各モジュールはそれぞれ独立した Gradle プロジェクトとして作られており、以下を個別に持っていた。

- `core/settings.gradle.kts`（`rootProject.name = "core"`）/ `domain/settings.gradle.kts`
- モジュールごとの Gradle ラッパー（`core/gradlew*`, `domain/gradlew*`）と `.gradle` キャッシュ
- 各モジュール独立の `*.iml`

ルート `settings.gradle.kts` には `include(...)` が無く、Gradle はモジュールをサブプロジェクトとして認識していなかった。

この状態で IntelliJ の Gradle 同期、および `core/build.gradle.kts` のエディタ上で次のエラーが発生した。

```
Task 'prepareKotlinBuildScriptModel' not found in project ':core'.
```

```
Unresolved reference 'testImplementation'.   // core/build.gradle.kts の dependencies ブロック
```

### 原因

`build.gradle.kts` の記述ミスではなく、IntelliJ が `core` / `domain` を依然として「独立した Gradle ルートプロジェクト」として認識していたことが原因。`.idea/gradle.xml` の `linkedExternalProjectsSettings` が **`$PROJECT_DIR$/core` と `$PROJECT_DIR$/domain` の2つを別々のルート**としてリンクし、ルート `$PROJECT_DIR$`（DocSearch）をリンクしていなかった。

エラーの連鎖は以下のとおり。

1. IntelliJ が `:core` を単独ルートとして同期しようとする
2. 統合後の `core` は `settings.gradle.kts` を持たない（= 単独ビルドではない）ため `prepareKotlinBuildScriptModel` が存在せず「Task not found in project ':core'」で失敗
3. 同期失敗によりビルドスクリプトのクラスパスが未解決となり、`java` プラグインが提供する `testImplementation` 等が Unresolved になる（同期失敗の二次症状）

### 切り分けの根拠

CLI から直接ビルドすると正常に解決できた。これにより「ビルドスクリプトは正しく、問題は IDE 側のリンク状態」と特定した。

```bash
./gradlew :core:dependencies --configuration testRuntimeClasspath -q
# → junit が version catalog 経由で正常に解決される
```

「IDE では失敗するが CLI（`./gradlew`）では成功する」場合、原因はビルドスクリプトではなく IDE の Gradle リンク／キャッシュにある。

## 決定

`core` / `domain` を個別の Gradle ビルドにせず、**ルート `DocSearch` を唯一のビルドとした単一ルートのマルチモジュール構成**を採用する。具体的には次を満たす。

- `settings.gradle.kts`・Gradle ラッパー・`.gradle` キャッシュは **ルートに1組だけ** 置く。サブモジュールに複製を残さない。
- ルート `settings.gradle.kts` で `include("core", "domain")` によりサブプロジェクトを宣言する。
- IDE の Gradle リンクはルート `$PROJECT_DIR$` 1本とする。

### 適用した対処

**1. `.idea/gradle.xml` をルート1本のリンクに修正**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="GradleMigrationSettings" migrationVersion="1" />
  <component name="GradleSettings">
    <option name="linkedExternalProjectsSettings">
      <GradleProjectSettings>
        <option name="externalProjectPath" value="$PROJECT_DIR$" />
        <option name="gradleJvm" value="21" />
        <option name="modules">
          <set>
            <option value="$PROJECT_DIR$" />
            <option value="$PROJECT_DIR$/core" />
            <option value="$PROJECT_DIR$/domain" />
          </set>
        </option>
      </GradleProjectSettings>
    </option>
  </component>
</project>
```

**2. 独立ビルド時代の残骸を削除**

```bash
# 入れ子の Gradle キャッシュ
rm -rf core/.gradle domain/.gradle

# モジュール内に複製された Gradle ラッパー（ラッパーはルートに1つだけで良い）
rm -f core/gradlew core/gradlew.bat domain/gradlew domain/gradlew.bat

# 旧 IntelliJ モジュールファイル（Gradle 再同期で自動再生成される）
rm -f core.iml domain.iml \
      .idea/modules/core.main.iml .idea/modules/core.test.iml \
      .idea/modules/domain.main.iml .idea/modules/domain.test.iml
```

> 各モジュールの `core/settings.gradle.kts` / `domain/settings.gradle.kts` も削除済みであること。

**3. IntelliJ で Gradle を再読み込み**

- Gradle ツールウィンドウ右上の **🔄 Reload All Gradle Projects**
- 解消しない場合は **File → Invalidate Caches → Invalidate and Restart**

### 確認方法

```bash
./gradlew projects
```

```
Root project 'DocSearch'
+--- Project ':core'
\--- Project ':domain'
```

再同期後、`.idea/modules.xml` が `DocSearch` + `DocSearch.core.*` + `DocSearch.domain.*` の構成で再生成されていれば IDE 側も正常。

## 結果

### 良い影響

- `./gradlew projects` でルート配下に `:core` / `:domain` が正しく認識され、IDE のエディタ警告も解消した。
- ビルド設定・ラッパー・キャッシュがルートに一元化され、モジュール追加時（将来の `app` 等）は `include(...)` に1語追加するだけで済む。

### 注意・トレードオフ

- 既存の単独プロジェクトを後からサブモジュール化する場合、ファイル削除だけでは不十分で、**`.idea/gradle.xml` のリンク状態（どこをルートとして登録しているか）も併せて確認・修正**する必要がある。
- IDE と CLI で挙動が食い違う場合は、まず CLI（`./gradlew`）で切り分け、IDE 固有のキャッシュ／リンクを疑う運用とする。
