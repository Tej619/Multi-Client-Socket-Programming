# Client Server Application to Maintain Address Book

This is a client-server address book application  made in Java. It lets users manage an address book using command-line interface in the terminal. A comma seperated text file is maintained to store the records of the book.

## Authors
- Tejas Vaity

## Components

1. `MultiThreadServer.java`: The Server-side application that manages the address book in this part the address.txt file manages the records of the Address with comma seperated values. Allows Multiple Client to connect to this Server. Loads the initial users information who can Login.
2. `ChildThread.java`: Allows multiple client to perform the action on the Server. 
2. `Client.java`: The client-side application that connects to the server and sends user commands like LOGIN, LOGOUT, LOOK, WHO, UPDATE, ADD, LIST, DELETE, SHUTDOWN, QUIT.

## Features

- LOGIN to the Server using ID and Password
- LOGOUT from the Server
- WHO to list the logged in user
- LOOK up a name in the address book  
- UPDATE the record from the existing records
- Add new records to the address book
- Delete existing records
- List all the records in the address book
- Shutdown the Server(only the root user)
- Quit the Client

## Server (MultiThreadServer.java)

The server listens on port 3811 and handles incoming Client connection. It supports the following commands:

- `LOGIN <userName> <password>`: Login to the Server
- `LOGOUT`: Logout from the server after logged in
- `WHO`: To List the logged in user
- `LOOK <Parameter> <Value>`: Look up a name in the address book. Parameter 1-for First name, Parameter 2-for Last name, Parameter 3-for Mobile No.
- `UPDATE <recordID> <Parameter> <Value>`: Update the record from the address book. Parameter 1-for First name, Parameter 2-for Last name, Parameter 3-for Mobile No.
- `ADD <firstName> <lastName> <phoneNumber>`: Adds a new record to the address book
- `DELETE <recordID>`: Deletes a record from the address book
- `LIST`: Lists all records in the address book
- `SHUTDOWN`: Shuts down the server and also the Client (command used only by root user)
- `QUIT`: Closes the client connection

## Client (Client.java)

The client connects to the server and allows users to send commands through a Command-Line Interface.

## How to Run

### Compile the Java files
2 ways to compile 

1st Method is
```
javac MultiThreadServer.java
javac ChildThread.java
javac Client.java

```  
2nd Method is

make clean
make all

### Start the Server
```
java MultiThreadServer

```
### Start the Client
Start this client when the Server is Up after 2 seconds.

java Client <server_host>
```
Replace `<server_host>` with the hostname or IP address of the Server.
Example : java Client localhost
```
### Usage
Here is the List of users:

UserID			Password

root			root05
john 			john06
david			david07
mary			mary08


Once Client is connected to the Server, Enter Commands at the Client prompt. The Server will respond with the results of each command.
A client-server interaction with the commands looks like

Example :
```
For LOGIN command

c : LOGIN root root05
s : 200 OK


```
For LOGOUT command

c : LOGOUT
s : 200 OK


```
For WHO command

c : WHO
s : 200 OK
The list of active users:
root    /127.0.0.1
john    /127.0.0.1
david   /127.0.0.1


```
For LOOK command (the Look command first and last name search are case insensitive)

c : LOOK 2 Vaity
s : 200 OK
Found 2 match
1001 Tejas Vaity 313-266-1654
1003 Siddhi Vaity 313-928-3965


```
For UPDATE command

c : UPDATE 1003 2 Dabholkar
s : 200 OK
Record 1003 updated:
1003 Siddhi Dabholkar 313-928-3965


```
For ADD command

c : ADD Tejas Vaity 313-266-1654
s : 200 OK
The new Record ID is 1001


```
For LIST command 

c : LIST
s : 200 OK
The list of records in the book:
1001    Tejas Vaity     313-266-1654
1002    Sayojya Patil   313-431-2341
1003    Siddhi Vaity    313-928-3965


```
For DELETE command

c : DELETE 1003
s : 200 OK


```
For QUIT command

c : QUIT
s : 200 OK


```
For SHUTDOWN command (only root user can use this command)

c : SHUTDOWN
s : 200 OK

This will go to all the connected clients
s: 210 the server is about to shutdown...
Server shutdown signal has been received. Terminating client...

```