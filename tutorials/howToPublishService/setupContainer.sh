docker pull nvidia/cuda:latest
docker run -v `pwd`:/root/install -w /root/install -i -t `docker images nvidia/cuda:latest -q` ./login.sh
