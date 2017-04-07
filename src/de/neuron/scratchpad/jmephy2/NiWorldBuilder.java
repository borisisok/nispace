/*
 * Copyright (c) 2003-2006 neuron interworks Ltd.
 */

package de.neuron.scratchpad.jmephy2;

import java.util.*;
import java.io.*;
import java.net.*;

import com.jme.math.FastMath;

import com.jme.scene.Spatial;
import com.jme.scene.Geometry;
import com.jme.scene.Node;

import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import com.jme.bounding.BoundingBox;

import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsNode;
import com.jmex.physics.geometry.PhysicsBox;
import com.jmex.physics.Joint;
import com.jmex.physics.RotationalJointAxis;
import com.jmex.physics.TranslationalJointAxis;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.material.Material;
import com.jmex.physics.util.SimplePhysicsGame;

/**
 * @author boris <boris@neuron-interworks.de>
 */

public class NiWorldBuilder {

	final static String FEATURE_GEO_FIX_LOAD_MIRROR = "geo.fix.loadmirror";

	final static String FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO = "geo.fix.nocullinfo";

	final static String TYPE_JOINT = "pj";

	final static String TYPE_PHYSICS_DYNAMIC_AUTOBOUND = "pda";

	final static String TYPE_PHYSICS_DYNAMIC_TRIMESH = "pdt";

	final static String TYPE_PHYSICS_STATIC_AUTOBOUND = "psa";

	final static String TYPE_PHYSICS_STATIC_TRIMESH = "pst";

	private HashMap props = new HashMap();

	private HashMap physicsObjects = new HashMap();

	private HashMap skeletons = new HashMap();

	private SimplePhysicsGame physicsGame = null;

	int level = 0;

	private JointBuilder jb = null;

	/** Creates a new instance of NiWorldBuilder */
	public NiWorldBuilder() {
	}

	public NiWorldBuilder(String jointlist[]) {
		setJointBuilder(new JointBuilder(jointlist));
	}

	public NiWorldBuilder(URL jointfile) {
		setJointBuilder(new JointBuilder(jointfile));
	}

	public Spatial buildFrom(Spatial source) {
		createPhysics(source);
		if (jb != null)
			skeletons = jb.getSkeletons();
		Node target = null;
		return target;
	}

	public Spatial createPhysics(Spatial source) {
		System.out.println("Level " + level + " Sourcenode: "
				+ source.getName());

		level++;
		if (source instanceof Geometry) {
			System.out.println("Is " + "a Geometry");

			if (isEnabled(FEATURE_GEO_FIX_LOAD_MIRROR)) {
				// fix bad / mirrored geometrys from xml / blender importer
				source.setLocalScale(new Vector3f(1f, 1f, -1f));
				source.setModelBound(new BoundingBox());
				source.updateModelBound();
				source.updateWorldBound();
			}

		} else if (source instanceof Node) {
			System.out.println("Is a Node");
			recurseChildren((Node) source);
		} else if (source instanceof Spatial) {
			System.out.println("Is a Spatial");
		} else {
			System.out.println("Is of unknown type");
		}

		level--;

		// NodeMetaInfo nmi = new NodeMetaInfo(source.getName());
		if (source.getName().startsWith(TYPE_PHYSICS_DYNAMIC_TRIMESH)) {
			// physics dynamic node with trimesh bounds
			source = makeDynamicPhysicsNode((Node) source, true);
			physicsObjects
					.put(getPhysicsNodeName(source).toLowerCase(), source);
		} else if (source.getName().startsWith(TYPE_PHYSICS_DYNAMIC_AUTOBOUND)) {
			// physics dynamic node with auto bounds
			source = makeDynamicPhysicsNode((Node) source, false);
			physicsObjects
					.put(getPhysicsNodeName(source).toLowerCase(), source);
		} else if (source.getName().startsWith(TYPE_PHYSICS_STATIC_TRIMESH)) {
			// physics static node with trimesh bounds
			source = makeStaticPhysicsNode((Node) source, true);
			physicsObjects
					.put(getPhysicsNodeName(source).toLowerCase(), source);
		} else if (source.getName().startsWith(TYPE_PHYSICS_STATIC_AUTOBOUND)) {
			// physics static node with auto bounds
			source = makeStaticPhysicsNode((Node) source, false);
			physicsObjects
					.put(getPhysicsNodeName(source).toLowerCase(), source);
		}

		// at the last stage we pass the nodes to the joint factory (physics and
		// joint meta objects which will be removed from scene after usage)
		if (source instanceof DynamicPhysicsNode
				|| source.getName().startsWith(TYPE_JOINT)) {
			System.out.println("Joint time!");
			if (jb != null)
				jb.handleNode((Node) source);
		}

		Node target = null;
		return target;
	}

