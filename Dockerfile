FROM maven:3-eclipse-temurin-17-alpine as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests

# Use Eclipse Temurin for base image.
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM eclipse-temurin:17.0.10_7-jre-alpine

ARG _IS_ACTUALLY_SELL_BUY
ARG _SELL_BUY_AMOUNT
ARG _SELL_BUY_PRICE
ARG _RETRY_DELAY_SEC
ARG _RETRY_LIMIT_COUNT
ARG _ORDER_INTERVAL

ENV \
IS_ACTUALLY_SELL_BUY=${_IS_ACTUALLY_SELL_BUY} \
SELL_BUY_AMOUNT=${_SELL_BUY_AMOUNT} \
SELL_BUY_PRICE=${_SELL_BUY_PRICE} \
RETRY_DELAY_SEC=${_RETRY_DELAY_SEC} \
RETRY_LIMIT_COUNT=${_RETRY_LIMIT_COUNT} \
ORDER_INTERVAL=${_ORDER_INTERVAL}


RUN echo $IS_ACTUALLY_SELL_BUY
RUN echo $SELL_BUY_AMOUNT
RUN echo $SELL_BUY_PRICE
RUN echo $RETRY_DELAY_SEC
RUN echo $RETRY_LIMIT_COUNT
RUN echo $ORDER_INTERVAL

COPY --from=builder /app/target/crypt-cat-*.jar /crypt-cat.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/crypt-cat.jar"]