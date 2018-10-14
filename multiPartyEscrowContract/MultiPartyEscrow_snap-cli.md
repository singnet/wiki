# snap-cli for MPE contract and channels

In this document we present snap-cli commands for manipulating
MultiPartyEscrow contract and calling services from the client side using
payment channels.

### Manipulating MultyPartyEscrow contract

All manipalation with MPE contract can be done via "snet-cli contract"
option, which is low level interface to smart contracts.

If you start ganache with the following mnemonics: 'gauge enact
biology destroy normal tunnel slight slide wide sauce ladder produce'.
Then after deploying platform-contracts you will have the following
addreses.

```bash
# Address of MultiPartyEscrow  :  0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e
# Address of Tokens            :  0x6e5f20669177f5bdf3703ec5ea9c4d4fe3aabd14
# First Address (snet identity):  0x592E3C0f3B038A0D673F19a18a773F993d4b2610
# Second Address (service)     :  0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB
```

We will assume that snet-cli use the first address as identity, and
the service use the second address.

You can run the following commands

```bash
# create idenity in snet-cli (probably you've alread done it) 
snet identity create snet-user key --private-key 0xc71478a6d0fe44e763649de0a0deb5a080b788eefbbcf9c6f7aef0dd5dbd67e0
snet identity snet-user

# deposit 1000000 cogs to MPE from the first address (0x592E3C0f3B038A0D673F19a18a773F993d4b2610)
snet contract SingularityNetToken --at 0x6e5f20669177f5bdf3703ec5ea9c4d4fe3aabd14 approve 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e 1000000 --transact -y
snet contract MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e  deposit  1000000 --transact -y

# check balance of the First account in MPE (it should be 1000000 cogs)
snet contract  MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e balances 0x592E3C0f3B038A0D673F19a18a773F993d4b2610
# 1000000

# open the channel for the second account (for 420000 cogs), replicaID=0
# Expiration is set in block number. 
# You can see the last block number with the following commands
snet mpe-client block_number 

# We set expiration, one block in the past. 
EXPIRATION=$((`snet mpe-client block_number` - 1))
snet contract MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e openChannel  0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB 420000 $EXPIRATION 0 --transact -y

# We can check this channel
snet contract MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e channels 0
# ['0x592E3C0f3B038A0D673F19a18a773F993d4b2610', '0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB', 0, 420000, 0, <last_block - 1>]

# check balance of the First account in MPE (it should be 580000 now)
snet contract  MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e balances 0x592E3C0f3B038A0D673F19a18a773F993d4b2610
# 580000

#We can immediately claim timeout 
snet contract MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e channelClaimTimeout 0 --transact -y

# check balance of the First account in MPE (it should be 1000000 now)
snet contract  MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e balances 0x592E3C0f3B038A0D673F19a18a773F993d4b2610
# 1000000


# We can check the channel and see that it was suspended and nonce of the channel was incremented
snet contract MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e channels 0
# ['0x592E3C0f3B038A0D673F19a18a773F993d4b2610', '0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB', 0, 0, 1, 0]


# We put 420000 back to the channel end set expiration +6000 blocks in the future (~24 hours with 15 second per block)
EXPIRATION=$((`snet mpe-client block_number` + 6000))
snet contract MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e channelExtendAndAddFunds 0 $EXPIRATION 420000 --transact -y

# We check the channel (nonce is still 1)
snet contract MultiPartyEscrow --at 0x5c7a4290f6f8ff64c69eeffdfafc8644a4ec3a4e channels 0
# ['0x592E3C0f3B038A0D673F19a18a773F993d4b2610', '0x3b2b3C2e2E7C93db335E69D827F3CC4bC2A2A2cB', 0, 420000, 1, <...>]

# You can try to claim timeout now. It will not be possible ~24 hours... 

```
