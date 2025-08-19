#!/bin/bash

# Student Management System Build and Run Script
# This script handles the complete setup and execution of the application

set -e  # Exit on any error

echo "=================================================="
echo "Student Management Learning System"
echo "Build and Run Script v1.0"
echo "=================================================="
echo

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
print_status "Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    print_error "Java is not installed or not in PATH"
    print_error "Please install Java 17 or higher"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    print_error "Java version $JAVA_VERSION is too old. Please install Java 17 or higher"
    exit 1
fi
print_success "Java version check passed"

# Check Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    print_error "Please install Apache Maven"
    exit 1
fi
print_success "Maven check passed"

# Check if XAMPP MySQL is running (optional check)
print_status "Checking MySQL connection..."
if command -v mysql &> /dev/null; then
    if mysql -u root -e "SELECT 1;" &> /dev/null; then
        print_success "MySQL connection successful"
    else
        print_warning "MySQL connection failed - XAMPP might not be running"
        print_warning "Please ensure XAMPP MySQL service is started"
    fi
else
    print_warning "MySQL client not found - cannot test connection"
fi

# Create logs directory
print_status "Creating logs directory..."
mkdir -p logs
print_success "Logs directory created"

# Clean and compile
print_status "Cleaning and compiling project..."
mvn clean compile -q
if [ $? -eq 0 ]; then
    print_success "Compilation successful"
else
    print_error "Compilation failed"
    exit 1
fi

# Run tests
print_status "Running tests..."
mvn test -q
if [ $? -eq 0 ]; then
    print_success "All tests passed"
else
    print_warning "Some tests failed - continuing anyway"
fi

# Package application
print_status "Packaging application..."
mvn package -q -DskipTests
if [ $? -eq 0 ]; then
    print_success "Packaging successful"
else
    print_error "Packaging failed"
    exit 1
fi

# Database setup reminder
echo
print_status "Database Setup Reminder:"
echo "1. Ensure XAMPP is running"
echo "2. Start MySQL service in XAMPP"
echo "3. Import database_setup.sql into phpMyAdmin"
echo "4. Verify 'student_management_db' database exists"
echo

# Ask user if they want to continue
read -p "Press Enter to start the application (or Ctrl+C to cancel)..."

# Start application
print_status "Starting Student Management Learning System..."
echo
print_success "Application is starting..."
print_status "Check the application window for the login screen"
print_status "Default admin credentials: ADMIN001 / admin123"
echo

# Run the application
mvn javafx:run

print_success "Application execution completed"