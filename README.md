# CommunicationNotebook

## 概要

小規模の組織内で使用する、連絡ノートアプリ。

エクセルの「誰でも書ける手軽さ」を残しつつ、「情報の埋没・検索性の低さ」というエクセル運用の致命的な欠点をシステムで解決することを目指す。

要件定義は [docs/requirements.md](docs/requirements.md) を参照。

## 技術スタック

### バックエンド

- Java 21
- Spring Boot 4.1.1(Web MVC / Data JPA / Validation / Security)
- PostgreSQL
- Flyway(マイグレーション管理)
- springdoc-openapi(API仕様書 / Swagger UI)

### フロントエンド

- React 19
- TypeScript
- Vite

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

前提: Node.js、上記バックエンドが起動済みであること

```bash
cd frontend
npm install
cp .env.example .env.local   # 任意、既定値で動作します
npm run dev
```

起動後、以下にアクセスするとログイン画面が表示されます。

http://localhost:5173

開発用アカウント(職員ID / パスワード):

| 職員ID | パスワード | 権限 |
|---|---|---|
| E001 | password123 | 管理者 |
| E002 | password123 | 一般ユーザー |

## テスト

### バックエンド

```bash
cd backend
./gradlew test   # Windowsの場合は gradlew.bat test
```

現時点では手動実行のみ。CIによる自動テスト導入は今後の対応予定。

### フロントエンド

現時点ではテスト未整備。

## 開発フロー

- 作業はタスクごとにブランチを切って行い、mainへの直接pushはしない。
- 変更はPull Requestを作成し、GitHub上でレビュー・マージする。
- 詳細な運用ルールはチーム内の取り決めに従う。

## ライセンス

社内利用のみ想定(TBD)
