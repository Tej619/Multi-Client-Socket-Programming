CC=javac

# The target
all: MultiThreadServer.class ClientThread.class Client.class

# To generate the class files
MultiThreadServer.class: MultiThreadServer.java
	$(CC) MultiThreadServer.java

ClientThread.class: ClientThread.java
	$(CC) ClientThread.java

Client.class: Client.java
	$(CC) Client.java

# clean out the dross
clean:
	-rm *.class
