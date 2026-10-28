$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
New-Item -ItemType Directory -Force -Path out | Out-Null
$core = 'PizzaCatalog.java', 'PizzaOrder.java', 'PizzaService.java', 'PizzaStatistics.java', 'PizzaOrderSystem.java'
$files = @($core | ForEach-Object { Join-Path 'src/main/java/com/pizzaops' $_ })
& javac -encoding UTF-8 -d out @files
if ($LASTEXITCODE -ne 0) { throw 'CLI compilation failed' }
& java -cp out com.pizzaops.PizzaOrderSystem
