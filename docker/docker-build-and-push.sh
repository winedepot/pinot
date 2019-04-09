#!/bin/bash

sh docker-build.sh $@
if [[ "$#" -gt 0 ]]
then
  DOCKER_TAG=$1
else
  DOCKER_TAG=test
fi
echo "Trying to push docker image to winedepot/pinot:${DOCKER_TAG}"
docker push winedepot/pinot:${DOCKER_TAG}