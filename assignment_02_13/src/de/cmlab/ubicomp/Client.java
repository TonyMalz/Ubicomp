package de.cmlab.ubicomp;

import java.util.Scanner;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;

/**
 * XML-RPC Client
 * <p>
 * <ul>
 * <li>Tries to connect to localhost on port 8080
 * <li>Expects two integers from command line
 * <li>Calls method {@link Server#sum(int, int)} with those two parameters
 * <li>Prints result of RPC call to stdout
 * </ul>
 * </p>
 */
public class Client {

	/**
	 * Main method
	 * <p>
	 * <ul>
	 * <li>Connects to XML-Service on localhost port 8080
	 * <li>Reads two integers and calls webservice method
	 * {@link Server#sum(int, int)}
	 * <li>Prints result to screen
	 * </ul>
	 * </p>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			XmlRpcClient server = new XmlRpcClient("http://localhost:8080/RPC2");
			Vector<Integer> params = new Vector<Integer>();

			Thread.sleep(3000);
			System.out.println("Sums up two numbers!");
			boolean error = false;
			do {
				try (Scanner sc = new Scanner(System.in)) {

					System.out.println("Type 1st number please!");
					params.addElement(new Integer(sc.nextInt()));
					System.out.println("Type 2nd number please!");
					params.addElement(new Integer(sc.nextInt()));
					error = false;
				} catch (Exception e) {
					System.err.println("Please enter a valid integer!!");
					params.clear();
					error = true;
				}
			} while (error);

			Object result = server.execute("sample.sum", params);

			int sum = ((Integer) result).intValue();
			System.out.println("The sum is: " + sum);

		} catch (Exception exception) {
			System.err.println("JavaClient: " + exception);
		}
	}
}
