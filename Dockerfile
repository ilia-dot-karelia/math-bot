FROM arm32v7/openjdk:11
COPY private/secrets.properties /usr/local/apps/bot/private/secrets.properties
COPY target/libs /usr/local/apps/bot/libs
COPY target/bot.jar /usr/local/apps/bot
WORKDIR /usr/local/apps/bot
CMD java -jar bot.jar
