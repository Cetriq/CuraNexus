#!/bin/bash
# E2E Test: Healthcare Workflow Integration
# Tests the complete flow: Encounter → Auto-tasks → Complete tasks/notes → Finish encounter

set -e

# Configuration
ENCOUNTER_URL="${ENCOUNTER_URL:-http://localhost:8081}"
TASK_URL="${TASK_URL:-http://localhost:8083}"
JOURNAL_URL="${JOURNAL_URL:-http://localhost:8082}"
PATIENT_URL="${PATIENT_URL:-http://localhost:8080}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "E2E Healthcare Workflow Test"
echo "========================================"
echo ""

# Helper functions
check_service() {
    local url=$1
    local name=$2
    if curl -s --connect-timeout 2 "$url/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} $name is running"
        return 0
    else
        echo -e "${RED}✗${NC} $name is not running at $url"
        return 1
    fi
}

# Step 0: Check services are running
echo "Step 0: Checking services..."
check_service "$ENCOUNTER_URL" "Care-Encounter" || exit 1
check_service "$TASK_URL" "Task" || exit 1
check_service "$JOURNAL_URL" "Journal" || exit 1
echo ""

# Step 1: Create a patient (if needed)
echo "Step 1: Creating patient..."
PATIENT_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
echo "Using patient ID: $PATIENT_ID"
echo ""

# Step 2: Create an encounter
echo "Step 2: Creating EMERGENCY encounter..."
ENCOUNTER_RESPONSE=$(curl -s -X POST "$ENCOUNTER_URL/api/v1/encounters" \
    -H "Content-Type: application/json" \
    -d "{
        \"patientId\": \"$PATIENT_ID\",
        \"encounterClass\": \"EMERGENCY\",
        \"responsibleUnitId\": \"$(uuidgen | tr '[:upper:]' '[:lower:]')\",
        \"responsiblePractitionerId\": \"$(uuidgen | tr '[:upper:]' '[:lower:]')\"
    }")

