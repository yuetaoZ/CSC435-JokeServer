package JokeServerAndClient;
/*--------------------------------------------------------

1. Name / Date: Yuetao Zhu / 04/17/2019

2. Java version used, if not the official version for the class:

build 1.8.0_181-b13

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java

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

class Worker extends Thread {	// Create a new thread Worker
	static Socket	sock;		// Local variable Socket for Worker
	Worker	(Socket s) {sock = s;}	
	volatile static boolean serverMode = true;
		

	public void run(){	// override run() for new Thread Worker
		
		try {
			// Create input and output streams for Server and Client
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			PrintStream out = new PrintStream(sock.getOutputStream());
			
			// Initialize local variable
			Integer[] stateArray = {0,0,0,0,0,0,0,0};	// Array to record which J/P is showed
			String line = "";	// line for save input
			
			do {
				 try {
					 line = in.readLine();
				 } catch(IOException i) { System.out.println(i); break; }
				 
				 if (serverMode == true) 	// serverMode == true, Joke mode.
					 printJoke(stateArray, out); 
				 else	// serverMode == false, Proverb mode.				
					 printProverb(stateArray, out);
			} while (line.indexOf("quit") < 0);	
			 
			// close sock and streams
			try {
				 in.close();
				 out.close();
				 sock.close();
			 } catch (IOException i) { System.out.println(i);}

		} catch (IOException ioe) {System.out.println(ioe);}
	}
	
	// function for send signals of printing Jokes or Proverbs for client
	static void printJoke (Integer[] stateArray, PrintStream out) {
		Random rand = new Random();
		int RandJoke = 0;
		int checkJoke = 0;
		
		do {
			 RandJoke = rand.nextInt(4);	// RandJoke will be a number between 0 to 3
		 } while(stateArray[RandJoke] != 0);	// re random if the Joke is already showed
		stateArray[RandJoke] = 1;	// update the stateArray
		// check whether all 4 Jokes are showed
		checkJoke = stateArray[0] + stateArray[1] + stateArray[2] + stateArray[3];
		
		if (checkJoke != 4) {
			switch(RandJoke) {
			case 0:
				out.println("JA");	// will send "JA" as signal to print Joke A
				break;
			case 1:
				out.println("JB");
				break;
			case 2:
				out.println("JC");
				break;
			case 3:
				out.println("JD");
				break;
			}
		}
		else {
			
			switch(RandJoke) {
			case 0:
				out.println("JAC");	// "JAC" as signal, means "Joke A, CYCLE COMPLETED"
				break;
			case 1:
				out.println("JBC");
				break;
			case 2:
				out.println("JCC");
				break;
			case 3:
				out.println("JDC");
				break;
			}
			// All Jokes are told, reset the state array to begin a new cycle
			stateArray[0] = 0; 
			stateArray[1] = 0; 
			stateArray[2] = 0; 
			stateArray[3] = 0; 
			
		}
		
	}
	
	// function for print proverbs, similar as printJoke
	static void printProverb (Integer[] stateArray, PrintStream out) {
		Random rand = new Random();
		int RandProverb = 0;
		int checkProverb = 0;
		
		do {
			 RandProverb = rand.nextInt(4);
		 } while(stateArray[RandProverb + 4] != 0);
		stateArray[RandProverb + 4] = 1;
		
		checkProverb = stateArray[4] + stateArray[5] + stateArray[6] + stateArray[7];
		
		if (checkProverb != 4) {
			switch(RandProverb) {
			case 0:
				out.println("PA");	// signal for Proverb A
				break;
			case 1:
				out.println("PB");
				break;
			case 2:
				out.println("PC");
				break;
			case 3:
				out.println("PD");
				break;
			}
		}
		else {
			
			switch(RandProverb) {
			case 0:
				out.println("PAC");	// signal for "Proverb A COMPLETED"
				break;
			case 1:
				out.println("PBC");
				break;
			case 2:
				out.println("PCC");
				break;
			case 3:
				out.println("PDC");
				break;
			}
			// reset the state array for new Proverb cycle
			stateArray[4] = 0; 
			stateArray[5] = 0; 
			stateArray[6] = 0; 
			stateArray[7] = 0; 
			
		}
		
	}

}


class AdminWorker extends Thread {
	Socket sock;
	AdminWorker(Socket s) { sock = s; }
	
	public void run() {
		try {
			// Initialize variables, create I/O streams for ClientAdmin
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			PrintStream out = new PrintStream(sock.getOutputStream());
			String line = "";
			
			System.out.println("Admin connected.");
			// Print out the Server's state on Server and Admin's window
			System.out.println("Server mode: " + (Worker.serverMode == true ? "Joke" : "Proverb"));
			out.println("Server mode: " + (Worker.serverMode == true ? "Joke" : "Proverb"));
			
			do {
				try {
					line = in.readLine();
				} catch(IOException i) { System.out.println(i); break; }
				
				// switch mode by received "Enter"
				Worker.serverMode = !Worker.serverMode;
				System.out.println("Server mode: " + (Worker.serverMode == true ? "Joke" : "Proverb"));
				out.println("Server mode: " + (Worker.serverMode == true ? "Joke" : "Proverb"));
					
			} while (line.indexOf("quit") < 0);	// quit the loop by receive "quit"
			
			System.out.println("Admin disconnected.");
			// close the streams and sock
			try {
				in.close();
				out.close();
				sock.close();
			} catch (IOException i) { System.out.println(i); }
		
			
		}catch (IOException ioe) {System.out.println(ioe);}
	} 
} 
	

// This part of codes for ClientAdmin come from Professor Elliott's web page
class AdminLooper implements Runnable { 
	  public static boolean adminControlSwitch = true;

	  public void run(){ 
	    System.out.println("In the admin looper thread");
	    
	    int q_len = 6; 
	    int port = 5050;  // Using different ports for ClientAdmin
	    Socket sock;

	    try{
	      ServerSocket servsock = new ServerSocket(port, q_len);
	      while (adminControlSwitch) {
			// wait for another clientAdmin connection
			sock = servsock.accept();
			new AdminWorker (sock).start(); 
	      }
	    }catch (IOException ioe) {System.out.println(ioe);}
	  }
}



public class JokeServer {
		  
	public static void main(String a[]) throws IOException {
		int q_len = 6; 
		int port = 4545;
		//int secondaryPort = 4546;
		Socket sock; 
		
		// run the Admin thread, this part of code from Professor Elliott
		AdminLooper AL = new AdminLooper(); 
		Thread t = new Thread(AL);
	    t.start();  
			  
		// create server socket
		ServerSocket servsock = new ServerSocket(port, q_len);
			  
		System.out.println("Yuetao's Joke server 1.8 starting up, listening at port " + port + ".\n");
		while(true) {
			 sock = servsock.accept();	// listen for a connection
			 new Worker(sock).start();
		}
	}
}

