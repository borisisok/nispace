package de.neuron.scratchpad.jmephy2;

import java.util.*;

import com.jmex.physics.DynamicPhysicsNode;

public class EnhancedPhysicsNode {

	final private List attachedObjects = new ArrayList(); 
	private DynamicPhysicsNode node;
	
	public EnhancedPhysicsNode(DynamicPhysicsNode dpn){
			node = dpn;
	}
  
	public void attachObject(DynamicPhysicsNode node){
		attachedObjects.add(node);
	}

	public List getAttachedObjects(){
		return attachedObjects;	
	} 
	
	public DynamicPhysicsNode getNode(){
		return node;
	}
	
}
