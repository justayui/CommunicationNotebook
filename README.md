# CommunicationNotebook

## 概要

小規模の組織内で使用する、連絡ノートアプリ。

エクセルの「誰でも書ける手軽さ」を残しつつ、「情報の埋没・検索性の低さ」というエクセル運用の致命的な欠点をシステムで解決することを目指す。

要件定義は [docs/requirements.md](docs/requirements.md) を参照。

## 技術スタック

### バックエンド

- Java 21
- Spring Boot 4.1.1(Web MVC / Data JPA / Validation / Security)
- Gradle(ビルドツール。Gradle Wrapper同梱のため個別インストール不要)
- Lombok
- PostgreSQL 16
- Flyway(マイグレーション管理)
- springdoc-openapi(API仕様書 / Swagger UI)

認証はSpring Securityによるセッション方式(Cookie)です。無操作30分でセッションが切れます。

### フロントエンド

- React 19
- TypeScript
- Vite
- oxlint(Lint)

### インフラ(開発環境)

- Docker / Docker Compose(PostgreSQLのみコンテナで起動)

## ディレクトリ構成

```
.
├── backend/    # Spring Bootバックエンド(API)
├── frontend/   # React + Viteフロントエンド
├── docs/       # 要件定義などのドキュメント
└── prototype/  # UI/UX検証用の静的プロトタイプ(単体HTML)
```

## セットアップ

```bash
git clone git@github.com:justayui/CommunicationNotebook.git
cd CommunicationNotebook
```

### DB(PostgreSQL / Docker)

前提: Docker / Docker Compose

```bash
cp .env.example .env   # 初回のみ
docker compose up -d
```

`.env` の `DB_PASSWORD` と `ADMIN_PASSWORD` には、任意のパスワードを設定してください(未設定の場合、DBとバックエンドは起動しません)。`.env` はGit管理外です。

#### 環境変数一覧(リポジトリ直下の `.env`)

`.env` はDocker Composeとバックエンドの両方が参照します。

| 変数名 | 必須 | 既定値 | 用途 |
|---|---|---|---|
| `DB_NAME` | | `communication_notebook` | DB名 |
| `DB_USER` | | `postgres` | DBのユーザー名 |
| `DB_PASSWORD` | ○ | なし | DBのパスワード |
| `ADMIN_EMPLOYEE_ID` | | `E001` | 初期管理者の職員ID |
| `ADMIN_NAME` | | `Admin` | 初期管理者の氏名 |
| `ADMIN_PASSWORD` | ○ | なし | 初期管理者のパスワード |
| `LOG_PATH` | | `logs` | ログファイルの出力先ディレクトリ(「ログ」を参照) |

#### 初期管理者の作成条件

バックエンド起動時に、以下の条件で初期管理者が自動作成されます。

- 有効な(削除されていない)管理者が1人も存在しない場合のみ、`ADMIN_EMPLOYEE_ID` / `ADMIN_NAME` / `ADMIN_PASSWORD` の値で作成されます。
- 管理者が既に存在する場合は作成されません。そのため、`ADMIN_PASSWORD` を後から変更しても、既存の管理者のパスワードには反映されません。パスワードの変更は画面の「パスワード変更」から行ってください。
- `ADMIN_EMPLOYEE_ID` と同じ職員IDのユーザーが既に存在する場合(一般ユーザー・削除済みユーザーを含む)は、安全のため作成も管理者への昇格も行わず、警告ログを出力します。

DBのパスワードはDBの初回起動時にのみ設定されます。あとから `DB_PASSWORD` を変更する場合は、`docker compose down -v` でデータを削除してから再起動してください(DB内のデータはすべて消えます)。

### バックエンド(Spring Boot)

前提: Java 21、上記PostgreSQLコンテナが起動済みであること

```bash
cd backend
./gradlew bootRun   # Windowsの場合は gradlew.bat bootRun
```

起動後、以下にアクセスして正常起動を確認できます。

http://localhost:8080/actuator/health

`{"status":"UP"}` が返れば起動成功です。

API仕様書(Swagger UI)は以下で確認できます。

http://localhost:8080/swagger-ui.html

