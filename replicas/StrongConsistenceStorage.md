# Strong consistency storage used by replicas

The aim of this document is to describe storage with strong consistency guaranties used by replicas.

# Justification

Design of multiPartyEscrowContract implies that there can be multiple replicas per service.

There are some options how a client can interact with replicas:
1. Open new payment channel for each replica
1. Using the same payment channel for all replicas

The first way allows to keep information about payment locally on each replica but the client needs to pay for each
new payment channel.

The second option requires that all replicas uses the same storage to save information about payment channel
and check that the same payment is not used twice on different replicas.


# Requirements

We expect that used storage will not be single point of failure. For example, if one node with the storage fails
replicas still will be available to read and write values from the storage.

The storage needs to provide strong consistency. This allows to avoid a use case where a client
provides a payment channel with the same payment to two replicas, one writes information about it to the storage and
the second replica does not see this record and also accepts the payment.
For this use case only one replica should be able to write a record that it accepts the payment and the another one
should reject it.

This can be summarised as:
1. Fault tolerance
1. Strong consistency

However, this choice does not allow us to use availability option (e. g. if there are live storage nodes they
can service read and write requests) because according to the [CAP theorem](https://en.wikipedia.org/wiki/CAP_theorem)
only one option is allowed either strong consistency or availability with eventually consistency for distributed systems.

# Considered storages

The following systems were considered:
- [etcd](https://github.com/etcd-io/etcd)
- [consoul](https://github.com/hashicorp/consul)
- [zookeeper](https://github.com/apache/zookeeper)

etcd was chosen because of two reasons: it has original support for
[embedded version](https://godoc.org/github.com/coreos/etcd/embed)
and it is written in Go and thus its nodes can be started and stopped by snet-daemon which is written in Go as well.

Drawback:
etcd use a quorum to get a consensus during leader election and values writings. It means that if number of
failed nodes more than half of all nodes then the etcd cluster stops working.
As it was described before it is a price for the system to have a strong consistency.
