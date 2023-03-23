# Build & Configuration

## Build
`mvn package` Will produce an executable jar in the target folder. 

`docker build -t cashaccount .` Will build a docker image `cashaccount:latest` that can be used to execute the application

## Configuration
The application expects the following environment variables.
| Env. Var  | Purpose |
| ------------- | ------------- |
| `JDBC_HOST`  | Host for the JDBC connection  |
| `JDBC_PORT`  | Port for the JDBC connection  |
| `JDBC_DB`  | Database for the JDBC connection  |
| `JDBC_ID`  | User ID for the JDBC connection  |
| `JDBC_PASSWORD`  | Password for the JDBC connection |
| `CURRENCY_API_URL`  | URL for the API |
