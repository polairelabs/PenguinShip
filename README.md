# NavaShip

## Continuous Integration

This project uses a Continuous Integration (CI) pipeline with GitHub Actions. When new changes are pushed to the `main` branch, the CI pipeline is triggered.

### CI Pipeline Workflow

The CI pipeline performs the following steps:

1. Build the Docker image for the API.
2. Push the Docker image to the GitHub Docker Registry.

The pipeline ensures that the API is containerized and available in the GitHub Docker Registry for deployment purposes.

## Continuous Deployment

This project uses Continuous Deployment (CD) with GitHub Actions. Certain environment variables and secrets are required for the deployment pipeline to function correctly.

### Deployed Stack

The Continuous Deployment (CD) pipeline uses `docker-compose` to deploy the application stack. The stack consists of the following services:

- **PostgreSQL Database**: A PostgreSQL database is used as the primary data store for the application.
- **API**: The API is a containerized application that serves as the backend for the project.

### CD Pipeline Workflow

The CD pipeline performs the following steps:

1. Check out the repository.
2. Copy the repository to the VPS using SCP (Secure Copy Protocol) with SSH.
3. SSH into the VPS.
4. Call `docker-compose.prod.yml` to deploy the application.

The pipeline ensures that the application is deployed to the VPS in an automated manner upon pushing changes to the `release` branch.

### Environment Variables
##### Under repository settings > Secrets and variables > Actions

The following environment variables are used in the continuous deployment pipeline to connect to the VPS:

- VPS_HOST
- VPS_PORT
- PASSPHRASE
- USERNAME

The table below lists all the other environment variables that are used to deploy the backend stack and whether they are stored as secrets (secure column) or plain environment variables 

| Variable Name                        | Description                                         | Secured | Dev Only |
|--------------------------------------|-----------------------------------------------------|---------|----------------------|
| DATABASE_NAME                        | Database name for the application                   |         |                      |
| DATABASE_PORT                        | Port number for the database connection             |         |                      |
| DATABASE_USER                        | Username for database access                        |         |                      |
| DATABASE_PASSWORD                    | Password for database user                          |   ✓     |                      |
| PGADMIN_DEFAULT_EMAIL               | Default email for PgAdmin access                    |         |         ✓            |
| PGADMIN_DEFAULT_PASSWORD            | Default password for PgAdmin access                 |   ✓     |         ✓            |
| API_DOMAIN                          | API domain for the application                      |         |                      |
| API_PORT                            | API port for the application                        |         |                      |
| WEBAPP_URL                          | URL for the web application                         |         |                      |
| SENDGRID_SENDER_EMAIL               | Sender email for SendGrid email service             |         |                      |
| SENDGRID_APIKEY                     | API key for SendGrid email service                  |   ✓     |                      |
| SENDGRID_VERIFY_EMAIL_TEMPLATE_ID   | Template ID for SendGrid email verification         |   ✓     |                      |
| SENDGRID_FORGOT_PASSWORD_EMAIL_TEMPLATE_ID | Template ID for SendGrid forgot password email  |   ✓     |                      |
| EASYPOST_APIKEY                     | API key for EasyPost shipping service               |   ✓     |                      |
| EASYPOST_WEBHOOK_ENDPOINT_SECRET    | Secret for EasyPost webhook endpoint                |   ✓     |                      |
| EASYPOST_WEBHOOK_ENDPOINT_URL       | URL for EasyPost webhook endpoint                   |         |                      |
| STRIPE_APIKEY                       | API key for Stripe payment processing               |   ✓     |                      |
| STRIPE_WEBHOOK_ENDPOINT_SECRET      | Secret for Stripe webhook endpoint                  |   ✓     |                      |

