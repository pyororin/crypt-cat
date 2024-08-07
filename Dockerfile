FROM gradle:8.8.0-jdk17 as builder

WORKDIR /app
COPY build.gradle .
COPY src ./src
RUN ls -l .

RUN gradle booJar
RUN ls -l /app/build/libs/

# Use Eclipse Temurin for base image.
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM eclipse-temurin:17.0.10_7-jre-alpine

ARG _IS_ACTUALLY_SELL_BUY
ARG _SELL_BUY_AMOUNT
ARG _SELL_BUY_PRICE
ARG _RETRY_DELAY_SEC
ARG _RETRY_LIMIT_COUNT
ARG _ORDER_INTERVAL
ARG _CANCEL_DELAY_MINUTES

ENV \
IS_ACTUALLY_SELL_BUY=${_IS_ACTUALLY_SELL_BUY} \
SELL_BUY_AMOUNT=${_SELL_BUY_AMOUNT} \
SELL_BUY_PRICE=${_SELL_BUY_PRICE} \
RETRY_DELAY_SEC=${_RETRY_DELAY_SEC} \
RETRY_LIMIT_COUNT=${_RETRY_LIMIT_COUNT} \
ORDER_INTERVAL=${_ORDER_INTERVAL} \
CANCEL_DELAY_MINUTES=${_CANCEL_DELAY_MINUTES}


RUN echo $IS_ACTUALLY_SELL_BUY
RUN echo $SELL_BUY_AMOUNT
RUN echo $SELL_BUY_PRICE
RUN echo $RETRY_DELAY_SEC
RUN echo $RETRY_LIMIT_COUNT
RUN echo $ORDER_INTERVAL
RUN echo $CANCEL_DELAY_MINUTES

COPY --from=builder /app/build/libs/*.jar /crypt-cat.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/crypt-cat.jar"]
