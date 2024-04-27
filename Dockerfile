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
ARG _ORDER_RETRY
ENV IS_ACTUALLY_SELL_BUY=${_IS_ACTUALLY_SELL_BUY} SELL_BUY_AMOUNT=${_SELL_BUY_AMOUNT} SELL_BUY_PRICE=${_SELL_BUY_PRICE} ORDER_RETRY=${_ORDER_RETRY}

RUN echo $IS_ACTUALLY_SELL_BUY
RUN echo $SELL_BUY_AMOUNT
RUN echo $SELL_BUY_PRICE
RUN echo$ORDER_RETRY

COPY --from=builder /app/target/crypt-cat-*.jar /crypt-cat.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/crypt-cat.jar"]