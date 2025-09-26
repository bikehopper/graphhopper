#!/bin/sh
check_node() {
    if which node > /dev/null
    then
        node_version=$(node -v | grep -Eo '[0-9]+' | head -1)
        if [ $node_version -eq 22 ]
        then
            return 1
        fi
    fi

    return 0
}

check_node
has_node=$?
# detect and install node version if necessary
if [ "$has_node" -eq "0" ] 
then
    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
    export NVM_DIR="$HOME/.nvm"
    [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"  # This loads nvm
    [ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"  # This loads nvm bash_completion
    nvm install 22
    nvm use 22
fi
npx --yes @bikehopper/data-mirror -o /source-data # download data
mvn clean install -DskipTests # install dependencies