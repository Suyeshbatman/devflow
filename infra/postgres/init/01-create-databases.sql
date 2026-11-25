-- Runs automatically ONLY on a brand-new (empty) Postgres volume.
-- The main database (devflow_users) is already created by POSTGRES_DB in .env;
-- this script adds one extra database per additional microservice.
-- Files here run in alphabetical order, so the "01-" prefix controls the order.

-- [DDL] CREATE DATABASE: a separate database on the same server, owned by the
-- user running this script (POSTGRES_USER = devflow).
CREATE DATABASE devflow_marketplace;