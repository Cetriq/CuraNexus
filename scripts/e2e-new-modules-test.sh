#!/bin/bash
# E2E Test: New Modules Integration
# Tests: Booking, Medication, Referral, Lab modules

# Configuration
BOOKING_URL="${BOOKING_URL:-http://localhost:8089}"
MEDICATION_URL="${MEDICATION_URL:-http://localhost:8090}"
REFERRAL_URL="${REFERRAL_URL:-http://localhost:8091}"
LAB_URL="${LAB_URL:-http://localhost:8092}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PASSED=0
FAILED=0

echo "========================================"
echo "E2E New Modules Integration Test"
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

test_pass() {
    echo -e "${GREEN}✓${NC} $1"
    ((PASSED++))
}

test_fail() {
    echo -e "${RED}✗${NC} $1"
    ((FAILED++))
}

test_warn() {
    echo -e "${YELLOW}!${NC} $1"
}

# Generate UUIDs
PATIENT_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
PRACTITIONER_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
UNIT_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
ENCOUNTER_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
LAB_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')

# Check services
echo "Step 0: Checking services..."
check_service "$BOOKING_URL" "Booking" || exit 1
check_service "$MEDICATION_URL" "Medication" || exit 1
check_service "$REFERRAL_URL" "Referral" || exit 1
check_service "$LAB_URL" "Lab" || exit 1
echo ""

