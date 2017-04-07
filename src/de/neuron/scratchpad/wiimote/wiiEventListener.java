package de.neuron.scratchpad.wiimote;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import com.jme.math.Vector3f;

import com.jmex.physics.DynamicPhysicsNode;

public class wiiEventListener implements Runnable {
	int norm_x = 125;

	int norm_y = 130;

	int norm_z = 156;

	DynamicPhysicsNode accNode;

	WiiAccHandler accHandler;

	WiiIrAPosHandler aposHandler;

	private final ServerSocket server;

	public wiiEventListener(int port) throws IOException {
		server = new ServerSocket(port);
	}

	public void setAccNode(DynamicPhysicsNode _accNode) {
		accNode = _accNode;
	}

	public void setAccHandler(WiiAccHandler ah) {
		this.accHandler = ah;
	}

	public void setIrAPosHandler(WiiIrAPosHandler ah) {
		this.aposHandler = ah;
	}

	public void run() {
		startServing();
	}

	private void startServing() {
		while (true) {
			Socket client = null;
			try {
				client = server.accept();
				handleConnection(client);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (client != null)
					try {
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}

	private void handleConnection(Socket client) throws IOException {
		System.out.println("New Conn: " + client);
		InputStream in = client.getInputStream();
		OutputStream out = client.getOutputStream();
		byte inbuff[] = new byte[4096 * 4];
		int len = 0;
		String data;
		String event[];
		String lines[];
		long lasttime = 0l;
		long now = 0l;
		long freq = 30;
		int i;
		while ((len = in.read(inbuff)) != -1) {
			lines = new String(inbuff).substring(0, len).split("\\n");
			// System.out.println("Read: " + data);
			now = System.currentTimeMillis();
			for (i = 0; i < lines.length; i++) {
				data = lines[i];
				if (data.startsWith("WM_ACC") && (now - lasttime) > freq) {
					lasttime = now;
					event = data.split("\\t");
					int x = Integer.parseInt(event[1]) - norm_x;
					int y = Integer.parseInt(event[2]) - norm_y;
					int z = Integer.parseInt(event[3]) - norm_z;
					// System.out.println("Read: " + x + " " + y + " " + z);
//					if (accNode != null) {
//						accNode.addForce(new Vector3f((float) x * 3500, 0f,
//								(float) y * -3500));
//					}
					if (accHandler != null){
						accHandler.handleWiiAcc(x, y, z);
					}
				} 
				if (data.startsWith("ABS_POS")) {
					event = data.split("\\t");
					float y = Float.parseFloat(event[1]);
					float x = Float.parseFloat(event[2]);
//					System.out.println("Read apos: " + x + " " + y);
					if (aposHandler != null){
						aposHandler.handleWiiAPos(x, y);
					}
				}
			}
		}
	}

}
