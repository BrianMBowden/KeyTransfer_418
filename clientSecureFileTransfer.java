import java.io.*;
import java.net.*;

import javax.crypto.spec.SecretKeySpec;

/*
 * Author: Brian Bowden || 10060818
 * CPSC 418 - Assignment 2
 * Due: November 7, 2018
 * 
 * clientSecureFileTransfer.java
 * 
 * compile with: javac clientSecureFileTransfer.java
 * execute with: java clientSecureFileTransfer [IP-Address] [Port #] [debug]
 * 
 * Purpose:
 * 		
 * 		encrypts files using AES-128-CBC standard with a shared session key between client and server
 *      HMAC-SHA-1 is used to provide data integrity for all protocol messages
 *      
 *      Client side prepares the encryption and hashes the encrypted data, sends data through
 *      a socket connected on the port to the server.
 *      
 *      Client has ability to shut down its process and all other processes
 *      
 * References:
 * 		original file provided by department of computer science, University of Calgary, CPSC418
 * 		encryption code provided and used from CryptoUtilities.java, SecureFile.java 
 */
public class clientSecureFileTransfer {
	private Socket sock;  //Socket to communicate with.
	private static boolean debug;
	
    /**
     * Main method, starts the client.
     * @param args args[0] needs to be a hostname, args[1] a port number, args[2] is debug mode (on/off).
     */
    public static void main (String [] args)
    {
	if (args.length != 3) {
	    System.out.println ("Usage: java Client hostname port#");
	    System.out.println ("hostname is a string identifying your server");
	    System.out.println ("port is a positive integer identifying the port to connect to the server");
	    System.out.println ("debug mode?");
	    return;
	}

	try {
		debug = args[2].compareTo("debug") == 0;
	    @SuppressWarnings("unused")
		clientSecureFileTransfer c = new clientSecureFileTransfer (args[0], Integer.parseInt(args[1]));
	}
	catch (NumberFormatException e) {
	    System.out.println ("Usage: java Client hostname port#");
	    System.out.println ("Second argument was not a port number");
	    return;
	}
    }
	
    /**
     * Constructor, in this case does everything.
     * @param ipaddress The hostname to connect to.
     * @param port The port to connect to.
     */
    public clientSecureFileTransfer (String ipaddress, int port)
    {
	/* Allows us to get input from the keyboard. */
	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	String userinput;
	DataInputStream in = null;
	DataOutputStream out;
		
	/* Try to connect to the specified host on the specified port. */
	try {
	    sock = new Socket (InetAddress.getByName(ipaddress), port);
	}
	catch (UnknownHostException e) {
	    System.out.println ("Usage: java Client hostname port#");
	    System.out.println ("First argument is not a valid hostname");
	    return;
	}
	catch (IOException e) {
	    System.out.println ("Could not connect to " + ipaddress + ".");
	    return;
	}
		
	/* Status info */
	if (debug) {
		System.out.println ("Connected to " + sock.getInetAddress().getHostAddress() + " on port " + port);
	}
	try {
	    out = new DataOutputStream(sock.getOutputStream());
	}
	catch (IOException e) {
	    System.out.println ("Could not create output stream.");
	    return;
	}
	
	/* server info */
	try {
	    in = new DataInputStream ((sock.getInputStream()));
	}
	catch (UnknownHostException e) {
	    System.out.println ("Unknown host error.");
	    return;
	}
	catch (IOException e) {
	    System.out.println ("Could not establish communication.");
	    return;
	}
	
	FileInputStream in_file = null;
	FileOutputStream out_file = null;
	messagePrompt();
	/* Wait for the user to type stuff. */
	try {
	    while ((userinput = stdIn.readLine()) != null) {

		/* Echo it to the screen. */
		out.writeUTF(userinput);
		
		if ((userinput.compareTo("exit") == 0) || (userinput.compareTo("die") == 0)) {
		    System.out.println ("Client exiting.");
		    stdIn.close ();
		    out.close ();
		    sock.close();
		    return;
		}
		if (userinput.compareTo("send") == 0){
			try {
				
			    /* prompt for password */
			    String my_key = null;
			    System.out.print("Enter shared password: ");
			    my_key = stdIn.readLine();
				/* prompt for file name*/
				System.out.print("Enter Input File Name: ");
				userinput = stdIn.readLine();
				
				try {
					File myFile = new File(userinput);
					myFile.createNewFile();
					in_file = new FileInputStream(myFile);
					
				} 
				catch(IOException e) {
					System.out.println("file does not exist");
					/* kill this process on both ends*/
					out.writeUTF("exit");
					sock.close();
					return;
				}
				
				/* set up file IO*/
				System.out.print("Enter Output File Name: ");
				userinput = stdIn.readLine();
				if (debug) {
					out_file = new FileOutputStream("debug.txt");
				}
/*				try {
					File endFile = new File(userinput);
					endFile.createNewFile();
					out_file = new FileOutputStream(endFile);
				}
				catch (Exception e) {
					System.out.println("failed to create file... ");
					sock.close();
					in.close();
				}*/
				out.writeUTF(userinput);
				byte[] msg = new byte[in_file.available()];
				@SuppressWarnings("unused")
				int read_bytes = in_file.read(msg);
							
				SecretKeySpec key = CryptoUtilities.key_from_seed(my_key.getBytes());
				
				byte[] hashed_key = CryptoUtilities.append_hash(msg, key);
				byte[] aes_ciphertext = CryptoUtilities.encrypt(hashed_key, key);
				

				
				if (debug) {
					out_file.write(aes_ciphertext);
					System.out.println("wrote to file");
					System.out.println("cipher text length: " + aes_ciphertext.length);
				}
				
				out.writeInt(aes_ciphertext.length);
				out.write(aes_ciphertext);
				
				if (debug) {
					System.out.println("wrote to socket");
				}
				out_file.close();
				if (in.readInt() == 0) {
					System.out.println("server read went well");
				}
				else {
					System.out.println("server read went poorly");
				}
				out.writeUTF("exit");
				sock.close();
				return;
				
				
			} catch(Exception e) {
				System.out.println(e);
			};
		}
		messagePrompt();
	    }
	} catch (IOException e) {
	    System.out.println ("Could not read from input.");
	    return;
	}		
    }
    
    private void messagePrompt() {
    	System.out.println("============== Secure File Transfer System ============== ");
    	System.out.println("Welcome...");
    	System.out.println("Here are your options: \n \n");
    	System.out.println("\t exit - kill current process");
    	System.out.println("\t die - kill all process");
    	System.out.println("\t send - send file to be encrypted");
    	System.out.println("\t or you can send simple messages to the server\n");
    	System.out.println();
    	System.out.println("========================================================= ");
    	System.out.print("your choice: ");
    	return;
    }

}
