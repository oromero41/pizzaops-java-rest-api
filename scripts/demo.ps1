$ErrorActionPreference = 'Stop'
$root = 'http://localhost:8080'
Write-Host "`n1. HEALTH"
Invoke-RestMethod "$root/actuator/health" | ConvertTo-Json
Write-Host "`n2. CREATE ORDER"
$body = @{ size='SMALL'; base='ORIGINAL'; sauce='TOMATO'; cheese='MOZZARELLA'; toppings=@('HAM','OLIVES','PEPPER','CHICKEN','BACON') } | ConvertTo-Json
$order = Invoke-RestMethod -Uri "$root/api/v1/orders" -Method Post -ContentType 'application/json' -Body $body
$order | ConvertTo-Json -Depth 5
Write-Host "`n3. STATISTICS"
Invoke-RestMethod "$root/api/v1/statistics" | ConvertTo-Json -Depth 6
