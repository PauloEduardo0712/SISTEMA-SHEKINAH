$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $root "backend"

. "$PSScriptRoot\network.ps1"

Set-Location $backendDir

$env:SERVER_PORT = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { "8081" }
$env:SERVER_ADDRESS = if ($env:SERVER_ADDRESS) { $env:SERVER_ADDRESS } else { "0.0.0.0" }
$ip = Get-ShekinahLanIp

Write-Host "Subindo MySQL..."
docker compose up -d

Write-Host ""
Write-Host "Backend:"
Write-Host "  Local: http://localhost:$env:SERVER_PORT/api"
if ($ip) {
    Write-Host "  Rede:  http://${ip}:$env:SERVER_PORT/api"
} else {
    Write-Host "  Rede:  http://SEU-IP:$env:SERVER_PORT/api"
}
Write-Host ""
Write-Host "Para descobrir seu IP, rode: .\scripts\show-network.ps1"
Write-Host ""

.\gradlew.bat bootRun
