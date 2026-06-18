#!/usr/bin/env bash
#
# release.sh — cut a release of the Common Eclipse Refactoring Framework.
#
# This automates the manual/Eclipse-GUI release flow as a repeatable CLI pipeline:
#   1. tycho set-version to the release version (poms, MANIFEST.MFs, feature.xml)
#   2. clean install (build + tests)
#   3. stage the freshly built feature/bundle jars into the committed P2 update
#      site (OSGi `name_version.jar` layout)
#   4. bump site.xml to the release feature
#   5. (re)generate the cumulative P2 index with the Eclipse p2 publisher apps,
#      APPENDING to the committed update site so all historical versions survive
#   6. verify the index (history preserved, new version present, no path leakage)
#   7. commit "Release vX.Y.Z.", tag vX.Y.Z, push, create the GitHub release
#   8. tycho set-version to the next development version, commit, push
#
# Usage:
#   ./release.sh <release-version> [next-dev-version]
#   e.g. ./release.sh 5.3.0 5.4.0
#   (next-dev-version defaults to bumping the minor; "-SNAPSHOT" is added for you)
#
# Requirements:
#   - An Eclipse install that provides the p2 publisher applications. Override the
#     default with ECLIPSE_HOME, e.g. ECLIPSE_HOME=~/eclipse/rcp-2024-12/eclipse
#   - gh (GitHub CLI) authenticated, for the GitHub release.
#
# Gotchas this script encodes (learned the hard way):
#   - The `eclipse-repository` (mvn) build produces an EMPTY P2 repo here, so the
#     update site must be assembled with the p2 publisher, not `mvn deploy`.
#   - CategoryPublisher qualifies the category IU id with the categoryQualifier
#     (or, failing that, the definition file's URL — which would leak a local
#     path). To reproduce the historical clean id `edu.cuny.citytech.refactoring
#     .category`, we use category-def name="category" + -categoryQualifier
#     edu.cuny.citytech.refactoring (qualifier + "." + name == the clean id).
#   - Stray maven-release-plugin leftovers (release.properties, *.releaseBackup)
#     trip spotless:check because its generic format does not honor .gitignore;
#     we refuse to run if they are present.
#
set -euo pipefail

# --- args -------------------------------------------------------------------
REL="${1:-}"
if [[ -z "$REL" ]]; then
	echo "usage: $0 <release-version> [next-dev-version]   e.g. $0 5.3.0 5.4.0" >&2
	exit 2
