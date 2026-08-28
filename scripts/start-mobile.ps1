$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$mobileDir = Join-Path $root "mobile"

. "$PSScriptRoot\network.ps1"

$ip = Get-ShekinahLanIp

if (-not $ip) {
    throw "Nao encontrei um IP de rede local. Verifique Wi-Fi/Ethernet."
}

$env:EXPO_PUBLIC_API_BASE_URL = "http://${ip}:8081/api"

Set-Location $mobileDir

Write-Host "Mobile/Expo:"
Write-Host "  API usada pelo app: $env:EXPO_PUBLIC_API_BASE_URL"
Write-Host "  Expo: porta 8082"
Write-Host ""
Write-Host "Se o celular nao abrir, confira se ele esta na mesma rede Wi-Fi do computador."
Write-Host ""

npx expo start --lan --port 8082
