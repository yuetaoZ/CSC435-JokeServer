package JokeServerAndClient;
/*--------------------------------------------------------

1. Name / Date: Yuetao Zhu / 04/17/2019

2. Java version used, if not the official version for the class:

build 1.8.0_181-b13

3. Precise command-line compilation examples / instructions:

> javac JokeClientAdmin.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For example, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

5. Notes:


----------------------------------------------------------*/
import java.io.*;	
import java.net.*;

public class JokeClientAdmin{
	public static void main (String args[]) {
		String serverName;
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		Socket sock = null;
		BufferedReader fromServer = null;
		PrintStream toServer = null;
		String textFromServer;
		String line = "";
		
		// Declare and initialize socket in stream as a BufferedReader
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Yuetao's Joke Client Admin, 1.8.\n");
		System.out.println("Using server: localhost, Port: 5050");
		// run before connection
		System.out.println("<Enter> to switch mode or type \"quit\" to exit:");
		
		try {
			line = in.readLine();
		}
		catch(IOException i) {System.out.println(i);}
		
		try {
			sock = new Socket(serverName, 5050);
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			
			do {
				// Read the information from server by read the input stream from the socket
				textFromServer = fromServer.readLine();
				System.out.println(textFromServer);
				
				try {
					line = in.readLine(); 
					toServer.println(line);
				}
				catch(IOException i) { System.out.println(i);}	
				
				
			} while (line.indexOf("quit") < 0); // do the loop until get command quit 
			
			System.out.println ("Cancelled by user request.");
			fromServer.close();
			toServer.close();
			sock.close();
		} 
		catch (IOException x) {
		System.out.println("Socket error.");
		x.printStackTrace();
		}
	}
	
}
