#!/bin/bash

apt update
apt install -y git
apt install -y wget
apt install -y curl
apt install -y zip
apt install -y npm

# Not actually required but handy
apt install -y vim

export DEBIAN_FRONTEND=noninteractive
apt install -y tzdata
ln -fs /usr/share/zoneinfo/America/New_York /etc/localtime
dpkg-reconfigure --frontend noninteractive tzdata

mkdir /opt/snet
cd /opt/snet

# Python 3.6

apt install -y python3
apt install -y python3-pip

################################################################################
# Manually install Python 3.6 in base images without the proper packages
#
#apt install -y build-essential checkinstall zlib1g-dev libreadline-gplv2-dev libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev
#wget https://www.python.org/ftp/python/3.6.5/Python-3.6.5.tar.xz
#tar xvf Python-3.6.5.tar.xz
#cd Python-3.6.5/
#./configure
#make -j8
#make altinstall
#cd..; rm -rf Python-3.6.5/; rm -f Python-3.6.5.tar.xz
################################################################################

# NodeJS 8.x

apt install -y nodejs
apt install -y npm
################################################################################
# Manually install nodejs and npm in base images without the proper packages
#
#curl -sL https://deb.nodesource.com/setup_8.x | sh
#apt install -y nodejs
#npm install -g npm
################################################################################

# Go

apt install -y golang-1.10
mkdir /opt/snet/go
#echo 'PS1="$PS1\n"' >> /root/.bashrc
echo 'export GOPATH="/opt/snet/go"' >> /root/.bashrc
echo 'export PATH="$PATH:/opt/snet/go/bin:/usr/lib/go-1.10/bin"' >> /root/.bashrc
source /root/.bashrc

# Go Extras

go get -v -u github.com/golang/dep/cmd/dep
go get -v -u github.com/golang/protobuf/protoc-gen-go
go get -v -u golang.org/x/lint/golint

# Proto3

cd /opt/snet
curl -OL https://github.com/google/protobuf/releases/download/v3.4.0/protoc-3.4.0-linux-x86_64.zip
unzip protoc-3.4.0-linux-x86_64.zip -d protoc3
mv protoc3/bin/* /usr/local/bin/
mv protoc3/include/* /usr/local/include/

# SNET Daemon

################################################################################
# Use pre-compiled release
curl -OL https://github.com/singnet/snet-daemon/releases/download/v0.1.0/snetd-0.1.0.tar.gz
tar xzvf snetd-0.1.0.tar.gz 
cp snetd-linux-amd64 /usr/local/bin/snetd
################################################################################
# Build using last available version
#cd /opt/snet/go/src
#git clone https://github.com/singnet/snet-daemon.git
#cd snet-daemon
#./scripts/install
#cp blockchain/agent.go vendor/github.com/singnet/snet-daemon/blockchain/
#./scripts/build linux amd64
#cp build/snetd-linux-amd64 ../../bin/snetd
################################################################################

# CLI

pip3.6 install --upgrade pip
apt install -y libusb-1.0-0-dev libudev1 libudev-dev
mkdir -p /tmp/install
cd /tmp/install
git clone https://github.com/singnet/snet-cli
cd snet-cli
./scripts/blockchain install
pip3.6 install -e .

cd ~
