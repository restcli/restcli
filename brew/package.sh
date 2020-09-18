#!/bin/bash
PACKAGE_DIRECTORY_ROOT="build/__restcli__"
PACKAGE_DIRECTORY="$PACKAGE_DIRECTORY_ROOT/brew_restcli"

# Clean & Create package directory structure
rm -rf $PACKAGE_DIRECTORY_ROOT
mkdir -p "$PACKAGE_DIRECTORY/brew"

# Copy necessary files for brew package.
cp "brew/restcli" $PACKAGE_DIRECTORY
cp "brew/restcli" "$PACKAGE_DIRECTORY/brew"
# shellcheck disable=SC2012
jar_file=$(ls -1 build/libs/*.jar | head -1)
cp "$jar_file" "$PACKAGE_DIRECTORY/restcli.jar"

# shellcheck disable=SC2164
pushd $PACKAGE_DIRECTORY_ROOT
zip -r brew_restcli.zip "brew_restcli"
zip -j restcli.zip "brew_restcli/restcli.jar"
shasum -a 256 brew_restcli.zip restcli.zip > checksums.txt
# shellcheck disable=SC2164
popd
