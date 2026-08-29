# CommunicationNotebook

## 概要

小規模の組織内で使用する、連絡ノートアプリ。

エクセルの「誰でも書ける手軽さ」を残しつつ、「情報の埋没・検索性の低さ」というエクセル運用の致命的な欠点をシステムで解決することを目指す。

## セットアップ

```bash
git clone git@github.com:justayui/CommunicationNotebook.git
cd CommunicationNotebook
```

### バックエンド(Spring Boot)

前提: Java 21

```bash
cd backend
./gradlew bootRun   # Windowsの場合は gradlew.bat bootRun
```

起動後、以下にアクセスして正常起動を確認できます。

```
http://localhost:8080/actuator/health
```

`{"status":"UP"}` が返れば起動成功です。

※ DB接続(PostgreSQL)や認証(Spring Security)は未設定です。今後のタスクで追加予定です。

## ライセンス

TBD
