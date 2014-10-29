package de.cmlab.ubicomp;

import java.util.Scanner;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;

public class Client {

	public static void main(String[] args) {
		try {

			// while (true) {
			// System.out.println("2");
			// Thread.sleep(500);
			// }

			XmlRpcClient server = new XmlRpcClient("http://localhost:8080/RPC2");
			Vector<Integer> params = new Vector<Integer>();

			Thread.sleep(3000);

			try (Scanner sc = new Scanner(System.in)) {

				System.out.println("Sums up two numbers!");
				System.out.println("Type 1st number please!");
				params.addElement(new Integer(sc.nextInt()));
				System.out.println("Type 2nd number please!");
				params.addElement(new Integer(sc.nextInt()));

			} catch (Exception e) {

			}

			Object result = server.execute("sample.sum", params);

			int sum = ((Integer) result).intValue();
			System.out.println("The sum is: " + sum);

		} catch (Exception exception) {
			System.err.println("JavaClient: " + exception);
		}
	}
}
