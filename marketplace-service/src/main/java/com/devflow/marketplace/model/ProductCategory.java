package com.devflow.marketplace.model;

// Categories a product can belong to in DevFlow marketplace
public enum ProductCategory {
    API_TOOL,        // REST/GraphQL API tools
    DEVOPS_TOOL,     // CI/CD, Docker, K8s tools
    MONITORING,      // Logging, metrics, alerting
    SECURITY,        // Auth, encryption, scanning
    DATABASE,        // DB tools, migrations, ORMs
    AI_ML,           // AI/ML tools and models
    FRONTEND,        // UI components, design systems
    TESTING,         // Test frameworks, mocking tools
    OTHER
}