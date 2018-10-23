#!/bin/sh

docker pull ubuntu:18.04
docker run -v `pwd`:/root/install -w /root/install -i -t `docker images ubuntu:18.04 -q` ./login.sh
