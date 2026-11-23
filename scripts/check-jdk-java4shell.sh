#!/usr/bin/env bash
# SK-3332 - Java4Shell (CVE-2026-114938) evidence step and version guard for CI.
#
# Logs the JDK the job is about to use (JAVA_HOME, else java on PATH), the runner CPU,
# whether the CPU has AVX-512 VBMI and the effective UseBASE64Intrinsics value. Then fails
# the job if that JDK is on an affected feature line (17, 21, 25, 27) and below the fixed
# build. JDK 8 / 11 (and anything below 17) only log evidence: the vulnerable AVX-512
# Base64 decode intrinsic does not exist there.
#
# Fixed builds per the Adoptium CVE-2026-114938 guidance (adoptium.net, 2026-11-18): Temurin
# 17.0.21.1+2, 21.0.13.1+2, 25.0.5.1+2, 27.0.1.1+4, all GA on api.adoptium.net. JDK 8 / 11 are
# listed as not affected. Non-LTS 18-20, 22-24, 26 are not covered by any fix, so they fail closed.
set -uo pipefail

java_bin="java"
if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
  java_bin="${JAVA_HOME}/bin/java"
fi

runtime_version=$("$java_bin" -XshowSettings:properties -version 2>&1 \
  | awk -F' = ' '$1 ~ /^[[:space:]]*java\.runtime\.version$/ { print $2 }' | head -n1)
cpu_model=$(awk -F': ' '/^model name/ { print $2 }' /proc/cpuinfo 2>/dev/null | head -n1)
if grep -qw avx512_vbmi /proc/cpuinfo 2>/dev/null; then vbmi="present"; else vbmi="absent"; fi
# UseBASE64Intrinsics does not exist on JDK 8; the unlock flag keeps it visible if it is
# ever reclassified as diagnostic. JAVA_TOOL_OPTIONS is honoured, so a workaround shows as false.
b64=$("$java_bin" -XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal -version 2>/dev/null \
  | awk '$2 == "UseBASE64Intrinsics" { print $4 }' | head -n1)

echo "::group::Java4Shell (CVE-2026-114938) JDK evidence"
"$java_bin" -version 2>&1
echo "java binary          : $(command -v "$java_bin" || echo "$java_bin")"
echo "java.runtime.version : ${runtime_version:-unknown}"
echo "CPU model            : ${cpu_model:-unknown}"
echo "avx512_vbmi          : ${vbmi}"
echo "UseBASE64Intrinsics  : ${b64:-n/a (flag not present on this JDK)}"
echo "JAVA_TOOL_OPTIONS    : ${JAVA_TOOL_OPTIONS:-<unset>}"
echo "::endgroup::"

if [ -z "$runtime_version" ]; then
  echo "::error::Could not determine the JDK runtime version; refusing to continue (SK-3332)."
  exit 1
fi

# "17.0.21.1+2-LTS" -> base "17.0.21.1", feature "17". Legacy "1.8.0_514-b09" -> feature "1".
base="${runtime_version%%[+-]*}"
feature="${base%%.*}"

case "$feature" in
  17) min="17.0.21.1" ;;
  21) min="21.0.13.1" ;;
  25) min="25.0.5.1" ;;
  27) min="27.0.1.1" ;;
  *)
    if [[ "$feature" =~ ^[0-9]+$ ]] && [ "$feature" -lt 17 ]; then
      echo "JDK ${runtime_version} is outside the Java4Shell affected range (17, 21, 25, 27). OK."
      exit 0
    fi
    # 18-20, 22-24, 26, 28+: carry the vulnerable intrinsic, or are not yet assessed, and
    # have no fixed build. Pin a supported line instead.
    echo "::error::JDK ${runtime_version} has no Java4Shell (CVE-2026-114938) fixed build. Use JDK 8, 11, or a patched 17/21/25/27 (SK-3332)."
    exit 1
    ;;
esac

lowest=$(printf '%s\n%s\n' "$min" "$base" | sort -V | head -n1)
if [ "$lowest" != "$min" ]; then
  echo "::error::JDK ${runtime_version} is vulnerable to Java4Shell (CVE-2026-114938); ${min} or later is required. Set check-latest: true on actions/setup-java (SK-3332)."
  exit 1
fi
echo "JDK ${runtime_version} is at or above the Java4Shell fixed build ${min}. OK."
