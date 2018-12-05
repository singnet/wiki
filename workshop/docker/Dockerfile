FROM ubuntu:18.04

# setup environment variables
ENV ROOT /singnet
ENV GOPATH ${ROOT}
ENV PATH ${GOPATH}/bin:${PATH}
ENV LOG ${ROOT}/log
ENV SINGNET ${GOPATH}/src/github.com/singnet

# setup folders needed
RUN mkdir -p ${LOG}
RUN mkdir -p ${SINGNET}

# update apt
RUN apt-get update
# install git
RUN apt-get install -y git
# install golang dev environment
RUN apt-get install -y golang go-dep golang-goprotobuf-dev golint
# install NodeJS dev environment
RUN apt-get install -y nodejs npm
RUN npm install -g ganache-cli truffle
# install Python dev environment
RUN apt-get install -y python3 python3-pip
# install other libraries
RUN apt-get install -y libudev-dev libusb-1.0-0-dev

# install IPFS
RUN apt-get install -y curl
COPY ./install_ipfs.sh ${GOPATH}/bin/
RUN install_ipfs.sh
# setup IPFS
ENV IPFS_PATH ${GOPATH}/bin
RUN ipfs init
RUN ipfs bootstrap rm --all
RUN ipfs config Addresses.API /ip4/127.0.0.1/tcp/5002
RUN ipfs config Addresses.Gateway /ip4/0.0.0.0/tcp/8081

# token-contracts
WORKDIR ${SINGNET}
RUN git clone https://github.com/singnet/token-contracts
WORKDIR ${SINGNET}/token-contracts
RUN git checkout -b demo v1.0.0

# plaform-contracts
WORKDIR ${SINGNET}
RUN git clone https://github.com/singnet/platform-contracts
WORKDIR ${SINGNET}/platform-contracts
RUN git checkout -b demo v0.2.4
RUN npm install
RUN truffle compile

# snet-cli
WORKDIR ${SINGNET}
RUN git clone https://github.com/singnet/snet-cli
WORKDIR ${SINGNET}/snet-cli
RUN git checkout -b demo v0.1.8
RUN ./scripts/blockchain install
RUN pip3 install -e .

# snet-daemon
WORKDIR ${SINGNET}
RUN git clone https://github.com/singnet/snet-daemon
WORKDIR ${SINGNET}/snet-daemon
RUN git checkout -b demo v0.1.3
RUN ./scripts/install
RUN ./scripts/build linux amd64
RUN cp ./build/snetd-linux-amd64 ${GOPATH}/bin

# wiki
WORKDIR ${SINGNET}
RUN git clone https://github.com/singnet/wiki

# publish Ethereum related environment variables
ENV NETWORK_ID 12345
ENV DEPLOYER_ADDR 0x592e3c0f3b038a0d673f19a18a773f993d4b2610
ENV DEPLOYER_KEY 0xc71478a6d0fe44e763649de0a0deb5a080b788eefbbcf9c6f7aef0dd5dbd67e0
ENV CLIENT_ADDR 0x3b2b3c2e2e7c93db335e69d827f3cc4bc2a2a2cb
ENV CLIENT_KEY 0x04899d5fd471ce68f84a5ec64e2e4b6b045d8b850599a57f5b307024be01f262
ENV PUBLISHER_ADDR 0x0067b427e299eb2a4cbafc0b04c723f77c6d8a18
ENV PUBLISHER_KEY 0xba398df3130586b0d5e6ef3f757bf7fe8a1299d4b7268fdaae415952ed30ba87

# creating identities
RUN snet
RUN snet network create local http://localhost:8545
RUN snet identity create deployer key --private-key 0xc71478a6d0fe44e763649de0a0deb5a080b788eefbbcf9c6f7aef0dd5dbd67e0 --network local
RUN snet identity create client key --private-key 0x04899d5fd471ce68f84a5ec64e2e4b6b045d8b850599a57f5b307024be01f262 --network local
RUN snet identity deployer

COPY ./start.sh ${GOPATH}/bin/
CMD start.sh
