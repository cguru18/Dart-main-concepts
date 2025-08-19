#!/bin/bash

echo "Starting Student Management Learning System..."
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Apache Maven"
    exit 1
fi

echo "Checking dependencies..."
mvn dependency:resolve -q

echo
echo "Starting application..."
mvn javafx:run