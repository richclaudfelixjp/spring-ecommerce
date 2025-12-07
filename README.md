# Spring Boot Eコマースバックエンド

## 概要

このプロジェクトは、Spring Bootを使用して構築されたEコマースプラットフォームのバックエンドAPIです。モダンなJava開発技術とベストプラクティスを取り入れ、セキュアでスケーラブルなアプリケーションの構築を目指しています。

**注意:** このプロジェクトは現在開発進行中です。今後、機能追加や改修を予定しています。

---

## 主な機能

### 実装済み

- **認証・認可**
  - JWT (JSON Web Token) を利用したステートレス認証
  - ユーザー登録、ログイン、ログアウト機能
  - ログアウト時のJWTブラックリスト管理
  - ロールベースのアクセス制御 (`ROLE_ADMIN`, `ROLE_USER`)
- **商品管理 (管理者向け)**
  - 商品のCRUD (作成, 読み取り, 更新, 削除) 操作
- **商品閲覧 (公開)**
  - 商品一覧および詳細情報の取得
- **ショッピングカート機能**
  - 商品の追加、数量変更、削除
- **注文処理・注文履歴機能**
  - カートからの注文作成
  - ユーザーの注文履歴取得
- **決済システム連携**
  - Stripeを利用した決済インテントの作成
- **テスト**
  - `JUnit5` と `Mockito` を用いたサービス層の単体テスト
  - `MockMvc` を用いたコントローラー層の結合テスト
- **CI/CD**
  - GitHub Actionsによる自動ビルド、テスト、AWS (S3, EC2) へのデプロイ

### 今後の実装予定

- ユーザープロファイル管理機能
- 商品検索・フィルタリング機能

---

## 使用技術

- **バックエンド:** Java 21, Spring Boot 3.5.7
- **認証:** Spring Security, JWT (jjwt)
- **データベース:** PostgreSQL, H2 (テスト用)
- **データアクセス:** Spring Data JPA, Hibernate
- **決済:** Stripe
- **ビルドツール:** Maven
- **CI/CD:** GitHub Actions, AWS (EC2, S3)
- **その他:** `spring-dotenv` (環境変数管理), Lombok

---

## セットアップ方法

### 前提条件

- JDK 21
- Maven
- PostgreSQL

### 手順

1.  リポジトリをクローンします。
    ```sh
    git clone https://github.com/your-username/spring-ecommerce.git
    cd spring-ecommerce
    ```

2.  `.env.example` を参考に `.env` ファイルを作成し、ご自身の環境に合わせてデータベース接続情報やJWTシークレットキーなどを設定します。
    ```sh
    cp .env.example .env
    ```

3.  Maven Wrapperを使用してアプリケーションを起動します。
    ```sh
    ./mvnw spring-boot:run
    ```

アプリケーションは `http://localhost:8080` で起動します。

---

## APIエンドポイント

主要なエンドポイントは以下の通りです。

### 認証

| HTTPメソッド | エンドポイント   | 説明                          | 権限        |
| :----------- | :--------------- | :---------------------------- | :---------- |
| `POST`       | `/auth/register` | 新規ユーザー登録              | `permitAll` |
| `POST`       | `/auth/login`    | ログインしてJWTトークンを取得 | `permitAll` |
| `POST`       | `/auth/logout`   | ログアウト (トークンを無効化) | `permitAll` |

### 商品

| HTTPメソッド | エンドポイント     | 説明                       | 権限        |
| :----------- | :----------------- | :------------------------- | :---------- |
| `GET`        | `/products`        | 全商品の一覧を取得         | `permitAll` |
| `GET`        | `/products/{id}`   | 指定したIDの商品情報を取得 | `permitAll` |

### カート

| HTTPメソッド | エンドポイント                      | 説明                               | 権限   |
| :----------- | :---------------------------------- | :--------------------------------- | :----- |
| `GET`        | `/user/cart`                        | 認証されたユーザーのカートを取得   | `USER` |
| `POST`       | `/user/cart/add`                    | カートに商品を追加                 | `USER` |
| `PUT`        | `/user/cart/update/{cartItemId}`    | カート内のアイテムの数量を更新     | `USER` |
| `DELETE`     | `/user/cart/remove/{cartItemId}`    | カートからアイテムを削除           | `USER` |

### 注文

| HTTPメソッド | エンドポイント           | 説明                             | 権限   |
| :----------- | :----------------------- | :------------------------------- | :----- |
| `POST`       | `/user/orders/create`    | カートから新しい注文を作成       | `USER` |
| `GET`        | `/user/orders`           | 認証されたユーザーの全注文を取得 | `USER` |
| `GET`        | `/user/orders/{orderId}` | IDで注文を取得                   | `USER` |

### 決済

| HTTPメソッド | エンドポイント                      | 説明                   | 権限   |
| :----------- | :---------------------------------- | :--------------------- | :----- |
| `POST`       | `/user/payment/create-payment-intent` | 支払いインテントを作成 | `USER` |

### 管理者

| HTTPメソッド | エンドポイント           | 説明                   | 権限    |
| :----------- | :----------------------- | :--------------------- | :------ |
| `GET`        | `/admin/dashboard`       | 管理者ダッシュボード   | `ADMIN` |
| `GET`        | `/admin/users`           | ユーザー管理           | `ADMIN` |
| `POST`       | `/admin/products`        | 新しい商品を登録       | `ADMIN` |
| `PUT`        | `/admin/products/{id}`   | 商品情報を更新         | `ADMIN` |
| `DELETE`     | `/admin/products/{id}`   | 指定したIDの商品を削除 | `ADMIN` |
| `DELETE`     | `/admin/products`        | 全ての商品を削除       | `ADMIN` |

### テスト

| HTTPメソッド | エンドポイント | 説明           | 権限        |
| :----------- | :------------- | :------------- | :---------- |
| `GET`        | `/test`        | 接続テスト用   | `permitAll` |

---

## プロジェクト構成

本プロジェクトは、関心の分離を目的としたレイヤードアーキテクチャを採用しています。

- `controller`: HTTPリクエストの受付とレスポンスの返却
- `service`: ビジネスロジックの処理
- `repository`: データベースとのやり取り (JPA)
- `model`: エンティティクラス (DBのテーブル定義)
- `dto`: データ転送オブジェクト (Data Transfer Object)
- `config`: セキュリティ設定や初期データ投入などの構成クラス
- `filter`: JWT認証フィルター
- `util`: JWT生成・検証などのユーティリティクラス