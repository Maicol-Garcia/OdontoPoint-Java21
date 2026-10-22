#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
APP_JAR=bin/odontopoint-web.jar
[ -f "$APP_JAR" ] || APP_JAR=target/odontopoint-web-2.0.0.jar
[ -f "$APP_JAR" ] || { echo "Compile con JDK 21 y mvn clean package"; exit 1; }
echo "Abra http://localhost:8080 cuando termine el inicio."
exec java -jar "$APP_JAR" "$@"
