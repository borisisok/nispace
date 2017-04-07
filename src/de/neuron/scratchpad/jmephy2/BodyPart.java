package de.neuron.scratchpad.jmephy2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.jme.scene.Node;

public class BodyPart {
	String name;

	Node jmeobj;

	HashMap children = new HashMap();

	HashMap parents = new HashMap();

	HashMap childJoints = new HashMap();

	HashMap parentJoints = new HashMap();

	public BodyPart() {
	}

	public void setJmeObj(Node _node) {
		System.out.println("setJmeObj: " + _node);
		jmeobj = _node;
	}

	public Node getJmeObj() {
		return jmeobj;
	}

	public void setName(String _name) {
		name = _name;
	}

	public String getName() {
		return name;
	}

	public void addChild(BodyPart _child) {
		children.put(_child.getName(), _child);
	}

	public BodyPart getChild(String _name) {
		return (BodyPart) children.get(_name);
	}

	public void addParent(BodyPart _parent) {
		parents.put(_parent.getName(), _parent);
	}

	public HashMap getChildren() {
		return children;
	}

	public HashMap getParents() {
		return parents;
	}

	public List getChildList() {
		Iterator childIt = children.keySet().iterator();
		ArrayList childList = new ArrayList();
		while (childIt.hasNext()) {
			childList.add(children.get(((String) childIt.next())));
		}
		return childList;
	}

	public void setChildJoints(Object child, ArrayList _list) {
		childJoints.put(child, _list);
	}

	public ArrayList getChildJoints(Object child) {
		return (ArrayList) childJoints.get(child);
	}

	public void setParentJoints(Object parent, ArrayList _list) {
		parentJoints.put(parent, _list);
	}

	public ArrayList getParentJoints(Object parent) {
		return (ArrayList) parentJoints.get(parent);
	}

}
