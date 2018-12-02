import java.net.*;

import javax.crypto.spec.SecretKeySpec;

import java.io.*;

/**
 * Thread to deal with clients who connect to Server.  Put what you want the
 * thread to do in it's run() method.
 */

/*
 * Author: Brian Bowden || 10060818
 * CPSC 418 - Assignment 2
 * Due: November 7, 2018
 * 
 * ServerThread.java
 * 
 * 
 * Purpose:
 * 		
 * 		Thread handler for multi-threaded server.
 * 
 *  	Decrypts message from client and verifies the HMAC-SHA-1 message digest
 *  	Extracts plaintext message on "good" (verified) read and outputs it to a file (file name supplied by user)
 *  	Acknowledges client on status of verification
 *      
 * References:
 * 		original file provided by department of computer science, University of Calgary, CPSC418
 * 		encryption code provided and used from CryptoUtilities.java, decryptFile.java 
 */
public class ServerThread extends Thread{
	    private Socket sock;  //The socket it communicates with the client on.
	    private serverSecureFileTransfer parent;  //Reference to Server object for message passing.
	    private int idnum;  //The client's id number.
	    private boolean debug;
		
	    /**
	     * Constructor, does the usual stuff.
	     * @param s Communication Socket.
	     * @param p Reference to parent thread.
	     * @param id ID Number.
	     */
	    public ServerThread (Socket s, serverSecureFileTransfer p, int id, Boolean d)
	    {
		parent = p;
		sock = s;
		idnum = id;
		debug = d;
	    }
		
	    /**
	     * Getter for id number.
	     * @return ID Number
	     */
	    public int getID ()
	    {
		return idnum;
	    }
		
	    /**
	     * Getter for the socket, this way the parent thread can
	     * access the socket and close it, causing the thread to
	     * stop blocking on IO operations and see that the server's
	     * shutdown flag is true and terminate.
	     * @return The Socket.
	     */
	    public Socket getSocket ()
	    {
		return sock;
	    }
	    
	    public boolean getDubug() {
	    	return debug;
	    }
		
	    /**
	     * This is what the thread does as it executes.  Listens on the socket
	     * for incoming data and then echos it to the screen.  A client can also
	     * ask to be disconnected with "exit" or to shutdown the server with "die".
	     */
	    public void run ()
	    {
	    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		DataInputStream in = null;
		DataOutputStream out = null;
		String incoming = null;
		File clientFile;
			
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
		
		try {
		    out = new DataOutputStream(sock.getOutputStream());
		}
		catch (IOException e) {
		    System.out.println ("Could not create output stream.");
		    return;
		}
		System.out.println("waiting for user...");
		
		/* Try to read from the socket */
		try {
		    incoming = in.readUTF();
		}
		catch (IOException e) {
		    if (parent.getFlag())
			{
			    System.out.println ("shutting down.");
			    return;
			}
		    return;
		}

		/* See if we've recieved something */
		while (incoming != null)
			{
				/* If the client has sent "exit", instruct the server to
				 * remove this thread from the vector of active connections.
				 * Then close the socket and exit.
				 */

				if (incoming.compareTo("exit") == 0)
				    {
					parent.kill (this);
					try {
					    in.close ();
					    sock.close ();
					}
					catch (IOException e)
					    {/*nothing to do*/}
					return;
				    }
					
				/* If the client has sent "die", instruct the server to
				 * signal all threads to shutdown, then exit.
				 */
				else if (incoming.compareTo("die") == 0)
				    {
					parent.killall ();
					return;
				    }	
					
				/* Otherwise, just echo what was recieved. */
				if (debug) {
					System.out.println ("Client " + idnum + ": " + incoming);
				}
				FileOutputStream out_file = null;
				if (incoming.compareTo("send") == 0) {
					try {
						
						/* prompt for shared password */
						String my_key = null;
						System.out.print("Enter shared password: ");
						my_key = stdIn.readLine();
						
						/* read output file name and create it*/
						incoming = in.readUTF();
						
						clientFile = new File(incoming);
						try {
							clientFile.createNewFile();
						}
						catch (Exception e) {
							System.out.println("failed to create file");
							sock.close();
							in.close();
						}
						out_file = new FileOutputStream(clientFile);
						
						/* read file size */
						int read_bytes = in.readInt();
						if (debug) {
							System.out.println("bytes read: " + read_bytes);
						}
						
						/* get the ciphertext */
						byte[] msg = new byte[read_bytes];
						in.read(msg, 0, read_bytes);
						
						SecretKeySpec key = CryptoUtilities.key_from_seed(my_key.getBytes());
						
						byte[] hashed_plaintext = CryptoUtilities.decrypt(msg, key);
						
						if (CryptoUtilities.verify_hash(hashed_plaintext, key)){
							System.out.println("Message Digest is good");
							byte[] plaintext = CryptoUtilities.extract_message(hashed_plaintext);
							if (debug) {
								System.out.println(new String(plaintext));
							}
							out.writeInt(0);
							out_file.write(plaintext);
							out_file.close();
						}
						else {
							System.out.println("Error: invalid message digest!");
							out.writeInt(1);
						}	
								
					} catch (Exception e) {
					System.out.println(e);
					}
				}
				System.out.println("waiting for user...");
				/* Try to get the next line.  If an IOException occurs it is
				 * probably because another client told the server to shutdown,
				 * the server has closed this thread's socket and is signalling
				 * for the thread to shutdown using the shutdown flag.
				 */
				try {
				    incoming = in.readUTF();
				}
				catch (IOException e) {
				    if (parent.getFlag())
					{
					    System.out.println ("shutting down.");
					    return;
					}
				    else
					{
					    System.out.println ("IO Error.");
					    return;
					}
				}
			}
	    }

}
