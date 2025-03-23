#!/bin/bash
# PingAm Site Configuration Tool
# Unix Shell Script

# Set the current directory as the base directory
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in the PATH. Please install Java 11 or higher."
    exit 1
fi

# Execute the Java application with all arguments passed to this script
java -jar "$BASE_DIR/lib/${project.build.finalName}.jar" "$@"

exit $?
