FROM mcr.microsoft.com/java/maven:8-zulu-debian9 as build
WORKDIR /src/siembol
COPY . .
RUN mvn package && rm /src/siembol/*/*/target/original-*.jar


FROM storm:1.2.3
WORKDIR /deploy
COPY --from=build /src/siembol/*/*-storm/target/*-storm-*.jar ./
