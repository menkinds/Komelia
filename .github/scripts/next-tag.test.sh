#!/usr/bin/env bash
# Test for next-tag.sh: bash .github/scripts/next-tag.test.sh
set -u
script="$(dirname "$0")/next-tag.sh"
fail=0
check() { # name expected tags...
  local name=$1 expected=$2; shift 2
  local got
  got=$(printf '%s\n' "$@" | "$script")
  if [ "$got" = "$expected" ]; then echo "ok   $name"; else echo "FAIL $name: expected $expected got $got"; fail=1; fi
}
got=$(printf '' | "$script"); [ "$got" = v0.3.0 ] && echo "ok   no tags" || { echo "FAIL no tags: $got"; fail=1; }
check "only non-matching tags" v0.3.0 v1.0.0-rc1 foo
check "v0.2.0" v0.2.1 v0.2.0
check "numeric sort" v0.10.1 v0.2.9 v0.10.0 v0.2.0
check "ignores suffix tags" v0.2.1 v0.2.0 v1.0.0-rc1 v1.0.0-beta
check "order independent" v1.0.1 v0.9.9 v1.0.0 v0.10.3
got=$(printf 'v0.2.9\nv0.10.0\nv1.0.0-rc1\n' | "$script" --latest); [ "$got" = v0.10.0 ] && echo "ok   --latest" || { echo "FAIL --latest: $got"; fail=1; }
exit $fail
