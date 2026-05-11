#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${KAVITA_URL:-}" || -z "${KAVITA_TOKEN:-}" ]]; then
  cat >&2 <<'USAGE'
Usage:
  KAVITA_URL="http://host:5051/" KAVITA_TOKEN="your-key-or-opds-token" scripts/kavita_probe.sh

Optional:
  KAVITA_LIBRARY_ID=10 scripts/kavita_probe.sh
USAGE
  exit 2
fi

base_url="${KAVITA_URL%/}/"
token="$KAVITA_TOKEN"
library_id="${KAVITA_LIBRARY_ID:-}"

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

request() {
  local output="$1"
  shift
  curl -L -sS --max-time 15 -o "$output" -w "%{http_code}" "$@"
}

print_result() {
  local name="$1"
  local code="$2"
  local file="$3"
  printf '%-28s HTTP %s' "$name" "$code"
  if [[ "$code" =~ ^2 ]]; then
    printf ' OK\n'
  else
    printf ' FAILED\n'
    sed -n '1,8p' "$file" | sed 's/^/  /'
  fi
}

echo "Kavita probe"
echo "Base URL: $base_url"
echo

rest_file="$tmp_dir/rest-libraries.json"
rest_code="$(request "$rest_file" -H "x-api-key: $token" "${base_url}api/Library/libraries")"
print_result "REST libraries" "$rest_code" "$rest_file"

opds_root_file="$tmp_dir/opds-root.xml"
opds_root_code="$(request "$opds_root_file" "${base_url}api/opds/${token}")"
print_result "OPDS root" "$opds_root_code" "$opds_root_file"

opds_libraries_file="$tmp_dir/opds-libraries.xml"
opds_libraries_code="$(request "$opds_libraries_file" "${base_url}api/opds/${token}/libraries")"
print_result "OPDS libraries" "$opds_libraries_code" "$opds_libraries_file"

if [[ "$opds_libraries_code" =~ ^2 ]]; then
  echo
  echo "OPDS libraries:"
  grep -E '<id>|<title>' "$opds_libraries_file" |
    sed -E 's/<[^>]+>//g; s/^[[:space:]]+//; s/[[:space:]]+$//' |
    awk 'NR > 2 { print "  " $0 }' |
    head -30
fi

if [[ -n "$library_id" ]]; then
  opds_books_file="$tmp_dir/opds-books.xml"
  opds_books_code="$(request "$opds_books_file" "${base_url}api/opds/${token}/libraries/${library_id}")"
  echo
  print_result "OPDS library ${library_id}" "$opds_books_code" "$opds_books_file"
  if [[ "$opds_books_code" =~ ^2 ]]; then
    echo
    echo "First entries:"
    grep -E '<id>|<title>|<summary>' "$opds_books_file" |
      sed -E 's/<[^>]+>//g; s/^[[:space:]]+//; s/[[:space:]]+$//' |
      awk 'NR > 2 { print "  " $0 }' |
      head -40
  fi
fi

echo
echo "Result:"
if [[ "$rest_code" =~ ^2 ]]; then
  echo "  Use REST Auth Key mode."
elif [[ "$opds_root_code" =~ ^2 && "$opds_libraries_code" =~ ^2 ]]; then
  echo "  Use OPDS fallback mode."
else
  echo "  Neither REST nor OPDS succeeded. Check URL, token, network, or Kavita permissions."
  exit 1
fi
