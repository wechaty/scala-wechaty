FROM java:openjdk-8-jdk-alpine
MAINTAINER Jun Tsai jcai AT Ganshane Dot Com
CMD ["java", "-version"]
ADD target/dependencies /usr/share/wechaty/lib
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/wechaty/wechaty.jar
ENTRYPOINT ["java", "-classpath","/usr/share/wechaty/lib/*:/usr/share/wechaty/wechaty.jar","wechaty.DingDongBot"]
