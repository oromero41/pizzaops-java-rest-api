param(
  [Parameter(ValueFromRemainingArguments=$true)]
  [string[]] $MavenArguments
)
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location $projectRoot
$maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($maven) {
    & $maven.Source @MavenArguments
    exit $LASTEXITCODE
}
$version = '3.9.11'
$cache = Join-Path $env:LOCALAPPDATA 'PizzaOps\tools'
$mvnCmd = Join-Path $cache "apache-maven-$version\bin\mvn.cmd"
if (-not (Test-Path $mvnCmd)) {
    New-Item -ItemType Directory -Force -Path $cache | Out-Null
    $zipPath = Join-Path $cache "apache-maven-$version-bin.zip"
    $url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip"
    Write-Host 'Maven is not installed. Downloading Apache Maven from Maven Central (first time only)...'
    Invoke-WebRequest -Uri $url -OutFile $zipPath
    # Official Apache SHA-512 for Maven 3.9.11 (published release archive).
    $expected = '03e2d65d4483a3396980629f260e25cac0d8b6f7f2791e4dc20bc83f9514db8d0f05b0479e699a5f34679250c49c8e52e961262ded468a20de0be254d8207076'
    $actual = (Get-FileHash $zipPath -Algorithm SHA512).Hash
    if ($expected.ToUpperInvariant() -ne $actual.ToUpperInvariant()) {
        Remove-Item $zipPath -ErrorAction SilentlyContinue
        throw 'Maven download hash mismatch! Refusing to run.'
    }
    Expand-Archive -Path $zipPath -DestinationPath $cache -Force
    Remove-Item $zipPath -Force
}
& $mvnCmd @MavenArguments
exit $LASTEXITCODE
