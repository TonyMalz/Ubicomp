package de.cmlab.ubicomp;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcClient;

/**
 * Sens-ation client for RFID-phidget
 * <p>
 * <ul>
 * <li>Starts local XML-RPC Server listening on port 12345 to receive
 * notifications from sens-ation
 * <li>Connects to sens-ation server on port 5000
 * <li>Checks whether RFID sensor is available
 * <li>Subscribes to RFID sensor at sens-ation
 * <li>Reacts to the values of RFID sensor received from sens-ation server
 * </ul>
 * </p>
 */
public class RFIDClient {
	private static int CLIENT_XML_RPC_PORT = 12345;

	private static XmlRpcClient server;
	private static final int SENSATION_XML_RPC_PORT = 5000;

	private static final String TEST_URL = "https://www.google.com";
	private static final String TEST_TOKEN = "1900c5ed12";

	/**
	 * This method is called by sens-ation, if the sensor got new values.<br>
	 * If the
	 * 
	 * 
	 * @param sensorID
	 * @param dateStamp
	 * @param value
	 * @return string "ok"
	 */
	public String notify(String sensorID, String dateStamp, String value) {
		System.out.println("RFIDClient: " + sensorID + " " + dateStamp + " "
				+ value);

		String[] tagInfo = value.split(" ");
		String event = tagInfo[0];
		String tag = tagInfo[1];

		if (event.equals(RFIDSensor.EVENT_TAG_GAINED) && tag.equals(TEST_TOKEN)) {
			System.out.println("RFIDClient: TEST_TOKEN recognized!");
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
				System.out.print("RFIDClient: opening browser with URL: " + url
						+ " ...");
				desktop.browse(new URI(url));
				System.out.println("ok");
			} catch (IOException | URISyntaxException e) {
				System.err.println(e);
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				System.out.print("RFIDClient: opening browser with URL: " + url
						+ " ...");
				runtime.exec("xdg-open " + url);
				System.out.println("ok");
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	/**
	 * Main Method
	 * <p>
	 * <ul>
	 * <li>Starts local XML-RPC Server listening on port
	 * {@link #CLIENT_XML_RPC_PORT} to receive notifications from sens-ation
	 * <li>Connects to sens-ation server on port{@link #SENSATION_XML_RPC_PORT}
	 * <li>Checks whether RFID sensor is available
	 * <li>Subscribes to RFID sensor at sens-ation
	 * <li>Reacts to the values of RFID sensor received from sens-ation server
	 * </ul>
	 * </p>
	 * 
	 */
	public static void main(String[] args) {
		try {

			System.out
					.println("RFIDClient: attempting to start XML-RPC Server...");
			WebServer client = new WebServer(CLIENT_XML_RPC_PORT);
			client.addHandler("StableXMLRPCClient", new RFIDClient());
			client.start();
			System.out.println("RFIDClient: started successfully.");

			// connect to sens-ation server
			System.out.println("RFIDClient: looking for RFID sensor...");
			server = new XmlRpcClient("http://localhost:"
					+ SENSATION_XML_RPC_PORT + "/RPC2");

			// check whether RFID sensor is available
			{
				Vector<String> params = new Vector<>();
				Object sensors = server.execute(
						"GatewayXMLRPC.getAllSensorsVector", params);
				if (!(sensors instanceof Vector) || sensors == null
						|| !((Vector) sensors).contains(RFIDSensor.SENSOR_ID)) {
					System.err
							.println("RPCClient: RFIDSensor not found... quitting!");
					System.exit(0);
				}
				System.out.println("OK");
			}

			System.out.println("RFIDClient: subscribing to sensor...");
			// register client
			{
				Vector<String> params = new Vector<>();
				params.add("127.0.0.1");// ip
				params.add(RFIDSensor.SENSOR_ID);// sensorID
				params.add("" + CLIENT_XML_RPC_PORT);// port
				String result = (String) server.execute(
						"GatewayXMLRPC.register", params);

				if (result != null && result.equals("done")) {
					System.out.println("OK");
				} else {
					System.err.println("Error registering RFIDClient!");
					System.exit(0);
				}
			}

		} catch (Exception exception) {
			System.err.println("RFIDClient: " + exception.getMessage());
			System.exit(0);
		}
	}
}
