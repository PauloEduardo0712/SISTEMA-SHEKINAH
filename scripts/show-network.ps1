$ErrorActionPreference = "Stop"

. "$PSScriptRoot\network.ps1"

$ips = Get-ShekinahNetworkIps

Write-Host "IPs deste computador:"
$ips | Format-Table -AutoSize

$mainIp = Get-ShekinahLanIp

if ($mainIp) {
    Write-Host ""
    Write-Host "Use em outros computadores/celular:"
    Write-Host "  API: http://${mainIp}:8081/api"
    Write-Host "  Frontend web: http://${mainIp}:5173"
}
