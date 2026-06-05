#!/usr/bin/env bash
set -euo pipefail

readonly platform_package="platforms;android-37"

mapfile -t build_tools_packages < <(
  sdkmanager --channel=3 --list |
    awk -F '|' '{ gsub(/^[[:space:]]+|[[:space:]]+$/, "", $1); if ($1 ~ /^build-tools;37([.].*)?$/) print $1 }' |
    sort -Vu
)

if (( ${#build_tools_packages[@]} == 0 )); then
  echo "No Android Build Tools 37.x package is available from sdkmanager channel 3." >&2
  exit 1
fi

# Prefer the newest stable 37.x package; fall back to the newest RC while Android 17 is in preview.
mapfile -t stable_build_tools_packages < <(
  printf '%s\n' "${build_tools_packages[@]}" |
    awk '/^build-tools;37[.][0-9]+[.][0-9]+$/' |
    sort -V
)

if (( ${#stable_build_tools_packages[@]} > 0 )); then
  build_tools_package="${stable_build_tools_packages[-1]}"
else
  build_tools_package="${build_tools_packages[-1]}"
fi
build_tools_version="${build_tools_package#build-tools;}"

sdkmanager --channel=3 "platform-tools" "$platform_package" "$build_tools_package"

test -f "$ANDROID_SDK_ROOT/platforms/android-37/android.jar"
test -x "$ANDROID_SDK_ROOT/build-tools/$build_tools_version/aapt2"

if [[ -n "${GITHUB_ENV:-}" ]]; then
  echo "ANDROID_BUILD_TOOLS_VERSION=$build_tools_version" >> "$GITHUB_ENV"
fi

echo "Android SDK 37 installed with Build Tools $build_tools_version"
