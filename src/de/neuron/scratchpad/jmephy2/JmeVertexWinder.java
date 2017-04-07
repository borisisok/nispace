package de.neuron.scratchpad.jmephy2;

import java.io.File;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.net.URL;

import java.util.logging.Level;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import org.dom4j.io.SAXReader;

public class JmeVertexWinder {
	private Document doc;

	private byte[] data;

	static final String INDEX_SEPERATOR = " ";

	public JmeVertexWinder() {
	}

	public JmeVertexWinder(URL model) {
		try {
			parseWithSAX(model);
			//processElement(getDoc().getRootElement());
			serializeToXml();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a document from a file.
	 * 
	 * @param aFile
	 *            the data source
	 * @throw a org.dom4j.DocumentExcepiton occurs on parsing failure.
	 */
	public void parseWithSAX(File aFile) throws DocumentException {
		SAXReader xmlReader = new SAXReader();
		this.doc = xmlReader.read(aFile);
	}

	public void parseWithSAX(URL url) throws DocumentException {
		SAXReader xmlReader = new SAXReader();
		try {
			this.doc = xmlReader.read(url.openStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processElement(Element element) throws DocumentException {
		Iterator nodeIt = element.nodeIterator();
		while (nodeIt.hasNext()) {
			Node node = (Node) nodeIt.next();
			if (node.getName() != null && node.getName().equals("node")) {
				System.out.println("Node name: " + node.getName());
				System.out.println("Node path: " + node.getPath());
				java.util.List tmpnodes = node.selectNodes("mesh/index/@data");
				// fallback to node without parent mesh node
				if (tmpnodes.size() == 0) 
					tmpnodes = node.selectNodes("index/@data");
				Node index = (Node) tmpnodes.get(0);
				String rawIndex[] = index.getStringValue().split(" ");
				// build pointarray
				if (isWindingConsistent(rawIndex)) {
	//				index.setText(reverseWinding(rawIndex));

				}

			}
		}
	}

	private boolean isWindingConsistent(String rawIndex[]) {
		int intIndex[] = new int[rawIndex.length];
		for (int i = 0; i < rawIndex.length; i++) {
			intIndex[i] = Integer.valueOf(rawIndex[i]).intValue();
		}
		// seek common edges
		for (int i = 0; i < rawIndex.length; i += 3) {
			intIndex[i] = Integer.valueOf(rawIndex[i]).intValue();
			System.out.println(intIndex[i] + ", " + intIndex[i + 1] + ", "
					+ intIndex[i + 2]);
			for (int x = 0; x < rawIndex.length; x += 3) {
				if (x == i)
					continue; // skip current poly
				if ((intIndex[i] == intIndex[x] && intIndex[i + 1] == intIndex[x + 1])
						|| (intIndex[i] == intIndex[x + 1] && intIndex[i + 1] == intIndex[x + 2])
						|| (intIndex[i] == intIndex[x + 2] && intIndex[i + 1] == intIndex[x])
						||

						(intIndex[i + 1] == intIndex[x] && intIndex[i + 2] == intIndex[x + 1])
						|| (intIndex[i + 1] == intIndex[x + 1] && intIndex[i + 2] == intIndex[x + 2])
						|| (intIndex[i + 1] == intIndex[x + 2] && intIndex[i + 2] == intIndex[x])
						||

						(intIndex[i + 2] == intIndex[x] && intIndex[i] == intIndex[x + 1])
						|| (intIndex[i + 2] == intIndex[x + 1] && intIndex[i] == intIndex[x + 2])
						|| (intIndex[i + 2] == intIndex[x + 2] && intIndex[i] == intIndex[x])) {
					System.out.println("found one identical line in poly: " + i
							+ " and poly: " + x);
					return false;
				}

			}
		}
		return true;
	}

	private String reverseWinding(String rawIndex[]) {
		StringBuffer reversedIndex = new StringBuffer();
		for (int i = 0; i < rawIndex.length; i += 3) {
			reversedIndex.append(rawIndex[i + 2]);
			reversedIndex.append(INDEX_SEPERATOR);
			reversedIndex.append(rawIndex[i + 1]);
			reversedIndex.append(INDEX_SEPERATOR);
			reversedIndex.append(rawIndex[i]);
			if (i < rawIndex.length + 2)
				reversedIndex.append(INDEX_SEPERATOR);
		}
		return reversedIndex.toString();
	}

	private void serializeToXml() {
		OutputStream out = new ByteArrayOutputStream();
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding("UTF-8");
		try {
			XMLWriter writer = new XMLWriter(out, outformat);
			writer.write(this.doc);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		data = out.toString().getBytes();
		System.out.println("data: \n"+out.toString());
	}

	public Document getDoc() {
		return doc;
	}

	public InputStream openStream() {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		return (InputStream) bis;
	}

	public static void main(String[] args) {
		try {
			JmeVertexWinder jVw = new JmeVertexWinder();
			jVw.parseWithSAX(new File(
					"/home/boris/devel/jme-phy-cvs/jmephysics/data/test2.xml"));
			jVw.processElement(jVw.getDoc().getRootElement());
			jVw.serializeToXml();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
