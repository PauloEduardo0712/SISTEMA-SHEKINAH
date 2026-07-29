$ErrorActionPreference = "Stop"

$ruleScript = @"
New-NetFirewallRule -DisplayName 'Sistema Shekinah Frontend 5173' -Direction Inbound -Action Allow -Protocol TCP -LocalPort 5173 -Profile Any -ErrorAction SilentlyContinue
New-NetFirewallRule -DisplayName 'Sistema Shekinah Backend 8081' -Direction Inbound -Action Allow -Protocol TCP -LocalPort 8081 -Profile Any -ErrorAction SilentlyContinue
Write-Host 'Regras do Sistema Shekinah liberadas no Windows Firewall.'
Read-Host 'Pressione Enter para fechar'
"@

$encoded = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($ruleScript))
Start-Process powershell.exe -Verb RunAs -ArgumentList "-NoProfile -EncodedCommand $encoded"
