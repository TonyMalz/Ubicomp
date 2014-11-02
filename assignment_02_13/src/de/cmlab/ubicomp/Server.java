package de.cmlab.ubicomp;

import org.apache.xmlrpc.WebServer;

/**
 * XML-RPC Server
 * <p>
 * <ul>
 * <li>Starts local XML-RPC Server listening on port 8080
 * <li>Has one public method {@link #sum(int, int)} which returns the sum of two
 * integers
 * </ul>
 * </p>
 */
public class Server {

	/**
	 * Sums up two integers
	 * 
	 * @param x
	 *            1st operand
	 * @param y
	 *            2nd operand
	 * @return result of x + y
	 */
	public Integer sum(int x, int y) {
		return new Integer(x + y);
	}

	/**
	 * Main method<br>
	 * Starts webservice on port 8080 and waits for clients to connect
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			System.out.println("Attempting to start XML-RPC Server...");
			WebServer server = new WebServer(8080);
			server.addHandler("sample", new Server());
			server.start();
			System.out.println("Started successfully.");
			System.out.println("Accepting requests. (Halt program to stop.)");
		} catch (Exception exception) {
			System.err.println("JavaServer: " + exception);
		}
	}
}
