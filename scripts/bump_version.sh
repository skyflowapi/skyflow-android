
Version=$1
SEMVER=$Version

if [ -z $2 ]
then
	echo "Bumping package version to $1"

	sed -E "s/mVersionName = .+/mVersionName = \'$SEMVER\'/g" Skyflow/build.gradle > tempfile && cat tempfile > Skyflow/build.gradle && rm -f tempfile
	
	echo --------------------------
	echo "Done, Package now at $1"
else
	echo "Bumping package version to $1-dev.$2"

	sed -E "s/mVersionName = .+/mVersionName = \'$SEMVER-dev.$2\'/g" Skyflow/build.gradle > tempfile && cat tempfile > Skyflow/build.gradle && rm -f tempfile
	
	echo --------------------------
	echo "Done, Package now at $1-dev.$2"
fi