	private String getPhysicsNodeName(Spatial spatial) {
		return spatial.getName().substring(3, spatial.getName().length());
	}

	public Node makeDynamicPhysicsNode(Node source, boolean trimeshBoxing) {
		System.out.println("Test goes here: localscale: "
				+ source.getLocalScale());
		Node tmp = source.getParent();
		source.removeFromParent();

		DynamicPhysicsNode dpNode = physicsGame.getPhysicsSpace()
				.createDynamicNode();

		source.setLocalScale(source.getLocalScale()); // avoid scale = 0
		ArrayList childs = ((Node) source).getChildren();
		Iterator childIt = childs.iterator();
		while (childIt.hasNext()) {
			Spatial child = (Spatial) childIt.next();
			System.out.println("Test goes here: localscale from child: "
					+ child.getLocalScale());
			// child.getLocalScale().;
			// child.setLocalScale(child.getLocalScale());
			child.setLocalScale(1f);
		}
		Vector3f sourceTrans = ((Node) source).getLocalTranslation();
		Quaternion sourceRot = ((Node) source).getLocalRotation();
		// System.out.println("Test goes here: localrot: parent: "+ ((Node)
		// source).getParent().getLocalRotation());
		System.out.println("Test goes here: localrot: child: "
				+ ((Node) source).getLocalRotation());

		((Node) source).setLocalTranslation(new Vector3f(0f, 0f, 0f));
		((Node) source).setLocalRotation(sourceRot.inverse());
		dpNode.setLocalTranslation(sourceTrans);
		// dpNode.setLocalRotation(new Quaternion(sourceRot.y, sourceRot.x,
		// sourceRot.z, sourceRot.w));

		dpNode.attachChild(source);
		dpNode.setName(((Node) source).getName());
		((PhysicsNode) dpNode).generatePhysicsGeometry(trimeshBoxing);
		dpNode.setMaterial(Material.WOOD);
		dpNode.computeMass();
		dpNode.updateRenderState();
		tmp.attachChild(dpNode);
		return dpNode;

	}

	public Node makeStaticPhysicsNode(Node source, boolean trimeshBoxing) {
		System.out.println("makeStaticPhysicsNode()");
		Node tmp = source.getParent();
		StaticPhysicsNode dpNode = physicsGame.getPhysicsSpace()
				.createStaticNode();
		source.removeFromParent();
		((Node) source).getChild(0).setLocalScale(1f);
		dpNode.attachChild(source);
		dpNode.setName(((Node) source).getName());
		((PhysicsNode) dpNode).generatePhysicsGeometry(trimeshBoxing);
		dpNode.setMaterial(Material.WOOD);
		dpNode.updateRenderState();
		tmp.attachChild(dpNode);
		return dpNode;
	}

	public void recurseChildren(Node node) {
		ArrayList spatials = node.getChildren();
		if (spatials == null)
			return;
		Object spa[] = spatials.toArray(); // avoid conc.-mod exceptions while
		// working on the list
		for (int i = 0; i < spa.length; i++) {
			this.createPhysics((Spatial) spa[i]);
		}
	}

