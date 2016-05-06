# Chat_java

this  project is the client and server of a java chat.

a client can:
1.send message to all the clients
2. send message to one client
3.ask for a list of connected clients
4.ask to disconnect
5.ask to connect

the server can: handle the message

_____________________________________________________

the server as 5 thread (all are event waiting):
1.connect New Client Thread
2.remove Old clients from client list
3.Queues Manger -handle the messages 
4.Server Writer - send messages
5.Listening Thread - listen to the clients socket

the client as 2 thread (all are event waiting):
1.Client Reader -read and handling the incoming message
2.Client Writer  -write a exit messsage to the server

_________________________________________________________

all the thread group working with one or two class with all the functions.
1. Client Communication Tools - with all the function 
2.Server Communication Tools - with all the functions.
3.Sync Client List - a syncronized client list

____________________________________________________________

a gui class that get for demonstration

