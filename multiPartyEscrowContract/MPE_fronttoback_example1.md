# Front to back example of MPE payment system (Example1)

Simple front to back example of using MPE payment system in
SingularityNET with one replica configuration.
Here we will demonstrate the following.

from the service side:

* How to publish your service (in MPE payment system)
* How to configure your daemon(s)
* How to claim the funds from the server side using "tresurer server"

from the client side:
* How to open the payment channel 
* How to make calls using MPE payment system

## Preparation 
Please follow the following tutorial
[Build-and-deploy-SingularityNET-locally](https://github.com/singnet/wiki/wiki/Tutorial:-Build-and-deploy-SingularityNET-locally).
You should follow this tutorial until "Publish example-service". 

We assume the following
* You have already registered organization "ExampleOrganization"
* You have already registered ExampleService in ExampleOrganization
and have an Agent at address 0x3b07411493C72c5aEC01b6Cf3cd0981cF0586fA7

We will not use Agent at all! But daemon still require one live Agent
because of some artifacts of old logic which are still presented. So
you need this Agent to run a daemon, but we don't use it.

We also assume the following addresses:

```bash
# Address of MultiPartyEscrow  : 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e
# Address of Tokens            : 0x6e5f20669177f5bdf3703ec5ea9c4d4fe3aabd14
# First Address (snet identity): 0x592E3C0f3B038A0D673F19a18a773F993d4b2610
# Second Address (service)     : 0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB
```

You will have them like this, If you started ganache with the
mnemonics from tutorial: 'gauge enact
biology destroy normal tunnel slight slide wide sauce ladder produce'.


## Configure, Register and start your service (service provider side)

#### Start the service (without a daemon)
We will use Basic_Template service from https://github.com/singnet/dnn-model-services

```
# $SINGNET_REPOS is path from tutorial, but it could be any directory
cd $SINGNET_REPOS
git clone https://github.com/singnet/dnn-model-services.git
cd dnn-model-services/Services/gRPC/Basic_Template/

# build protobuf
. buildproto.sh
python run_basic_service.py
```
It will start the service at the port 7003.

#### Register your service in Registry

This step is optional for the moment, because client and daemon do not get
information from the Registry for the moment. 

```bash
```

#### Configure and start your daemom 

###### Preparation
```
# You could start the daemon from any directory
# We will use directory of the service
cd $SINGNET_REPOS
cd dnn-model-services/Services/gRPC/Basic_Template/

# ../../../../snet-daemon/build/snetd-linux-amd64 is a path to daemon

# we make a link for simplicity (service is already running)
ln -s ../../../../snet-daemon/build/snetd-linux-amd64
```
###### Make configuration file for the daemon

```
cd $SINGNET_REPOS
cd dnn-model-services/Services/gRPC/Basic_Template/

cat > snetd.config.json << EOF
{
    "AGENT_CONTRACT_ADDRESS": "0x3b07411493C72c5aEC01b6Cf3cd0981cF0586fA7",
    "MULTI_PARTY_ESCROW_CONTRACT_ADDRESS": "0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e",
    "PRIVATE_KEY": "1000000000000000000000000000000000000000000000000000000000000000",
    "DAEMON_LISTENING_PORT": 8080,
    "DB_PATH": "./db",
    "ETHEREUM_JSON_RPC_ENDPOINT": "http://localhost:8545",
    "PASSTHROUGH_ENABLED": true,
    "PASSTHROUGH_ENDPOINT": "http://localhost:7003",
"log": {
    "level": "debug",
    "output": {
    "type": "stdout"
    }
 }
}
EOF
```

It should be noted that
* We set wrong private address, because this daemon will not need it 
* We set AGENT_CONTRACT_ADDRESS, but we will not use at all

###### Run daemon

```
./snetd-linux-amd64
```

## Open payment channel and make a call (client side)


#### Open the payment channel with service provider 

Open the payment channel with 
* Ethereum address 0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB  (it is the "second" ganache address)
* group_id 0 (it should be a random number, and we will get it from metadata.json in next version, so user will specify not a group_id, but a group_name)
 

```
# create identity in snet-cli (probably you've already done it) 
snet identity create snet-user key --private-key 0xc71478a6d0fe44e763649de0a0deb5a080b788eefbbcf9c6f7aef0dd5dbd67e0
snet identity snet-user

# deposit 1000000 cogs to MPE from the first address (0x592E3C0f3B038A0D673F19a18a773F993d4b2610)
snet contract SingularityNetToken --at 0x6e5f20669177f5bdf3703ec5ea9c4d4fe3aabd14 approve 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e 1000000 --transact -y
snet contract MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e  deposit  1000000 --transact -y

# open the channel for the second account (for 420000 cogs), groupID=0
# Expiration is set in block numbers. 

# We set expiration +6000 blocks in the future (~24 hours with 15 second per block)
EXPIRATION=$((`snet mpe-client block_number` + 6000))
snet contract MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e openChannel  0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB 420000 $EXPIRATION 0 --transact -y
```

#### Compile protobuf (temporaly section!)

In the future version, we will take the protobuf from metadata and compile it automatically. But it is not supported yet, so we will do it manualy.
We will need .proto file from the running service

```
# compile protobuf for payment channel 0
snet mpe-client compile_from_file $SINGNET_REPOS dnn-model-services/Services/gRPC/Basic_Template/service/service_spec/ basic_tamplate_rpc.proto 0
```



#### Make a call using stateless logic

We are going to make a call using stateless logic (see https://github.com/singnet/wiki/blob/master/multiPartyEscrowContract/MultiPartyEscrow_stateless_client.md).
It means that client don't need to persist any information, except number of payment channel he want to use. In principle the client don't need to persist any data, 
because he can get the list of open channels from blockchain log (we have special function for it). But this operation is rather slow, so the client cannot make it at each call. 

First let's take from blockchain the list of open channel.

```
# take the list of channels from blockchain (from events!)
snet mpe-client print_my_channels 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e
```

We should have one channel with 0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB, and we should have 420000 cogs in it.

Now we can make a call
```
# we make call using stateless logic with the following arguments
#mpe_address      = 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e
#channel_id       = 0
#price            = 10
#endpoint         = localhost:8080
#protobuf_service = Addition
#protobuf_method  = add
#parameters       = '{"a":10,"b":32}'

snet  mpe-client call_server_lowlevel 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e 1 4 200 localhost:8080 "Addition" add '{"a":10,"b":32}'
```
During this call we ask the daemon to send us the last state of the channel, and make a call using this state (see https://github.com/singnet/wiki/blob/master/multiPartyEscrowContract/MultiPartyEscrow_stateless_client.m)

we can repeat this call until we spend all money in the channel

```
snet  mpe-client call_server_lowlevel 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e 1 4 200 localhost:8080 "Addition" add '{"a":1000,"b":3332}'
snet  mpe-client call_server_lowlevel 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e 1 4 200 localhost:8080 "Addition" add '{"a":567,"b":1234}'
#....
```

## Claim channel by treasurer server

At the moment tresurer server logic is implemented as part of the daemon. 

#### Configure tresurer 

```
cd $SINGNET_REPOS
mkdir treasurer
cd treasurer




