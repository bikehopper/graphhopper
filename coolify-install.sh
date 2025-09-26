#!/bin/sh
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
source ~/.bashrc
nvm install 22
nvm use 22
npx --yes @bikehopper/data-mirror # download data
mvn clean install -DskipTests # install dependencies