	public void enable(String feature) {
		props.put(feature, new Boolean(true));
	}

	public void disable(String feature) {
		props.remove(feature);
	}

	public boolean isEnabled(String feature) {
		if (props.containsKey(feature))
			return true;
		return false;
	}

	public void setPhysicsGame(SimplePhysicsGame _spg) {
		this.physicsGame = _spg;
	}

	public void setJointBuilder(JointBuilder _jb) {
		this.jb = _jb;
	}

	private class NodeMetaInfo {
		HashMap keymap = new HashMap();

		public NodeMetaInfo(String data) {
			String[] pairs = data.split(",");
			for (int i = 0; i < pairs.length; i++) {
				if (isEnabled(FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO)) {
					String valueToRemove = ".nocull";
					if (pairs[i].endsWith(valueToRemove)) {
						pairs[i] = pairs[i].substring(0, pairs[i].length()
								- valueToRemove.length());
					}
				}
				System.out.println("pair: " + pairs[i]);
			}
		}

	}

	private class JointBuilder {
		final String OBJ_FROM = "fromObj";

		final String OBJ_TO = "toObj";

		final String OBJ_ANCHOR = "ancObj";

		HashMap jointFromMap = new HashMap();

		HashMap jointToMap = new HashMap();

		HashMap jointFromToMap = new HashMap();

		HashMap rotJointsToParent = new HashMap();

		HashMap rotJointsToChild = new HashMap();

		HashMap transJointsToParent = new HashMap();

		HashMap transJointsToChild = new HashMap();

		public JointBuilder(String data[]) {

			for (int i = 0; i < data.length; i++) {
				parseLine(data[i]);
			}

		}