### フロントエンド(React / Vite)

前提: Node.js 20.19以上または22.12以上、上記バックエンドが起動済みであること

```bash
cd frontend
npm install
cp .env.example .env.local   # 任意、既定値で動作します
npm run dev
```

フロントエンドの環境変数(`frontend/.env.local`)は以下のとおりです。

| 変数名 | 既定値 | 用途 |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | バックエンドAPIのURL |

起動後、以下にアクセスするとログイン画面が表示されます。

http://localhost:5173

初期管理者アカウントでログインできます。

- 職員ID: `.env` の `ADMIN_EMPLOYEE_ID`(既定値 `E001`)
- パスワード: `.env` の `ADMIN_PASSWORD`

一般ユーザーは、ログイン画面の新規登録から作成してください。

## テスト

### CI(GitHub Actions)

Pull Requestの作成・更新時、およびmainへのpush時に、以下のワークフローが自動実行されます。結果はPull Requestの画面で確認できます。

| ワークフロー | 定義ファイル | 実行内容 |
|---|---|---|
| Backend CI with Gradle | [.github/workflows/backend-ci.yml](.github/workflows/backend-ci.yml) | `./gradlew test` |
| Frontend CI | [.github/workflows/frontend-ci.yml](.github/workflows/frontend-ci.yml) | `npm ci` → `npm run lint` → `npm run build` |

- バックエンドのテストは実際のPostgreSQLに接続するため、ワークフロー内でPostgreSQL 16をサービスコンテナとして起動しています。`DB_PASSWORD`・`ADMIN_PASSWORD` はCI専用のダミー値をワークフロー内で設定しています(ローカルの `.env` や本番環境の値とは無関係です)。
- 変更箇所に関わらず、両方のワークフローが毎回実行されます。

### バックエンド

```bash
cd backend
./gradlew test   # Windowsの場合は gradlew.bat test
```

ローカルで実行する場合は、上記PostgreSQLコンテナが起動済みである必要があります。

### フロントエンド

現時点では自動テスト未整備(主要ユースケースに基づく手動結合テストで確認)。

Lint・ビルドはCIで自動実行されます。ローカルで実行する場合は以下のとおりです。

```bash
cd frontend
npm run lint    # oxlintによる静的解析
npm run build   # 型チェック(tsc)+本番用ビルド(dist/に出力)
```

## ログ

バックエンドのログは、コンソールに加えてファイルにも出力されます(設定: [backend/src/main/resources/logback-spring.xml](backend/src/main/resources/logback-spring.xml))。

- 出力先: `${LOG_PATH}/app.log`(`LOG_PATH` の既定値は `logs` で、起動時のカレントディレクトリからの相対パス。`backend/` で `bootRun` した場合は `backend/logs/app.log`)
- ローテーション: 日付ごと、または1ファイル50MBを超えた時点で `app.yyyy-MM-dd.N.log.gz` に圧縮して切り替え
- 保持期間: 30日分(合計1GBを超えた場合は古いものから削除)
- 出力先を変更する場合は、`.env` または環境変数で `LOG_PATH` を指定してください

エラー発生時は、ステータス・リクエストパス・原因がWARN/ERRORレベルで出力されます。

## 運用メモ

### カテゴリの追加・変更

カテゴリの選択肢は `categories` テーブルで管理しています。管理画面は未実装(将来の拡張候補)のため、追加・変更する場合はFlywayのマイグレーションファイルを新規作成してください。既存のマイグレーションファイルは変更しないでください。

```sql
-- 例: backend/src/main/resources/db/migration/V8__add_category_meeting.sql
INSERT INTO categories (name) VALUES ('会議');
```

バックエンドの次回起動時に自動で適用されます。なお、既存の投稿の `category` は文字列で保持しているため、カテゴリ名を変更・削除しても既存の投稿の表示は変わりません。

## 開発フロー

- 作業はタスクごとにブランチを切って行い、mainへの直接pushはしない。
- 変更はPull Requestを作成し、GitHub上でレビュー・マージする。
- 詳細な運用ルールはチーム内の取り決めに従う。

## ライセンス

社内利用のみ想定(TBD)
