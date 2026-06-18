#!/usr/bin/env bash
#
# p2-composite.sh — (re)generate p2 composite repository indexes.
#
# A composite p2 repository is just two XML files that reference per-version
# child repositories. This lets a single, stable update-site URL expose every
# released version without a monolithic cumulative index and without keeping
# any binaries in the source repo.
#
# Usage:  p2-composite.sh <site-root> <repo-name> <epoch-millis>
#   <site-root>   directory whose immediate subdirectories are child p2 repos
#                 (each containing content.jar / artifacts.jar)
#   <repo-name>   human-readable repository name
#   <epoch-millis> timestamp to stamp (pass in; CI provides it)
#
set -euo pipefail
ROOT="${1:?site-root}"; NAME="${2:?repo-name}"; TS="${3:?epoch-millis}"

# children = immediate subdirs that look like a p2 repo
mapfile -t CHILDREN < <(
	find "$ROOT" -mindepth 2 -maxdepth 2 -name content.jar -printf '%h\n' \
		| sed "s#^$ROOT/##" | sort -V
)

emit() { # $1 = filename, $2 = processing-instruction, $3 = repo type
	local out="$ROOT/$1" pi="$2" type="$3"
	{
		echo "<?xml version='1.0' encoding='UTF-8'?>"
		echo "<?$pi version='1.0.0'?>"
		echo "<repository name='$NAME' type='$type' version='1.0.0'>"
		echo "  <properties size='1'>"
		echo "    <property name='p2.timestamp' value='$TS'/>"
		echo "  </properties>"
		echo "  <children size='${#CHILDREN[@]}'>"
		for c in "${CHILDREN[@]}"; do echo "    <child location='$c'/>"; done
		echo "  </children>"
		echo "</repository>"
	} > "$out"
	echo "wrote $out (${#CHILDREN[@]} children)"
}

emit compositeContent.xml   compositeMetadataRepository \
	org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository
emit compositeArtifacts.xml compositeArtifactRepository \
	org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository
