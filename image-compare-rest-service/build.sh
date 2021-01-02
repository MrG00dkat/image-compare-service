#!/bin/bash

set -o xtrace

mvn clean package

mkdir target/dependency
(cd target/dependency; jar -xf ../*.jar)
docker build -t image-compare-service -f src/main/docker/Dcokerfile .

