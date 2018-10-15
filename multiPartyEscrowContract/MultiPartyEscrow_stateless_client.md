# Stateless client with MPE
In this document we demonstrate that the client who communicate with SingularityNet services using MPE payment channels doesn't
require to store the state of the payment channel. He only need to store his Ethereum identity.  
 
1. The client can easily obtain the list of its payment channels (payment channels with "sender==client") from MPE (see EventChannelOpen). However, we need to take into account the situation in which the request to open the channel has been already sent, but not yet mined. It can happen when the client has sent this request and died ("lost" his state).
2. The client can ask the last state of the given payment channel from the server.
    * The server is not able to forge this state, because it was signed by the client (of course the client should check his own signature).
    * The server is obviously interested in saving and sending the last state, otherwise it loses money.

Actually the client don't even need to use the special call to ask the server (=daemon) about the last state of the channel.
The daemon return the state of the channel in the response to any non-authorize call. So the client only need to correctly specify
channe_id in the call, and he will receive the last state of the channel.
We will also implement special "empty" grpc method for requesting state of the channel.

The client receive the following information from the daemon
* current_nonce - current nonce of the payment channel
* current_value - current value of the payment channel (we will need it in one very specific case, and we might replace it by "last_state_used_in_channelClaim")
* signed_nonce  - nonce of the payment channel for which the daemon has the last signed authorization from the client (it can be different from current_nonce in the case of close/reopen situation)
* signed_amount - amount from the last signed authorization
* signature     - last signature
 
Here we should consider the difficult case, namely the situation in which the server starts close/reopen procedure for the channel.
The client doesn't need to wait (or recieve at all) confirmation from the blockchain, because it is not in the interest of the server to lie. But the server also doesn't need to wait the confirmation from the blockchain (if he makes sure that the request is mined before expiration of the channel).

Before considering all possible cases, let's define the following parameters
* blockchain_nonce - nonce of the channel in the blockchain
* blockchain_value - value of the channel in the blockchain

We also assume that the daemon starts close/reopen procedure only after previous channelClaim request was mined.
It means that the current_nonce, at maximum, one point ahead of blockchain_nonce. We can easily relax this assumption if necessary.   

In all cases we assume that client verify that it's own signature is authentic.  

In all cases we are interesting in two numbers:
* The amount of tokens which haven't been already spent (unspent_amount)
* Next amount which has to be signed (next_signed_amount), taking into account the price for the current call (price).
 
#### Simple case current_nonce == signed_nonce == blockchain_nonce
Everything is obvious.
* unspent_amount = blockchain_value - signed_amount
* next_signed_amount = signed_amount + price

#### current_nonce = signed_nonce + 1, blockchain_nonce = signed_nonce
It is the situation in which the server has initiated close/reopen procedure, but blockchain still contains the old state,
and the last signed message has the old nonce.  
* unspent_amount = blockchain_amount - signed_amount
* next_signed_amount = price

#### current_nonce = signed_nonce + 1, blockchain_nonce = current_nonce
It is the situation in which the server has initiated close/reopen procedure, and blockchain already contains the new state (the close/reopen request has been already mined). But the last signed message has the old nonce.
* unspent_amount = blockchain_amount
* next_signed_amount = price

#### current_nonce = signed_nonce, blockchain_nonce = current_nonce - 1
It is the situation in which the server has initiated close/reopen procedure, but blockchain still contains the old state. But the client has already used this new channel, and last signed message has the new nonce.
* unspent_amount = current_value - signed_amount
* next_signed_amount = signed_amount + price

It should be noted that in this case, in order to calculate unspent_amount, we rely on information from the server (current_value).
Server cannot gain tokens by manipulating this value, but he could "force" the client to put more tokens in the channel (by saying that current_value is low). If we consider it as a problem, we could ask the daemon to send the last state which was actually used in channelClaim (for close/reopen logic). Maybe we will include this fields in the final design instead of "current_value".
