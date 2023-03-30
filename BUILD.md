# Build & Configuration

## Build
`docker build -t cashaccount .` Will build a docker image `cashaccount:latest` that can be used to execute the application

There are a few issues you might come across when building this project.

### Issue 1: Skipping Maven Tests
In our docker file, we specifically use `RUN mvn clean -e package spring-boot:repackage -DskipTests=true` to skip tests. Because Maven defaults to creating a test run before creating the jar file, Cash Account requires several environment variables before passing it. One can manually set the environment variables and remove `-DskipTests=true` if they would like to test any changes before building the image. But for the sake of convenience and the ability to dynamically set the variables during deployment, the Dockerfile will use that as a default.

### Issue 2: ARM64 vs AMD64
When building the Dockerfile from an M1 Mac, the image produced will be using an ARM64 architecture. This is fine when building and testing locally on your personal machine. However, when building and running the image on Intel-based worker node for Kubernetes, one can uncomment line 15 of the Dockerfile or append `--platform amd64` to the docker build command to produce the correct image.


## Configuration
The application expects the following environment variables.
| Env. Var  | Purpose |
| ------------- | ------------- |
| `JDBC_HOST`  | Host for the JDBC connection  |
| `JDBC_PORT`  | Port for the JDBC connection  |
| `JDBC_DB`  | Database for the JDBC connection  |
| `JDBC_ID`  | User ID for the JDBC connection  |
| `JDBC_PASSWORD`  | Password for the JDBC connection |
| `REDIS_URL`  | URL for the Redis Server |
| `CURRENCY_API_URL`  | URL for the API |

The application also expects a preexisting database with the provided [createTables.ddl](src/main/resources/createTables.ddl) statements.
