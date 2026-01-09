FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /opt/app
COPY build/distributions/*.tar /opt/app/app.tar
RUN tar -xf /opt/app/app.tar && mv ordo-eventi-* ordo-eventi

FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/app

COPY --from=builder /opt/app/ordo-eventi/lib ./lib
COPY --from=builder /opt/app/ordo-eventi/bin ./bin

ENTRYPOINT ["./bin/ordo-eventi"]
