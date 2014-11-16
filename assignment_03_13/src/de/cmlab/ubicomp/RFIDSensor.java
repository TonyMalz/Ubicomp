/**
 * 
 */
package de.cmlab.ubicomp;

import java.io.IOException;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import com.phidgets.PhidgetException;
import com.phidgets.RFIDPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;
import com.phidgets.event.OutputChangeEvent;
import com.phidgets.event.OutputChangeListener;
import com.phidgets.event.TagGainEvent;
import com.phidgets.event.TagGainListener;
import com.phidgets.event.TagLossEvent;
import com.phidgets.event.TagLossListener;

/**
 * RFID Phidget Test Class
 * <p>
 * Waits for an RFID Phidget to connect via USB or webservice <br/>
 * <ul>
 * <li>Blinks on tag recognition
 * <li>Tries to open a browser window if {@link #TEST_TOKEN} was identified
 * </ul>
 * </p>
 */
public class RFIDSensor {

	private static final int SENSATION_XML_RPC_PORT = 5000;

	public static final String SENSOR_ID = "RFIDSensor";
	public static final String EVENT_TAG_GAINED = "TAG_GAINED";
	public static final String EVENT_TAG_LOST = "TAG_LOST";

	private static XmlRpcClient server;

	/**
	 * Main method<br/>
	 * 
	 * Tries to initialize the RFID phidget and starts listening for events
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		RFIDPhidget rfid;
		try {
			rfid = new RFIDPhidget();

			initPhidget(rfid);

			rfid.openAny();
			System.out.println("waiting for RFID attachment...");
			rfid.waitForAttachment();

			// connect to sens-sation server
			server = new XmlRpcClient("http://localhost:"
					+ SENSATION_XML_RPC_PORT + "/RPC2");

			// check server status
			System.out.println("Pinging server at port:"
					+ SENSATION_XML_RPC_PORT);
			try {
				String ok = (String) server.execute("SensorPort.ping",
						new Vector<String>());
				if (ok.equals("Server running")) {
					System.out.println("Server is running...");
				}
			} catch (Exception e) {
				System.err.println("XML request failed: " + e.getMessage());
				System.exit(0);
			}

			// register phidget via xml description
			System.out.println("Registering sensor...");
			try {
				Vector<String> xml = new Vector<>();
				xml.add("<Sensor id=\""
						+ SENSOR_ID
						+ "\" class=\"Presence\"><Description>RFID Reader, reports scanned tags (String)</Description><HardwareID /><Command /><LocationID>WE5/01.045</LocationID><Owner>Mutti</Owner><Comment /><AvailableSince>2005-01-01 10:00:00</AvailableSince><AvailableUntil>3014-12-01 12:00:00</AvailableUntil><SensorActivity activity=\"active\" /><NativeDataType>String</NativeDataType><MaximumValue>0.0</MaximumValue><MinimumValue>0.0</MinimumValue></Sensor>");

				String response = (String) server.execute(
						"SensorPort.updateSensor", xml);
				if (response.equals(SENSOR_ID)) {
					System.out.println("Sensor with id: " + SENSOR_ID
							+ " was registered successfully");
				}
			} catch (Exception e) {
				System.err.println("XML request failed:" + e.getMessage());
				System.exit(0);
			}

			System.out.println("\nPress any key to EXIT\n");
			System.out.println("Listening for events...\n");
			System.in.read();

			System.out.println("closing...");
			rfid.close();

			// unregistering sensor
			System.out.println("Unregistering sensor...");
			try {
				Vector<String> param = new Vector<>();
				param.add(SENSOR_ID);
				server.execute("SensorPort.unregisterSensor", param);
			} catch (Exception e) {
				System.err.println("XML request failed:" + e.getMessage());
			}

			// DONE
			System.out.println("DONE");

		} catch (PhidgetException | IOException e) {
			System.err.println(e);
		}
	}

	/**
	 * Adds Listeners to configure the given {@link RFIDPhidget}'s
	 * behavior:</br>
	 * <ul>
	 * <li>AttachListener: &nbsp; &nbsp; antenna on; LED off</li>
	 * <li>DetachListener: &nbsp;&nbsp;&nbsp;log event</li>
	 * <li>ErrorListener: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;log event</li>
	 * <li>TagGainListener: &nbsp; LED on->off ->on; eventually
	 * {@link #openBrowser(String)}</li>
	 * <li>TagLossListener: &nbsp; LED off</li>
	 * </ul>
	 * </br> If the tag's id equals <i>{@link #TEST_TOKEN}</i>,
	 * {@link #openBrowser()} will be executed with <i>{@link #TEST_URL}</i> as
	 * input.
	 * 
	 * @param rfid
	 *            : the RFIDPhidget object, which will get the listeners
	 */
	private static void initPhidget(RFIDPhidget rfid) {
		rfid.addAttachListener(new AttachListener() {
			@Override
			public void attached(AttachEvent ae) {
				try {
					((RFIDPhidget) ae.getSource()).setAntennaOn(true);
					((RFIDPhidget) ae.getSource()).setLEDOn(false);
				} catch (PhidgetException ex) {
				}
				System.out.println("attachment of " + ae);
			}
		});

		rfid.addDetachListener(new DetachListener() {
			@Override
			public void detached(DetachEvent ae) {
				System.out.println("detachment of " + ae);
			}
		});

		rfid.addErrorListener(new ErrorListener() {
			@Override
			public void error(ErrorEvent ee) {
				System.err.println("error event for " + ee);
			}
		});

		rfid.addTagGainListener(new TagGainListener() {
			@Override
			public void tagGained(TagGainEvent oe) {

				Vector<String> params = new Vector<>();
				params.add(SENSOR_ID);
				params.add("");
				params.add(EVENT_TAG_GAINED + " " + oe.getValue());
				try {
					if (server == null) {
						System.out
								.println("Server not yet ready, trying again..");
						Thread.sleep(500);
						if (server == null) {
							System.err.println("Server is gone!!");
							System.exit(0);
						}
					}
					server.execute("SensorPort.notify", params);
				} catch (Exception e) {
					System.err.println("XMLRPC Error:" + e.getMessage());
				}

				System.out.println("Tag Gained: " + oe.getValue() + " (Proto:"
						+ oe.getProtocol() + ")");
				try {
					blink(((RFIDPhidget) oe.getSource()));

				} catch (PhidgetException | InterruptedException e) {
					System.err.println(e);
				}
			}

			/**
			 * Executes a blink pattern
			 * 
			 * @param rfid
			 *            current RFID Phidget
			 * @throws PhidgetException
			 * @throws InterruptedException
			 */
			private void blink(RFIDPhidget rfid) throws PhidgetException,
					InterruptedException {
				rfid.setLEDOn(true);
				Thread.sleep(200);
				rfid.setLEDOn(false);
				Thread.sleep(100);
				rfid.setLEDOn(true);
			}

		});
		rfid.addTagLossListener(new TagLossListener() {
			@Override
			public void tagLost(TagLossEvent oe) {
				Vector<String> params = new Vector<>();
				params.add(SENSOR_ID);
				params.add("");
				params.add(EVENT_TAG_LOST + " " + oe.getValue());
				try {
					server.execute("SensorPort.notify", params);
				} catch (XmlRpcException | IOException e) {
					System.err.println("XMLRPC Error:" + e.getMessage());
				}

				try {
					((RFIDPhidget) oe.getSource()).setLEDOn(false);
				} catch (PhidgetException e) {
				}
				System.out.println("Tag Lost: " + oe.getValue() + " (Proto:"
						+ oe.getProtocol() + ")");
				System.out.println();
			}
		});

		rfid.addOutputChangeListener(new OutputChangeListener() {
			@Override
			public void outputChanged(OutputChangeEvent oe) {
				System.out.println(oe);
			}
		});
	}
}
