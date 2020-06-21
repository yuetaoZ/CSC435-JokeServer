/*--------------------------------------------------------

1. Name / Date: Yuetao Zhu / 05/12/2019

2. Java version used, if not the official version for the class:

build 1.8.0_181-b13

3. Precise command-line compilation examples / instructions:

> javac MyWebServer.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java MyWebServer

All acceptable commands are displayed on the various consoles.

5. Notes:

This program was compiled and run on mac computer.

----------------------------------------------------------*/

import java.io.*; // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries
import java.util.*;

class WebWorker extends Thread { // Class definition
	Socket sock; // Class member, socket, local to ListnWorker.

	WebWorker(Socket s) {
		sock = s;
	} // Constructor, assign arg s


	public void run() {
		// Get I/O streams from the socket:
		OutputStream out = null;
		BufferedReader in = null;
		PrintStream pout = null;
		try {
			out = new BufferedOutputStream(sock.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			pout = new PrintStream(out);
			String sockdata;
			
			
			// read first line of request (ignore the rest)
			sockdata = in.readLine();
			String request = sockdata.substring(4, sockdata.length() - 9).trim();
			if (!request.equals("/favicon.ico")) {
				System.out.println("New connection: " + sock.toString());	// for server log
				System.out.println("Request string: " + sockdata); // for server log
			}
			
			if (sockdata==null)
                System.out.println("continue");
			//log(sock, sockdata);
			while (true) {
                String misc = in.readLine();
                if (misc==null || misc.length()==0)
                    break;
            }
			
			// parse the line
			if (!sockdata.startsWith("GET") || sockdata.length() < 14
					|| !(sockdata.endsWith("HTTP/1.0") || sockdata.endsWith("HTTP/1.1"))) {
				// bad request
				errorReport(pout, sock, "400", "Bad Request",
						"Your browser sent a request that " + "this server could not understand.");
			} else {
				String req = sockdata.substring(4, sockdata.length() - 9).trim();
				if (req.indexOf("..") != -1 || req.indexOf("/.ht") != -1 || req.endsWith("~")) {
					// evil hacker trying to read non-dir or secret file
					errorReport(pout, sock, "403", "Forbidden",
							"You don't have permission to access the requested URL.");
				} else {
					// get the root directory
					File f1 = new File(".");
					String directoryRoot = f1.getCanonicalPath();
					File[] strFilesDirs = f1.listFiles ( );
					String path = directoryRoot + req;
					File f2 = new File(path);

					if (f2.isDirectory() && !path.endsWith("/")) {
						// redirect browser if referring to directory without final '/'
						pout.print("HTTP/1.1 301 Moved Permanently\r\n" + "Location: http://"
								+ sock.getLocalAddress().getHostAddress() + ":" + sock.getLocalPort() + "/" + req
								+ "/\r\n\r\n");
						log(sock, "301 Moved Permanently");
					} else {
						if (f2.isDirectory() || req.equals("/index.html")) {
							// if directory, send file list of directory
							pout.print("HTTP/1.1 200 OK\r\n" + 
									"Content-Type: " + "text/html" + "\r\n"
									+ "Date: " + new Date() + "\r\n" + "Server: FileServer 1.1\r\n\r\n"
									+ "<pre>\r\n" + "\r\n"
									+ "<h1> Index of" + path + "</h1>" + "\r\n"
									+ "<a href=" + ".." + ">Parent Directory</a> <br>" +"\r\n"
									);
							strFilesDirs = f2.listFiles();
							for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
								 if ( strFilesDirs[i].isDirectory ( ) ) {
									 String DirName = strFilesDirs[i].getName() + "/";
									 pout.println ("<a href=" + DirName + ">" + DirName + "</a><br>");
								 }
										
								 else if ( strFilesDirs[i].isFile ( ) )
									    pout.println ("<a href=" + strFilesDirs[i].getName() + ">" + strFilesDirs[i].getName() + "</a><br>");
							}
							// for server log
							System.out.println("This is traversal for subdirectories: ");
							for ( int i = 0 ; i < strFilesDirs.length ; i ++ ) {
								 if ( strFilesDirs[i].isDirectory ( ) ) {
									 String DirName = strFilesDirs[i].getName() + "/";
									 System.out.println ("directory: " + "<a href=" + DirName + ">" + DirName + "</a><br>");
								 }
										
								 else if ( strFilesDirs[i].isFile ( ) )
									 System.out.println ("file: " + "<a href=" + strFilesDirs[i].getName() + ">" + strFilesDirs[i].getName() + "</a><br>");
							}
						}	
						else {	
							try {
								if (req.indexOf("cgi") != -1) {
									Integer num1 = Integer.valueOf(req.substring(req.indexOf("num1") + 5, req.indexOf("num2")-1));
									Integer num2 = Integer.valueOf(req.substring(req.indexOf("num2") + 5));
									String person = req.substring(req.indexOf("person")+7, req.indexOf("num1")-1);
									addnums(num1, num2, person ,pout);
								}
								else {
									// send file
									InputStream file = new FileInputStream(f2);
									pout.print("HTTP/1.1 200 OK\r\n" + "Content-Type: " + guessContentType(path) + "\r\n"
											+ "Date: " + new Date() + "\r\n" + "Server: FileServer 1.1\r\n\r\n");
									// for server log
									System.out.print("For sending file: " + "HTTP/1.1 200 OK\r\n" + "Content-Type: " + guessContentType(path) + "\r\n"
											+ "Date: " + new Date() + "\r\n" + "Server: FileServer 1.1\r\n\r\n");
									sendFile(file, out); // send raw file
									
									//log(sock, "200 OK");
								}
							} catch (FileNotFoundException e) {
								// file not found
								errorReport(pout, sock, "404", "Not Found",
										"The requested URL was not found on this server.");
							}
						}
						
					}
				}
			}
			out.flush();
			

			sock.close(); // close this connection, but not the server;
		} catch (IOException x) {
			System.out.println("Connetion reset. Listening again...");
		}

	}

