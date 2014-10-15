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
 * @author tony
 * 
 */
public class RFID {

	/**
	 * @param args
	 * @throws PhidgetException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		RFIDPhidget rfid;
		try {
			rfid = new RFIDPhidget();

			initPhidget(rfid);

			rfid.openAny();

			System.out.println("waiting for RFID attachment...");
			rfid.waitForAttachment(1000);

			System.out.println("Serial: " + rfid.getSerialNumber());
			System.out.println("Outputs: " + rfid.getOutputCount());

			System.out.println("Outputting events.  Input to stop.");
			System.in.read();

			System.out.print("closing...");
			rfid.close();
			rfid = null;
			System.out.println(" ok");

		} catch (PhidgetException e) {
			System.err.println(e);
		}
	}

	/**
	 * @param rfid
	 */
	private static void initPhidget(RFIDPhidget rfid) {
		rfid.addAttachListener(new AttachListener() {
			public void attached(AttachEvent ae) {
				try {
					((RFIDPhidget) ae.getSource()).setAntennaOn(true);
					((RFIDPhidget) ae.getSource()).setLEDOn(true);
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
				System.out.println("error event for " + ee);
			}
		});

		rfid.addTagGainListener(new TagGainListener() {
			public void tagGained(TagGainEvent oe) {
				try {
					// blink
					((RFIDPhidget) oe.getSource()).setLEDOn(true);
					Thread.sleep(200);
					((RFIDPhidget) oe.getSource()).setLEDOn(false);
					Thread.sleep(100);
					((RFIDPhidget) oe.getSource()).setLEDOn(true);

					if (oe.getValue().equals("0102ac837c")) {
						String url = "https://www.google.com";
						openBrowser(url);
					}

				} catch (PhidgetException | InterruptedException e) {
				}
				System.out.println("Tag Gained: " + oe.getValue() + " (Proto:"
						+ oe.getProtocol() + ")");
			}

		});
		rfid.addTagLossListener(new TagLossListener() {
			public void tagLost(TagLossEvent oe) {
				try {
					((RFIDPhidget) oe.getSource()).setLEDOn(false);
				} catch (PhidgetException e) {
				}
				System.out.println(oe);
			}
		});
		rfid.addOutputChangeListener(new OutputChangeListener() {
			public void outputChanged(OutputChangeEvent oe) {
				System.out.println(oe);
			}
		});

		// How to write a tag:
		// rfid.write("A TAG!!", RFIDPhidget.PHIDGET_RFID_PROTOCOL_PHIDGETS,
		// false);
	}

	/**
	 * @param url
	 */
	private static void openBrowser(String url) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
