package de.neuron.scratchpad.jmephy2;

import java.util.*;
import com.jmex.physics.*;
import com.jme.math.*;
import groovy.util.GroovyScriptEngine;

public class BodyController implements Runnable {

	BodyPart body;

	ArrayList parts = new ArrayList();

	boolean neutral = false;

	float maxac = 1000f;

	float maxvel = 80f;

	long interval = 1000l;

	long scriptReloadInterval = 6000l;

	long lastReload;

	GroovyScriptEngine gse;

	GControllerScript gscript;

	Class gclass;

	String gname;

	BodyPart part;

	Class script;

	Object sobj;

	public BodyController(BodyPart _body) {
		body = _body;
	}

	public void setGScript(GControllerScript _gscript) {
		gscript = _gscript;
	}

	public void setGScriptName(String _gname) {
		gname = _gname;
	}

	public void setGSE(GroovyScriptEngine _gse) {
		gse = _gse;
	}

	public void setGScriptClass(Class _gclasst) {
		gclass = _gclasst;
	}

	public void run() {
		try {
			recurseBodyParts(body);

			while (true) {
				Thread.sleep(interval);
				update();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init() {
		recurseBodyParts(body);
	}
	
	public void update() {
		/*
		 * if (gscript!=null){ gscript.execute(); } else if (gclass != null){
		 * Object sobj = gclass.newInstance(); gscript = (GScript) sobj;
		 * gscript.execute(); }
		 */
		if (gname != null) {
			try {
				if (System.currentTimeMillis() - lastReload > scriptReloadInterval) {
					lastReload = System.currentTimeMillis();

					script = gse.loadScriptByName(this.gname);
					if (gclass != script) {
						System.out.println("Reloading!");
						sobj = script.newInstance();
						gscript = (GControllerScript) sobj;
						gscript.setBodyController(this);
						gclass = script;
						gscript.init();
					}
				}
				gscript.update();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void recurseBodyParts(BodyPart body) {
		List children = body.getChildList();
		if (children == null || children.size() == 0)
			return;
		parts.add(body);
		Iterator chit = children.iterator();
		while (chit.hasNext()) {
			BodyPart p = (BodyPart) chit.next();
			// System.out.println(p.getName());

			recurseBodyParts(p);
			// applyForce((DynamicPhysicsNode) p.getJmeObj());
		}
	}

	public void applyForce(DynamicPhysicsNode node) {
		int scale = 200;
		int offset = 100;
		double f = 0;

		double x = (Math.random() * scale) + scale / 2;
		double y = (Math.random() * scale) + scale / 2;
		double z = (Math.random() * scale) + scale / 2;

		// Vector3f v = new Vector3f((float) x, (float) y, (float) z);
		Vector3f v = new Vector3f((float) Math.random() * 360, (float) Math
				.random() * 360, (float) Math.random() * 360);
		// Vector3f v = new Vector3f((float)f,(float)f,(float)f);

		// System.out.println(p.getName() + " " +p.getJmeObj() + " " + v);
		// node.clearForce();
		// node.setAngularVelocity(v);
		// node.getWorldTranslation().set(v);
		// node.setLinearVelocity(v);
		// ((DynamicPhysicsNode) p.getJmeObj()).addForce(v2, v);
		// node.addTorque(new Vector3f(24.0f,24.0f, 24.0f));
		// node.lookAt(arg0, arg1)
	}
}
