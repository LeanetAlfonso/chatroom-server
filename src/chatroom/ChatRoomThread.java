package chatroom;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Programming Assignment 1
 * 
 * @author leanetalfonsoazcona (6101692)
 * 
 * COP4338 U01B 1205 (Summer B 2020)
 *
 * Runnable class that handles user's requests through IO
 */
public class ChatRoomThread implements Runnable {

	private Socket clientSocket;
	private String ID;
	private Scanner in;
	private PrintWriter out;
	private Lock lock = new ReentrantLock();
	private ArrayList <ChatRoomThread> cr = new ArrayList <ChatRoomThread>();


	/**
	 * Constructor of ChatRoomThread initializes clientSocket and an ArrayList of chat room threads
	 * @param clientSocket
	 * @param cr
	 * @throws IOException
	 */
	public ChatRoomThread (Socket clientSocket, ArrayList<ChatRoomThread> cr) throws IOException {
		this.clientSocket = clientSocket;
		this.cr = cr;
	}

	// Default constructor
	public ChatRoomThread () {

	}

	/**
	 * Prompt, listen, and respond to client's requests accordingly
	 * @Override
	 */
	public void run() {
		try {
			try {
				// instantiate IO objects
				in = new Scanner (clientSocket.getInputStream());
				out = new PrintWriter (clientSocket.getOutputStream(), true);

				// prompt client to enter username (ensure client has a username prior chatting)
				out.println("Please enter your username");
				ID = in.nextLine();

				// validate that no two clients have the same ID name in a chat room
				while (isDuplicate(ID)) {
					out.println("Username \""+ID+"\" is taken. Please enter a different username");
					ID = in.nextLine();
				}

				// safely add current client to chat room ArrayList
				lock.lock();
				try {
					cr.add(this);
				}
				finally {
					lock.unlock();
				}

				// broadcast welcome message (public to all chat room participants)
				Broadcast("*** "+ID+" has joined the chat ***");

				// display command message (private to client)
				out.println("You can now start chatting. To display the list of commands type \"COMMANDS\"");

				// wait for client's input
				while (in.hasNext()) {

					String inputLn = in.nextLine();

					// handle exit request
					if (inputLn.equalsIgnoreCase("EXIT") || inputLn.equals(null)) {

						lock.lock();
						try {
							if (cr!=null) 
								cr.remove(this);
							
						}
						finally {
							lock.unlock();

						}
						break;
					}
					else 
						chatRoomProtocol(inputLn); // handle any other request that does not require exiting
				}
			}
			finally {
				// broadcast exit message when someone leaves the chat
				Broadcast("*** "+ID+" has left the chat ***");
				clientSocket.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	} // end of run()


	/**
	 * Handles clients' requests and generates responses accordingly
	 * @param inputLn the clients input
	 * @throws IOException
	 */
	private void chatRoomProtocol (String inputLn) throws IOException {

		// list all commands
		if (inputLn.equalsIgnoreCase("COMMANDS") || inputLn == null) {

			out.println("\nEXIT:     Exit chat"
						+ "\nLISTALL:  List usernames of all participants in the chat room"
						+ "\nCOUNTALL: Display number of participants in the chat room"
						+ "\nWHOAMI:   Display your username"
						+ "\nEDITUSER: Change your username"
						+ "\nCOMMANDS: Display list of commands\n");
			}

		// list all current chat room participants
		else if (inputLn.equalsIgnoreCase("LISTALL")) {
			listAll();
		}

		// display number of current chat room participants
		else if (inputLn.equalsIgnoreCase("COUNTALL")) {
			out.println(cr.size());
		}

		// display number of current chat room participants
				else if (inputLn.equalsIgnoreCase("WHOAMI")) {
					out.println("Your current username is \""+ID+"\"");
				}
		
		// change username
		else if (inputLn.equalsIgnoreCase("EDITUSER")) {

			out.println("Please enter new username");
			String tempID = in.nextLine();

			// validate that no two clients have the same ID name in a chat room
			while (isDuplicate(tempID)) {
				out.println("Username \""+tempID+"\" is taken. Please enter a different username");
				tempID = in.nextLine();
			}

			out.println("Username changed to \""+tempID+"\"");
			ID = tempID;
		}

		// chat message relayed to all chat room participants in message format
		else 
			Broadcast(ID+": "+inputLn);
	}

	/**
	 * Relay a message to all chat room participants
	 * @param msg message to be printed
	 */
	public void Broadcast (String msg) {
		for (ChatRoomThread client : cr) 
			client.out.println(msg);
	}

	/**
	 * Determine whether the ID received as parameter currently belongs 
	 * to other chat room participant
	 * @param tempID 
	 * @param ID 
	 * @return true or false accordingly
	 */
	public boolean isDuplicate (String tempID) {
		for (ChatRoomThread client : cr) {
			if (tempID.equalsIgnoreCase(client.getID()))
				return true;
		}
		return false;
	}

	/**
	 * List all occupants of chat room
	 * @param chatRoomList
	 */
	public void listAll () {
		for (ChatRoomThread client : cr)
			out.println("  - "+ client);
		out.println();
	}

	/**
	 * Return the ID of client
	 * @return client ID 
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Return a string representation of the class in form of client ID 
	 * (used to list all current chat room participants by their usernames)
	 * @return ID
	 * @Override 
	 */
	public String toString() {
		return ID;
	}
}
