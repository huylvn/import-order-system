#!/usr/bin/env bash
set -euo pipefail

SKIP_TESTS="${SKIP_TESTS:-false}"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="$PROJECT_ROOT/.tools"
JDK_DIR="$TOOLS_DIR/jdk-21"
MAVEN_DIR="$TOOLS_DIR/apache-maven-3.9.9"
DOWNLOADS_DIR="$TOOLS_DIR/downloads"
MAVEN_TGZ="$DOWNLOADS_DIR/apache-maven-3.9.9-bin.tar.gz"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz"

ARCH="$(uname -m)"
case "$ARCH" in
  arm64|aarch64)
    ADOPTIUM_ARCH="aarch64"
    ;;
  x86_64|amd64)
    ADOPTIUM_ARCH="x64"
    ;;
  *)
    echo "Unsupported macOS architecture: $ARCH" >&2
    exit 1
    ;;
esac

JDK_TGZ="$DOWNLOADS_DIR/jdk-21-macos-$ADOPTIUM_ARCH.tar.gz"
JDK_URL="https://api.adoptium.net/v3/binary/latest/21/ga/mac/$ADOPTIUM_ARCH/jdk/hotspot/normal/eclipse?project=jdk"

download_file() {
  local url="$1"
  local output="$2"
  if [[ -f "$output" ]]; then
    return
  fi
  echo "Downloading $url"
  curl -L "$url" -o "$output"
}

extract_single_root_archive() {
  local archive="$1"
  local destination="$2"
  local temp_dir
  temp_dir="$(mktemp -d "$TOOLS_DIR/extract.XXXXXX")"
  tar -xzf "$archive" -C "$temp_dir"
  local root
  root="$(find "$temp_dir" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
  if [[ -z "$root" ]]; then
    echo "Archive does not contain a root directory: $archive" >&2
    exit 1
  fi
  rm -rf "$destination"
  mv "$root" "$destination"
  rm -rf "$temp_dir"
}

mkdir -p "$DOWNLOADS_DIR"

if [[ ! -d "$JDK_DIR" ]]; then
  download_file "$JDK_URL" "$JDK_TGZ"
  extract_single_root_archive "$JDK_TGZ" "$JDK_DIR"
fi

if [[ ! -d "$MAVEN_DIR" ]]; then
  download_file "$MAVEN_URL" "$MAVEN_TGZ"
  extract_single_root_archive "$MAVEN_TGZ" "$MAVEN_DIR"
fi

if [[ -d "$JDK_DIR/Contents/Home" ]]; then
  export JAVA_HOME="$JDK_DIR/Contents/Home"
else
  export JAVA_HOME="$JDK_DIR"
fi

export PATH="$JAVA_HOME/bin:$MAVEN_DIR/bin:$PATH"

cd "$PROJECT_ROOT"
java -version
mvn -version

if [[ "$SKIP_TESTS" != "true" ]]; then
  mvn test
fi

mvn javafx:run
