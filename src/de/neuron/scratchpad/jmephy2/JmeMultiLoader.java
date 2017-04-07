package de.neuron.scratchpad.jmephy2;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.util.export.binary.BinaryImporter;

import com.jmex.model.XMLparser.Converters.FormatConverter;
import com.jmex.model.XMLparser.Converters.ObjToJme;
import com.jmex.model.XMLparser.XMLtoBinary;
import com.jmex.model.XMLparser.JmeBinaryReader;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsNode;
import com.jmex.physics.material.Material;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.StaticPhysicsNode;



import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;


public class JmeMultiLoader {
   
	public static Node loadPhyObjModel(PhysicsSpace physpace, String name, boolean useMtLib) {
    	// convert the input file to a jme -binary model
    	FormatConverter converter = new ObjToJme();
    	try{
    	    ByteArrayOutputStream modelData = new ByteArrayOutputStream();
    	    URL modelFile = TestArea1.class.getClassLoader().getResource(name);
    	    URL texFile = new URL( "file://"+modelFile.getPath().substring(0, modelFile.getPath().lastIndexOf("/")+1)  );
    	    
    	    if (useMtLib){
    		converter.setProperty("mtllib", modelFile );
    		converter.setProperty("texdir", texFile );
    	    } else {
    		converter.setProperty("sillycolors", "true" );
    	    }
    	    
    	    if (modelFile == null) return null;
    	    converter.convert( new BufferedInputStream( modelFile.openStream() ), modelData );
    	    // get the model
    	    Spatial model=(Spatial) BinaryImporter.getInstance().load( new ByteArrayInputStream( modelData.toByteArray() ) );
    	    DynamicPhysicsNode dpNode = physpace.createDynamicNode();
    	    dpNode.attachChild(model);
    	    ((PhysicsNode) dpNode).generatePhysicsGeometry(true);
    	    dpNode.setMaterial( Material.WOOD );
    	    dpNode.updateRenderState();
    	    return dpNode;
    	} catch ( IOException e ) {
    	    e. printStackTrace();
    	    return null;
    	}
    	
        }
        
        public static Node loadStaticPhyObjModel(PhysicsSpace physpace, String name, boolean useMtLib) {
    	// convert the input file to a jme -binary model
    	FormatConverter converter = new ObjToJme();
    	try{
    	    ByteArrayOutputStream modelData = new ByteArrayOutputStream();
    	    URL modelFile = TestArea1.class.getClassLoader().getResource(name);
    	    URL texFile = new URL( "file://"+modelFile.getPath().substring(0, modelFile.getPath().lastIndexOf("/")+1)  );
    	    
    	    if (useMtLib){
    		converter.setProperty("mtllib", modelFile );
    		converter.setProperty("texdir", texFile );
    	    } else {
    		converter.setProperty("sillycolors", "true" );
    	    }
    	    if (modelFile == null) return null;
    	    converter.convert( new BufferedInputStream( modelFile.openStream() ), modelData );
    	    // get the model
    	    Spatial model=(Spatial) BinaryImporter.getInstance().load( new ByteArrayInputStream( modelData.toByteArray() ) );
    	    StaticPhysicsNode dpNode = physpace.createStaticNode();
    	    dpNode.attachChild(model);
    	    ((PhysicsNode) dpNode).generatePhysicsGeometry(true);
    	    dpNode.setMaterial( Material.WOOD );
    	    dpNode.updateRenderState();
    	    return dpNode;
    	} catch ( IOException e ) {
    	    e. printStackTrace();
    	    return null;
    	}
    	
        }
        
        public static Node loadXmlModel(String name, String bound ) {
    	// convert the input file to a jme -binary model
    	XMLtoBinary converter = new XMLtoBinary();
    	
    	try{
    	    ByteArrayOutputStream modelData = new ByteArrayOutputStream();
    	    URL modelFile = TestArea1.class.getClassLoader().getResource(name);
    	    converter.sendXMLtoBinary(new JmeVertexWinder(modelFile).openStream(), modelData);
        
    	    if (modelFile == null) return null;

    	    JmeBinaryReader jbr = new JmeBinaryReader();
    	    jbr.setProperty("bound", bound );
//    	    jbr.setProperty("texclasspath", "data/");
//   	    System.out.println(new URL("file://data"));
    	    Node modelNode = (Node) jbr.loadBinaryFormat(new ByteArrayInputStream(modelData.toByteArray()));
    /*
               Quaternion q = new Quaternion();
                q.fromAngleAxis(-FastMath.DEG_TO_RAD*180f, new Vector3f(0,0,1));
                modelNode.setLocalRotation(q);

    	     modelNode.setLocalScale(new Vector3f(-1f,-1f,-1f));
    	    modelNode.setModelBound(new BoundingBox());
                modelNode.updateModelBound();
                modelNode.updateWorldBound();	   
    */	   
    	   return modelNode;

    	} catch ( IOException e ) {
    	    e. printStackTrace();
    	    return null;
    	}
    	
        }
        
        
        public static Node loadObjModel(String name, String bound, boolean useMtLib) {
    	// convert the input file to a jme -binary model
    	FormatConverter converter = new ObjToJme();
    	try{
    	    ByteArrayOutputStream modelData = new ByteArrayOutputStream();
    	    URL modelFile = TestArea1.class.getClassLoader().getResource(name);
    	    URL texFile = new URL( "file://"+modelFile.getPath().substring(0, modelFile.getPath().lastIndexOf("/")+1)  );
    	    
    	    if (useMtLib){
    		converter.setProperty("mtllib", modelFile );
    		converter.setProperty("texdir", texFile );
    	    } else {
    		converter.setProperty("sillycolors", "true" );
    	    }
    	    if (modelFile == null) return null;
    	    
    	    converter.convert( new BufferedInputStream( modelFile.openStream() ), modelData );
    	    // get the model
    	    Spatial model=(Spatial) BinaryImporter.getInstance().load( new ByteArrayInputStream( modelData.toByteArray() ) );
    	    Node node = new Node("obj scene");
    	    node.attachChild(model);
    	    return node;
    	} catch ( IOException e ) {
    	    e. printStackTrace();
    	    return null;
    	}
    	
        }

}
