-- CuraNexus Database Initialization Script
-- This script creates all required databases for the CuraNexus platform

-- Create main database (shared by patient, care-encounter, journal, task, authorization)
CREATE DATABASE curanexus;

-- Create audit database (separate for compliance/PDL requirements)
CREATE DATABASE curanexus_audit;

-- Create triage database (separate for emergency department module)
CREATE DATABASE curanexus_triage;

-- Grant privileges to the default user
GRANT ALL PRIVILEGES ON DATABASE curanexus TO postgres;
GRANT ALL PRIVILEGES ON DATABASE curanexus_audit TO postgres;
GRANT ALL PRIVILEGES ON DATABASE curanexus_triage TO postgres;

-- Connect to each database and enable UUID extension
\c curanexus
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c curanexus_audit
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c curanexus_triage
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Return to default database
\c postgres
