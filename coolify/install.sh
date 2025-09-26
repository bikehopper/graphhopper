#!/bin/sh
node_version=$(node -v | grep -Eo '[0-9]+' | head -1)
# detect and install node version if necessary
if [ $node_version -ne 22 ]
then
    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
    export NVM_DIR="$HOME/.nvm"
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"  # This loads nvm
    [ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"  # This loads nvm bash_completion
    nvm install 22
    nvm use 22
fi
npx --yes @bikehopper/data-mirror # download data
mvn clean install -DskipTests # install dependencies