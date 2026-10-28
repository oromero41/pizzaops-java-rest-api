$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'maven.ps1') 'clean' 'test'
if ($LASTEXITCODE -ne 0) { throw "Tests failed (exit code $LASTEXITCODE)" }
