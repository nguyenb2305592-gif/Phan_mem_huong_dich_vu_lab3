# TH1-1: kiểm tra môi trường thực hành CT230 (Windows PowerShell 5.1 hoặc PowerShell 7)
# Chạy từ thư mục starter:  .\scripts\kiem_tra_moi_truong.ps1
# Tệp lưu dạng UTF-8 có BOM để Windows PowerShell 5.1 hiển thị đúng tiếng Việt.
$ErrorActionPreference = "Continue"
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch {}
$script:loi = 0

function Ghi-OK($ten, $ketQua)     { Write-Host ("  [OK]        {0,-15} {1}" -f $ten, $ketQua) -ForegroundColor Green }
function Ghi-Loi($ten, $huongDan)  { Write-Host ("  [LỖI]       {0,-15} {1}" -f $ten, $huongDan) -ForegroundColor Red; $script:loi = 1 }
function Ghi-CanhBao($noiDung)     { Write-Host ("  [CẢNH BÁO]  {0}" -f $noiDung) -ForegroundColor Yellow }

# Chạy một lệnh ngoài, gom TOÀN BỘ đầu ra (cả stderr) rồi mới xử lý.
# Không dùng "| Select-Object -First 1" trực tiếp sau lệnh ngoài: Windows PowerShell 5.1
# sẽ dừng tiến trình giữa chừng và đặt $LASTEXITCODE khác 0 dù lệnh chạy đúng.
# Kết luận đạt/không đạt dựa vào nội dung đầu ra khớp mẫu $mau, không dựa vào mã thoát.
function Chay([scriptblock]$lenh, [string]$mau = '.') {
  $dong = @()
  try { $dong = @(& $lenh 2>&1 | ForEach-Object { "$_".Trim() } | Where-Object { $_ -ne '' }) }
  catch { $dong = @("$_") }
  $khop = @($dong | Where-Object { $_ -match $mau })
  if ($khop.Count -gt 0) { return @{ Ok = $true;  Out = $khop[0]; All = $dong } }
  if ($dong.Count -gt 0) { return @{ Ok = $false; Out = $dong[0]; All = $dong } }
  return @{ Ok = $false; Out = '(không có đầu ra)'; All = $dong }
}

Write-Host "== Kiểm tra môi trường CT230 v7 =="

# 1. JDK 21 trở lên (Spring Boot 4 yêu cầu Java 21)
if (Get-Command java -ErrorAction SilentlyContinue) {
  $r = Chay { java -version } 'version "'
  $phienBan = 0
  if ("$($r.Out)" -match '"(\d+)') { $phienBan = [int]$Matches[1] }
  if ($phienBan -ge 21) { Ghi-OK "JDK" $r.Out }
  else { Ghi-Loi "JDK" "đang là Java $phienBan — cần JDK 21 (Temurin 21), đặt JAVA_HOME và PATH rồi mở lại cửa sổ PowerShell" }
} else { Ghi-Loi "JDK" "chưa cài — cài Eclipse Temurin 21" }

# 2. Maven wrapper (lần đầu chạy sẽ tải Maven về %USERPROFILE%\.m2)
if (Test-Path .\mvnw.cmd) {
  $r = Chay { .\mvnw.cmd -v } 'Apache Maven \d'
  if ($r.Ok) {
    Ghi-OK "Maven wrapper" $r.Out
    # Maven KHÔNG dùng java trong PATH mà dùng JAVA_HOME: kiểm tra JDK mà Maven thực sự chạy
    $dongJava = @($r.All | Where-Object { $_ -match 'Java version:\s*(\d+)' }) | Select-Object -First 1
    $javaMaven = 0
    if ("$dongJava" -match 'Java version:\s*(\d+)') { $javaMaven = [int]$Matches[1] }
    $javaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { '(chưa đặt)' }
    if ($javaMaven -ge 21) { Ghi-OK "JDK của Maven" "Java $javaMaven — JAVA_HOME=$javaHome" }
    else {
      # Tìm thư mục JDK 21 đã cài ở các vị trí thường gặp; nếu không có thì suy từ java trong PATH
      $goiY = Get-ChildItem 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Java', 'C:\Program Files\Microsoft' -Directory -Filter 'jdk-21*' -ErrorAction SilentlyContinue |
              Select-Object -First 1 -ExpandProperty FullName
      if (-not $goiY) {
        $javaPath = Get-Command java -ErrorAction SilentlyContinue
        if ($javaPath -and $javaPath.Source -notmatch 'javapath') { $goiY = Split-Path (Split-Path $javaPath.Source) }
        else { $goiY = 'C:\Program Files\Eclipse Adoptium\jdk-21...' }
      }
      Ghi-Loi "JDK của Maven" "Maven đang chạy bằng Java $javaMaven (JAVA_HOME=$javaHome). Đặt JAVA_HOME về JDK 21, ví dụ: `$env:JAVA_HOME = '$goiY' rồi chạy lại"
    }
  } else { Ghi-Loi "Maven wrapper" "chạy .\mvnw.cmd -v lỗi: $($r.Out)" }
} else { Ghi-Loi "Maven wrapper" "không thấy mvnw.cmd — hãy chạy script từ thư mục starter" }

# 3. Docker: cần cả client lẫn Docker Engine đang chạy
if (Get-Command docker -ErrorAction SilentlyContinue) {
  $r = Chay { docker version --format '{{.Server.Version}}' } '^\d+\.\d+'
  if ($r.Ok) { Ghi-OK "Docker Engine" $r.Out }
  else { Ghi-Loi "Docker Engine" "chưa chạy ($($r.Out)) — mở Docker Desktop, đợi 'Engine running' rồi chạy lại" }
  $r = Chay { docker compose version } 'Docker Compose version'
  if ($r.Ok) { Ghi-OK "Docker Compose" $r.Out } else { Ghi-Loi "Docker Compose" "lỗi: $($r.Out)" }
} else { Ghi-Loi "Docker" "chưa cài Docker Desktop (WSL 2)" }

# 4. Git
if (Get-Command git -ErrorAction SilentlyContinue) {
  $r = Chay { git --version } '^git version'
  if ($r.Ok) { Ghi-OK "Git" $r.Out } else { Ghi-Loi "Git" "chạy git --version lỗi: $($r.Out)" }
} else { Ghi-Loi "Git" "không thấy git trong PATH — cài Git for Windows rồi mở lại PowerShell" }

# 5. Cổng của học phần
$congHocPhan = 8080,8081,8082,8083,8090,8091,8180,8761,8888
if (Get-Command Get-NetTCPConnection -ErrorAction SilentlyContinue) {
  foreach ($p in $congHocPhan) {
    if (Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue) { Ghi-CanhBao "cổng $p đang bận" }
  }
} elseif (Get-Command netstat -ErrorAction SilentlyContinue) {
  $dangNghe = netstat -an 2>$null | Select-String "LISTEN"
  foreach ($p in $congHocPhan) { if ($dangNghe -match "[:.]$p\s") { Ghi-CanhBao "cổng $p đang bận" } }
}

if ($script:loi -eq 0) { Write-Host "== Môi trường đạt yêu cầu ==" -ForegroundColor Green }
else { Write-Host "== Còn mục [LỖI] — sửa theo hướng dẫn rồi chạy lại ==" -ForegroundColor Red }
exit $script:loi
