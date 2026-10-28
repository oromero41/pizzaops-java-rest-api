$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrEmpty($env:PIZZAOPS_DB_PASSWORD)) {
    $secure = Read-Host 'PostgreSQL password for postgres (input hidden)' -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { $env:PIZZAOPS_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
Write-Host 'Starting PizzaOps with PostgreSQL at localhost:5432/pizzaops...'
& (Join-Path $PSScriptRoot 'maven.ps1') 'spring-boot:run'
if ($LASTEXITCODE -ne 0) { throw "Spring Boot failed (exit code $LASTEXITCODE)" }