fi
if [[ ! "$REL" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
	echo "release version must be X.Y.Z (got '$REL')" >&2
	exit 2
fi
NEXT="${2:-}"
if [[ -z "$NEXT" ]]; then
	IFS=. read -r MA MI PA <<<"$REL"
	NEXT="${MA}.$((MI + 1)).0"
fi
NEXT_SNAPSHOT="${NEXT}-SNAPSHOT"

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
US="$ROOT/edu.cuny.citytech.refactoring.common.updatesite"
ECLIPSE_HOME="${ECLIPSE_HOME:-$HOME/eclipse/rcp-2024-12/eclipse}"
ECL="$ECLIPSE_HOME/eclipse"
CATEGORY_ID="edu.cuny.citytech.refactoring.category"
CATEGORY_QUALIFIER="edu.cuny.citytech.refactoring"
CATEGORY_LABEL="Common Refactoring Framework"
FEATURE_ID="edu.cuny.citytech.refactoring.common.feature"

echo ">>> releasing $REL (next dev: $NEXT_SNAPSHOT)"

# --- pre-flight -------------------------------------------------------------
[[ -x "$ECL" ]] || { echo "Eclipse launcher not found/executable at $ECL (set ECLIPSE_HOME)" >&2; exit 1; }
command -v gh >/dev/null || { echo "gh (GitHub CLI) not found on PATH" >&2; exit 1; }
[[ "$(git rev-parse --abbrev-ref HEAD)" == "master" ]] || { echo "not on master" >&2; exit 1; }
if [[ -n "$(git status --porcelain)" ]]; then echo "working tree not clean" >&2; exit 1; fi
if git tag -l "v$REL" | grep -q .; then echo "tag v$REL already exists" >&2; exit 1; fi
# spotless gotcha: refuse to run with maven-release leftovers present
if compgen -G "$ROOT/**/release.properties" >/dev/null 2>&1 || \
   find "$ROOT" -name '*.releaseBackup' -o -name 'pom.xml.next' -o -name 'pom.xml.tag' 2>/dev/null | grep -q .; then
	echo "maven-release leftovers present (release.properties / *.releaseBackup / pom.xml.next|tag); remove them first" >&2
	exit 1
fi
git pull --ff-only

# --- 1. set release version -------------------------------------------------
echo ">>> set-version $REL"
./mvnw -q org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion="$REL"

# --- 2. build ---------------------------------------------------------------
echo ">>> clean install"
./mvnw -U -DtrimStackTrace=true clean install -B
echo ">>> spotless:check"
./mvnw spotless:check -B

# --- 3. stage built jars into the update site (OSGi name_version.jar) --------
echo ">>> staging jars into update site"
for d in "$ROOT"/*/; do
	mod="$(basename "$d")"
	jar="$d/target/${mod}-${REL}.jar"
	[[ -f "$jar" ]] || continue
	if [[ -f "$d/feature.xml" ]]; then
		cp "$jar" "$US/features/${mod}_${REL}.jar"
	elif [[ -f "$d/META-INF/MANIFEST.MF" ]]; then
		cp "$jar" "$US/plugins/${mod}_${REL}.jar"
	fi
done
echo "    staged: $(ls "$US"/features/*_"${REL}".jar "$US"/plugins/*_"${REL}".jar | wc -l) jars"

# --- 4. bump site.xml -------------------------------------------------------
echo ">>> bump site.xml"
sed -i -E "s#(${FEATURE_ID})_[0-9]+\.[0-9]+\.[0-9]+\.jar#\1_${REL}.jar#; \
           s#(id=\"${FEATURE_ID}\" version=\")[0-9]+\.[0-9]+\.[0-9]+#\1${REL}#" "$US/site.xml"

# --- 5. (re)generate the cumulative P2 index (APPEND) ------------------------
echo ">>> p2 publish (append)"
SRC="$(mktemp -d)"; mkdir -p "$SRC/features" "$SRC/plugins"
cp "$US"/features/*_"${REL}".jar "$SRC/features/"
cp "$US"/plugins/*_"${REL}".jar "$SRC/plugins/"
CATXML="$(mktemp /tmp/category.XXXXXX.xml)"
cat >"$CATXML" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<site>
   <feature url="features/${FEATURE_ID}_${REL}.jar" id="${FEATURE_ID}" version="${REL}">
      <category name="category"/>
   </feature>
   <category-def name="category" label="${CATEGORY_LABEL}">
      <description>
         A framework with some common functionality for refactoring tool development.
      </description>
   </category-def>
</site>
EOF
run_p2() {
	"$ECL" -nosplash -consoleLog -data "$(mktemp -d)" -application "$1" "${@:2}"
}
run_p2 org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher \
	-metadataRepository "file:$US" -artifactRepository "file:$US" \
	-source "$SRC" -append -compress -publishArtifacts
run_p2 org.eclipse.equinox.p2.publisher.CategoryPublisher \
	-metadataRepository "file:$US" -categoryDefinition "file:$CATXML" \
	-categoryQualifier "$CATEGORY_QUALIFIER" -compress

# --- 6. verify the index ----------------------------------------------------
echo ">>> verify index"
tmp="$(mktemp -d)"; cp "$US/content.jar" "$tmp/"; (cd "$tmp" && unzip -oq content.jar)
C="$tmp/content.xml"
grep -q "file:/" "$C" && { echo "FAIL: local path leaked into content.xml" >&2; exit 1; }
grep -qE "id='${FEATURE_ID}.feature.group' version='${REL}'" "$C" || { echo "FAIL: feature $REL missing" >&2; exit 1; }
awk "/<unit id='${CATEGORY_ID}'/{b=\$0;c=1;next} c{b=b ORS \$0} /<\/unit>/{if(c&&b~/\[${REL},${REL}\]/)n++;c=0} END{exit n?0:1}" "$C" \
	|| { echo "FAIL: no clean category bound to $REL" >&2; exit 1; }
# every previously-released feature version must still be present
git show HEAD:"edu.cuny.citytech.refactoring.common.updatesite/content.jar" >"$tmp/old.jar"
( cd "$tmp" && mkdir old && cd old && unzip -oq ../old.jar )
comm -23 \
	<(grep -oE "${FEATURE_ID}.feature.group' version='[^']*'" "$tmp/old/content.xml" | sort -u) \
	<(grep -oE "${FEATURE_ID}.feature.group' version='[^']*'" "$C" | sort -u) \
	| grep -q . && { echo "FAIL: a historical feature version was dropped" >&2; exit 1; }
echo "    index OK (history preserved, $REL present, clean ids)"

# --- 7. commit, tag, push, GitHub release -----------------------------------
echo ">>> commit + tag + push"
git add -A
git commit -q -m "Release v${REL}."
git tag -a "v${REL}" -m "Release v${REL}."
git push origin master
git push origin "v${REL}"
gh release create "v${REL}" --title "v${REL}" --generate-notes

# --- 8. next development version --------------------------------------------
echo ">>> prepare next development version $NEXT_SNAPSHOT"
./mvnw -q org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion="$NEXT_SNAPSHOT"
git add -A
git commit -q -m "Prepare next development version."
git push origin master

echo ">>> done: released v${REL}, master now on ${NEXT_SNAPSHOT}"
