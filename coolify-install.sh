#!/bin/sh
npx --yes @bikehopper/data-mirror # download data
mvn clean install -DskipTests # install dependencies