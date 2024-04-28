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
ARG _IS_ORDER_RETRY
ARG _RETRY_DELAY
ARG _RETRY_INTERVAL
ENV IS_ACTUALLY_SELL_BUY=${_IS_ACTUALLY_SELL_BUY} SELL_BUY_AMOUNT=${_SELL_BUY_AMOUNT} SELL_BUY_PRICE=${_SELL_BUY_PRICE} IS_ORDER_RETRY=${_IS_ORDER_RETRY} RETRY_DELAY=${_RETRY_DELAY} RETRY_INTERVAL=${_RETRY_INTERVAL}

RUN echo $IS_ACTUALLY_SELL_BUY
RUN echo $SELL_BUY_AMOUNT
RUN echo $SELL_BUY_PRICE
RUN echo $IS_ORDER_RETRY
RUN echo $RETRY_DELAY
RUN echo $RETRY_INTERVAL

COPY --from=builder /app/target/crypt-cat-*.jar /crypt-cat.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/crypt-cat.jar"]