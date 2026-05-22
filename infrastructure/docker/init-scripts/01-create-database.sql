-- This script runs automatically when PostgreSQL container
-- starts for the first time (via docker-entrypoint-initdb.d)

-- Create the marketplace database
-- devflow_users already created by POSTGRES_DB env variable
CREATE DATABASE devflow_marketplace;

-- Grant all permissions to our devflow user
GRANT ALL PRIVILEGES ON DATABASE devflow_users TO devflow;
GRANT ALL PRIVILEGES ON DATABASE devflow_marketplace TO devflow;