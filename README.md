# spring-cloud-MSA-Service


### discovery Service
- server.port: 8761

### user Service
- server.port: 9001, 9002, 9003, 9004

### springBoot run with gradlew
- `./gradlew bootRun --args='--server.port=9003'`
- `./gradlew bootRun`

### gradlew build
- `./gradlew build`
- `java -jar -Dserver.port=9004 ./build/libs/user-0.0.1-SNAPSHOT.jar`

### maven SpringBoot run
- `mvn spring-boot:run -Dspring-boot.run.jvmArguments=’-Dserver.port=9003’`

### maven Build
- `mvn clean` <br>
- `mvn complie package`<br>
- `java -jar -Dserver.port=9004 ./target/user-0.0.1-SNAPSHOT.jar`


