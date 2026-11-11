#!/usr/bin/env bash
# Kiểm tra môi trường thực hành CT230 (Ubuntu/Linux, macOS, WSL2)
# Chạy từ thư mục starter:  ./scripts/kiem_tra_moi_truong.sh
# Mỗi mục được đánh giá theo NỘI DUNG đầu ra (không dựa vào mã thoát của đường ống lệnh).
set -u
loi=0
ok()       { printf '  [OK]        %-15s %s\n' "$1" "$2"; }
hong()     { printf '  [LỖI]       %-15s %s\n' "$1" "$2"; loi=1; }
canh_bao() { printf '  [CẢNH BÁO]  %s\n' "$1"; }

# chay <mẫu> <lệnh...>: chạy lệnh (gộp stderr); in dòng đầu khớp mẫu và trả 0,
# hoặc in dòng đầu tiên không rỗng và trả 1.
chay() {
  local mau="$1"; shift
  local out khop
  out=$("$@" 2>&1) || true
  khop=$(printf '%s\n' "$out" | grep -m1 -E -- "$mau")
  if [ -n "$khop" ]; then printf '%s' "$khop"; return 0; fi
  printf '%s' "$(printf '%s\n' "$out" | grep -m1 -v '^[[:space:]]*$')"
  return 1
}

goi_y_java_home() {
  local d
  d=$(ls -d /usr/lib/jvm/java-21-openjdk-* /usr/lib/jvm/temurin-21-jdk-* 2>/dev/null | head -1)
  printf '%s' "${d:-/usr/lib/jvm/java-21-openjdk-amd64}"
}

echo "== Kiểm tra môi trường CT230 v7 =="

# 1. JDK 21 trở lên (Spring Boot 4 cần Java 21)
if command -v java >/dev/null 2>&1; then
  r=$(chay 'version "' java -version)
  jv=$(printf '%s' "$r" | sed -nE 's/.*version "([0-9]+).*/\1/p')
  if [ "${jv:-0}" -ge 21 ]; then ok "JDK" "$r"
  else hong "JDK" "đang là Java ${jv:-?} — cần JDK 21: sudo apt install openjdk-21-jdk; sudo update-alternatives --config java"; fi
else
  hong "JDK" "chưa cài — sudo apt install openjdk-21-jdk"
fi

# 2. Maven wrapper và JDK mà Maven thực sự dùng (JAVA_HOME, nếu không có thì java trong PATH)
if [ ! -f ./mvnw ]; then
  hong "Maven wrapper" "không thấy ./mvnw — hãy chạy script từ thư mục starter"
elif grep -q $'\r' ./mvnw; then
  hong "Maven wrapper" "mvnw có ký tự xuống dòng Windows (CRLF) — sed -i 's/\r\$//' mvnw scripts/*.sh"
elif [ ! -x ./mvnw ]; then
  hong "Maven wrapper" "mvnw chưa có quyền thực thi — chmod +x mvnw scripts/*.sh"
else
  mv_out=$(./mvnw -v 2>&1) || true
  r=$(printf '%s\n' "$mv_out" | grep -m1 -E 'Apache Maven [0-9]')
  if [ -n "$r" ]; then
    ok "Maven wrapper" "$r"
    jm=$(printf '%s\n' "$mv_out" | sed -nE 's/^Java version: ([0-9]+).*/\1/p' | head -1)
    if [ "${jm:-0}" -ge 21 ]; then ok "JDK (Maven)" "Java $jm — JAVA_HOME=${JAVA_HOME:-(chưa đặt, dùng java trong PATH)}"
    else hong "JDK (Maven)" "Maven đang chạy bằng Java ${jm:-?} (JAVA_HOME=${JAVA_HOME:-chưa đặt}) — export JAVA_HOME=$(goi_y_java_home)"; fi
  else
    hong "Maven wrapper" "chạy ./mvnw -v lỗi: $(printf '%s\n' "$mv_out" | grep -m1 -v '^[[:space:]]*$')"
  fi
fi

# 3. Docker Engine (daemon phải đang chạy) và Docker Compose v2
if command -v docker >/dev/null 2>&1; then
  if r=$(chay '^[0-9]+\.[0-9]+' docker version --format '{{.Server.Version}}'); then
    ok "Docker Engine" "$r"
  elif printf '%s' "$r" | grep -qi 'permission denied'; then
    hong "Docker Engine" "tài khoản chưa có quyền dùng Docker — sudo usermod -aG docker \$USER, rồi đăng xuất và đăng nhập lại"
  else
    hong "Docker Engine" "chưa chạy (${r:-không có phản hồi}) — sudo systemctl start docker"
  fi
  if r=$(chay '^Docker Compose version' docker compose version); then ok "Docker Compose" "$r"
  else hong "Docker Compose" "thiếu plugin Compose v2 — sudo apt install docker-compose-plugin"; fi
else
  hong "Docker" "chưa cài Docker Engine (xem hướng dẫn cài đặt môi trường)"
fi

# 4. Git
if command -v git >/dev/null 2>&1; then
  if r=$(chay '^git version' git --version); then ok "Git" "$r"; else hong "Git" "chạy git --version lỗi: $r"; fi
else
  hong "Git" "chưa cài — sudo apt install git"
fi

# 5. Cổng của học phần (TH1-1)
for p in 8080 8081 8082 8083 8090 8091 8180 8761 8888; do
  if (exec 3<>/dev/tcp/127.0.0.1/$p) 2>/dev/null; then canh_bao "cổng $p đang bận (xem tiến trình: ss -ltnp | grep :$p)"; fi
done

if [ "$loi" -eq 0 ]; then echo "== Môi trường đạt yêu cầu =="
else echo "== Còn mục [LỖI] — sửa theo hướng dẫn rồi chạy lại =="; fi
exit $loi
