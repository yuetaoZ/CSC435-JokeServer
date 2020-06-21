package JokeServerAndClient;
/*--------------------------------------------------------

1. Name / Date: Yuetao Zhu / 04/17/2019

2. Java version used, if not the official version for the class:

build 1.8.0_181-b13

3. Precise command-line compilation examples / instructions:

> javac JokeClient.java

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
import java.util.Random;
public class JokeClient{
	public static void main (String args[]) {
		String serverName;
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		//System.out.println("The length of args[] is: " + args.length);
		Random rand = new Random();
		Integer userid = rand.nextInt(100000);
		Socket sock = null;
		BufferedReader fromServer = null;
		PrintStream toServer = null;
		String textFromServer;
		String userName = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		// ask for user's name
		System.out.println("Please enter a uer's name: ");
		try {
			userName = in.readLine();
		}
		catch(IOException i) {System.out.println(i);}
		
		try {
			// establish a connection
			sock = new Socket(serverName, 4545);
			// Initialize variables
			// Define and initialize the input and output stream for socket
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			
			String line = "";
			
			// Report connection statement
			System.out.println("Server one: localhost, port 4545");
			System.out.println("Using server: " + serverName + ", Port: " + sock.getPort() + " UserID: " + userid + " UserName: " + userName);
			
			
			do {
				System.out.print("Enter return to get a Joke/Proverb or (quit) to end: ");
				System.out.flush();
				try {
					line = in.readLine(); // readLine() will block the loop until get sth or exception is thrown
					toServer.println(line);
				}
				catch(IOException i) { System.out.println(i);}	
				// Read the information from server by read the input stream from the socket
				textFromServer = fromServer.readLine();
				printJokeProverb(textFromServer, userName);	// use a function to print Jokes and Proverbs
				
			} while (line.indexOf("quit") < 0); // do the loop until get quit command  
			
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
	public static void printJokeProverb(String textFromServer, String userName) {
		switch (textFromServer) {
		case "JA":
			System.out.print("JA " + userName + ": \nA computer scientist named Bob was about to leave to rent a movie. As Bob was heading out, his wife said, \"while you're out, pick up some eggs.\" \n" + 
					 "Bob never came back.\n");
			break;
		case "JB":
			System.out.print("JB " + userName + ": \nQ: What would BITS Pilani become after it has opened up 8 campuses ?\nA: BYTES Pilani  :-P \n");
			break;
		case "JC":
			System.out.print("JC " + userName + ": \nQ: Why do Java programmers wear glasses?\nA: They can't C#.\n");
			break;
		case "JD":
			System.out.print("JD " + userName + ": \nQ: How do you explain the Inception movie to a programmer?\n" + 
					"A: Basically, when you run a VM inside another VM, inside another VM, inside another VM..., everything runs real slow! :P\n");
			break;
		case "JAC":
			System.out.print("JA " + userName + ": \nA computer scientist named Bob was about to leave to rent a movie. As Bob was heading out, his wife said, \"while you're out, pick up some eggs.\" \n" + 
					 "Bob never came back.\n");
			System.out.println("JOKE CYCLE COMPLETED");
			break;
		case "JBC":
			System.out.print("JB " + userName + ": \nQ: What would BITS Pilani become after it has opened up 8 campuses ?\nA: BYTES Pilani  :-P\n");
			System.out.println("JOKE CYCLE COMPLETED");
			break;
		case "JCC":
			System.out.print("JC " + userName + ": \nQ: Why do Java programmers wear glasses?\nA: They can't C#.\n");
			System.out.println("JOKE CYCLE COMPLETED");
			break;
		case "JDC":
			System.out.print("JD " + userName + ": \nQ: How do you explain the Inception movie to a programmer?\n" + 
					"A: Basically, when you run a VM inside another VM, inside another VM, inside another VM..., everything runs real slow! :P\n");
			System.out.println("JOKE CYCLE COMPLETED");
			break;
		case "PA":
			System.out.println("PA " + userName + ": Computer Science is no more about computers than astronomy is about telescopes.  ~ Edsger W. Dijkstra");
			break;
		case "PB":
			System.out.println("PB " + userName + ": The computer was born to solve problems that did not exist before.  ~ Bill Gates");
			break;
		case "PC":
			System.out.println("PC " + userName + ": The question of whether computers can think is just like the question of whether submarines can swim.  ~ Edsger W. Dijkstra");
			break;
		case "PD":
			System.out.println("PD " + userName + ": Computers are useless. They can only give you answers.  ~ Pablo Picasso");
			break;
		case "PAC":
			System.out.println("PA " + userName + ": Computer Science is no more about computers than astronomy is about telescopes.  ~ Edsger W. Dijkstra");
			System.out.println("PROVERB CYCLE COMPLETED");
			break;
		case "PBC":
			System.out.println("PB " + userName + ": The computer was born to solve problems that did not exist before.  ~ Bill Gates");
			System.out.println("PROVERB CYCLE COMPLETED");
			break;
		case "PCC":
			System.out.println("PC " + userName + ": The question of whether computers can think is just like the question of whether submarines can swim.  ~ Edsger W. Dijkstra");
			System.out.println("PROVERB CYCLE COMPLETED");
			break;
		case "PDC":
			System.out.println("PD " + userName + ": Computers are useless. They can only give you answers.  ~ Pablo Picasso");
			System.out.println("PROVERB CYCLE COMPLETED");
			break;
		}
		
	}
	
}
