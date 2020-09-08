# Java Generics Concurrency and Synchronization
Java

Implementation of a simple Pub-Sub framework, which will later be used to implement a system for the MI6, On Her Majesty’s Secret Service (that is the sixth in James Bond Series, what implies the properties of the system will be implemented.).
Pub-Sub is shorthand for Publish-Subscribe messaging, an asynchronous communication method in which messages are exchanged between applications without knowing the identity of
the sender or recipient.

The different Subscribers\Publishers will be able to communicate with each other using only a shared object -the MessageBroker. 
The MessageBroker supports sending and receiving of two types of events: Broadcast messages (which upon being sent are delivered to every subscriber of the specific message type), and Event messages (which upon being sent are delivered to only one of its subscribers in a round-robin manner). The different Subscribers will be able to subscribe
for message types they would like to receive using the MessageBroker. The different Subscribers\Publishers do not know of each other’s existence. All they know of are the
messages that are received in their message-queue which is located in the MessageBroker.