		public JointBuilder(URL url) {

			try {
				String data[] = getFileContent(url).split("\\n");
				for (int i = 0; i < data.length; i++) {
					System.out.println("LINE: " + data[i]);
					parseLine(data[i]);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public String getFileContent(URL url) throws IOException {
			BufferedInputStream bis = new BufferedInputStream(url.openStream());
			StringBuffer sbuf = new StringBuffer();
			while (bis.available() > 0) {
				byte bbuf[] = new byte[bis.available()];
				bis.read(bbuf);
				System.out.println("DA BUFFA: " + new String(bbuf));
				sbuf.append(new String(bbuf));
			}
			return sbuf.toString();
		}

		private void parseLine(String line) {
			String[] pairs = line.split(",");
			HashMap keyMap = new HashMap();
			for (int pi = 0; pi < pairs.length; pi++) {
				String keyVal[] = pairs[pi].split("=");
				keyMap.put(keyVal[0], keyVal[1]);
				System.out.println("pair: " + pairs[pi]);
			}
			System.out.println("?????????????????"
					+ (String) keyMap.get("from"));
			// jointFromMap.put( (String) keyMap.get("from"),keyMap );
			// jointToMap.put( (String) keyMap.get("to"),keyMap );
			jointFromToMap.put(((String) keyMap.get("from")) + "_"
					+ ((String) keyMap.get("to")), keyMap);
		}

		public HashMap getSkeletons() {
			HashMap result = new HashMap();
			HashMap skelObjects = new HashMap();
			Iterator jointIt = jointFromToMap.keySet().iterator();
			String jointname;
			String objname[];

			while (jointIt.hasNext()) {
				jointname = ((String) jointIt.next());
				System.out.println("JOINT: " + jointname);
				objname = jointname.split("_");

				BodyPart parent;
				BodyPart current;
				if (!skelObjects.containsKey(objname[0])) {
					parent = new BodyPart();
					parent.setName(objname[0]);
					parent.setJmeObj((PhysicsNode) physicsObjects
							.get(objname[0]));
					skelObjects.put(objname[0], parent);
				} else {
					parent = (BodyPart) skelObjects.get(objname[0]);
				}
				if (!skelObjects.containsKey(objname[1])) {
					current = new BodyPart();
					current.setName(objname[1]);
					current.setJmeObj((PhysicsNode) physicsObjects
							.get(objname[1]));
					skelObjects.put(objname[1], current);
				} else {
					current = (BodyPart) skelObjects.get(objname[1]);
				}
				current.addParent(parent);
				parent.addChild(current);
				// attach joints to body part
				if (rotJointsToChild.containsKey(parent.getJmeObj())) {
					System.out.println("?"
							+ (ArrayList) ((HashMap) rotJointsToChild
									.get(parent.getJmeObj())).get(current
									.getJmeObj()));
					parent.setChildJoints(current.getJmeObj(),
							(ArrayList) ((HashMap) rotJointsToChild.get(parent
									.getJmeObj())).get(current.getJmeObj())

					);
				}
				// if (rotJointsToParent.containsKey(current.getJmeObj())) {
				// current.setParentJoints((ArrayList) rotJointsToParent
				// .get(current.getJmeObj()));
				// }

			}

			Iterator skelIt = skelObjects.keySet().iterator();
			while (skelIt.hasNext()) {
				BodyPart skelObj = (BodyPart) skelObjects.get((String) skelIt
						.next());
				if (skelObj.getParents().isEmpty()) {
					System.out.println("SKELROOT: " + skelObj.getName());
					result.put(skelObj.getName(), skelObj);
				}
			}

			return result;
		}

		public void handleNode(Node node) {

			String nodename = node.getName();

			// get rid of all type meta information in the nodenames
			if (node.getName().startsWith(TYPE_PHYSICS_DYNAMIC_TRIMESH)
					|| node.getName()
							.startsWith(TYPE_PHYSICS_DYNAMIC_AUTOBOUND)
					|| node.getName().startsWith(TYPE_PHYSICS_STATIC_TRIMESH)
					|| node.getName().startsWith(TYPE_PHYSICS_STATIC_AUTOBOUND)) {
				// make clean nodename, that means we strip of the type
				// information and lowercase the rest
				nodename = nodename.substring(3, node.getName().length())
						.toLowerCase();
			} else if (node.getName().startsWith(TYPE_JOINT)) {
				node.removeFromParent(); // remove metaobj from scene
				System.out.println("found joint metaobject: " + nodename);
				nodename = nodename.substring(2).toLowerCase(); // make nodename
				// equal to the
				// used key in
				// jointFromToMap
			}

			System.out.println("handleNode: " + nodename);

			HashMap joint = null;

			Iterator jointIt = jointFromToMap.keySet().iterator();
			String jointname;
			ArrayList modifiedJoints = new ArrayList();
			while (jointIt.hasNext()) {
				jointname = ((String) jointIt.next());
				System.out.println("Checking joint : " + jointname);

				if (jointname.equals(nodename)) {
					System.out.println("Found joint metaobj: " + jointname);
					joint = (HashMap) jointFromToMap.get(jointname);
					if (!joint.containsKey(OBJ_ANCHOR)) // node is already set
					{
						System.out.println("Put OBJ_ANCHOR : " + nodename);
						joint.put(OBJ_ANCHOR, node); // put obj ref to key
						// val map
						modifiedJoints.add(joint);
					}
				} else if (jointname.startsWith(nodename)) {
					System.out.println("Found matching joint : " + jointname);
					joint = (HashMap) jointFromToMap.get(jointname);
					if (!joint.containsKey(OBJ_FROM)) // node is already set
					{
						System.out.println("Put OBJ_FROM : " + nodename);
						joint.put(OBJ_FROM, node); // put obj ref to key val
						// map
						modifiedJoints.add(joint);
					}
				} else if (jointname.endsWith(nodename)) {
					joint = (HashMap) jointFromToMap.get(jointname);
					if (!joint.containsKey(OBJ_TO)) // node is already set
					{
						System.out.println("Put OBJ_TO : " + nodename);
						joint.put(OBJ_TO, node); // put obj ref to key val
						// map
						modifiedJoints.add(joint);
					}
				}
			}

			Iterator modJointIt = modifiedJoints.iterator();
			while (modJointIt.hasNext()) {
				joint = (HashMap) modJointIt.next();
				// is joint data complete (from,to and anchor) ?
				if (joint != null && joint.containsKey(OBJ_FROM)
						&& joint.containsKey(OBJ_TO)
						&& joint.containsKey(OBJ_ANCHOR)) {
					// looks good, lets try to build a nice joint here
					System.out
							.println("looks very good, building joint !!!!!!!!!!!!!!!!"
									+ joint.get(OBJ_FROM));
					System.out
							.println("looks very good, building joint !!!!!!!!!!!!!!!!"
									+ joint.get(OBJ_TO));

					int axis = 0;
					int current = 0;
					String dirsRot[] = new String[0];
					String dirsTrans[] = new String[0];
					String minRotLims[] = new String[0];
					String maxRotLims[] = new String[0];
					String minTransLims[] = new String[0];
					String maxTransLims[] = new String[0];

					if (joint.containsKey("transDir")) {
						// get "attributes" for all translational axis
						dirsTrans = ((String) joint.get("transDir")).split(":");
						axis += dirsTrans.length;
						minTransLims = ((String) joint.get("transMin"))
								.split(":");
						maxTransLims = ((String) joint.get("transMax"))
								.split(":");
					}
					if (joint.containsKey("rotDir")) {
						// get "attributes" for all rotational axis
						dirsRot = ((String) joint.get("rotDir")).split(":");
						axis += dirsRot.length;
						minRotLims = ((String) joint.get("rotMin")).split(":");
						maxRotLims = ((String) joint.get("rotMax")).split(":");
					}

					// prepare lists where all refs to all used parent and child
					// rotational joints where stored
					/*
					 * List rotJointsToParentList; if
					 * (rotJointsToParent.containsKey(joint.get(OBJ_FROM))) {
					 * rotJointsToParentList = (ArrayList) rotJointsToParent
					 * .get(joint.get(OBJ_FROM)); } else { rotJointsToParentList =
					 * new ArrayList();
					 * rotJointsToParent.put(joint.get(OBJ_FROM),
					 * rotJointsToParentList); }
					 */List rotJoints;
					if (rotJointsToChild.containsKey(joint.get(OBJ_FROM))) {

						if (((HashMap) rotJointsToChild
								.get(joint.get(OBJ_FROM))).containsKey(joint
								.get(OBJ_TO))) {
							rotJoints = (ArrayList) ((HashMap) rotJointsToChild
									.get(joint.get(OBJ_FROM))).get(joint
									.get(OBJ_TO));
						} else {

							rotJoints = new ArrayList();
							((HashMap) rotJointsToChild
									.get(joint.get(OBJ_FROM))).put(joint
									.get(OBJ_TO), rotJoints);
						}

					} else {
						HashMap children = new HashMap();
						rotJoints = new ArrayList();
						children.put(joint.get(OBJ_TO), rotJoints);
						rotJointsToChild.put(joint.get(OBJ_FROM), children);
					}

					// now create all joints (due to limitations of only one
					// angluar limit axis we create an own joint for each axis
					// and link them together in place)
					for (int i = 0; i < dirsRot.length; i++) {
						final Joint tmpJoint = physicsGame.getPhysicsSpace()
								.createJoint();
						tmpJoint.setSpring(200000f, 200000f);
						// setup joint object attributes
						String dir[] = dirsRot[i].split("_");
						Float dirX = new Float(0);
						Float dirY = new Float(0);
						Float dirZ = new Float(0);
						dirX = dirX.parseFloat(dir[0]);
						dirY = dirY.parseFloat(dir[1]);
						dirZ = dirZ.parseFloat(dir[2]);

						final RotationalJointAxis rotationalAxis = tmpJoint
								.createRotationalAxis();
						
						rotationalAxis.setAvailableAcceleration(4000f);
						rotationalAxis.setDirection(new Vector3f(dirX, dirY,
								dirZ));
						Float min = new Float(0);
						Float max = new Float(0);
						min = min.parseFloat(minRotLims[i]);
						max = max.parseFloat(maxRotLims[i]);
						rotationalAxis.setPositionMinimum(min
								* FastMath.DEG_TO_RAD);
						rotationalAxis.setPositionMaximum(max
								* FastMath.DEG_TO_RAD);

						// add to list for further use in our BodyPart based
						// skeleton
						rotJoints.add(tmpJoint);
						System.out.println("rotJointsToParentList: "
								+ rotJoints);

						// setup joint attachment

						DynamicPhysicsNode source;
						DynamicPhysicsNode target;

						if (current == 0) {
							// attach first axis to the real "from obj"
							source = (DynamicPhysicsNode) joint.get(OBJ_FROM);
						} else {
							source = (DynamicPhysicsNode) joint
									.get("last_attached");
						}
						if (current == axis - 1) {
							// attach last axis to the real "to obj"
							target = (DynamicPhysicsNode) joint.get(OBJ_TO);
						} else {
							// any additional axis gets it own metaobject
							// because of a limitation in ode max and min
							// positions on rotational axis
							target = physicsGame.getPhysicsSpace()
									.createDynamicNode();
							source.getParent().attachChild(target);

							Node anchor = (Node) joint.get(OBJ_ANCHOR);
							Vector3f sourceTrans = (Vector3f) anchor
									.getLocalTranslation().clone();

							target.setLocalTranslation(sourceTrans);

							target.setName("dummy" + i);
							joint.put("last_attached", target);

						}
						// setup joint position
						if (i == 0)
							tmpJoint.setAnchor(((Node) source)
									.getWorldTranslation().subtract(
											((Node) joint.get(OBJ_ANCHOR))
													.getWorldTranslation())
									.negate());

						tmpJoint.attach(source, target);

						current++;
					}

					if (dirsTrans != null && dirsTrans.length > 0) {
						final Joint tmpJoint = physicsGame.getPhysicsSpace()
								.createJoint();
						for (int i = 0; i < dirsTrans.length; i++) {

							// setup joint object attributes
							String dir[] = dirsTrans[i].split("_");
							Float dirX = new Float(0);
							Float dirY = new Float(0);
							Float dirZ = new Float(0);
							dirX = dirX.parseFloat(dir[0]);
							dirY = dirY.parseFloat(dir[1]);
							dirZ = dirZ.parseFloat(dir[2]);

							final TranslationalJointAxis translationalAxis = tmpJoint
									.createTranslationalAxis();

							translationalAxis.setDirection(new Vector3f(dirX,
									dirY, dirZ));

							Float min = new Float(0);
							Float max = new Float(0);
							min = min.parseFloat(minTransLims[i]);
							max = max.parseFloat(maxTransLims[i]);
							translationalAxis.setPositionMinimum(min);
							translationalAxis.setPositionMaximum(max);

						}
						// setup joint position
						tmpJoint.setAnchor(((Node) joint.get(OBJ_FROM))
								.getWorldTranslation().subtract(
										((Node) joint.get(OBJ_ANCHOR))
												.getWorldTranslation())
								.negate());

						tmpJoint.attach((DynamicPhysicsNode) joint
								.get(OBJ_FROM), (DynamicPhysicsNode) joint
								.get(OBJ_TO));

					}

				}

			}
		}
	}

	public HashMap getPhysicsObjects() {
		return this.physicsObjects;
	}

	public HashMap getSkeletons() {
		return this.skeletons;
	}

	public BodyPart getSkeleton(String _name) {
		return (BodyPart) this.skeletons.get(_name);
	}

}