	// define function addnums
	private void addnums(Integer num1, Integer num2, String person, PrintStream pout) {
		Integer sum = num1 + num2;
		pout.print("HTTP/1.1 200 OK\r\n" + 
				"Content-Type: " + "text/html" + "\r\n"
				+ "Date: " + new Date() + "\r\n" + "Server: FileServer 1.1\r\n\r\n"
				+ "<pre>\r\n" + "\r\n"
				+ "<center>\r\n"
				+ "<h1> Dear " + person + ", the sum of " + num1 +" and " + num2 + " is " + sum + "." + "</h1>" + "\r\n"
				+ "</center>\r\n"
				);
	}


	// define function log
	private static void log(Socket connection, String msg) {
		System.err.println(new Date() + " [" + connection.getInetAddress().getHostAddress() + ":" + connection.getPort()
				+ "] " + "\n" + msg);
	}

	// define function errorReport
	private static void errorReport(PrintStream pout, Socket connection, String code, String title, String msg) {
		pout.print("HTTP/1.1 " + code + " " + title + "\r\n" + "\r\n"
				+ "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" + "<TITLE>" + code + " " + title
				+ "</TITLE>\r\n" + "</HEAD><BODY>\r\n" + "<H1>" + title + "</H1>\r\n" + msg + "<P>\r\n"
				+ "<HR><ADDRESS>FileServer 1.0 at " + connection.getLocalAddress().getHostName() + " Port "
				+ connection.getLocalPort() + "</ADDRESS>\r\n" + "</BODY></HTML>\r\n");
		log(connection, code + " " + title);
	}

	// define function guessContentType
	private static String guessContentType(String path) {
		if (path.endsWith(".html") || path.endsWith(".htm"))
			return "text/html";
		else if (path.endsWith(".txt") || path.endsWith(".java"))
			return "text/plain";
		else if (path.endsWith(".gif"))
			return "image/gif";
		else if (path.endsWith(".class"))
			return "application/octet-stream";
		else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
			return "image/jpeg";
		else
			return "text/plain";
	}

	// define function sendFile
	private static void sendFile(InputStream file, OutputStream out) {
		try {
			byte[] buffer = new byte[1000];
			while (file.available() > 0)
				out.write(buffer, 0, file.read(buffer));
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}

public class MyWebServer {

	public static boolean controlSwitch = true;

	public static void main(String a[]) throws IOException {
		int q_len = 6; /* Number of requests for OpSys to queue */
		int port = 2540;
		Socket sock;

		ServerSocket servsock = new ServerSocket(port, q_len);

		System.out.println("Yuetao's Port listener running at 2540.\n");
		while (controlSwitch) {
			// wait for the next client connection:
			sock = servsock.accept();
			new WebWorker(sock).start(); // Uncomment to see shutdown bug:
			// try{Thread.sleep(10000);} catch(InterruptedException ex) {}
		}
	}
}