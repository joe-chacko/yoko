# Yoko Release Guide

## Prerequisites

- **git-cliff** — CHANGELOG generation: https://git-cliff.org/docs/installation
- **GitHub CLI (`gh`)** — creating releases: https://cli.github.com/
- **`gh auth login`** — authenticated before running step 4

---

## The five steps

### Step 1 — Begin

```bash
./gradlew release-1-begin
```

Shows an interactive menu to pick a version bump (major / minor / patch).
Creates `release/X.Y.Z` from `origin/dev` and switches to it.

Make any release mechanics changes (build scripts, docs, etc.), commit and push them before continuing.

---

### Step 2 — Describe

```bash
./gradlew release-2-describe
```

Runs `git-cliff` to generate the changelog for the new version, commits and pushes it.

Open `CHANGELOG.md` and edit the generated entries as needed, then commit and push.

---

### Step 3 — Check

```bash
./gradlew release-3-check
```

Runs validation checks, then builds and tests:

| Check | What it verifies |
|-------|-----------------|
| CHANGELOG | Version heading `## [vX.Y.Z]` is present |
| Working directory | No uncommitted changes |
| Branch sync | Local branch matches `origin/release/X.Y.Z` |
| Tag | `vX.Y.Z` does not already exist |
| Build + test | Full `./gradlew build` must pass |

If any check fails, fix the issue and re-run `release-3-check`. Nothing
destructive has happened yet — it is safe to retry as many times as needed.

---

### Step 4 — Seal

```bash
./gradlew release-4-seal
```

On success:
1. Creates signed annotated tag `vX.Y.Z`
2. Pushes the tag
3. Fast-forward merges `release/X.Y.Z` → `main`
4. Pushes `main`
5. Deletes the release branch (local and remote)

---

### Step 5 — Deliver

```bash
./gradlew release-5-deliver
```

Builds all release artifacts, creates the GitHub release, and merges main back to dev:

- 6 modules × main JAR + sources JAR + javadoc JAR + SHA-256 + SHA-512
- `yoko-X.Y.Z-dist.zip` — complete distribution archive
- GitHub release at `https://github.com/OpenLiberty/yoko/releases/tag/vX.Y.Z`
- Fast-forward merges `main` → `dev`

---

## Rolling back a release

```bash
# 1. Delete the GitHub release
gh release delete vX.Y.Z --yes

# 2. Delete the git tag
git tag -d vX.Y.Z
git push origin :refs/tags/vX.Y.Z

# 3. Reset main to the commit before the release merge
git switch main
git pull origin main
git revert <merge-commit-hash>   # or git reset --hard <pre-release-sha>
git push origin main
```

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| `Version X.Y.Z not found in CHANGELOG.md` | Run `release-2-describe` or add the heading manually |
| `Git working directory not clean` | Commit or stash changes |
| `Local branch not in sync with origin` | `git push origin release/X.Y.Z` |
| `Git tag vX.Y.Z already exists` | `git tag -d vX.Y.Z && git push origin :refs/tags/vX.Y.Z` |
| `GitHub CLI (gh) not found` | `brew install gh` or see https://cli.github.com/ |
| `GitHub authentication not configured` | `gh auth login` |
| `Multiple version tags found on HEAD` | Remove the extra tag: `git tag -d <tag>` |
| `Not on a release branch` | Run `release-1-begin` first |
| `No tag found on HEAD` | Run `release-4-seal` first |
