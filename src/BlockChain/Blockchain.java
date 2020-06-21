//package BlockChain;
/* 

Version 1.2 2019-05-18

2019-05-18: Added PublicKey to String and String back to Public Key functionality.

Author: Clark Elliott, with ample help from the below web sources.

You are free to use this code in your assignment, but you MUST add
your own comments. Leave in the web source references.

This is pedagogical code and should not be considered current for secure applications.

The web sources:

http://www.java2s.com/Code/Java/Security/SignatureSignAndVerify.htm
https://www.mkyong.com/java/java-digital-signatures-example/ (not so clear)
https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
https://www.programcreek.com/java-api-examples/index.php?api=java.security.SecureRandom
https://www.mkyong.com/java/java-sha-hashing-example/
https://stackoverflow.com/questions/19818550/java-retrieve-the-actual-value-of-the-public-key-from-the-keypair-object

XML validator:
https://www.w3schools.com/xml/xml_validator.asp

XML / Object conversion:
https://www.mkyong.com/java/jaxb-hello-world-example/
*/

/* CDE: The JAXB libraries: */
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;

/* CDE: The encryption needed for signing the hash: */

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;

import java.security.spec.*;
// Ah, heck:
import java.security.*;

/* CDE Some other uitilities: */

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.text.*;
import java.util.Base64;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;
// Produces a 64-bye string representing 256 bits of the hash output. 4 bits per character
import java.security.MessageDigest; // To produce the SHA-256 hash.
// For create process P0, P1, P2
import java.lang.Process;

@XmlRootElement
class BlockRecord {
	/* Examples of block fields: */
	String VerificationProcessID;
	String PreviousHash;
	String SignedSHA256;
	String BlockID;
	String Fname;
	String Lname;
	String SSNum;
	String DOB;
	String Diag;
	String Treat;
	String Rx;
	String Timestamp;

	/* Examples of accessors for the BlockRecord fields: */
	public String getSSNum() {
		return SSNum;
	}

	@XmlElement
	public void setSSNum(String SS) {
		this.SSNum = SS;
	}

	public String getFname() {
		return Fname;
	}

	@XmlElement
	public void setFname(String FN) {
		this.Fname = FN;
	}

	public String getLname() {
		return Lname;
	}

	@XmlElement
	public void setLname(String LN) {
		this.Lname = LN;
	}

	public String getVerificationProcessID() {
		return VerificationProcessID;
	}

	@XmlElement
	public void setVerificationProcessID(String VID) {
		this.VerificationProcessID = VID;
	}

	public String getBlockID() {
		return BlockID;
	}

	@XmlElement
	public void setBlockID(String BID) {
		this.BlockID = BID;
	}

	@XmlElement
	public void setDOB(String DB) {
		this.DOB = DB;
	}

	public String getDOB() {
		return DOB;
	}

	@XmlElement
	public void setDiag(String Dg) {
		this.Diag = Dg;
	}

	public String getDiag() {
		return Diag;
	}

	@XmlElement
	public void setTreat(String Tre) {
		this.Treat = Tre;
	}

	public String getTreat() {
		return Treat;
	}

	@XmlElement
	public void setRx(String rx) {
		this.Rx = rx;
	}

	public String getRx() {
		return Rx;
	}

	@XmlElement
	public void setTimestamp(String TStamp) {
		this.Timestamp = TStamp;
	}

	public String getTimestamp() {
		return Timestamp;
	}
	
	@XmlElement
	public void setPreviousHash(String PHash) {
		this.PreviousHash = PHash;
	}

	public String getPreviousHash() {
		return PreviousHash;
	}
	
	@XmlElement
	public void setSignedSHA256(String SSHA256) {
		this.SignedSHA256 = SSHA256;
	}

	public String getSignedSHA256() {
		return SignedSHA256;
	}
}

