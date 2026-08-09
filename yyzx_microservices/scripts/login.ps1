<#
  yyzx Login Script - get token for API testing
  Usage: .\scripts\login.ps1
  Token saved to scripts\token.txt (valid 2 hours)
  After login, use: call-api /admin/findUserPage?current=1
#>
chcp 65001 > $null
$BASE = "http://localhost:8080/yyzx"

# 1. Get captcha
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  yyzx Login Script" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/3] Getting captcha..." -ForegroundColor Yellow
$captcha = Invoke-RestMethod -Uri "$BASE/admin/generate" -ErrorAction Stop
$uuid = $captcha.data.uuid

# 2. Save captcha image and open it
$imgFile = "$env:TEMP\yyzx_captcha.jpg"
$captcha.data.base64 -replace "^data:image/\w+;base64,", "" |
  ForEach-Object { [Convert]::FromBase64String($_) } |
  Set-Content $imgFile -Encoding Byte
Start-Process $imgFile
Write-Host "[2/3] Captcha image opened: $imgFile" -ForegroundColor Yellow
Write-Host "       Look at the image and find the 4-digit code." -ForegroundColor Gray

# 3. Enter code and login
$code = Read-Host "[3/3] Enter the 4-digit captcha code"
if ([string]::IsNullOrWhiteSpace($code)) {
    Write-Host "ERROR: No code entered!" -ForegroundColor Red
    exit 1
}

$body = @{ username="admin"; password="admin"; captcha=$code; uuid=$uuid } | ConvertTo-Json
Write-Host "Logging in with code: $code ..." -ForegroundColor Gray
$result = Invoke-RestMethod -Uri "$BASE/admin/loginWithCaptcha" -Method POST -Body $body -ContentType "application/json; charset=utf-8"

if ($result.flag) {
    $global:token = $result.message
    $global:token | Set-Content "$PSScriptRoot\token.txt"
    Write-Host ""
    Write-Host "LOGIN SUCCESS!" -ForegroundColor Green
    Write-Host "Token saved to scripts\token.txt (valid 2 hours)" -ForegroundColor Green
    Write-Host ""
    Write-Host "Test commands:" -ForegroundColor Cyan
    Write-Host '  call-api /admin/findUserPage?current=1'
    Write-Host '  call-api /customer/listKhxxPage?current=1\&pageSize=5'
    Write-Host '  call-api /food/listFood'
    Write-Host '  call-api /bed/findBed'
    Write-Host '  call-api /report/customerStats?startDate=2026-07-01\&endDate=2026-08-01'
} else {
    Write-Host "LOGIN FAILED: $($result.message)" -ForegroundColor Red
    Write-Host "Try again: .\scripts\login.ps1" -ForegroundColor Yellow
}

# Register helper function
function global:call-api {
    param([string]$path, [string]$method = "GET", $body = $null)
    $uri = "http://localhost:8080/yyzx$path"
    $headers = @{token = $global:token}
    $params = @{ Uri = $uri; Method = $method; Headers = $headers }
    if ($body) { $params.Body = ($body | ConvertTo-Json -Compress); $params.ContentType = "application/json; charset=utf-8" }
    Invoke-RestMethod @params
}
