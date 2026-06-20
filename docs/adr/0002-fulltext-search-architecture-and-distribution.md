# ADR 0002: 全文検索システムのアーキテクチャと JRE 同梱配布

- ステータス: Accepted
- 日付: 2026-06-20

## コンテキスト

利用者が自分のPCのフォルダ（複数指定可）を画面から指定し、その配下のファイルを検索できるシステムを作る。対象には git 管理のマークダウンや、NAS 上の pptx / xlsx / docx などが含まれる。次の前提・制約がある。

- **デプロイしない**。利用者が各自のローカルPCで起動して使う。
- 起動するPCに **Java はインストールされていない**。
- 既存の Gradle マルチモジュール構成（`app → domain → core`、ADR 0001）を踏襲する。

着手前に方針を確認し、以下に決定した。

- 検索の種類: **全文検索**（ファイル名だけでなく本文も対象）
- 検索範囲: **再帰**（サブフォルダを含む）
- 配布形態: **JRE 同梱の実行ファイル**（Java 不要で起動できること）
- フォルダ指定UI: **ディレクトリブラウザ**（画面で階層をたどって選択）

## 決定

### 1. 全体構成（ローカル Web アプリ）

`app` を起動すると内蔵 Web サーバ（Spring Boot）が立ち上がり、ブラウザで `localhost:8080` の画面を操作する。サーバとブラウザが同一PCのため、画面で指定したパスをそのままサーバ側のファイルパスとして扱える。

### 2. モジュール配置（機能固有のものは domain、core は汎用のみ）

- 検索特有のモデル（`SearchHit` / `DirectoryEntry` / `IndexStatus` / `IndexResult`）と業務ロジックは **すべて `domain`** に置く。`core` は Spring 非依存の汎用モデル/ユーティリティに限定する（ADR 0001・AGENTS.md の方針を維持）。
- **`domain` は Spring に依存してよい**こととした（本セッションで制約を緩和）。domain のサービスは Spring ステレオタイプ（`@Component` / `@Service`）で直接 DI 配線し、`app` 側に配線用の `@Configuration` を置かない。Lucene 等のリソース後始末は `DisposableBean#destroy()` で行う。
  - ただし **`core` は引き続き Spring 非依存**を厳守する。

### 3. 全文検索エンジン: Apache Lucene + Tika

- 検索・索引は **Apache Lucene 10.1.0**。日本語の分かち書きのため **kuromoji（`JapaneseAnalyzer`）** を採用。
- 本文抽出は **Apache Tika 3.1.0**（`tika-parsers-standard-package`）。pptx / xlsx / docx / pdf / md / txt 等を統一的に抽出する（内部で POI / PDFBox を使用）。
- 検索は本文（`content`）とファイル名（`filename`）を `MultiFieldQueryParser` で横断（ファイル名をブースト）。既定演算子は AND。ユーザー入力は `QueryParser.escape` でプレーン語として扱う。
- 一致箇所はハイライタで本文抜粋（スニペット）を生成。`<mark>` 強調は **ASCII センチネル文字列でマークしてから HTML エスケープし、最後に `<mark>` へ置換**することで、本文中の `< > &` による崩れ・XSS を防ぐ。

### 4. ディレクトリブラウザ（サーバ側走査）

ブラウザの仕様上、フォルダ選択ダイアログから実パスは取得できない。同一PCで動くローカルアプリである利点を活かし、**サーバ側でドライブ／サブフォルダ一覧を返す API** を用意し、画面から階層をたどって選択する方式とした。

### 5. データ保存先と索引運用

