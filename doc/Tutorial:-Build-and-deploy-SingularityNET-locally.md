## Table of contents

- [Install prerequisites](#install-prerequisites)
- [Deploy local environment](#deploy-local-environment)
- [Start environment](#start-environment)
- [Publish and run example-service](#publish-and-run-example-service)

## Overview

This tutorial describes the process of building and deploying fully functional local SingularityNET environment. In such environment one can publish services, call them and have full control over local test blockchain network.

## Install prerequisites

This document describes the process of environment setup in Ubuntu 18.04. Some commands can be different under other linux distributions.

### Go toolset

- Go 1.10+
- Dep 0.4.1+
- Go Protobuf Compiler
- Golint

Part of the code is written in [Go](https://golang.org) language so you need a set of tools to compile Go code and manage Go dependencies.

```
sudo apt-get install golang go-dep golang-goprotobuf-dev golint
```

### NodeJS toolset

- NodeJS 8+ 
- NPM

[Truffle](https://truffleframework.com/truffle) and [Ganache](https://truffleframework.com/ganache) are used to develop and test Ethereum contracts so NodeJS development tools are required.

```
sudo apt-get install nodejs npm
```

### IPFS

IPFS is used to keep RPC models of the services which are published via SingularityNET platform. Follow instructions at https://ipfs.io/docs/install to download and install IPFS. Following steps expects that ```ipfs``` is installed and can be run from the command line.

### Python toolset

- Python 3.6.5
- Pip

Part of the code is written in Python so you need a Python interpreter and Pip as python package manager.

```
sudo apt-get install python3 python3-pip
```

### Other

- libudev
- libusb 1.0

```
sudo apt-get install libudev-dev libusb-1.0-0-dev
```

## Deploy local environment

### Setup Go building environments

Go compiler expects that path to the workspace is exported as ```GOPATH``` variable. ```SINGNET_REPOS``` is exported to simplify change directory commands below.

```
mkdir -p singnet/src/github.com/singnet
cd singnet
mkdir log
export GOPATH=`pwd`
export SINGNET_REPOS=${GOPATH}/src/github.com/singnet
export PATH=${GOPATH}/bin:${PATH}
```

### Deploy local IPFS instance

IPFS is used by SingularityNET to keep published services RPC models. For local test environment we will setup private local IPFS instance.

Initialize IPFS data folder:

```
export IPFS_PATH=$GOPATH/ipfs
ipfs init
```

Remove all default IPFS bootstrap instances from default IPFS configuration (see [IPFS private network](https://github.com/ipfs/go-ipfs/blob/master/docs/experimental-features.md#private-networks)).

```
ipfs bootstrap rm --all
```

Change IPFS API and Gateway ports because they intersect with default ```example-service``` and snet-daemon ports.

```
ipfs config Addresses.API /ip4/127.0.0.1/tcp/5002
ipfs config Addresses.Gateway /ip4/0.0.0.0/tcp/8081
```

### Compile platform contracts

Clone platform-contracts repository:

```
cd $SINGNET_REPOS
git clone https://github.com/singnet/platform-contracts
cd platform-contracts
```

Install dependencies and Ganache using NPM:

```
npm install
npm install ganache-cli
```

Compile contracts using Truffle:

```
./node_modules/.bin/truffle compile
```

### Setup ```snet``` command line interface

Clone snet-cli repository:

```
cd $SINGNET_REPOS
git clone https://github.com/singnet/snet-cli
cd snet-cli
```

Install blockchain dependencies and snet-cli package in development mode.

```
# you need python 3.6 here, with python 3.5 you will get an error
./scripts/blockchain install
pip3 install -e .
```

After this ```snet``` command will be available. Run it for the first time to
create default config:

```
snet
```

Add local Ethereum network to the ```snet``` configuration and use this network by default. 

```
cat >> ~/.snet/config << EOF
[network.local]
default_eth_rpc_endpoint = http://localhost:8545
EOF
```

Replace IPFS instance in ```snet``` configuration by local instance: 

```
sed -ie '/ipfs/,+2d' ~/.snet/config
cat >> ~/.snet/config << EOF
[ipfs]
default_ipfs_endpoint = http://localhost:5002
EOF
```

### Build snet-daemon

Clone ```snet-daemon``` repository:

```
cd $SINGNET_REPOS
git clone https://github.com/singnet/snet-daemon
cd snet-daemon
```

Build ```snet-daemon```:

```
./scripts/install # install dependencies
./scripts/build linux amd64  # build project
```

### Prepare example service

Clone example-service repository:

```
cd $SINGNET_REPOS
git clone https://github.com/singnet/example-service
cd example-service
```

Install example-service dependencies: 

```
cd $SINGNET_REPOS/example-service 
pip3 install -r requirements.txt
```

Make link to snet-daemon executable file:

```
ln -s $SINGNET_REPOS/snet-daemon/build/snetd-linux-amd64 ./snetd-linux-amd64
```

## Start environment

### Start local IPFS instance

Start IPFS daemon:

```
ipfs daemon >$GOPATH/log/ipfs.log 2>&1 &
```

### Start local Ethereum network

Start local Ethereum network. Pass mnemonic to produce deterministic blockchain environment: accounts, private keys and behavior.

```
cd $SINGNET_REPOS/platform-contracts
./node_modules/.bin/ganache-cli --mnemonic 'gauge enact biology destroy normal tunnel slight slide wide sauce ladder produce' >$GOPATH/log/ganache.log 2>&1 &
```

Accounts and private keys printed by Ganache will be used in next steps. Deploy contracts using Truffle.

```
./node_modules/.bin/truffle migrate --network local 
```

Contract addresses printed after deployment will be used in next steps. 

Truffle deploys contracts using first account of the test network. As SingularityNETToken contract is deployed using this account this account's balance keeps all of SingularityNET tokens issued during deployment. Other contracts deployed are Registry and AgentFactory. Registry keeps the list of organization and published services. AgentFactory is used by service publishing to issue new Agent contract instance.

## Publish and run example-service

### Create Ethereum identity

Create new identity to call SingularityNET using ```snet``` tool and make it default identity:

```
snet identity create snet-user key --private-key 0xc71478a6d0fe44e763649de0a0deb5a080b788eefbbcf9c6f7aef0dd5dbd67e0
snet identity snet-user
```
Example above uses private key for the first account generated by ganache. As this account was used for deploying platform-contracts it has all of AGI tokens emitted during deployment as described in previous step.

0xc71478a6d0fe44e763649de0a0deb5a080b788eefbbcf9c6f7aef0dd5dbd67e0 - is a private key of the first account which will be generated by Ganache test network.

### Use local Ethereum network

Use ```snet``` tool to select local Ethereum network:
```
snet network local
```

### Add test organization

Use ```snet``` command to call createOrganization method of Registry contract. Method will persist in Registry new organization named 'ExampleOrganization' with one member which address is 0x3b2b3c2e2e7c93db335e69d827f3cc4bc2a2a2cb (second account generated by Ganache). Registry contract address can be seen in ```truffle migrate``` output.

```
snet contract Registry --at 0x8d1c8634f032d1c65c540faca15f7df83fbb9f8c createOrganization ExampleOrganization '["0x3b2b3c2e2e7c93db335e69d827f3cc4bc2a2a2cb"]' --transact
```

0x8d1c8634f032d1c65c540faca15f7df83fbb9f8c - is a Registry contract address
0x3b2b3c2e2e7c93db335e69d827f3cc4bc2a2a2cb - is an organization member

### Publish example-service

Generate new ```service.json``` to register local service:

```
cd $SINGNET_REPOS/example-service
snet service init
```

```snet service init``` command runs interactive dialog to fill service settings. Answer all questions as below:

```
Please provide values to populate your service.json file

Choose a name for your service: (default: "example-service")

Choose the path to your service's spec directory: (default: "service_spec/")

Choose an organization to register your service under: (default: "")
ExampleOrganization
Choose the path under which your Service registration will be created: (default: "")

Choose a price in AGI to call your service: (default: 0)
1
Endpoint to call the API for your service: (default: "")
http://localhost:8080
Input a list of tags for your service: (default: [])
example service
Input a description for your service: (default: "")
Example service
```

Endpoint field is a snet-daemon endpoint. Snet-daemon will receive requests for service and forward them to the real service endpoint which can be found in service configuration. 

Publish service to the local test network. AgentFactory contract address can be seen in ```truffle migrate``` output.

```
snet service publish local --config ./service.json --agent-factory-at 0x4e74fefa82e83e0964f0d9f53c68e03f7298a8b2 --registry-at 0x8d1c8634f032d1c65c540faca15f7df83fbb9f8c
```

0x4e74fefa82e83e0964f0d9f53c68e03f7298a8b2 - is an AgentFactory contract address
0x8d1c8634f032d1c65c540faca15f7df83fbb9f8c - is a Registry contract address

```snet``` tool creates archive with service protobuf API and puts it to the IPFS. Then AgentFactory contract is called. AgentFactory creates new instance of Agent contract for the service and puts the following information as Agent contract state:
- AGI tokens contract address to fund new Jobs
- service API IPFS hash to provide this information to the service clients

### Run example-service with snet-daemon 

Create snet-daemon configuration. It requires few magic numbers. AGENT_CONTRACT_ADDRESS is printed by ```snet``` tool when new service is published. Also you can read it in $SINGNET_REPOS/example-service/service.json file. PRIVATE_KEY is a private key of the account which will receive AGI tokens after service job is completed. In example below PRIVATE_KEY field contains third account generated by Ganache.

```
cat > snetd.config.json << EOF
{
    "AGENT_CONTRACT_ADDRESS": "0x3b07411493C72c5aEC01b6Cf3cd0981cF0586fA7",
    "AUTO_SSL_DOMAIN": "",
    "AUTO_SSL_CACHE_DIR": "",
    "BLOCKCHAIN_ENABLED": true,
    "CONFIG_PATH": "",
    "DAEMON_LISTENING_PORT": 8080,
    "DAEMON_TYPE": "grpc",
    "DB_PATH": "./db",
    "ETHEREUM_JSON_RPC_ENDPOINT": "http://localhost:8545",
    "EXECUTABLE_PATH": "",
    "LOG_LEVEL": 5,
    "PASSTHROUGH_ENABLED": true,
    "PASSTHROUGH_ENDPOINT": "http://localhost:5001",
    "POLL_SLEEP": "5s",
    "PRIVATE_KEY": "ba398df3130586b0d5e6ef3f757bf7fe8a1299d4b7268fdaae415952ed30ba87",
    "SERVICE_TYPE": "jsonrpc",
    "SSL_CERT": "",
    "SSL_KEY": "",
    "WIRE_ENCODING": "json"
}
EOF
```

Run example-service and snet-daemon:

```
./scripts/run-snet-service >$GOPATH/log/example-service.log 2>&1 &
```

Test service directly without SingularityNET infrastructure:

```
./scripts/test-call
```

```test-call``` script calls a service using two JPEG images encoded in base64 and receives response. It doesn't use snet-daemon proxy, it just calls the service directly using service endpoint.

### Make call to example-service via SingularityNET 

Create, fund and sign SingularityNET Job to pay for the call:

```
snet agent --at 0x3b07411493C72c5aEC01b6Cf3cd0981cF0586fA7 create-jobs --funded --signed --max-price 100000000 # create job
```

0x3b07411493C72c5aEC01b6Cf3cd0981cF0586fA7 - example-service agent address from $SINGNET_REPOS/example-service/service.json

This step is not required when using ```snet``` tool because ```snet client call``` will generate job automatically. But it is placed here to illustrate how to create jobs. Job is a contract between client and service which states that client will pay some number of AGI tokens when service handles the call. New instance of the Job contract is created by Agent. It contains Token contract address, job price, service address to pay to and Job state. After this step Job is in FUNDED state.

Make call to example service, ```snet``` will automatically use last Job created:

```
snet client call classify '{"image_type": "jpg", "image": "<jpeg_image_in_base64>"}' --agent-at 0x3b07411493C72c5aEC01b6Cf3cd0981cF0586fA7
```

Client sends request which contains Job address. ```snet-daemon``` receives the request checks the Job state and forwards request to the service. After service response ```snet-daemon``` completes the Job and service owner receives AGI tokens. Response is delivered to client. 

Here client may send any image in JPEG base64 encoded format. And example-service tries to classify it and returns response.