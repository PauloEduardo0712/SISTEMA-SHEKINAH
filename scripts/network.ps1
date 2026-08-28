function Get-ShekinahLanIp {
    $adapterNamesToIgnore = @(
        "Loopback*",
        "vEthernet*",
        "VMware*",
        "VirtualBox*",
        "Bluetooth*"
    )

    $ips = Get-NetIPAddress -AddressFamily IPv4 |
        Where-Object {
            $_.IPAddress -notlike "127.*" -and
            $_.IPAddress -notlike "169.254.*"
        }

    foreach ($pattern in $adapterNamesToIgnore) {
        $ips = $ips | Where-Object { $_.InterfaceAlias -notlike $pattern }
    }

    $ips |
        Sort-Object `
            @{ Expression = { if ($_.InterfaceAlias -match "Wi-Fi|Wireless|Ethernet") { 0 } else { 1 } } },
            InterfaceMetric |
        Select-Object -First 1 -ExpandProperty IPAddress
}

function Get-ShekinahNetworkIps {
    Get-NetIPAddress -AddressFamily IPv4 |
        Where-Object {
            $_.IPAddress -notlike "127.*" -and
            $_.IPAddress -notlike "169.254.*"
        } |
        Select-Object IPAddress, InterfaceAlias
}
