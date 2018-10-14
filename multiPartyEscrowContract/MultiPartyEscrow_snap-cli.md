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

