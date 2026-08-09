<#
  Load saved token (no re-login needed)
  Usage: . .\scripts\token-init.ps1
  If token expired, run: .\scripts\login.ps1
#>
chcp 65001 > $null

if (Test-Path "$PSScriptRoot\token.txt") {
    $global:token = Get-Content "$PSScriptRoot\token.txt"
    Write-Host "Token loaded from scripts\token.txt" -ForegroundColor Green
} else {
    Write-Host "No saved token. Run: .\scripts\login.ps1" -ForegroundColor Yellow
}

function global:call-api {
    param([string]$path, [string]$method = "GET", $body = $null)
    $uri = "http://localhost:8080/yyzx$path"
    $headers = @{token = $global:token}
    $params = @{ Uri = $uri; Method = $method; Headers = $headers }
    if ($body) { $params.Body = ($body | ConvertTo-Json -Compress); $params.ContentType = "application/json; charset=utf-8" }
    Invoke-RestMethod @params
}
