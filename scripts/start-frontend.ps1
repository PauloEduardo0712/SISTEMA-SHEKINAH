$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $root "frontend"

. "$PSScriptRoot\network.ps1"

$ip = Get-ShekinahLanIp

Set-Location $frontendDir
$env:VITE_API_BASE_URL = "/api"

Write-Host "Frontend web legado:"
Write-Host "  Local: http://localhost:5173"
if ($ip) {
    Write-Host "  Rede:  http://${ip}:5173"
    Write-Host "  API:   /api pelo proxy do Vite para http://localhost:8081"
} else {
    Write-Host "  Rede:  http://SEU-IP:5173"
    Write-Host "  API:   /api pelo proxy do Vite para http://localhost:8081"
}
Write-Host ""

npm.cmd run dev -- --host 0.0.0.0 --port 5173
