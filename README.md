# Spring Boot ECサイトバックエンドサービス

このプロジェクトは、Spring Boot と PostgreSQL を使用して構築されたECサイト向けのバックエンドサービスです。JWT認証やStripe決済を実装しており、Amazon Web Servicesにデプロイされています。

## デモ

- **API**: http://3.106.69.208:8080
- **フロントエンド**: https://cloudjp-ec.netlify.com

---

## 主な機能

### ユーザー管理
- JWT認証・認可システム
- ユーザー登録・ログイン・ログアウト
- BCryptによるパスワード暗号化
- ロールベース認可（USER/ADMIN）

### 商品管理
- 商品のCRUD操作
- AWS S3への画像アップロード
- 在庫・価格管理

### ショッピングカート
- カート内商品の追加・削除・更新
- リアルタイム合計金額計算
- ユーザー別カート永続化

### 注文管理
- カートからの注文作成
- 注文履歴の確認
- ステータス管理（保留中・処理中・発送済・配達完了・キャンセル）
- 管理者による注文管理

### 決済処理
- Stripe決済連携
- Payment Intent作成
- Webhook による決済確認
- 自動注文ステータス更新

### 管理者機能
- 専用管理者エンドポイント
- 商品・注文管理
- ロールベース認可による保護

---

## 技術スタック

| カテゴリ | 技術 |
|---------|------|
| **フレームワーク** | Spring Boot 3.5.7, Java 21 |
| **セキュリティ** | Spring Security 6, JWT, BCrypt |
| **データベース** | PostgreSQL, Spring Data JPA |
| **テスト** | JUnit 5, Mockito, H2 Database |
| **クラウドインフラ** | AWS EC2, AWS RDS (PostgreSQL), AWS S3 |
| **決済** | Stripe API |
| **ビルドツール** | Maven |
| **その他** | Jakarta Validation, Spring Dotenv |

---

## API エンドポイント

### 認証 `/auth`
| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| POST | `/auth/register` | ユーザー登録 | ❌ |
| POST | `/auth/login` | ログイン | ❌ |
| POST | `/auth/logout` | ログアウト | ✅ |

### 商品 `/products`
| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/products` | 商品一覧取得 | ❌ |
| GET | `/products/{id}` | 商品詳細取得 | ❌ |

### カート `/user/cart`
| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/user/cart` | カート取得 | ✅ USER |
| POST | `/user/cart/add` | 商品追加 | ✅ USER |
| PUT | `/user/cart/update/{itemId}` | 数量更新 | ✅ USER |
| DELETE | `/user/cart/remove/{itemId}` | 商品削除 | ✅ USER |

### 注文 `/user/orders`
| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/user/orders` | 注文履歴取得 | ✅ USER |
| GET | `/user/orders/{id}` | 注文詳細取得 | ✅ USER |
| POST | `/user/orders` | 注文作成 | ✅ USER |

### 決済 `/user/payment`
| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| POST | `/user/payment/create-intent` | 決済Intent作成 | ✅ USER |
| POST | `/user/payment/confirm/{orderId}` | 決済確認 | ✅ USER |

### 管理者 `/admin`
| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| GET | `/admin/dashboard` | ダッシュボード | ✅ ADMIN |
| POST | `/admin/products` | 商品作成 | ✅ ADMIN |
| PUT | `/admin/products/{id}` | 商品更新 | ✅ ADMIN |
| DELETE | `/admin/products/{id}` | 商品削除 | ✅ ADMIN |
| GET | `/admin/orders` | 全注文取得 | ✅ ADMIN |
| PUT | `/admin/orders/{id}/status` | ステータス更新 | ✅ ADMIN |

### Webhook `/webhook`
| メソッド | エンドポイント | 説明 | 認証 |
|---------|--------------|------|-----|
| POST | `/webhook/stripe` | Stripe Webhook | ❌ |

---

### ローカル起動手順

```bash
# データベース作成
CREATE DATABASE ecommercedb;

# プロジェクトビルド
./mvnw clean install

# アプリケーション起動
./mvnw spring-boot:run
```

起動後、`http://localhost:8080` でAPIにアクセス可能。