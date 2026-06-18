# Releasing

Releases are built in CI and published to GitHub Pages as a p2 composite repository. The older in-repo committed update site is **legacy** and is being retired.

## How to Cut a Release

Run the **Release** workflow: GitHub → Actions → "Release (CI-published p2 site)" → Run workflow, and enter the release version (e.g. `5.4.0`). It will set the version, build and test, tag `vX.Y.Z`, create a GitHub Release (with the p2 repo attached as a zip), publish the per-version repo to `gh-pages`, regenerate the composite index, and bump `master` to the next development version. No binaries are committed to `master`.

Update-site URL (composite, exposes every released version):

```
https://ponder-lab.github.io/Common-Eclipse-Refactoring-Framework/releases/
```

## How It Works (CI→GitHub Pages Composite Repo)

The update site is built in CI and published to the `gh-pages` branch, never committed to `master`. Each release publishes its **own** small p2 repo under `releases/X.Y.Z/`, and a p2 **composite repository** at the root ties them all together—so one stable URL exposes every version without a monolithic index.

Pieces:

- **`edu.cuny.citytech.refactoring.common.updatesite/category.xml`**—makes Tycho's `eclipse-repository` build produce a **complete** per-version p2 repo at `updatesite/target/repository` (feature + 8 bundles + category); a plain `./mvnw clean install` is enough (no GUI, no p2 publisher, no `-append`).
- **`tools/p2-composite.sh`**—regenerates `compositeContent.xml`/`compositeArtifacts.xml` from the per-version child dirs.
- **`.github/workflows/release.yml`**—the manual-dispatch pipeline described above.

Properties:

- No binaries in `master`—source history stays lean. The published bits live on the orphan `gh-pages` branch, independent of source clones.
- One CLI build produces the repo; no GUI, no hand-assembled index.
- The composite layout scales to any number of versions; each release is an isolated, immutable child.
- The shipped repo always matches what the build compiled against (no dep-vs-site drift).

## Legacy Model (`release.sh`, Committed Update Site)

Before the CI flow, the p2 update site lived **inside the source repo** (`edu.cuny.citytech.refactoring.common.updatesite/`) and was **cumulative**: every release committed its feature + plugin jars and rewrote the shared `artifacts.jar`/`content.jar` index, served from `raw.githubusercontent.com/.../updatesite`. `./release.sh X.Y.Z` automated that flow.

It is retained only as an offline fallback. Drawbacks that motivated the move: binaries accumulate in `master` history forever (every release grows every clone); it needs a local Eclipse install with the p2 publisher and a fragile hand-assembled (`-append`) index; and `raw.githubusercontent.com` is not a real artifact host.

## Remaining Cleanup

1. Drop the committed `updatesite/` binaries from `master` (history is preserved on `gh-pages` under `releases/archive/`).
2. Retire `release.sh` (or keep as an offline fallback).
3. Once confident, remove the legacy `raw.githubusercontent` URL from the README.