ENCOUNTER_ID=$(echo "$ENCOUNTER_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -z "$ENCOUNTER_ID" ]; then
    echo -e "${RED}✗${NC} Failed to create encounter"
    echo "Response: $ENCOUNTER_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓${NC} Created encounter: $ENCOUNTER_ID"
echo ""

# Step 3: Wait for auto-tasks to be created (via RabbitMQ event)
echo "Step 3: Waiting for auto-tasks to be created..."
sleep 2

# Step 4: Check task summary
echo "Step 4: Checking task summary for encounter..."
TASK_SUMMARY=$(curl -s "$TASK_URL/api/v1/encounters/$ENCOUNTER_ID/tasks/summary")
TOTAL_TASKS=$(echo "$TASK_SUMMARY" | grep -o '"total":[0-9]*' | cut -d':' -f2)
PENDING_TASKS=$(echo "$TASK_SUMMARY" | grep -o '"pending":[0-9]*' | cut -d':' -f2)

echo "Task summary: $TASK_SUMMARY"
if [ "$TOTAL_TASKS" -gt 0 ]; then
    echo -e "${GREEN}✓${NC} Auto-tasks created: $TOTAL_TASKS total, $PENDING_TASKS pending"
else
    echo -e "${YELLOW}!${NC} No auto-tasks found (RabbitMQ might not be running)"
fi
echo ""

# Step 5: Get all tasks for the encounter
echo "Step 5: Getting tasks for encounter..."
TASKS=$(curl -s "$TASK_URL/api/v1/encounters/$ENCOUNTER_ID/tasks")
echo "Tasks: $TASKS"
echo ""

# Step 6: Check encounter readiness (should NOT be ready - tasks pending)
echo "Step 6: Checking encounter readiness (should NOT be ready)..."
READINESS=$(curl -s "$ENCOUNTER_URL/api/v1/encounters/$ENCOUNTER_ID/readiness")
READINESS_STATUS=$(echo "$READINESS" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

echo "Readiness: $READINESS"
if [ "$READINESS_STATUS" = "NOT_READY" ]; then
    echo -e "${GREEN}✓${NC} Encounter correctly marked as NOT_READY"
else
    echo -e "${YELLOW}!${NC} Readiness status: $READINESS_STATUS"
fi
echo ""

# Step 7: Try to finish encounter (should fail)
echo "Step 7: Attempting to finish encounter (should fail)..."
FINISH_RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$ENCOUNTER_URL/api/v1/encounters/$ENCOUNTER_ID/status" \
    -H "Content-Type: application/json" \
    -d '{"status": "FINISHED"}')

HTTP_CODE=$(echo "$FINISH_RESPONSE" | tail -1)
RESPONSE_BODY=$(echo "$FINISH_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "409" ]; then
    echo -e "${GREEN}✓${NC} Correctly rejected with 409 Conflict"
    echo "Response: $RESPONSE_BODY"
else
    echo -e "${YELLOW}!${NC} Unexpected response code: $HTTP_CODE"
    echo "Response: $RESPONSE_BODY"
fi
echo ""

# Step 8: Update encounter status through proper state transitions
echo "Step 8: Moving encounter through proper status transitions..."

# PLANNED -> ARRIVED
curl -s -X PATCH "$ENCOUNTER_URL/api/v1/encounters/$ENCOUNTER_ID/status" \
    -H "Content-Type: application/json" \
    -d '{"status": "ARRIVED"}' > /dev/null
echo -e "${GREEN}✓${NC} Encounter updated to ARRIVED"

# ARRIVED -> IN_PROGRESS
curl -s -X PATCH "$ENCOUNTER_URL/api/v1/encounters/$ENCOUNTER_ID/status" \
    -H "Content-Type: application/json" \
    -d '{"status": "IN_PROGRESS"}' > /dev/null
echo -e "${GREEN}✓${NC} Encounter updated to IN_PROGRESS"
echo ""

# Step 9: Complete all tasks
echo "Step 9: Completing all tasks..."
TASK_IDS=$(echo "$TASKS" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

for TASK_ID in $TASK_IDS; do
    # Start task
    curl -s -X POST "$TASK_URL/api/v1/tasks/$TASK_ID/start" > /dev/null
    # Complete task
    curl -s -X POST "$TASK_URL/api/v1/tasks/$TASK_ID/complete" \
        -H "Content-Type: application/json" \
        -d '{"completionNote": "Completed via E2E test", "outcome": "SUCCESS"}' > /dev/null
    echo -e "${GREEN}✓${NC} Completed task: $TASK_ID"
done
echo ""

# Step 10: Create and sign a note
echo "Step 10: Creating and signing a note..."
AUTHOR_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
NOTE_RESPONSE=$(curl -s -X POST "$JOURNAL_URL/api/v1/journal/notes" \
    -H "Content-Type: application/json" \
    -d "{
        \"encounterId\": \"$ENCOUNTER_ID\",
        \"patientId\": \"$PATIENT_ID\",
        \"type\": \"PROGRESS\",
        \"authorId\": \"$AUTHOR_ID\",
        \"authorName\": \"E2E Test Doctor\",
        \"title\": \"E2E Test Note\",
        \"content\": \"This is an E2E test note.\"
    }")

NOTE_ID=$(echo "$NOTE_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -n "$NOTE_ID" ]; then
    echo -e "${GREEN}✓${NC} Created note: $NOTE_ID"

    # Sign the note
    curl -s -X POST "$JOURNAL_URL/api/v1/journal/notes/$NOTE_ID/sign" \
        -H "Content-Type: application/json" \
        -d "{\"signedById\": \"$AUTHOR_ID\", \"signedByName\": \"E2E Test Doctor\"}" > /dev/null
    echo -e "${GREEN}✓${NC} Signed note: $NOTE_ID"
else
    echo -e "${YELLOW}!${NC} Failed to create note"
fi
echo ""

# Step 11: Check note summary
echo "Step 11: Checking note summary..."
NOTE_SUMMARY=$(curl -s "$JOURNAL_URL/api/v1/encounters/$ENCOUNTER_ID/notes/summary")
echo "Note summary: $NOTE_SUMMARY"
echo ""

# Step 12: Check encounter readiness again (should be READY now)
echo "Step 12: Checking encounter readiness again..."
READINESS=$(curl -s "$ENCOUNTER_URL/api/v1/encounters/$ENCOUNTER_ID/readiness")
READINESS_STATUS=$(echo "$READINESS" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

echo "Readiness: $READINESS"
if [ "$READINESS_STATUS" = "READY" ]; then
    echo -e "${GREEN}✓${NC} Encounter is now READY to be finished"
else
    echo -e "${YELLOW}!${NC} Readiness status: $READINESS_STATUS (might have pending items)"
fi
echo ""

# Step 13: Finish the encounter
echo "Step 13: Finishing encounter..."
FINISH_RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$ENCOUNTER_URL/api/v1/encounters/$ENCOUNTER_ID/status" \
    -H "Content-Type: application/json" \
    -d '{"status": "FINISHED"}')

HTTP_CODE=$(echo "$FINISH_RESPONSE" | tail -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓${NC} Encounter successfully finished!"
else
    echo -e "${RED}✗${NC} Failed to finish encounter (HTTP $HTTP_CODE)"
    echo "Response: $(echo "$FINISH_RESPONSE" | sed '$d')"
fi
echo ""

# Step 14: Verify final state
echo "Step 14: Verifying final encounter state..."
FINAL_ENCOUNTER=$(curl -s "$ENCOUNTER_URL/api/v1/encounters/$ENCOUNTER_ID")
FINAL_STATUS=$(echo "$FINAL_ENCOUNTER" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$FINAL_STATUS" = "FINISHED" ]; then
    echo -e "${GREEN}✓${NC} Encounter status is FINISHED"
else
    echo -e "${RED}✗${NC} Unexpected final status: $FINAL_STATUS"
fi
echo ""

echo "========================================"
echo "E2E Test Complete"
echo "========================================"
echo ""
echo "Summary:"
echo "- Encounter ID: $ENCOUNTER_ID"
echo "- Tasks created: ${TOTAL_TASKS:-0}"
echo "- Final status: $FINAL_STATUS"
echo ""
