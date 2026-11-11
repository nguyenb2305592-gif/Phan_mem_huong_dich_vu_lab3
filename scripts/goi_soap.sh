#!/usr/bin/env bash
# Gọi BookCatalogService bằng curl: ./scripts/goi_soap.sh GetBook scripts/soap/GetBook.xml
set -euo pipefail
ACTION="${1:-GetBook}"; FILE="${2:-scripts/soap/GetBook.xml}"
URL="${LEGACY_URL:-http://localhost:8091/ws/catalog}"
curl -s -X POST "$URL" \
  -H 'Content-Type: text/xml; charset=utf-8' \
  -H "SOAPAction: \"http://bookstore.edu.vn/catalog/${ACTION}\"" \
  --data-binary @"$FILE"
echo
