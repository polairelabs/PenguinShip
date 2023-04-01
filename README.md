# NavaShip

### Environnemet variables

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