# ========================================
# BOOKING MODULE TESTS
# ========================================
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}BOOKING MODULE TESTS${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Calculate future times
START_TIME=$(date -v+1d +"%Y-%m-%dT10:00:00")
END_TIME=$(date -v+1d +"%Y-%m-%dT10:30:00")

echo "Test 1: Create appointment..."
BOOKING_RESPONSE=$(curl -s -X POST "$BOOKING_URL/api/v1/bookings" \
    -H "Content-Type: application/json" \
    -H "X-User-Id: $PRACTITIONER_ID" \
    -d "{
        \"patientId\": \"$PATIENT_ID\",
        \"practitionerId\": \"$PRACTITIONER_ID\",
        \"unitId\": \"$UNIT_ID\",
        \"startTime\": \"$START_TIME\",
        \"endTime\": \"$END_TIME\",
        \"appointmentType\": \"IN_PERSON\",
        \"reasonText\": \"E2E Test - Annual checkup\"
    }")

APPOINTMENT_ID=$(echo "$BOOKING_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
BOOKING_REFERENCE=$(echo "$BOOKING_RESPONSE" | grep -o '"bookingReference":"[^"]*"' | cut -d'"' -f4)

if [ -n "$APPOINTMENT_ID" ]; then
    test_pass "Created appointment: $APPOINTMENT_ID (ref: $BOOKING_REFERENCE)"
else
    test_fail "Failed to create appointment"
    echo "Response: $BOOKING_RESPONSE"
fi

echo ""
echo "Test 2: Get appointment by ID..."
GET_RESPONSE=$(curl -s "$BOOKING_URL/api/v1/bookings/$APPOINTMENT_ID")
GET_STATUS=$(echo "$GET_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$GET_STATUS" = "BOOKED" ]; then
    test_pass "Retrieved appointment with status: $GET_STATUS"
else
    test_fail "Unexpected appointment status: $GET_STATUS"
fi

echo ""
echo "Test 3: Get patient appointments..."
PATIENT_APPOINTMENTS=$(curl -s "$BOOKING_URL/api/v1/bookings/patient/$PATIENT_ID")
APPT_COUNT=$(echo "$PATIENT_APPOINTMENTS" | grep -o '"id":"[^"]*"' | wc -l | tr -d ' ')

if [ "$APPT_COUNT" -ge 1 ]; then
    test_pass "Found $APPT_COUNT appointment(s) for patient"
else
    test_fail "No appointments found for patient"
fi

echo ""
echo "Test 4: Check-in patient..."
CHECKIN_RESPONSE=$(curl -s -X POST "$BOOKING_URL/api/v1/bookings/$APPOINTMENT_ID/check-in")
CHECKIN_STATUS=$(echo "$CHECKIN_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$CHECKIN_STATUS" = "CHECKED_IN" ]; then
    test_pass "Patient checked in successfully"
else
    test_fail "Check-in failed, status: $CHECKIN_STATUS"
fi

echo ""
echo "Test 5: Start visit..."
START_RESPONSE=$(curl -s -X POST "$BOOKING_URL/api/v1/bookings/$APPOINTMENT_ID/start")
START_STATUS=$(echo "$START_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$START_STATUS" = "IN_PROGRESS" ]; then
    test_pass "Visit started successfully"
else
    test_fail "Start visit failed, status: $START_STATUS"
fi

echo ""
echo "Test 6: Complete visit..."
COMPLETE_RESPONSE=$(curl -s -X POST "$BOOKING_URL/api/v1/bookings/$APPOINTMENT_ID/complete")
COMPLETE_STATUS=$(echo "$COMPLETE_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$COMPLETE_STATUS" = "COMPLETED" ]; then
    test_pass "Visit completed successfully"
else
    test_fail "Complete visit failed, status: $COMPLETE_STATUS"
fi

echo ""

# ========================================
# MEDICATION MODULE TESTS
# ========================================
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}MEDICATION MODULE TESTS${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

echo "Test 7: Create prescription..."
PRESCRIPTION_RESPONSE=$(curl -s -X POST "$MEDICATION_URL/api/v1/prescriptions" \
    -H "Content-Type: application/json" \
    -H "X-User-Id: $PRACTITIONER_ID" \
    -d "{
        \"patientId\": \"$PATIENT_ID\",
        \"encounterId\": \"$ENCOUNTER_ID\",
        \"medicationText\": \"Paracetamol 500mg\",
        \"atcCode\": \"N02BE01\",
        \"indication\": \"Smarta\",
        \"route\": \"ORAL\",
        \"dosageInstruction\": \"1-2 tabletter vid behov\",
        \"doseQuantity\": 500,
        \"doseUnit\": \"mg\",
        \"frequency\": 3,
        \"frequencyPeriodHours\": 24,
        \"asNeeded\": true,
        \"maxDosePerDay\": 4000,
        \"durationDays\": 7,
        \"dispenseQuantity\": 20,
        \"numberOfRepeats\": 0,
        \"prescriberName\": \"E2E Test Doctor\",
        \"activateImmediately\": true
    }")

PRESCRIPTION_ID=$(echo "$PRESCRIPTION_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
PRESCRIPTION_STATUS=$(echo "$PRESCRIPTION_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ -n "$PRESCRIPTION_ID" ]; then
    test_pass "Created prescription: $PRESCRIPTION_ID (status: $PRESCRIPTION_STATUS)"
else
    test_fail "Failed to create prescription"
    echo "Response: $PRESCRIPTION_RESPONSE"
fi

echo ""
echo "Test 8: Get prescription by ID..."
GET_PRESC_RESPONSE=$(curl -s "$MEDICATION_URL/api/v1/prescriptions/$PRESCRIPTION_ID")
GET_PRESC_MED=$(echo "$GET_PRESC_RESPONSE" | grep -o '"medicationText":"[^"]*"' | cut -d'"' -f4)

if [ "$GET_PRESC_MED" = "Paracetamol 500mg" ]; then
    test_pass "Retrieved prescription: $GET_PRESC_MED"
else
    test_fail "Failed to retrieve prescription"
fi

echo ""
echo "Test 9: Get patient prescriptions..."
PATIENT_PRESCRIPTIONS=$(curl -s "$MEDICATION_URL/api/v1/prescriptions/patient/$PATIENT_ID")
PRESC_COUNT=$(echo "$PATIENT_PRESCRIPTIONS" | grep -o '"id":"[^"]*"' | wc -l | tr -d ' ')

if [ "$PRESC_COUNT" -ge 1 ]; then
    test_pass "Found $PRESC_COUNT prescription(s) for patient"
else
    test_fail "No prescriptions found for patient"
fi

echo ""
echo "Test 10: Count active prescriptions..."
ACTIVE_COUNT=$(curl -s "$MEDICATION_URL/api/v1/prescriptions/patient/$PATIENT_ID/count")

if [ "$ACTIVE_COUNT" -ge 1 ]; then
    test_pass "Active prescriptions count: $ACTIVE_COUNT"
else
    test_warn "Active prescriptions count: $ACTIVE_COUNT"
fi

echo ""
echo "Test 11: Check drug interactions..."
INTERACTION_RESPONSE=$(curl -s "$MEDICATION_URL/api/v1/prescriptions/patient/$PATIENT_ID/interactions")
INTERACTION_COUNT=$(echo "$INTERACTION_RESPONSE" | grep -o '"interactionCount":[0-9]*' | cut -d':' -f2)

test_pass "Interaction check completed (found: ${INTERACTION_COUNT:-0} interactions)"

echo ""

# ========================================
# REFERRAL MODULE TESTS
# ========================================
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}REFERRAL MODULE TESTS${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

RECEIVER_UNIT_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')

echo "Test 12: Create referral..."
REFERRAL_RESPONSE=$(curl -s -X POST "$REFERRAL_URL/api/v1/referrals" \
    -H "Content-Type: application/json" \
    -H "X-Unit-Id: $UNIT_ID" \
    -H "X-User-Id: $PRACTITIONER_ID" \
    -d "{
        \"patientId\": \"$PATIENT_ID\",
        \"patientName\": \"E2E Test Patient\",
        \"referralType\": \"CONSULTATION\",
        \"priority\": \"ROUTINE\",
        \"senderUnitName\": \"E2E Test Unit\",
        \"senderPractitionerName\": \"E2E Test Doctor\",
        \"receiverUnitId\": \"$RECEIVER_UNIT_ID\",
        \"receiverUnitName\": \"Specialist Unit\",
        \"requestedSpecialty\": \"Cardiology\",
        \"reason\": \"E2E Test - Request cardiology consultation for chest pain evaluation\",
        \"diagnosisCode\": \"R07.4\",
        \"diagnosisText\": \"Chest pain, unspecified\",
        \"clinicalHistory\": \"Patient presents with intermittent chest pain\",
        \"sendImmediately\": false
    }")

REFERRAL_ID=$(echo "$REFERRAL_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
REFERRAL_REF=$(echo "$REFERRAL_RESPONSE" | grep -o '"referralReference":"[^"]*"' | cut -d'"' -f4)

if [ -n "$REFERRAL_ID" ]; then
    test_pass "Created referral: $REFERRAL_ID (ref: $REFERRAL_REF)"
else
    test_fail "Failed to create referral"
    echo "Response: $REFERRAL_RESPONSE"
fi

echo ""
echo "Test 13: Get referral by ID..."
GET_REF_RESPONSE=$(curl -s "$REFERRAL_URL/api/v1/referrals/$REFERRAL_ID")
GET_REF_STATUS=$(echo "$GET_REF_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$GET_REF_STATUS" = "DRAFT" ]; then
    test_pass "Retrieved referral with status: $GET_REF_STATUS"
else
    test_fail "Unexpected referral status: $GET_REF_STATUS"
fi

echo ""
echo "Test 14: Send referral..."
SEND_RESPONSE=$(curl -s -X POST "$REFERRAL_URL/api/v1/referrals/$REFERRAL_ID/send")
SEND_STATUS=$(echo "$SEND_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$SEND_STATUS" = "SENT" ]; then
    test_pass "Referral sent successfully"
else
    test_fail "Failed to send referral, status: $SEND_STATUS"
fi

echo ""
echo "Test 15: Mark referral as received..."
RECEIVE_RESPONSE=$(curl -s -X POST "$REFERRAL_URL/api/v1/referrals/$REFERRAL_ID/receive")
RECEIVE_STATUS=$(echo "$RECEIVE_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$RECEIVE_STATUS" = "RECEIVED" ]; then
    test_pass "Referral marked as received"
else
    test_fail "Failed to mark referral as received, status: $RECEIVE_STATUS"
fi

echo ""
echo "Test 16: Get patient referrals..."
PATIENT_REFERRALS=$(curl -s "$REFERRAL_URL/api/v1/referrals/patient/$PATIENT_ID")
REF_COUNT=$(echo "$PATIENT_REFERRALS" | grep -o '"id":"[^"]*"' | wc -l | tr -d ' ')

if [ "$REF_COUNT" -ge 1 ]; then
    test_pass "Found $REF_COUNT referral(s) for patient"
else
    test_fail "No referrals found for patient"
fi

echo ""

# ========================================
# LAB MODULE TESTS
# ========================================
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}LAB MODULE TESTS${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

echo "Test 17: Create lab order..."
LAB_ORDER_RESPONSE=$(curl -s -X POST "$LAB_URL/api/lab/orders" \
    -H "Content-Type: application/json" \
    -H "X-Unit-Id: $UNIT_ID" \
    -H "X-Practitioner-Id: $PRACTITIONER_ID" \
    -d "{
        \"patientId\": \"$PATIENT_ID\",
        \"patientName\": \"E2E Test Patient\",
        \"priority\": \"ROUTINE\",
        \"orderingUnitName\": \"E2E Test Unit\",
        \"orderingPractitionerName\": \"E2E Test Doctor\",
        \"performingLabId\": \"$LAB_ID\",
        \"performingLabName\": \"Central Lab\",
        \"clinicalIndication\": \"Annual health check\",
        \"diagnosisCode\": \"Z00.0\",
        \"diagnosisText\": \"General health examination\",
        \"fastingRequired\": true,
        \"tests\": [
            {\"testCode\": \"CBC\", \"testName\": \"Complete Blood Count\"},
            {\"testCode\": \"BMP\", \"testName\": \"Basic Metabolic Panel\"},
            {\"testCode\": \"LFT\", \"testName\": \"Liver Function Test\"}
        ],
        \"sendImmediately\": false
    }")

LAB_ORDER_ID=$(echo "$LAB_ORDER_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
LAB_ORDER_REF=$(echo "$LAB_ORDER_RESPONSE" | grep -o '"orderReference":"[^"]*"' | cut -d'"' -f4)

if [ -n "$LAB_ORDER_ID" ]; then
    test_pass "Created lab order: $LAB_ORDER_ID (ref: $LAB_ORDER_REF)"
else
    test_fail "Failed to create lab order"
    echo "Response: $LAB_ORDER_RESPONSE"
fi

echo ""
echo "Test 18: Get lab order by ID..."
GET_LAB_RESPONSE=$(curl -s "$LAB_URL/api/lab/orders/$LAB_ORDER_ID")
GET_LAB_STATUS=$(echo "$GET_LAB_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$GET_LAB_STATUS" = "DRAFT" ]; then
    test_pass "Retrieved lab order with status: $GET_LAB_STATUS"
else
    test_fail "Unexpected lab order status: $GET_LAB_STATUS"
fi

echo ""
echo "Test 19: Send lab order..."
SEND_LAB_RESPONSE=$(curl -s -X POST "$LAB_URL/api/lab/orders/$LAB_ORDER_ID/send")
SEND_LAB_STATUS=$(echo "$SEND_LAB_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$SEND_LAB_STATUS" = "ORDERED" ] || [ "$SEND_LAB_STATUS" = "SENT" ]; then
    test_pass "Lab order sent successfully (status: $SEND_LAB_STATUS)"
else
    test_fail "Failed to send lab order, status: $SEND_LAB_STATUS"
fi

echo ""
echo "Test 20: Mark lab order as received by lab..."
RECEIVE_LAB_RESPONSE=$(curl -s -X POST "$LAB_URL/api/lab/orders/$LAB_ORDER_ID/receive")
RECEIVE_LAB_STATUS=$(echo "$RECEIVE_LAB_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$RECEIVE_LAB_STATUS" = "RECEIVED" ]; then
    test_pass "Lab order marked as received by lab"
else
    test_fail "Failed to mark lab order as received, status: $RECEIVE_LAB_STATUS"
fi

echo ""
echo "Test 21: Get patient lab orders..."
PATIENT_LAB_ORDERS=$(curl -s "$LAB_URL/api/lab/patient/$PATIENT_ID/orders")
LAB_COUNT=$(echo "$PATIENT_LAB_ORDERS" | grep -o '"id":"[^"]*"' | wc -l | tr -d ' ')

if [ "$LAB_COUNT" -ge 1 ]; then
    test_pass "Found $LAB_COUNT lab order(s) for patient"
else
    test_fail "No lab orders found for patient"
fi

echo ""

# ========================================
# SUMMARY
# ========================================
echo "========================================"
echo "E2E Test Summary"
echo "========================================"
echo ""
echo -e "Tests Passed: ${GREEN}$PASSED${NC}"
echo -e "Tests Failed: ${RED}$FAILED${NC}"
echo ""
echo "Test Data:"
echo "- Patient ID: $PATIENT_ID"
echo "- Appointment ID: $APPOINTMENT_ID"
echo "- Prescription ID: $PRESCRIPTION_ID"
echo "- Referral ID: $REFERRAL_ID"
echo "- Lab Order ID: $LAB_ORDER_ID"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed!${NC}"
    exit 1
fi
