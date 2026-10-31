#!/usr/bin/env bash
# Merge the latest Snd-R/Komelia release into a sync branch and open a PR to main.
# On conflict: no PR, an issue listing the conflicting files is opened instead.
# Needs: git, gh, GH_TOKEN, GITHUB_REPOSITORY. Run with: bash .github/scripts/upstream-sync.sh
set -euo pipefail

UPSTREAM_REPO="${UPSTREAM_REPO:-Snd-R/Komelia}"
BASE_BRANCH="${BASE_BRANCH:-main}"

tag=$(gh api "repos/${UPSTREAM_REPO}/releases/latest" --jq .tag_name)
if [ -z "$tag" ]; then
  echo "::error::could not determine latest upstream release"
  exit 1
fi
echo "latest upstream release: $tag"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"

git remote get-url upstream >/dev/null 2>&1 || git remote add upstream "https://github.com/${UPSTREAM_REPO}.git"
git fetch --no-tags upstream "refs/tags/${tag}:refs/upstream-tags/${tag}"

git checkout "$BASE_BRANCH"
if git merge-base --is-ancestor "refs/upstream-tags/${tag}" HEAD; then
  echo "$tag is already merged into $BASE_BRANCH; nothing to do"
  exit 0
fi

branch="upstream-sync/${tag}"
if gh pr list --head "$branch" --state open --json number --jq '.[0].number' | grep -q .; then
  echo "an open PR already exists for $branch"
  exit 0
fi

git checkout -B "$branch"
if git merge --no-ff --no-edit -m "Merge upstream ${UPSTREAM_REPO} ${tag}" "refs/upstream-tags/${tag}"; then
  git push --force origin "$branch"
  gh pr create --base "$BASE_BRANCH" --head "$branch" \
    --title "Sync upstream ${tag}" \
    --body "Merges ${UPSTREAM_REPO} release \`${tag}\` into \`${BASE_BRANCH}\` (no conflicts).

Merge, then push a \`v*\` tag to build and publish the APK."
else
  conflicts=$(git diff --name-only --diff-filter=U)
  git merge --abort || true
  git checkout "$BASE_BRANCH"
  title="Upstream sync ${tag}: merge conflicts"
  body="Merging ${UPSTREAM_REPO} \`${tag}\` into \`${BASE_BRANCH}\` conflicts in:

$(printf '%s\n' "$conflicts" | sed 's/^/- /')

Resolve locally: \`git fetch upstream tag ${tag} && git merge ${tag}\`."
  if gh issue list --state open --search "\"${title}\" in:title" --json number --jq '.[0].number' | grep -q .; then
    echo "conflict issue already open"
  else
    gh issue create --title "$title" --body "$body"
  fi
  echo "::warning::merge conflicts: $conflicts"
fi
