package InetServerAndClient;
//package InetServerAndClient;

import java.io.*;	
import java.net.*;	
public class InetClient{
	public static void main (String args[]) {
		String serverName;
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];
		
		System.out.println("Yuetao's Inet Client, 1.8.\n");
		System.out.println("Using server: " + serverName + ", Port: 1864");
		// Declare and initialize socket in stream as a BufferedReader
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			String name;
			do {
				System.out.print("Enter a hostname or an IP address, (quit) to end: ");
				System.out.flush();
				name = in.readLine(); // readLine() will block the loop until get sth or exception is thrown
				if (name.indexOf("quit") < 0)
					getRemoteAddress(name, serverName);
			} while (name.indexOf("quit") < 0); // do the loop until get arg quit 
			System.out.println ("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace ();}
	}
	
	// not interesting for us
	static String toText (byte ip[]) { 
		StringBuffer result = new StringBuffer ();
		for (int i = 0; i < ip.length; ++ i) {
			if (i > 0) result.append(".");
			result.append(0xff & ip[i]);
		}
		return result.toString();
	}
	
	static void getRemoteAddress (String name, String serverName) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try {
			// intitialize a socket
			sock = new Socket(serverName, 1864);
			// Define and initialize the input and output stream for socket
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			// Send the massage "name" to server
			toServer.println(name); toServer.flush();
			// Read the information from server by read the input stream created by socket
			for (int i = 1; i <= 3; i++) {
				textFromServer = fromServer.readLine();
				if (textFromServer != null) System.out.println(textFromServer);
			}
			sock.close();	// close the socket		
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
}
