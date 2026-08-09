#!/usr/bin/env bash
# Manual/CI test harness for toggle_beta_banner.sh.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOGGLE="$SCRIPT_DIR/toggle_beta_banner.sh"
FIXTURE="$(mktemp)"
MARKER_START="<!-- SKYFLOW-BETA-DISCLAIMER:START -->"

fail() {
    echo "FAIL: $1"
    rm -f "$FIXTURE"
    exit 1
}

cat > "$FIXTURE" <<'EOF'
# skyflow-android
---
Some intro text.
EOF

# 1. Beta version inserts the banner.
"$TOGGLE" "$FIXTURE" "1.28.0-beta.1"
[ "$(grep -c -F "$MARKER_START" "$FIXTURE")" -eq 1 ] || fail "banner not inserted for beta version"

# 2. Re-running with the same beta version stays idempotent (no duplicate).
"$TOGGLE" "$FIXTURE" "1.28.0-beta.1"
[ "$(grep -c -F "$MARKER_START" "$FIXTURE")" -eq 1 ] || fail "banner duplicated on repeat run"

# 3. GA version removes the banner.
"$TOGGLE" "$FIXTURE" "1.28.0"
[ "$(grep -c -F "$MARKER_START" "$FIXTURE")" -eq 0 ] || fail "banner not removed for GA version"

# 4. Original content survives untouched.
grep -qF "Some intro text." "$FIXTURE" || fail "original README content lost"

rm -f "$FIXTURE"
echo "PASS: toggle_beta_banner.sh"
