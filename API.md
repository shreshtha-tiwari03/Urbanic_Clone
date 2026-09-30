# DemoCart API

## Run locally

The app uses the MySQL database `ShoppingCart_DB` by default and updates its tables on startup. Set your database credentials in the ignored `.env` file before starting it:

```powershell
DB_URL=jdbc:mysql://localhost:3306/ShoppingCart_DB
DB_USERNAME=root
DB_PASSWORD=replace-with-your-local-mysql-password
```

Then start the API:

```powershell
.\gradlew.bat bootRun
```

Base URL: `http://localhost:8080`

Automated Gradle tests use an isolated in-memory H2 database; normal app runs use MySQL and keep data in `ShoppingCart_DB`.

## Randomized API tests

With the app running, execute the randomized HTTP test suite from PowerShell:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
.\scripts\Test-RandomApi.ps1 -ProductCount 4
```

`ProductCount` must be at least four. The suite reports every case with its expected and actual result, exits nonzero on failures, and creates test products and an order in the connected database. Remove those test rows from MySQL if you do not want to keep them.

## Complete checkout example

Run these commands in PowerShell while the app is running:

```powershell
$productBody = @{
  name = "Everyday sneakers"
  description = "Lightweight walking shoes"
  category = "SHOES"
  price = 59.99
  currency = "USD"
  stockQuantity = 10
} | ConvertTo-Json

$product = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/products" -ContentType "application/json" -Body $productBody
$productId = $product.productId
$shopperId = 1001

$cartBody = @{ productId = $productId; quantity = 2 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/carts/$shopperId/items" -ContentType "application/json" -Body $cartBody

Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/carts/$shopperId"

$orderBody = @{ paymentMethod = "DEMO" } | ConvertTo-Json
$order = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/carts/$shopperId/checkout" -ContentType "application/json" -Body $orderBody
$order

Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/orders/$($order.orderId)"
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/shoppers/$shopperId/orders"
```

Checkout is a local demo operation: it records the supplied payment method and confirms the order, but it does not charge a payment provider.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/products?name=shoe&category=SHOES` | List/search products; both filters are optional |
| `GET` | `/api/products/{productId}` | Get a product |
| `POST` | `/api/products` | Create a product |
| `PUT` | `/api/products/{productId}` | Replace product details and stock |
| `DELETE` | `/api/products/{productId}` | Delete a product |
| `GET` | `/api/search/products?name=shoe&category=SHOES` | Search products |
| `GET` | `/api/search/products/{productId}` | Search by product ID |
| `GET` | `/api/carts/{shopperId}` | Get or create a shopper's cart |
| `POST` | `/api/carts/{shopperId}/items` | Add a product and quantity |
| `PUT` | `/api/carts/{shopperId}/items/{productId}` | Set an item's quantity |
| `DELETE` | `/api/carts/{shopperId}/items/{productId}` | Remove an item |
| `DELETE` | `/api/carts/{shopperId}` | Empty the cart |
| `POST` | `/api/carts/{shopperId}/checkout` | Create an order and reduce stock |
| `GET` | `/api/orders/{orderId}` | Get an order |
| `GET` | `/api/shoppers/{shopperId}/orders` | List a shopper's orders |

Product creation/update JSON:

```json
{
  "name": "Everyday sneakers",
  "description": "Lightweight walking shoes",
  "category": "SHOES",
  "price": 59.99,
  "currency": "USD",
  "stockQuantity": 10
}
```

Cart item JSON is `{ "productId": 1, "quantity": 2 }`. Checkout JSON is `{ "paymentMethod": "DEMO" }`. Quantities must be positive, prices and stock cannot be negative, and checkout rejects empty carts or insufficient stock. Product category values are `CLOTHES`, `JWELLERY`, and `SHOES`.
