# Cash Account

Kyndryl Stocktrader Cash Account Microservice prototype made using Spring. This service is to manage an account for a portfolio. This service uses a Postgres database using JDBC to keep track of accounts and a Redis cache for storing currency rates.

The following operations are available:

- `GET /cash-account/{owner}` - gets account data from a specific owner
- `GET /cash-account/currency/{owner} ` - gets the currency rate from USD to specified owner's currency
- `POST /cash-account` - creates an account
- `PUT /cash-account/{owner}` - updates the account of a specific owner
- `PUT /cash-account/debit/{owner}/{debitAmount}` - subtracts money (USD) from a specific owner's account
- `PUT /cash-account/credit/{owner}/{creditAmount}` - adds money (USD) from a specific owner's account
- `DELETE /cash-account/{owner}` - deletes the account of a specific owner

This project is developed by [Celina Chen](https://github.com/CC9759) with the help of the Kyndryl Cloud Journey Optimization team!



