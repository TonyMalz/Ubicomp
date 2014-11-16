package de.cmlab.ubicomp;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcClient;

public class RFIDClient {
	private static int CLIENT_XML_RPC_PORT = 12345;

	private static XmlRpcClient server;
	private static final int SENSATION_XML_RPC_PORT = 5000;

	private static final String TEST_URL = "https://www.google.com";
	private static final String TEST_TOKEN = "1900c5ed12";

	public String notify(String sensorID, String dateStamp, String value) {
		System.out.println("RFIDClient: " + sensorID + " " + dateStamp + " "
				+ value);

		String[] tagInfo = value.split(" ");
		String event = tagInfo[0];
		String tag = tagInfo[1];

		if (event.equals(RFIDSensor.EVENT_TAG_GAINED) && tag.equals(TEST_TOKEN)) {
			System.out.println("TEST_TOKEN recognized!");
			openBrowser(TEST_URL);
		}

		return "ok";
	}
	/**
	 * Displays the given <b>url</b> in the default browser.
	 * 
	 * @param url
	 *            , to open
	 */
	private static void openBrowser(String url) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				System.out.print("opening browser with URL: " + url + " ...");
				desktop.browse(new URI(url));
				System.out.println("ok");
			} catch (IOException | URISyntaxException e) {
				System.err.println(e);
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				System.out.print("opening browser with URL: " + url + " ...");
				runtime.exec("xdg-open " + url);
				System.out.println("ok");
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	public static void main(String[] args) {
		try {

			System.out
					.println("RFIDClient: attempting to start XML-RPC Server...");
			WebServer client = new WebServer(CLIENT_XML_RPC_PORT);
			client.addHandler("StableXMLRPCClient", new RFIDClient());
			client.start();
			System.out.println("RFIDClient: started successfully.");

			// register to sens-sation server
			System.out.println("RFIDClient: subscribing to sensor...");
			server = new XmlRpcClient("http://localhost:"
					+ SENSATION_XML_RPC_PORT + "/RPC2");

			Vector<String> params = new Vector<>();
			params.add("localhost");// ip
			params.add(RFIDSensor.SENSOR_ID);// sensorID
			params.add("" + CLIENT_XML_RPC_PORT);// port
			String result = (String) server.execute("GatewayXMLRPC.register",
					params);

			if (result != null && result.equals("done")) {
				System.out.println("OK");
			} else {
				System.err.println("Error registering RFIDClient!");
			}

		} catch (Exception exception) {
			System.err.println("RFIDClient: " + exception.getMessage());
		}
	}
}
