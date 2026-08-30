# CommunicationNotebook

## 概要

小規模の組織内で使用する、連絡ノートアプリ。

エクセルの「誰でも書ける手軽さ」を残しつつ、「情報の埋没・検索性の低さ」というエクセル運用の致命的な欠点をシステムで解決することを目指す。

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

```
http://localhost:8080/actuator/health
```

`{"status":"UP"}` が返れば起動成功です。

### フロントエンド(React / Vite)

前提: Node.js、上記バックエンドが起動済みであること

```bash
cd frontend
npm install
cp .env.example .env.local   # 任意、既定値で動作します
npm run dev
```

起動後、以下にアクセスするとログイン画面が表示されます。

```
http://localhost:5173
```

開発用アカウント(職員ID / パスワード):

| 職員ID | パスワード | 権限 |
|---|---|---|
| E001 | password123 | 管理者 |
| E002 | password123 | 一般ユーザー |

## ライセンス

TBD
