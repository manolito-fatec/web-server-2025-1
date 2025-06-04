#!/bin/sh
./mvnw package
exec java -jar target/*.jar