# crypt-cat

Coincheck(暗号資産取引所)の取引 API を呼び出して自動売買を行う、Java / Spring Boot 製の個人開発用バックエンドです。TradingView 等のアラート(Webhook)を受け取って指値注文を発行し、GCP(Cloud Run / Secret Manager / Firestore)上での運用を想定しています。

> **免責事項**
> - 本リポジトリは個人の学習・実験目的のコードであり、**投資助言ではありません**。特定の売買・手法・銘柄を推奨するものではありません。
> - 実際の売買は**すべて自己責任**で行ってください。本コードの利用によって生じた損失について、作者は一切責任を負いません。
> - 既定では発注は行われません(`IS_ACTUALLY_SELL_BUY` が `true` でない限り、注文 API は呼ばずにログ出力のみ)。

## ステータス

個人運用向けの**実験的なプロジェクト**です。サービス実装が `V2`〜`V6` と世代違いで複数残っており(後述)、整理・ドキュメント化は途中です。汎用ライブラリやそのまま使えるボットとしての利用は想定していません。

## 特長

- Coincheck 取引所 API(ティッカー・残高・未約定注文・取引履歴・指値/成行注文・キャンセル)のクライアントを HMAC-SHA256 署名つきで実装
- 注文用 REST エンドポイント(JSON の Webhook を受信し、非同期で発注)
- 指値価格の決め方を `HIGH / MEDIUM / LOW / EVEN` から選択(板の bid / ask / last から算出)
- 発注後に未約定であればキャンセルし、リトライ(回数・間隔は設定可能)
- 注文状態を Firestore に保存(スキップ回数なども記録)
- 許可 IP(`x-forwarded-for` ヘッダ)による簡易アクセス制限
- 構造化ログ(Logstash Encoder / Google Cloud Logging 向け JSON 出力)
- TradingView のストラテジー(Pine Script)と、パラメータ探索用の Selenium スクリプト(Python)を同梱

## 技術スタック

- Java 17 / Spring Boot 3.3.0 / Gradle(Wrapper 同梱)
- Spring Web, Validation, AOP, Retry, WebFlux(HTTP クライアントは `RestClient`)
- Google Cloud: Secret Manager, Firestore, Cloud Logging
- springdoc-openapi(Swagger UI)、Slack API クライアント(依存には含まれますが、コード内での利用は確認できませんでした)
- Docker(マルチステージ)/ Cloud Build → Cloud Run
- テスト: JUnit 5, Spring Boot Test

## セットアップ

前提: JDK 17。

```bash
# ビルド
./gradlew build

# テスト
./gradlew test

# ローカル起動(既定: 発注せずログのみ)
./gradlew bootRun
```

注意:
- `application.properties` は `spring.profiles.active=production` で、起動時に Secret Manager から API キーを取得します。GCP の認証情報(Application Default Credentials)が無い環境では、そのままでは起動できない可能性があります。テスト用設定は `src/test/resources/application.properties`(`test` プロファイル、キーはダミー値)にあります。
- Dockerfile のビルドステップは `gradle booJar` と記述されており、`bootJar` の誤記と思われます(未修正。Docker ビルド前に要確認)。

## 設定項目

`src/main/resources/application.properties` で、次の環境変数から値を受け取ります(値はここには記載しません)。

| 環境変数 | 設定キー | 用途 |
|---|---|---|
| `PORT` | `server.port` | 待受ポート |
| `IS_ACTUALLY_SELL_BUY` | `coincheck.actually` | `true` で実際に発注。未設定/`false` はスキップ(ログのみ) |
| `SELL_BUY_AMOUNT` | `coincheck.amount` | 売買数量 |
| `SELL_BUY_PRICE` | `coincheck.price` | 売買価格 |
| `ORDER_INTERVAL` | `coincheck.interval` | 注文間隔 |
| `CANCEL_DELAY_MINUTES` | `coincheck.cancel-delay-minutes` | 未約定注文のキャンセルまでの待ち時間(分) |
| `RETRY_DELAY_SEC` | `coincheck.retry.delay-sec` | リトライ間隔(秒) |
| `RETRY_LIMIT_COUNT` | `coincheck.retry.limit-count` | リトライ上限回数 |

そのほか `project.id`(GCP プロジェクト ID)、`coincheck.order-logic`、`allowed.ips`(許可 IP のカンマ区切り)を properties で設定します。

### 認証情報(API キー)の扱い

Coincheck の API キー/シークレットは **Google Cloud Secret Manager**(`coincheck-api-access-key`, `coincheck-api-secret`)から実行時に取得します。キー値そのものはコードにも設定ファイルにも含めていません。fork して利用する場合も、キーをソースや properties にコミットしないでください。

## API(エンドポイント)

`OrderController` で定義されています。注文系は POST の JSON ボディ(`reason`, `group`, `ratio` が必須、`price` は任意、`order-type` は `buy` / `sell`)を受け取ります。`{pair}` は `btc_jpy` など(`Pair` 列挙型で定義)。

| メソッド | パス | 概要 |
|---|---|---|
| POST | `/order/btcfix/{pair}` | BTC 数量固定の注文(初期版) |
| POST | `/order/jpyfix/{pair}`, `/v2`〜`/v5/order/jpyfix/{pair}` | JPY 金額固定の注文(世代別) |
| POST | `/v6/order/jpyfix/{pair}` | JPY 固定(買い/売りで別実装) |
| POST | `/v2/order/allin/{pair}` | 全 JPY での買い / 全 BTC の売り |
| GET | `/transactions/{minutes}` | 直近の約定をログ集計 |
| GET | `/balance/` | 残高・評価額をログ出力 |
| DELETE | `/transaction/{minutes}` | Firestore 上の古い ORDERED 状態を削除 |
| POST | `/retry/sell` | 売りのリトライ(最大60分、1分間隔) |

## ディレクトリ構成

```
src/main/java/pyororin/cryptcat/
  controller/   REST エンドポイント、IP 制限フィルタ
  service/      売買ロジック(世代別 Trade*Service、レート算出)
  repository/   Coincheck API / Firestore / Secret Manager アクセス
                (actually=false 時は Skip* 実装が有効)
  batch/        集計・残高・リトライ処理
  config/       設定クラス
src/test/       ユニットテスト
trading_view/   Pine Script のストラテジー、Selenium でのパラメータ探索スクリプト(Python)
Dockerfile, cloudbuild.yaml   Cloud Build → Cloud Run へのデプロイ定義
```

## 注意事項・制限

- 売買ロジックの有効性・収益性は検証しておらず、バックテスト結果等も含みません。
- 古い世代(V2〜V5 等)の実装が残っており、現行でどれを使うかはコードからは断定できません。
- `trading_view/selenium` のスクリプトは作者個人の Windows 環境のパスを前提としており、そのままでは動作しません。
- ライセンスは未設定です。
