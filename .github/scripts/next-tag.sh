#!/usr/bin/env bash
# Prints the next release tag: highest vX.Y.Z (strict, no suffix) with PATCH+1.
# Reads tag names (one per line) from stdin. No such tag -> v0.3.0.
# Usage: git tag | .github/scripts/next-tag.sh        (also: --latest prints the highest tag or nothing)
set -eu

latest_tag() {
  grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | sort -t. -k1.2,1n -k2,2n -k3,3n | tail -n 1 || true
}

next_tag() {
  local latest major minor patch
  latest=$(latest_tag)
  if [ -z "$latest" ]; then
    echo "v0.3.0"
    return
  fi
  IFS=. read -r major minor patch <<< "${latest#v}"
  echo "v$((10#$major)).$((10#$minor)).$((10#$patch + 1))"
}

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  case "${1:-}" in
    --latest) latest_tag ;;
    *) next_tag ;;
  esac
fi
