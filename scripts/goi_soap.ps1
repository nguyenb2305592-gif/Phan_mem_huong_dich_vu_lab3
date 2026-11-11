# Gọi BookCatalogService từ PowerShell: .\scripts\goi_soap.ps1 GetBook scripts\soap\GetBook.xml
param([string]$Action = "GetBook", [string]$File = "scripts\soap\GetBook.xml")
$url = if ($env:LEGACY_URL) { $env:LEGACY_URL } else { "http://localhost:8091/ws/catalog" }
$body = Get-Content -Raw -Encoding UTF8 $File
try {
  (Invoke-WebRequest -Uri $url -Method Post -Body $body -ContentType "text/xml; charset=utf-8" `
     -Headers @{ SOAPAction = "`"http://bookstore.edu.vn/catalog/$Action`"" }).Content
} catch { $_.Exception.Response.GetResponseStream() | % { (New-Object IO.StreamReader($_)).ReadToEnd() } }
