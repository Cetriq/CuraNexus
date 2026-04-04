-- CuraNexus Database Initialization Script
-- This script creates all required databases for the CuraNexus platform

-- Create main database (shared by patient, care-encounter, journal, task, authorization)
CREATE DATABASE curanexus;

-- Create audit database (separate for compliance/PDL requirements)
CREATE DATABASE curanexus_audit;

-- Create triage database (separate for emergency department module)
CREATE DATABASE curanexus_triage;

-- Create forms database (for form templates and submissions)
CREATE DATABASE curanexus_forms;

-- Create certificates database (for medical certificates)
CREATE DATABASE curanexus_certificates;

-- Create consent database (for patient consent and access blocks)
CREATE DATABASE curanexus_consent;

-- Grant privileges to the default user
GRANT ALL PRIVILEGES ON DATABASE curanexus TO postgres;
GRANT ALL PRIVILEGES ON DATABASE curanexus_audit TO postgres;
GRANT ALL PRIVILEGES ON DATABASE curanexus_triage TO postgres;
GRANT ALL PRIVILEGES ON DATABASE curanexus_forms TO postgres;
GRANT ALL PRIVILEGES ON DATABASE curanexus_certificates TO postgres;
GRANT ALL PRIVILEGES ON DATABASE curanexus_consent TO postgres;

-- Connect to each database and enable UUID extension
\c curanexus
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c curanexus_audit
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c curanexus_triage
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c curanexus_forms
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c curanexus_certificates
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c curanexus_consent
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Return to default database
\c postgres
