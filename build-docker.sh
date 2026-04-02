#!/bin/bash
# CuraNexus Docker Build Script
# Builds all modules and creates Docker images

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Modules to build
MODULES=(
    "patient"
    "care-encounter"
    "journal"
    "task"
    "authorization"
    "audit"
    "triage"
    "integration"
)

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  CuraNexus Docker Build Script${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Step 1: Build Maven projects
echo -e "${YELLOW}Step 1: Building Maven projects...${NC}"
if [ "$1" == "--skip-maven" ]; then
    echo -e "${YELLOW}Skipping Maven build (--skip-maven flag)${NC}"
else
    mvn clean package -DskipTests -q
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Maven build completed successfully${NC}"
    else
        echo -e "${RED}Maven build failed!${NC}"
        exit 1
    fi
fi
echo ""

# Step 2: Build Docker images
echo -e "${YELLOW}Step 2: Building Docker images...${NC}"
echo ""

for module in "${MODULES[@]}"; do
    echo -e "${YELLOW}Building ${module}...${NC}"

    # Check if JAR exists
    JAR_FILE=$(ls modules/${module}/target/*.jar 2>/dev/null | head -1)
    if [ -z "$JAR_FILE" ]; then
        echo -e "${RED}JAR file not found for ${module}. Run Maven build first.${NC}"
        exit 1
    fi

    docker build \
        --build-arg MODULE_NAME="${module}" \
        -t "curanexus/${module}:latest" \
        -t "curanexus/${module}:$(date +%Y%m%d)" \
        .

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ curanexus/${module}:latest${NC}"
    else
        echo -e "${RED}✗ Failed to build ${module}${NC}"
        exit 1
    fi
    echo ""
done

# Summary
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Build Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Built images:"
for module in "${MODULES[@]}"; do
    echo "  - curanexus/${module}:latest"
done
echo ""
echo "To start all services:"
echo "  docker-compose up -d"
echo ""
echo "To start with dev tools (pgAdmin):"
echo "  docker-compose --profile dev up -d"
echo ""
echo "To view logs:"
echo "  docker-compose logs -f"
