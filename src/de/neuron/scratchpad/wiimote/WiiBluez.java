package de.neuron.scratchpad.wiimote;

import com.appliancestudio.jbluez.*;
import java.util.*;

public class WiiBluez {

	private BlueZ bz;

	// Local HCI device ID. This is the same as that reported by the
	// hciconfig tool provided by BlueZ.
	private int hciDevID = 0;

	// Device descriptor. This is the reference to an open HCI device.
	private int dd;

	// The handle of an established HCI connection to a remote BT device.
	int handle;

	private String wii_addr = "00:19:1D:86:3A:C9"; // my wiimote address

	public static void main(String[] args) {
		WiiBluez i = new WiiBluez();
//		i.inquire();
		i.readFeatures();
	}

	public void inquire() {
		bz = new BlueZ();
		InquiryInfo info;

		try {
			info = bz.hciInquiry(hciDevID);
			printResults(info);
		} catch (BlueZException bze) {
			System.out.println(bze.getMessage());
		}

	}

	public void printResults(InquiryInfo info) {
		Vector devices = info.devices();
		System.out.println(devices.size() + " devices found.\n");

		for (Enumeration e = devices.elements(); e.hasMoreElements();) {
			InquiryInfoDevice dev = (InquiryInfoDevice) e.nextElement();
			BTAddress bdaddr = dev.bdaddr;
			System.out.println("\t" + bdaddr.toString());
			System.out.println("\t" + dev.toString());
		}
	}

	public void readFeatures() {
		bz = new BlueZ();
		HCIDeviceInfo info;
		HCIFeatures features;
		// remember: run as root, otherwise connect fails!
		try {
			dd = bz.hciOpenDevice(hciDevID);
			System.out.println("dd: "+dd+"\n");
			handle = bz.hciCreateConnection(dd, wii_addr, 8, 0,
					(short) 0, 60000);
			features = bz.hciReadRemoteFeatures(dd, handle, 25000);
			bz.hciDisconnect(dd, handle, (short) 13, 10000);
			bz.hciCloseDevice(dd);

			System.out.println("Remote features:\n");
			System.out.println(features.toString() + "\n");
		} catch (BlueZException bze) {
			System.out.println(bze.getMessage());
			bz.hciCloseDevice(dd);
		}

	}

}
