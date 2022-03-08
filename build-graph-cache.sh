#!/bin/sh
echo 'Fetching osm.pbf...';
curl -J "http://proxy-cache-svc.$POD_NAMESPACE/geofabrik$GEOFABRIK_PATH" -o $OSM_PBF_FILE_NAME --silent;
echo 'Finished downloading osm.pbf.';

echo 'Fetching gtfs...';
curl -J "http://proxy-cache-svc.$POD_NAMESPACE/511$API_511_PATH" -o $API_511_FILE_NAME --silent;
echo 'Finished downloading gtfs.';

ls -halt

echo 'Building graph-cache...';
java -jar *.jar import /graphhopper/bay-area/config.yml
echo 'Finished graph-cache.';

ls -halt

echo 'Building graph-cache tarball...';
tar -zvcf "graph-cache.tar.gz" ./graph-cache
echo 'Finished building tarball.';

ls -halt

echo 'Uploading tarball to MINIO...'
echo "BUCKET_NAME: ${BUCKET_NAME}, "
AWS_ACCESS_KEY_ID=$ACCESS_KEY AWS_SECRET_ACCESS_KEY=$SECRET_KEY aws s3api put-object --endpoint-url http://$MINIO_HOST.$POD_NAMESPACE --bucket $BUCKET_NAME --key graphhopper/graph-cache.tar.gz --body graph-cache.tar.gz
