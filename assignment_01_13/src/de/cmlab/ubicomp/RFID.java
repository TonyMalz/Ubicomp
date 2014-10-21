/**
 * 
 */
package de.cmlab.ubicomp;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
public class RFID {

	private static final String TEST_URL = "https://www.google.com";
	private static final String TEST_TOKEN = "1900c5ed12";

	/**
	 * Main method
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

			printPhidgetInfo(rfid);

			System.out.println("\nPress any key to EXIT\n");
			System.out.println("Listening for events...\n");
			System.in.read();

			System.out.print("closing...");
			rfid.close();
			System.out.println(" ok");

		} catch (PhidgetException | IOException e) {
			System.err.println(e);
		}
	}

	/**
	 * Display the phidget's serial number and available outputs
	 * 
	 * @param rfid
	 * @throws PhidgetException
	 */
	private static void printPhidgetInfo(RFIDPhidget rfid)
			throws PhidgetException {
		System.out.println("Serial: " + rfid.getSerialNumber());
		System.out.println("Outputs: " + rfid.getOutputCount());
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
			public void detached(DetachEvent ae) {
				System.out.println("detachment of " + ae);
			}
		});

		rfid.addErrorListener(new ErrorListener() {
			public void error(ErrorEvent ee) {
				System.err.println("error event for " + ee);
			}
		});

		rfid.addTagGainListener(new TagGainListener() {
			public void tagGained(TagGainEvent oe) {
				System.out.println("Tag Gained: " + oe.getValue() + " (Proto:"
						+ oe.getProtocol() + ")");
				try {
					blink(((RFIDPhidget) oe.getSource()));
					if (oe.getValue().equals(TEST_TOKEN)) {
						System.out.println("TEST_TOKEN recognized!");
						openBrowser(TEST_URL);
					}

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
			public void tagLost(TagLossEvent oe) {
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
			public void outputChanged(OutputChangeEvent oe) {
				System.out.println(oe);
			}
		});
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
}
