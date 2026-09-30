param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$ProductCount = 4
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http
$client = [System.Net.Http.HttpClient]::new()
$results = [System.Collections.Generic.List[object]]::new()
$random = [System.Random]::new()

function Invoke-ApiCase {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Path,
        [int]$ExpectedStatus,
        [object]$Body = $null
    )

    $request = [System.Net.Http.HttpRequestMessage]::new(
        [System.Net.Http.HttpMethod]::new($Method), "$BaseUrl$Path")
    if ($null -ne $Body) {
        $json = ConvertTo-Json -InputObject $Body -Depth 6 -Compress
        $request.Content = [System.Net.Http.StringContent]::new(
            $json, [System.Text.Encoding]::UTF8, "application/json")
    }

    try {
        $response = $client.SendAsync($request).GetAwaiter().GetResult()
        $status = [int]$response.StatusCode
        $content = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        $data = $null
        if (-not [string]::IsNullOrWhiteSpace($content)) {
            try { $data = ConvertFrom-Json -InputObject $content -ErrorAction Stop }
            catch { $data = $content }
        }
        $passed = $status -eq $ExpectedStatus
        $detail = if ($data -is [string]) { $data } elseif ($null -eq $data) { "No response body" }
            elseif ($data -is [System.Array]) { "Items: $($data.Count)" }
            else { $data | ConvertTo-Json -Depth 5 -Compress }
        $results.Add([pscustomobject]@{
            Case = $Name
            Expected = $ExpectedStatus
            Actual = $status
            Result = if ($passed) { "PASS" } else { "FAIL" }
            Detail = $detail
            Data = $data
        })
        return @{ Passed = $passed; Data = $data; Status = $status }
    }
    finally {
        $request.Dispose()
        if ($null -ne $response) { $response.Dispose() }
    }
}

