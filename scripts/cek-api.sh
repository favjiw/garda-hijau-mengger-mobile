#!/usr/bin/env bash
# Cek balasan asli server Garda Hijau dengan akun sendiri.
# Kata sandi diketik tersembunyi dan tidak disimpan. Token TIDAK ikut ditulis ke file hasil.
# Hasil: build/cek-api/<endpoint>.json
#
# Jalankan dari folder proyek:  bash scripts/cek-api.sh

set -u
BASE="${BASE_URL:-https://gardahijaukemdiktisaintek.digital}"
OUT="build/cek-api"
mkdir -p "$OUT"

read -r -p "Nama pengguna: " USERNAME
read -r -s -p "Kata sandi: " PASSWORD
echo

# Escape backslash dan tanda kutip supaya kata sandi apa pun tetap jadi JSON yang valid.
esc() {
  local v="$1"
  v="${v//\\/\\\\}"
  v="${v//\"/\\\"}"
  printf '%s' "$v"
}

LOGIN=$(printf '{"username":"%s","password":"%s"}' "$(esc "$USERNAME")" "$(esc "$PASSWORD")" \
  | curl -sS -m 30 -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" --data-binary @-)
unset PASSWORD

TOKEN=$(printf '%s' "$LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -z "$TOKEN" ]; then
  echo "Login gagal: $LOGIN"
  exit 1
fi
printf '%s' "$LOGIN" | sed 's/"token":"[^"]*"/"token":"<disembunyikan>"/' > "$OUT/login.json"
echo "Login berhasil."

ambil() {
  local nama="$1" path="$2"
  local kode
  kode=$(curl -sS -m 60 -o "$OUT/$nama.json" -w "%{http_code}" "$BASE$path" -H "Authorization: Bearer $TOKEN")
  echo "[$kode] GET $path -> $OUT/$nama.json"
}

ambil me /api/auth/me
ambil siklus /api/siklus
ambil pencacahan /api/pencacahan
ambil prediksi-total /api/prediksi/total
ambil prediksi-opsi /api/prediksi/opsi
ambil wadah-aktif /api/prediksi/wadah-aktif
ambil notifikasi /api/notifikasi

SIKLUS_ID=$(grep -o '"id":[0-9]*' "$OUT/siklus.json" | head -1 | cut -d: -f2)
if [ -n "$SIKLUS_ID" ]; then
  ambil "fase-siklus-$SIKLUS_ID" "/api/siklus/$SIKLUS_ID/fase"
  ambil "prediksi-siklus-$SIKLUS_ID" "/api/prediksi/siklus/$SIKLUS_ID"
else
  echo "Belum ada siklus; fase dan prediksi per siklus dilewati."
fi

unset TOKEN
echo "Selesai. File di $OUT tidak berisi token atau kata sandi."
echo "Kirim isi folder $OUT supaya model aplikasi bisa dicocokkan dengan balasan asli server."
