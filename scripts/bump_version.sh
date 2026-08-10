#!/usr/bin/env bash

# Usage: bump_version.sh <product> <version> [devsha]
#   product : which SDK's build.gradle to edit — "legacy" (skyvault) or "flowvault"
#   version : semver to stamp into mVersionName
#   devsha  : optional short sha; when present stamps "<version>-dev.<devsha>" (internal channel)
#
# The two SDKs ship from one repo with independent version lines, so the release workflows pass
# the product explicitly. See docs/sdk-split-plan.md.

PRODUCT=$1
Version=$2
SEMVER=$Version

case "$PRODUCT" in
	legacy)   GRADLE_FILE="skyvault/build.gradle" ;;
	flowvault) GRADLE_FILE="flowvault/build.gradle" ;;
	*)
		echo "Error: unknown product '$PRODUCT' (expected 'legacy' or 'flowvault')"
		exit 1
		;;
esac

if [ -z "$3" ]
then
	echo "Bumping $PRODUCT package version to $Version"

	sed -E "s/mVersionName = .+/mVersionName = \"$SEMVER\"/g" "$GRADLE_FILE" > tempfile && cat tempfile > "$GRADLE_FILE" && rm -f tempfile

	echo --------------------------
	echo "Done, $PRODUCT package now at $Version"
else
	echo "Bumping $PRODUCT package version to $Version-dev.$3"

	sed -E "s/mVersionName = .+/mVersionName = \"$SEMVER-dev.$3\"/g" "$GRADLE_FILE" > tempfile && cat tempfile > "$GRADLE_FILE" && rm -f tempfile

	echo --------------------------
	echo "Done, $PRODUCT package now at $Version-dev.$3"
fi
