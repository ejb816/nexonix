#!/usr/bin/env bash
# Report by default; explicit --fix only creates missing agent links.
set -u
root="${1:?usage: check_links.sh <repo-root> [--fix]}"
mode="${2:-}"
case "$mode" in
  ''|--fix) ;;
  *) echo "ERROR: unknown mode $mode"; exit 2 ;;
esac
cd "$root" || exit 2
[ -f DRACO.md ] || { echo "ERROR: DRACO.md not found in $root"; exit 2; }
rc=0
for link in CLAUDE.md AGENTS.md; do
  if [ -L "$link" ]; then
    target="$(readlink "$link")"
    if [ "$target" = DRACO.md ]; then
      echo "OK       $link -> DRACO.md"
    else
      echo "CONFLICT $link -> $target; unchanged"
      rc=1
    fi
  elif [ -e "$link" ]; then
    echo "CONFLICT $link exists and is not a symlink; unchanged"
    rc=1
  elif [ "$mode" = --fix ]; then
    if ln -s DRACO.md "$link"; then
      echo "CREATED  $link -> DRACO.md"
    else
      rc=1
    fi
  else
    echo "MISSING  $link; unchanged"
    rc=1
  fi
  if git ls-files --error-unmatch -- "$link" >/dev/null 2>&1; then
    echo "INFO     $link is tracked"
  fi
done
if [ -L .claude ]; then echo "INFO     .claude -> $(readlink .claude)"; fi
exit "$rc"