- 設定・索引は `%LOCALAPPDATA%\DocSearch\` に永続化する（取得不可環境は `~/.docsearch`）。
  - `folders.txt`（検索対象フォルダ。1行1パス）
  - `index/`（Lucene 索引）
  - `index.meta`（最終索引時刻）
- 再索引は **更新日時による差分方式**（変化のないファイルはスキップ、削除済みファイルは索引から除去）。
- 再索引は **バックグラウンドスレッドで非同期実行**し、HTTP タイムアウトを避ける。画面は `GET /api/index/status` をポーリングして進捗表示する。

### 6. 配布: jlink フル JRE + jpackage app-image

- **jlink で `--add-modules ALL-MODULE-PATH` のフル JRE を生成**する。Tika / POI / Lucene は `ServiceLoader` 等を多用するため、モジュールを絞った最小 JRE は実行時の取りこぼしリスクが高い。エンドユーザー向けの確実性を優先し、フル JRE を同梱する。
- **jpackage `--type app-image`** で、インストール不要・管理者権限不要のポータブルアプリ（`DocSearch.exe` ＋ 同梱 `runtime\`）を生成する。`--win-console` でコンソール窓を表示し、閉じると終了する分かりやすい挙動とする。
- 起動完了時に既定ブラウザで画面を自動的に開く（`BrowserLauncher`、`docsearch.browser.auto-open=false` で無効化可能）。
- JDK ツール（jlink / jpackage）のパスは Gradle の Java toolchain から解決し、ハードコードしない。

## 実装手順（Phase 0〜3）

### Phase 0: Gradle 整備と起動疎通

1. `app` に Spring Boot プラグイン（`org.springframework.boot` / `io.spring.dependency-management`）を適用。これにより `bootJar` / `bootRun` が有効化。
2. 全モジュールに **Java 21 toolchain** を設定。
3. `app` に `application.yml`（アプリ名・ポート 8080）、`HealthController`（`GET /api/health`）、確認用 `static/index.html` を追加。
4. `domain` 側にあった空の `application.yml` を削除（Spring 設定は `app` に集約）。
5. `bootJar` → `java -jar` 起動 → `/api/health` 応答を確認。

### Phase 1: フォルダ設定とファイル名の再帰検索

1. `domain` にモデル（`SearchHit` / `DirectoryEntry`）とロジック（`AppPaths` / `SettingsRepository` / `DirectoryBrowser` / ファイル名検索）を追加。
2. `app` に REST（`GET/PUT /api/settings`、`GET /api/browse[?path=]`、`GET /api/search`）と、ディレクトリブラウザのモーダルを持つ画面を追加。
3. 設定保存 → ブラウズ → 再帰でのファイル名検索を画面まで疎通。

### Phase 2: Lucene + Tika による全文検索

1. `domain` に Lucene / Tika / Spring（BOM 管理）依存を追加。
2. `TextExtractor`（Tika）、`LuceneIndexService`（差分索引・非同期・`DisposableBean`）、`FullTextSearchService`（横断検索・スニペット強調）を実装。
3. ファイル名検索を全文検索に置き換え（ファイル名は索引のフィールドとして包含）。Phase 1 のファイル名専用サービスは廃止。
4. `app` に `IndexController`（`POST /api/index/reindex`、`GET /api/index/status`）を追加し、画面に索引状態表示・再索引ボタン・スニペット表示を追加。
5. 索引作成・全文検索（日本語本文）・差分更新・削除反映を実機で確認。

### Phase 3: JRE 同梱の配布物生成

1. `app/web/BrowserLauncher` で起動時のブラウザ自動オープンを実装。
2. `app/build.gradle.kts` に配布タスク（`distribution` グループ）を追加。
   - `jlinkRuntime`（フル JRE 生成）
   - `prepareJpackageInput`（実行可能 JAR のみを入力に用意）
   - `jpackageImage`（app-image 生成）
   - `packageZip`（配布 zip 化）
3. `./gradlew :app:packageZip` で生成 → 同梱 JRE で `DocSearch.exe` を起動し、各エンドポイント応答を確認。

## 確認方法

```powershell
# 配布物（JRE 同梱の app-image ＋ zip）を生成
./gradlew :app:packageZip

# 生成物
#   app/build/jpackage/dist/DocSearch/DocSearch.exe   （同梱 runtime\ と app\ を含む）
#   app/build/dist/DocSearch-1.0.0-win-x64.zip

# 利用: zip を展開して DocSearch.exe をダブルクリック → ブラウザが開く → フォルダ追加 → 再索引 → 検索
```

主な API:

| メソッド・パス | 役割 |
|---|---|
| `GET /api/health` | 稼働確認 |
| `GET/PUT /api/settings` | 検索対象フォルダの取得・保存 |
| `GET /api/browse[?path=]` | ディレクトリ一覧（ドライブ／サブフォルダ） |
| `POST /api/index/reindex` | 再索引の開始（非同期） |
| `GET /api/index/status` | 索引状態（実行中・件数・最終索引時刻） |
| `GET /api/search?q=` | 全文検索（本文＋ファイル名、スニペット付き） |

## 結果

### 良い影響

- Java 未インストールのPCでも、zip を展開して `DocSearch.exe` をダブルクリックするだけで利用できる（インストール・管理者権限・Java 不要）。
- 本文（pptx/xlsx/docx/pdf/md 等）まで含む日本語全文検索を、再帰・差分索引・スニペット強調付きで提供できる。
- 機能固有要素を `domain` に集約し、`core` の汎用性と `app` の責務（Web/配線/配布）を保てた。

### 注意・トレードオフ

- フル JRE 同梱のため配布サイズが大きい（app-image 約 231MB、zip 約 124MB）。最小化より確実性を優先した結果。
- 本セッションの動作確認は md / txt（Tika のテキスト経路）中心。pptx/xlsx/docx/pdf は POI/PDFBox を同梱済みだが、実ファイルでの確認は未実施。
- 検索は索引済みデータが対象。フォルダ追加・変更後は再索引が必要（更新のないファイルはスキップされる）。
- 配布は Windows の app-image 前提。インストーラ（`.msi`/`.exe`）化は WiX 導入と `--type msi` 等の追加が必要。
- AGENTS.md の依存方針のうち、**`domain` の Spring 非依存は本 ADR で緩和**した（`core` の Spring 非依存は維持）。
