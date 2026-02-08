#!/bin/sh

File Path: mvnw (Root Directory)

This is a simplified wrapper script to trigger Maven builds in Docker/Linux

set -e

if [ -z "$MAVEN_HOME" ]; then

Download Maven if not found (standard wrapper behavior)

mvn clean package "$@"
else
$MAVEN_HOME/bin/mvn clean package "$@"
fi