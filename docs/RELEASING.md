# Releasing

This repo has two release mechanisms. The **current** one is in-repo and
manual/scripted; the **prototype** one is CI-published. This document compares
them so we can decide whether to migrate.

## Current model (`release.sh`, committed update site)

The P2 update site lives **inside the source repo**
(`edu.cuny.citytech.refactoring.common.updatesite/`) and is **cumulative**:
every release commits its feature + plugin jars and rewrites the shared
`artifacts.jar` / `content.jar` index, served from
`raw.githubusercontent.com/.../updatesite`.

`./release.sh X.Y.Z` automates it: `set-version` → build → stage jars → bump
`site.xml` → regenerate the cumulative index with the Eclipse p2 publisher
(`-append`) → commit/tag/push → GitHub Release → next-dev bump.

Trade-offs:

- ➖ Binaries accumulate in `master` history **forever** — every release grows
every clone, unrecoverably.
- ➖ Requires a local Eclipse install with the p2 publisher apps; the index is
hand-assembled (`-append`), which is fragile (category-id qualification, easy
to leak a local path).
- ➖ `raw.githubusercontent.com` is not a real artifact host (no CDN/SLA).
- ➕ Zero infra; everything is in one repo; offline-capable.

## Prototype model (this branch: CI → GitHub Pages composite repo)

The update site is **built in CI and published to the `gh-pages` branch**, never
committed to `master`. Each release publishes its **own** small p2 repo under
`releases/X.Y.Z/`, and a p2 **composite repository** at the root ties them all
together — so one stable URL exposes every version without a monolithic index.

Stable update-site URL (composite):

```
https://ponder-lab.github.io/Common-Eclipse-Refactoring-Framework/releases/
```

Pieces added on this branch:

- **`edu.cuny.citytech.refactoring.common.updatesite/category.xml`** — the key
fix. Tycho's `eclipse-repository` build was producing an *empty* repo because
the module only had the legacy `site.xml`. With a `category.xml`, a plain
`./mvnw clean install` produces a **complete** per-version p2 repo at
`updatesite/target/repository` (feature + 8 bundles + category) — no GUI, no
p2 publisher, no `-append`.
- **`tools/p2-composite.sh`** — regenerates `compositeContent.xml` /
`compositeArtifacts.xml` from the per-version child dirs. (Verified: the
Eclipse p2 director loads the composite and resolves the feature.)
- **`.github/workflows/release.yml`** — manual-dispatch pipeline: set-version →
build/test → tag + GitHub Release (with the p2 repo zipped as an asset) →
publish to `gh-pages` as a composite → next-dev bump.

Trade-offs:

- ➕ **No binaries in `master`** — source history stays lean. The published bits
live on the orphan `gh-pages` branch, independent of source clones.
- ➕ One CLI build produces the repo; no GUI, no hand-assembled index.
- ➕ Composite layout scales to any number of versions; each release is an
isolated, immutable child — no risk of corrupting a shared index.
- ➕ Released p2 repo is also attached to the GitHub Release as a zip.
- ➖ Relies on GitHub Pages + Actions (infra, perms).
- ➖ `gh-pages` still accumulates binaries, but on a branch you can prune/squash
without touching source history.

## Migration notes (not done on this branch)

1. Enable GitHub Pages for the `gh-pages` branch.
2. Update the README's update-site URL to the Pages composite URL.
3. Optionally seed `gh-pages` with the existing historical versions (one child
dir per past release) so nothing 404s, then **drop the committed
`updatesite/` binaries** from `master` going forward (and, if desired, purge
them from history).
4. Retire `release.sh` (or keep as an offline fallback).
