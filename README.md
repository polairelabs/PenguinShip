# NavaShip

## Running the Stack Locally

To run the stack locally, follow these steps:

1. Edit the `.env` file and update the values as needed.
2. Run `docker-compose -f docker-compose.dev.yml up` to start the Postgres database and pgAdmin.
3. Load the environment variables from the `.env` file. If you are using IntelliJ IDEA, you can follow the example in the next section 
4. Run the API

### Loading Environment Variables in IntelliJ IDEA

If you're using IntelliJ IDEA, you can load environment variables from your .env file directly into the IDE. Follow these steps:

1. Open your project in IntelliJ IDEA.
2. Go to File > Settings (or IntelliJ IDEA > Preferences on macOS).
3. In the Settings dialog, navigate to Plugins.
4. Click the Marketplace tab and search for EnvFile.
5. Install the EnvFile plugin and restart IntelliJ IDEA when prompted.

Now, you can configure IntelliJ to load the environment variables from the .env file when running your application:

1. In the top-right corner, click on the Edit Configurations button (it looks like a dropdown list with a gear icon).
2. In the Run/Debug Configurations dialog, select your Spring Boot application configuration in the left pane.
3. In the right pane, under the Configuration tab, scroll down to the Environment section.
4. Check the Enable box next to EnvFile.
5. Click the + button and browse to your .env file in the project directory. Select the file and click OK.
6. Click Apply and then OK to save the configuration.


## Continuous Integration

This project uses a Continuous Integration (CI) pipeline with GitHub Actions. When new changes are pushed to the `main` branch, the CI pipeline is triggered.

### CI Pipeline Workflow

The CI pipeline performs the following steps:

1. Build the Docker image for the API.
2. Push the Docker image to the GitHub Docker Registry.

The pipeline ensures that the API is containerized and available in the GitHub Docker Registry for deployment purposes.

## Continuous Deployment

This project utilizes Continuous Deployment (CD) with GitHub Actions, automatically deploying the application to the VPS when changes are pushed to the `release` branch.

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

