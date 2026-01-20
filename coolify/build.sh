#!/bin/sh
java -Xmx8g -jar web/target/graphhopper-web-*.jar import bay-area/config.yml
npx @bikehopper/node-tippecanoe -zg -Z8 -l ways_dump -P -o web-bundle/src/main/resources/com/graphhopper/maps/ways.pmtiles --drop-densest-as-needed logs/ways_dump.ldgeojson
