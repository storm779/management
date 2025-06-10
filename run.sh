#!/bin/bash

# Visakh Refinery Portal - Run Script
# This script builds and runs the Spring Boot application

echo "🏭 Visakh Refinery Portal - Starting Application"
echo "================================================"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or later."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [ "$JAVA_VERSION" -lt "17" ]; then
    echo "❌ Java 17 or later is required. Current version: $JAVA_VERSION"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or later."
    exit 1
fi

echo "✅ Java and Maven are installed"

# Clean and build the project
echo "🔨 Building the project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

echo "✅ Build successful"

# Check if MariaDB/MySQL is running (optional check)
if command -v mysql &> /dev/null; then
    if mysql -e "SELECT 1" &> /dev/null; then
        echo "✅ Database connection available"
    else
        echo "⚠️  Database connection not available. Make sure MariaDB/MySQL is running."
        echo "   You can still run the application, but database features won't work."
    fi
fi

# Start the application
echo "🚀 Starting Visakh Refinery Portal..."
echo "   Access the application at: http://localhost:8080"
echo "   Press Ctrl+C to stop the application"
echo ""

mvn spring-boot:run 