//Comparator to compare Strings 
class COMPARING implements Comparator<BlockRecord> {
	public int compare(BlockRecord BR1, BlockRecord BR2) {
		Date d1 = null;
		Date d2 = null;
		String time1 = BR1.getTimestamp();
		String time2 = BR2.getTimestamp();
		SimpleDateFormat tm = new SimpleDateFormat("YYYY-MM-DD.HH:MM:SS");
		try {
			d1 = tm.parse(time1);
			d2 = tm.parse(time2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int elapsed = (int) (d2.getTime() - d1.getTime());

		return elapsed;
	}
}

//Create PublicKeyworker
class PublicKeyWorker extends Thread {
	Socket sock;
	KeyPair keyPair;

	PublicKeyWorker(Socket s, KeyPair k) {
		sock = s;
		keyPair = k;
	}

	public void run() {
		try {
			// Initialize variables, create I/O streams
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			PrintStream out = new PrintStream(sock.getOutputStream());
			String line = "";

			try {
				line = in.readLine();
			} catch (IOException i) {
				System.out.println(i);
			}

			byte[] bytePubkey = keyPair.getPublic().getEncoded();
			// System.out.println("Key in Byte[] form: " + bytePubkey);

			String stringKey = Base64.getEncoder().encodeToString(bytePubkey);
			// System.out.println("Key in String form: " + stringKey);

			// Deal with the public key
			if (line.equals("multicast")) {
				System.out.println("process 123? is multicasting...");

				try {

					Integer[] portArray = { 4710, 4711, 4712 };

					for (int i = 0; i < portArray.length; i++) {
						Socket psock = new Socket("localhost", portArray[i]);
						PrintStream topServer = new PrintStream(psock.getOutputStream());
						topServer.println(sock.getLocalPort() + stringKey);
						psock.close();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				Blockchain.proKmap.put(line.substring(0, 4), line.substring(5));
				System.out.println(Blockchain.proKmap.keySet());
			}

			// close the streams and sock
			try {
				in.close();
				out.close();
				sock.close();
			} catch (IOException i) {
				System.out.println(i);
			}

		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

}

// Create UnverifiedBlockWorker
class UnverifiedBlockWorker extends Thread {
	Socket sock;

	UnverifiedBlockWorker(Socket s) {
		sock = s;
	}

	public void run() {
		try {
			// Initialize variables, create I/O streams
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			PrintStream out = new PrintStream(sock.getOutputStream());
			String line = "";

			try {
				line = in.readLine();
			} catch (IOException i) {
				System.out.println(i);
			}

			// Deal with the Unverified Block

			try {
				// put String back into BlockRecord
				JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

				// CDE Make the output pretty printed:
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				/* CDE Here's how we put the XML back into java object form: */
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				StringReader reader = new StringReader(line.substring(line.indexOf("<blockRecord>")));

				BlockRecord blockRecord2 = (BlockRecord) jaxbUnmarshaller.unmarshal(reader);

				// put the Block Record into a priority queue
				Blockchain.unverifiedBlocks.add(blockRecord2);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// close the streams and sock
			try {
				in.close();
				out.close();
				sock.close();
			} catch (IOException i) {
				System.out.println(i);
			}

		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

// Create UpdatedBlockchainWorker
class UpdatedBlockchainWorker extends Thread {
	Socket sock;
	int pnum;

	UpdatedBlockchainWorker(Socket s, int p) {
		sock = s;
		pnum = p;
	}

	public void run() {
		try {
			// Initialize variables, create I/O streams
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			PrintStream out = new PrintStream(sock.getOutputStream());
			String line = "";


			try {
				line = in.readLine();
			} catch (IOException i) {
				System.out.println(i);
			}

			// Deal with the Updated Updated Blockchain

			try {

				/* The XML conversion tools: */
				JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				StringWriter sw = new StringWriter();

				// CDE Make the output pretty printed:
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				/* CDE Here's how we put the XML back into java object form: */
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				StringReader reader = new StringReader(line);

				BlockRecord newBlockRecord = (BlockRecord) jaxbUnmarshaller.unmarshal(reader); // for Java read info
																								// from block


				if (newBlockRecord.VerificationProcessID.equals("DummyBlock")) {
					newBlockRecord.setSignedSHA256("Dummy");
					Blockchain.BlockChainList.add(newBlockRecord);
					//System.out.println("DummyBlock's SSHA256: " + Blockchain.BlockChainList.get(0).getSignedSHA256());
		
				}
				else {
					if (verifyNewBlock(newBlockRecord) == true) {
						Blockchain.BlockChainList.add(newBlockRecord);
						System.out.println("The Block Chain list is not with length: " + Blockchain.BlockChainList.size());
						if (pnum == 0) {
							try {

								// CDE Make the output pretty printed:
								jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
								File file = new File(".\\BlockchainLedger.xml");
								String stringXML = "";
								PrintWriter Fout = new PrintWriter("BlockchainLedger.xml");
								
								for (int i = 0; i < Blockchain.BlockChainList.size(); i++) {
									StringWriter swF = new StringWriter();
									jaxbMarshaller.marshal(Blockchain.BlockChainList.get(i), swF);	
									stringXML = swF.toString();
									
									Fout.println(stringXML);
									
									
								}
								
								Fout.close();
								
								
								
							}catch(Exception e) {e.printStackTrace();}
							
						}
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// close the streams and sock
			try {
				in.close();
				out.close();
				sock.close();
			} catch (IOException i) {
				System.out.println(i);
			}

		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	private boolean verifyNewBlock(BlockRecord newBlockRecord) {
		// TODO Auto-generated method stub
		int index = Blockchain.BlockChainList.size() - 1;
		BlockRecord lastBlock = Blockchain.BlockChainList.get(index);
		if (newBlockRecord.getPreviousHash().equals(lastBlock.getSignedSHA256()))
			return true;
		else
			return false;
	}
}

// we need 3 Looper:
// PublicKeyLooper: for incoming public keys
// UnverifiedBlockLooper: for incoming unverified Blocks
// UpdatedBlockchainLooper: for incoming updated Block chain.
class PublicKeyLooper implements Runnable {
	int PublicKeyPort;
	KeyPair keyPair;

	PublicKeyLooper(int p, KeyPair kp) {
		PublicKeyPort = p;
		keyPair = kp;
	}

	public void run() {
		System.out.println("In the PublicKey looper thread");

		int q_len = 6;
		Socket sock;

		try {
			ServerSocket servsock = new ServerSocket(PublicKeyPort, q_len);
			while (true) {
				// wait for another public key connection
				sock = servsock.accept();
				new PublicKeyWorker(sock, keyPair).start();
			}

		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

// UnverifiedBlockLooper
class UnverifiedBlockLooper implements Runnable {
	int UnverifiedBlockPort;

	UnverifiedBlockLooper(int port) {
		UnverifiedBlockPort = port;
	}

	public void run() {
		System.out.println("In the Unverified Block Looper thread");

		int q_len = 6;
		Socket sock;

		try {
			ServerSocket servsock = new ServerSocket(UnverifiedBlockPort, q_len);
			while (true) {
				// wait for another unverified Block connection
				sock = servsock.accept();
				new UnverifiedBlockWorker(sock).start();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

// UpdatedBlockchainLooper
class UpdatedBlockchainLooper implements Runnable {
	int UpdatedBlockchainPort;
	int pnum;

	UpdatedBlockchainLooper(int port, int p) {
		UpdatedBlockchainPort = port;
		pnum = p;
	}

	public void run() {
		System.out.println("In the Updated Blockchain Looper thread");

		int q_len = 6;
		Socket sock;

		try {
			ServerSocket servsock = new ServerSocket(UpdatedBlockchainPort, q_len);
			while (true) {
				// wait for another unverified Block connection
				sock = servsock.accept();
				new UpdatedBlockchainWorker(sock, pnum).start();
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

/*
 * Starting point for the BlockRecord:
 * 
 * <?xml version="1.0" encoding="UTF-8"?> <BlockRecord> <SIGNED-SHA256>
 * [B@5f150435 </SIGNED-SHA256> <!-- Verification procees SignedSHA-256-String
 * --> <SHA-256-String>
 * 63b95d9c17799463acb7d37c85f255a511f23d7588d871375d0119ba4a96a
 * </SHA-256-String> <!-- Start SHA-256 Data that was hashed -->
 * <VerificationProcessID> 1 </VerificationProcessID> <!-- Process that is
 * verifying this block, for credit--> <PreviousHash> From the previous block in
 * the chain </PreviousHash> <Seed> Your random 256 bit string </Seed> <!--
 * guess the value to complete the work--> <BlockNum> 1 </BlockNum> <!--
 * increment with each block prepended --> <BlockID> UUID </BlockID> <!-- Unique
 * identifier for this block --> <SignedBlockID> BlockID signed by creating
 * process </SignedBlockID> <!-- Creating process signature -->
 * <CreatingProcessID> 0 </CreatingProcessID> <!-- Process that made the ledger
 * entry --> <TimeStamp> 2017-09-01.10:26:35 </TimeStamp> <DataHash> The
 * creating process SHA-256 hash of the input data </DataHash> <!-- for auditing
 * if Secret Key exposed --> <FName> Joseph </FName> <LName> Ng </LName> <DOB>
 * 1995.06.22 </DOB> <!-- date of birth --> <SSNUM> 987-65-4321 </SSNUM>
 * <Diagnosis> Measels </Diagnosis> <Treatment> Bedrest </Treatment> <Rx>
 * aspirin </Rx> <Notes> Use for debugging and extension </Notes> <!-- End
 * SHA-256 Data that was hashed --> </BlockRecord>
 */

public class Blockchain {

	public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
		Signature signer = Signature.getInstance("SHA1withRSA");
		signer.initSign(key);
		signer.update(data);
		return (signer.sign());
	}

	public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
		Signature signer = Signature.getInstance("SHA1withRSA");
		signer.initVerify(key);
		signer.update(data);

		return (signer.verify(sig));
	}

	public static KeyPair generateKeyPair(long seed) throws Exception {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
		SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
		rng.setSeed(seed);
		keyGenerator.initialize(1024, rng);

		return (keyGenerator.generateKeyPair());
	}

	public static String CSC435Block = "We will build this dynamically: <?xml version = \"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

	public static final String ALGORITHM = "RSA"; /* Name of encryption algorithm used */

	public static HashMap<String, String> proKmap = new HashMap<String, String>(); // for save public keys from
																					// different process

	public static ArrayList<BlockRecord> BlockChainList = new ArrayList<BlockRecord>(); // for save blocks into
																						// blockchain

	public static PriorityBlockingQueue<BlockRecord> unverifiedBlocks = new PriorityBlockingQueue<BlockRecord>(20,
			new COMPARING()); // for save unverified Blocks
	// public static ArrayList<BlockRecord> unverifiedBlocks = new
	// ArrayList<BlockRecord>(); // for save unverified Blocks

	public static ArrayList<BlockRecord> blockArrayList = new ArrayList<BlockRecord>(); // for read blocks from file

	/* Header fields for the block: */
	public static String SignedSHA256;

	/* CDE NOTE: we do not need this method for the CSC435 blockchain assignment. */
	public static byte[] encrypt(String text, PublicKey key) {
		byte[] cipherText = null;
		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM); // Get RSA cipher object
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipherText;
	}

	/* CDE NOTE: we do not need this method for the CSC435 blockchain assignment. */
	public static String decrypt(byte[] text, PrivateKey key) {
		byte[] decryptedText = null;
		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key);
			decryptedText = cipher.doFinal(text);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new String(decryptedText);
	}

	// For create processes
	private static void runProcess(int pnum, KeyPair kp) {
		int PublicKeyPort;
		int UnverifiedBlockPort;
		int UpdatedBlockchainPort;
		KeyPair keyPair;

		PublicKeyPort = 4710 + pnum;
		UnverifiedBlockPort = 4820 + pnum;
		UpdatedBlockchainPort = 4930 + pnum;
		keyPair = kp;

		System.out.println("Process number: " + pnum + " Ports: " + PublicKeyPort + " " + UnverifiedBlockPort + " "
				+ UpdatedBlockchainPort + "\n");
		try {

			PublicKeyLooper publicKeyL = new PublicKeyLooper(PublicKeyPort, keyPair);
			Thread pubKL = new Thread(publicKeyL);
			pubKL.start();

			UnverifiedBlockLooper unverifiedBL = new UnverifiedBlockLooper(UnverifiedBlockPort);
			Thread uBL = new Thread(unverifiedBL);
			uBL.start();

			UpdatedBlockchainLooper updateBCL = new UpdatedBlockchainLooper(UpdatedBlockchainPort, pnum);
			Thread upBCL = new Thread(updateBCL);
			upBCL.start();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {

		/* CDE: Process numbers and port numbers to be used: */
		int pnum;
		KeyPair keyPair = generateKeyPair((int) (Math.random() * 100)); // Use a random seed in real life

		/* CDE If you want to trigger bragging rights functionality... */
		if (args.length > 1)
			System.out.println("Special functionality is present \n");

		if (args.length < 1)
			pnum = 0;
		else if (args[0].equals("0"))
			pnum = 0;
		else if (args[0].equals("1"))
			pnum = 1;
		else if (args[0].equals("2"))
			pnum = 2;
		else
			pnum = 0; /* Default for badly formed argument */

		// Using the start script, start your servers in the order P0, P1, P2
		if (pnum == 2) {
			// When P2 starts, it also triggers the multicast of public keys, and starts the
			// whole system running.
			runProcess(2, keyPair);
			multicastPublicKeys();

			// All processes start with the same initial one-block (dummy entry) form of the
			// blockchain.
			DummyBlock(keyPair);

		} else
			runProcess(pnum, keyPair);

		// After all public keys have been established read the data file for this
		// process.
		readDataFile(pnum);

		while (Blockchain.proKmap.size() < 3 && Blockchain.BlockChainList.size() < 1) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Create unverified blocks from the data and using XML as the external data
		// format, multicast each unverified block in turn to all other processes.

		System.out.println("Multicast each unverified block in turn to all other processes...");

		multicastBlock();

		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("This is the unverified Block list's size: " + Blockchain.unverifiedBlocks.size());

		// Each process then, one by one, pops the unverified blocks from the priority
		// queue, attempts to solve the puzzle and verify the block in competition with
		// the other processes.
		
		while (Blockchain.unverifiedBlocks.size() != 0) {
			
			if (verifyBlock() == true) {
				Blockchain.unverifiedBlocks.poll();
			}
			else {
				BlockRecord BR = Blockchain.unverifiedBlocks.peek();
				verifySignature(BR);
				solvePuzzle(BR);
			}
			
			
		}
		

	}

	private static void verifySignature(BlockRecord bR) {
		String hashcode = bR.PreviousHash;
		
	}

	private static boolean verifyBlock() {
		boolean blockIn = false;
		BlockRecord BR = Blockchain.unverifiedBlocks.peek();
		for(int i = 0; i < BlockChainList.size(); i++) {
			if (BR.getBlockID().equals(BlockChainList.get(i).getBlockID())) 
				blockIn = true;
		}
		return blockIn;
	}

	static String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	private static void solvePuzzle(BlockRecord BlockRcd) {
		String randString;
		String concatString = ""; // Random seed string concatenated with the existing data
		String stringOut = ""; // Will contain the new SHA256 string converted to HEX and printable.
		String blockString = "";
		int workNumber = 0;
		BlockRecord LastBR = Blockchain.BlockChainList.get(BlockChainList.size() - 1);
		String LastBRID = LastBR.getBlockID();
		BlockRecord BR = BlockRcd;
		BR.setPreviousHash(LastBR.getSignedSHA256()); 		// set the Block record with previous hash then guess
		
		try {
		
		randString = randomAlphaNumeric(8); // seed

		/* The XML conversion tools: */
		JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		StringWriter sw = new StringWriter();

		// CDE Make the output pretty printed:
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		/*
		 * CDE We marshal the block object into an XML string so it can be sent over the
		 * network:
		 */
		jaxbMarshaller.marshal(BR, sw);
		String stringXML = sw.toString();
		blockString = stringXML;
		} catch(Exception e) {e.printStackTrace();}

		try {

			for (int i = 1; i < 20; i++) { // Limit how long we try for this example.
				randString = randomAlphaNumeric(8); // Get a new random AlphaNumeric seed string
				concatString = blockString + randString; // Concatenate with our input string (which represents
															// Blockdata)
				MessageDigest MD = MessageDigest.getInstance("SHA-256");
				byte[] bytesHash = MD.digest(concatString.getBytes("UTF-8")); // Get the hash value
				stringOut = DatatypeConverter.printHexBinary(bytesHash); // Turn into a string of hex values
				System.out.println("Hash is: " + stringOut);
				workNumber = Integer.parseInt(stringOut.substring(0, 4), 16); // Between 0000 (0) and FFFF (65535)
				System.out.println(
						"First 16 bits in Hex and Decimal: " + stringOut.substring(0, 4) + " and " + workNumber);
				if (!(workNumber < 20000)) { // lower number = more work.
					System.out.format("%d is not less than 20,000 so we did not solve the puzzle\n\n", workNumber);
				}
				if (workNumber < 20000) {
					System.out.format("%d IS less than 20,000 so puzzle solved!\n", workNumber);
					System.out.println("The seed (puzzle answer) was: " + randString);
					break;
				}
				// Here is where you would periodically check to see if the blockchain has been
				// updated
				// ...if so, then abandon this verification effort and start over.
				BlockRecord LastBRnew = Blockchain.BlockChainList.get(BlockChainList.size() - 1);
				if (!LastBRID.equals(LastBRnew.getBlockID()))
					break;
				// Here is where you will sleep if you want to extend the time up to a second or
				// two.
				try {
					Thread.sleep(1000);
				} catch(Exception e) {e.printStackTrace();}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	
		
		BR.setSignedSHA256(stringOut);	// String "UB" 
		
		updateTopeers(BR);	// update Blockchain list to all processes
		

	}

	private static void updateTopeers(BlockRecord bR) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// CDE Make the output pretty printed:
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			// send the unverified Blocks to each process
				StringWriter swNEW = new StringWriter();
				jaxbMarshaller.marshal(bR, swNEW);
				String BlockNEW = swNEW.toString();

				Socket BCsock0 = new Socket("localhost", 4930);
				PrintStream topServer0 = new PrintStream(BCsock0.getOutputStream());
				topServer0.println(BlockNEW.replaceAll("\n", ""));
				topServer0.close();
				BCsock0.close();

				Socket BCsock1 = new Socket("localhost", 4931);
				PrintStream topServer1 = new PrintStream(BCsock1.getOutputStream());
				topServer1.println(BlockNEW.replaceAll("\n", ""));
				topServer1.close();
				BCsock1.close();

				Socket BCsock2 = new Socket("localhost", 4932);
				PrintStream topServer2 = new PrintStream(BCsock2.getOutputStream());
				topServer2.println(BlockNEW.replaceAll("\n", ""));
				topServer2.close();
				BCsock2.close();


		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static void multicastBlock() {

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// CDE Make the output pretty printed:
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			// send the unverified Blocks to each process
			for (int i = 0; i < blockArrayList.size(); i++) {
				StringWriter swNEW = new StringWriter();
				jaxbMarshaller.marshal(blockArrayList.get(i), swNEW);
				String BlockNEW = swNEW.toString();

				Socket BCsock0 = new Socket("localhost", 4820);
				PrintStream topServer0 = new PrintStream(BCsock0.getOutputStream());
				topServer0.println(BlockNEW.replaceAll("\n", ""));
				topServer0.close();
				BCsock0.close();

				Socket BCsock1 = new Socket("localhost", 4821);
				PrintStream topServer1 = new PrintStream(BCsock1.getOutputStream());
				topServer1.println(BlockNEW.replaceAll("\n", ""));
				topServer1.close();
				BCsock1.close();

				Socket BCsock2 = new Socket("localhost", 4822);
				PrintStream topServer2 = new PrintStream(BCsock2.getOutputStream());
				topServer2.println(BlockNEW.replaceAll("\n", ""));
				topServer2.close();
				BCsock2.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void readDataFile(int pnum) {
		String FILENAME;

		switch (pnum) {
		case 1:
			FILENAME = "BlockInput1.txt";
			break;
		case 2:
			FILENAME = "BlockInput2.txt";
			break;
		default:
			FILENAME = "BlockInput0.txt";
			break;
		}

		try {
			try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {
				String[] tokens = new String[10];
				String stringXML;
				String InputLineStr;
				String suuid;
				UUID idA;

				JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				StringWriter sw = new StringWriter();

				// CDE Make the output pretty printed:
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				while ((InputLineStr = br.readLine()) != null) {

					BlockRecord newBlock = new BlockRecord();
					/* CDE For the timestamp in the block entry: */
					Date date = new Date();
					// String T1 = String.format("%1$s %2$tF.%2$tT", "Timestamp:", date);
					String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
					String TimeStampString = T1 + "." + pnum + "\n"; // No timestamp collisions!

					/*
					 * CDE: Generate a unique blockID. This would also be signed by creating
					 * process:
					 */
					idA = UUID.randomUUID();
					suuid = new String(UUID.randomUUID().toString());
					newBlock.setBlockID(suuid);
					newBlock.setVerificationProcessID("Process" + String.valueOf(pnum));

					/* CDE put the file data into the block record: */
					tokens = InputLineStr.split(" +"); // Tokenize the input
					newBlock.setSSNum(tokens[3]);
					newBlock.setFname(tokens[0]);
					newBlock.setLname(tokens[1]);
					newBlock.setDOB(tokens[2]);
					newBlock.setDiag(tokens[4]);
					newBlock.setTreat(tokens[5]);
					newBlock.setRx(tokens[6]);
					newBlock.setTimestamp(TimeStampString);

					blockArrayList.add(newBlock);
				}
				br.close();

				System.out.println(blockArrayList.size() + " records read.");
				System.out.println("Names from input:");
				for (int i = 0; i < blockArrayList.size(); i++) {
					System.out
							.println("  " + blockArrayList.get(i).getFname() + " " + blockArrayList.get(i).getLname());
				}
				System.out.println("\n");

				for (int i = 0; i < blockArrayList.size(); i++) {
					jaxbMarshaller.marshal(blockArrayList.get(i), sw);
				}
				String fullBlock = sw.toString();
				String XMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
				String cleanBlock = fullBlock.replace(XMLHeader, "");
				// Show the string of concatenated, individual XML blocks:
				String XMLBlock = XMLHeader + "\n<BlockLedger>" + cleanBlock + "</BlockLedger>";
				System.out.println(XMLBlock);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void DummyBlock(KeyPair keyPair) {

		try {
			/*
			 * CDE: Example of generating a unique blockID. This would also be signed by
			 * creating process:
			 */
			UUID idA = UUID.randomUUID();
			String suuid = UUID.randomUUID().toString();
			System.out.println("Unique Block ID: " + suuid + "\n");

			/* CDE For the timestamp in the block entry: */
			Date date = new Date();
			// String T1 = String.format("%1$s %2$tF.%2$tT", "Timestamp:", date);
			String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
			String TimeStampString = T1 + "." + "dummy" + "\n"; // No timestamp collisions!
			System.out.println("Timestamp: " + TimeStampString);

			/* CDE put some data into the block record: */
			BlockRecord blockRecord = new BlockRecord();
			blockRecord.setVerificationProcessID("DummyBlock");
			blockRecord.setBlockID(suuid);
			blockRecord.setSSNum("123-45-6789");
			blockRecord.setFname("Joseph");
			blockRecord.setLname("Chang");

			/* The XML conversion tools: */
			JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			StringWriter sw = new StringWriter();

			// CDE Make the output pretty printed:
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			/*
			 * CDE We marshal the block object into an XML string so it can be sent over the
			 * network:
			 */
			jaxbMarshaller.marshal(blockRecord, sw);
			String stringXML = sw.toString();
			CSC435Block = stringXML;

			/* Make the SHA-256 Digest of the block: */
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(CSC435Block.getBytes());
			byte byteData[] = md.digest();

			// CDE: Convert the byte[] to hex format. THIS IS NOT VERFIED CODE:
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}

			String SHA256String = sb.toString();

			byte[] digitalSignature = signData(SHA256String.getBytes(), keyPair.getPrivate());
			SignedSHA256 = Base64.getEncoder().encodeToString(digitalSignature);
			
		
			String fullBlock = stringXML.replaceAll("\n", "");

			// send the fullBlock to updateBlockChainWorker

			try {

				Integer[] BCportArray = { 4930, 4931, 4932 };

				for (int i = 0; i < BCportArray.length; i++) {
					Socket BCsock = new Socket("localhost", BCportArray[i]);
					PrintStream topServer = new PrintStream(BCsock.getOutputStream());
					topServer.println(fullBlock);
					BCsock.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void multicastPublicKeys() {
		try {

			Integer[] portArray = { 4710, 4711, 4712 };

			for (int i = 0; i < portArray.length; i++) {
				Socket sock = new Socket("localhost", portArray[i]);
				PrintStream toServer = new PrintStream(sock.getOutputStream());
				toServer.println("multicast");
				sock.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}