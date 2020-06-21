package InetServerAndClient;
//package InetServerAndClient;

/*--------------------------------------------------------

1. Name / Date: Yuetao Zhu / 04/09/2019

2. Java version used, if not the official version for the class:

build 1.8.0_181-b13 (official version)

3. Precise command-line compilation examples / instructions:

> javac InetServer.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java InetServer
> java InetClient

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java InetClient 140.192.1.22

5. List of files needed for running the program.

 a. InetAll.html
 b. InetServer.java
 c. InetClient.java

5. Notes:

This is the foundation program for JokeServer & Client program, the purpose for this program is to understand the concept of transaction data between machines with Socket stream. 

----------------------------------------------------------*/
import java.io.*;   
import java.net.*;

import JokeServerAndClient.Worker;  

class InetWorker extends Thread {	// Create a new thread called Worker, it inherit characters from class Thread
	Socket	sock;		// Local variable Socket for Worker
	InetWorker	(Socket s) {sock = s;}	

	public void run() {	// override run() for the new Thread Worker
	PrintStream out = null;	// Declare out as a Print stream
	BufferedReader in = null; // Declare in as a BufferedReader
	try {
	  // initialize in and out as socket in and out stream (similar as file descriptor and pipe)
	  in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
  	  out = new PrintStream(sock.getOutputStream());
	try {
      String name;
	  name = in.readLine();
	  System.out.println("Looking up " + name);
	  System.out.println("sock is " + sock);
	  printRemoteAddress(name, out); // call the function to print out results to client
	} catch (IOException x) { // do catch if get error from try
      	  System.out.println("Server read error");
	  x.printStackTrace ();
	} 
	sock.close();
      } catch (IOException ioe) {System.out.println(ioe);}
    }	

	// function to print results for client
	static void printRemoteAddress (String name, PrintStream out) {
		try { 
			out.println("Looking up " + name + "...");
			InetAddress machine = InetAddress.getByName(name);
			out.println("Host name : " + machine.getHostName()); 
			out.println("Host IP : " + toText(machine.getAddress()));
		} catch(UnknownHostException ex) {
			out.println ("Failed in atempt to look up " + name);
		}
	}
	
	// not interesting for us
	static String toText (byte ip[]) { 
		StringBuffer result = new StringBuffer ();
		for (int i = 0; i < ip.length; ++i) {
			if (i > 0) result.append(".");
			result.append(0xff & ip[i]);
		}
		return result.toString();
	}
  }

  public class InetServer {
	  
	  public static void main(String a[]) throws IOException {
		  int q_len = 6; 
		  int port = 1864;
		  Socket sock;
		  
		  // create server socket
		  @SuppressWarnings("resource")
		ServerSocket servsock = new ServerSocket(port, q_len);
		  
		  System.out.println("Yuetao's Inet server 1.8 starting up, listening at port 1864.\n");
		  while(true) {
			  sock = servsock.accept();	// listen for a connection
			  new Worker(sock).start();
		  }
	  }
  }