try {
    $health = Invoke-ApiCase -Name "List products" -Method "GET" -Path "/api/products" -ExpectedStatus 200
    if (-not $health.Passed) { throw "API is not ready at $BaseUrl" }

    $categories = @("CLOTHES", "JWELLERY", "SHOES")
    $products = [System.Collections.Generic.List[object]]::new()
    for ($index = 1; $index -le $ProductCount; $index++) {
        $productBody = @{
            name = "Random-$([guid]::NewGuid().ToString('N').Substring(0, 8))"
            description = "Generated API test product $index"
            category = $categories[$random.Next(0, $categories.Count)]
            price = [math]::Round($random.NextDouble() * 90 + 10, 2)
            currency = "USD"
            stockQuantity = $random.Next(5, 16)
        }
        $created = Invoke-ApiCase -Name "Create random product $index" -Method "POST" `
            -Path "/api/products" -ExpectedStatus 201 -Body $productBody
        if ($created.Passed) { $products.Add($created.Data) }
    }
    if ($ProductCount -lt 4 -or $products.Count -lt 4) {
        throw "ProductCount must be at least four for all API scenarios"
    }

    $mainProduct = $products[0]
    $secondaryProduct = $products[1]
    $removalProduct = $products[2]
    $shopperId = $random.Next(100000, 900000)
    $searchName = [uri]::EscapeDataString($mainProduct.name)

    Invoke-ApiCase -Name "Get random product" -Method "GET" `
        -Path "/api/products/$($mainProduct.productId)" -ExpectedStatus 200 | Out-Null
    Invoke-ApiCase -Name "Search random product by name/category" -Method "GET" `
        -Path "/api/search/products?name=$searchName&category=$($mainProduct.category)" `
        -ExpectedStatus 200 | Out-Null

    $updatedPrice = [math]::Round([double]$mainProduct.price + 1.25, 2)
    $updateBody = @{
        name = $mainProduct.name
        description = "Updated by randomized API test"
        category = $mainProduct.category
        price = $updatedPrice
        currency = "USD"
        stockQuantity = $mainProduct.stockQuantity
    }
    $updated = Invoke-ApiCase -Name "Update random product" -Method "PUT" `
        -Path "/api/products/$($mainProduct.productId)" -ExpectedStatus 200 -Body $updateBody
    $mainProduct = $updated.Data

    $firstQuantity = $random.Next(1, 3)
    $cartAddBody = @{ productId = $mainProduct.productId; quantity = $firstQuantity }
    Invoke-ApiCase -Name "Add random quantity to cart" -Method "POST" `
        -Path "/api/carts/$shopperId/items" -ExpectedStatus 200 -Body $cartAddBody | Out-Null
    $addedAgain = $random.Next(1, 3)
    Invoke-ApiCase -Name "Add same item again" -Method "POST" `
        -Path "/api/carts/$shopperId/items" -ExpectedStatus 200 `
        -Body @{ productId = $mainProduct.productId; quantity = $addedAgain } | Out-Null
    $checkoutQuantity = $random.Next(1, 4)
    Invoke-ApiCase -Name "Replace cart quantity" -Method "PUT" `
        -Path "/api/carts/$shopperId/items/$($mainProduct.productId)" -ExpectedStatus 200 `
        -Body @{ quantity = $checkoutQuantity } | Out-Null

    $secondaryQuantity = $random.Next(1, 3)
    Invoke-ApiCase -Name "Add second USD product" -Method "POST" `
        -Path "/api/carts/$shopperId/items" -ExpectedStatus 200 `
        -Body @{ productId = $secondaryProduct.productId; quantity = $secondaryQuantity } | Out-Null
    Invoke-ApiCase -Name "Add removable cart item" -Method "POST" `
        -Path "/api/carts/$shopperId/items" -ExpectedStatus 200 `
        -Body @{ productId = $removalProduct.productId; quantity = 1 } | Out-Null
    Invoke-ApiCase -Name "Remove cart item" -Method "DELETE" `
        -Path "/api/carts/$shopperId/items/$($removalProduct.productId)" -ExpectedStatus 200 | Out-Null

    Invoke-ApiCase -Name "Reject zero quantity" -Method "POST" `
        -Path "/api/carts/$shopperId/items" -ExpectedStatus 400 `
        -Body @{ productId = $mainProduct.productId; quantity = 0 } | Out-Null
    Invoke-ApiCase -Name "Reject quantity above stock" -Method "POST" `
        -Path "/api/carts/$shopperId/items" -ExpectedStatus 409 `
        -Body @{ productId = $mainProduct.productId; quantity = ([long]$mainProduct.stockQuantity + 1) } | Out-Null

    $eurProductBody = @{
        name = "Currency-$([guid]::NewGuid().ToString('N').Substring(0, 8))"
        description = "Currency validation case"
        category = "SHOES"
        price = 15.25
        currency = "EUR"
        stockQuantity = 4
    }
    $eurProduct = Invoke-ApiCase -Name "Create non-USD product" -Method "POST" `
        -Path "/api/products" -ExpectedStatus 201 -Body $eurProductBody
    if ($eurProduct.Passed) {
        Invoke-ApiCase -Name "Reject mixed currency cart" -Method "POST" `
            -Path "/api/carts/$shopperId/items" -ExpectedStatus 400 `
            -Body @{ productId = $eurProduct.Data.productId; quantity = 1 } | Out-Null
    }

    $cartBeforeCheckout = Invoke-ApiCase -Name "Read cart before checkout" -Method "GET" `
        -Path "/api/carts/$shopperId" -ExpectedStatus 200
    $checkoutBody = @{ paymentMethod = "DEMO-$($random.Next(1000, 9999))" }
    $order = Invoke-ApiCase -Name "Checkout random cart" -Method "POST" `
        -Path "/api/carts/$shopperId/checkout" -ExpectedStatus 201 -Body $checkoutBody

    if ($order.Passed) {
        Invoke-ApiCase -Name "Get created order" -Method "GET" `
            -Path "/api/orders/$($order.Data.orderId)" -ExpectedStatus 200 | Out-Null
        Invoke-ApiCase -Name "List shopper orders" -Method "GET" `
            -Path "/api/shoppers/$shopperId/orders" -ExpectedStatus 200 | Out-Null
    }
    Invoke-ApiCase -Name "Reject checkout of empty cart" -Method "POST" `
        -Path "/api/carts/$shopperId/checkout" -ExpectedStatus 400 `
        -Body @{ paymentMethod = "DEMO" } | Out-Null
    Invoke-ApiCase -Name "Reject unknown product" -Method "GET" `
        -Path "/api/products/999999999" -ExpectedStatus 404 | Out-Null
    Invoke-ApiCase -Name "Reject negative price" -Method "POST" -Path "/api/products" `
        -ExpectedStatus 400 -Body @{
            name = "Invalid-$([guid]::NewGuid().ToString('N').Substring(0, 8))"
            category = "CLOTHES"
            price = -1
            currency = "USD"
            stockQuantity = 1
        } | Out-Null

    if ($order.Passed) {
        $expectedTotal = [math]::Round(
            ([double]$mainProduct.price * $checkoutQuantity) +
            ([double]$secondaryProduct.price * $secondaryQuantity), 2)
        $actualTotal = [math]::Round([double]$order.Data.total, 2)
        $mainOrderLine = $order.Data.items | Where-Object { $_.productId -eq $mainProduct.productId } | Select-Object -First 1
        $secondaryOrderLine = $order.Data.items | Where-Object { $_.productId -eq $secondaryProduct.productId } | Select-Object -First 1
        $totalMatches = $expectedTotal -eq $actualTotal -and
            $null -ne $mainOrderLine -and $mainOrderLine.quantity -eq $checkoutQuantity -and
            $null -ne $secondaryOrderLine -and $secondaryOrderLine.quantity -eq $secondaryQuantity
        $results.Add([pscustomobject]@{
            Case = "Verify randomized order lines and total"
            Expected = "$checkoutQuantity + $secondaryQuantity units, total $expectedTotal USD"
            Actual = "$(($order.Data.items | Measure-Object -Property quantity -Sum).Sum) units, total $actualTotal $($order.Data.currency)"
            Result = if ($totalMatches) { "PASS" } else { "FAIL" }
            Detail = "Order $($order.Data.orderId)"
            Data = $null
        })
        $mainStock = Invoke-ApiCase -Name "Read main product stock after checkout" -Method "GET" `
            -Path "/api/products/$($mainProduct.productId)" -ExpectedStatus 200
        $expectedMainStock = [long]$mainProduct.stockQuantity - $checkoutQuantity
        $results.Add([pscustomobject]@{
            Case = "Verify main product stock reduction"
            Expected = $expectedMainStock
            Actual = $mainStock.Data.stockQuantity
            Result = if ($mainStock.Data.stockQuantity -eq $expectedMainStock) { "PASS" } else { "FAIL" }
            Detail = "Product $($mainProduct.productId)"
            Data = $null
        })
        $secondaryStock = Invoke-ApiCase -Name "Read second product stock after checkout" -Method "GET" `
            -Path "/api/products/$($secondaryProduct.productId)" -ExpectedStatus 200
        $expectedSecondaryStock = [long]$secondaryProduct.stockQuantity - $secondaryQuantity
        $results.Add([pscustomobject]@{
            Case = "Verify second product stock reduction"
            Expected = $expectedSecondaryStock
            Actual = $secondaryStock.Data.stockQuantity
            Result = if ($secondaryStock.Data.stockQuantity -eq $expectedSecondaryStock) { "PASS" } else { "FAIL" }
            Detail = "Product $($secondaryProduct.productId)"
            Data = $null
        })
        $emptyCart = Invoke-ApiCase -Name "Read cart after checkout" -Method "GET" `
            -Path "/api/carts/$shopperId" -ExpectedStatus 200
        $cartIsEmpty = $emptyCart.Data.items.Count -eq 0
        $results.Add([pscustomobject]@{
            Case = "Verify cart emptied"
            Expected = 0
            Actual = $emptyCart.Data.items.Count
            Result = if ($cartIsEmpty) { "PASS" } else { "FAIL" }
            Detail = "Shopper $shopperId"
            Data = $null
        })
    }

    $deleteCandidate = $products[$products.Count - 1]
    Invoke-ApiCase -Name "Delete unused random product" -Method "DELETE" `
        -Path "/api/products/$($deleteCandidate.productId)" -ExpectedStatus 204 | Out-Null
}
catch {
    Write-Error $_
}
finally {
    $client.Dispose()
}

foreach ($result in $results) {
    Write-Output ("[{0}] {1} | expected={2} | actual={3}" -f `
        $result.Result, $result.Case, $result.Expected, $result.Actual)
}
$passedCount = @($results | Where-Object { $_.Result -eq "PASS" }).Count
$failedCount = @($results | Where-Object { $_.Result -eq "FAIL" }).Count
Write-Output "RESULTS: $passedCount passed, $failedCount failed, $($results.Count) total"

if ($failedCount -gt 0) { exit 1 }