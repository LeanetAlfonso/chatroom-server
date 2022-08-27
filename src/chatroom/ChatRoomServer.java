package chatroom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Programming Assignment 1
 * 
 * @author leanetalfonsoazcona (6101692)
 * 
 * COP4338 U01B 1205 (Summer B 2020)
 *
 * Server program that implements a multi-user chat room using sockets and multi-threading
 */
public class ChatRoomServer {

	final private static int CHAT_ROOM_PORT = 3333;

	public static void main(String[] args) throws IOException {

		// Arraylist to store connections from each respective chat room
		ArrayList <ChatRoomThread> cr = new ArrayList <ChatRoomThread>();

		// Server socket
		ServerSocket server = new ServerSocket(CHAT_ROOM_PORT);

		// Loop to accept multiple clients using MultiThreading
		while (true) {
			// client socket
			Socket clientSocket = server.accept();

			// Thread instantiation and run call of Runnable class instantiation using client socket
			ChatRoomThread crt = new ChatRoomThread (clientSocket,cr);
			Thread t = new Thread(crt);
			t.start();
		}
	}
}
