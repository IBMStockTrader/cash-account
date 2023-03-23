#        Copyright 2023 Kyndryl, All Rights Reserved

#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at

#        http://www.apache.org/licenses/LICENSE-2.0

#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.

# FROM maven:3.8.7-openjdk-18-slim@sha256:de5262140ec5c7ddb053f11ff5569184d8988947d771d3beca98c0400fbd3f19
FROM maven:3.8.7-openjdk-18-slim

RUN mkdir /app

COPY . /app

WORKDIR /app

RUN mvn clean -e package spring-boot:repackage -DskipTests=true

RUN ls target/
CMD ["java", "-jar", "target/cashaccount-0.0.1-SNAPSHOT.jar"]