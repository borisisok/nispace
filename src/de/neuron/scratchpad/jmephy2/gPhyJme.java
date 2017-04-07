package de.neuron.scratchpad.jmephy2;

import groovy.util.GroovyScriptEngine;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.*;

import jmetest.effects.TestParticleSystem;

import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.ShadeState;
import com.jme.scene.state.lwjgl.LWJGLShadeState;
import com.jme.scene.shape.Disk;
import com.jme.scene.SceneElement;
import com.jme.scene.state.AlphaState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.LoggingSystem;
import com.jme.util.TextureManager;
import com.jmex.physics.util.SimplePhysicsGame;
import com.jmex.effects.particles.ParticleFactory;
import com.jmex.effects.particles.ParticleMesh;

public class gPhyJme extends SimplePhysicsGame {

	private ParticleMesh pMesh;
	  private Vector3f currentPos = new Vector3f(), newPos = new Vector3f();
	  private float frameRate = 0;

	
	private static GroovyScriptEngine gse;

	String gname;

	GSimScript gscript;

	Class gclass;

	Class script;

	Object sobj;

	ArrayList controller = new ArrayList();
	
	long scriptReloadInterval = 6000l;

	long lastReload;

	Iterator it;
	BodyController ctl;
	
	public gPhyJme(String scriptname) {
		gname = scriptname;
	}

	protected void simpleInitGame() {

		// init groovy
		String groot[] = new String[1];
		groot[0] = ".";
		try {
			gse = new GroovyScriptEngine(groot);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.getPhysicsSpace().setupBinaryClassLoader(
				BinaryImporter.getInstance());

		LoggingSystem.getLogger().warning("Starting gPhyJme");

//		viewSetup();
//		stateSetup();

//		startScene();
//		addTestStuffB();
	}

	private void startScene() {

	}

	private Node loadSimObj(String filename) {
		Node obj = JmeMultiLoader.loadXmlModel(filename + ".xml", "sphere");

		URL botPhy = TestArea1.class.getClassLoader().getResource(
				filename + ".phy");

		NiWorldBuilder nwb = new NiWorldBuilder(botPhy);
		nwb.setPhysicsGame(this);
		nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_MIRROR);
		nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO);
		nwb.buildFrom(obj);

		BodyController ctl = new BodyController(nwb.getSkeleton("z"));
		ctl.setGSE(gse);
		ctl.setGScriptName("groovy." + filename);
//		new Thread(ctl).start();
		ctl.init();
		controller.add(ctl);
		return obj;
	}

	public void update(float interpolation) {
		// process controller updates;
		it = controller.iterator();
		while (it.hasNext()){
			ctl = (BodyController) it.next();
			ctl.update();
		}
		
		if (System.currentTimeMillis() - lastReload > scriptReloadInterval
				&& gname != null) {
			lastReload = System.currentTimeMillis();

			try {
				script = gse.loadScriptByName(this.gname);
				if (gclass != script) {
					System.out.println("Reloading!");
					sobj = script.newInstance();
					gscript = (GSimScript) sobj;
					gscript.setGPhyJme(this);
					gclass = script;
					gscript.init();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		gscript.update();

		super.update(interpolation);
	}

	private void viewSetup() {
		display.setVSyncEnabled(true);
		// display.getDisplaySystem().recreateWindow()
		int width = 1280;
		int height = 800;
		// int width = 320;
		// int height = 200;

		int depth = 24;
		int freq = 60;
		boolean fullscreen = true;
		display.recreateWindow(width, height, depth, freq, fullscreen);
		// Setup camera
		cam.setFrustumPerspective(50.0f, (float) display.getWidth()
				/ (float) display.getHeight(), 1f, 5000);
		cam.setLocation(new Vector3f(0f, 10f, 100f));
	}

	public void stateSetup(Node node) {
		// cameraAxisVector.normalizeLocal();
		// cam.setParallelProjection(false);
		// cam.update();

		final boolean USE_TEXTURE_STATES = false;
		final boolean USE_LIGHT_STATES = true;
		final boolean USE_MATERIAL_STATES = false;
		final boolean USE_RENDER_STATES = false;

		// rootNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
		/*
		 * pManager = new BasicPassManager();
		 * 
		 * sPass.add(rootNode); sPass.setRenderShadows(true);
		 * sPass.setLightingMethod(ShadowedRenderPass.ADDITIVE);
		 * pManager.add(sPass); RenderPass rPass = new RenderPass();
		 * rPass.add(this.fpsNode); pManager.add(rPass);
		 */
		if (USE_RENDER_STATES) {
			ShadeState st = new LWJGLShadeState();
			st.setShade(ShadeState.SM_SMOOTH);
		}

		if (USE_LIGHT_STATES) {
			rootNode.clearRenderState(LightState.RS_LIGHT);
			/** Set up a basic, default light. */
			DirectionalLight light = new DirectionalLight();
			light.setDiffuse(new ColorRGBA(0.0f, 1.0f, 1.0f, 1.0f));
			light.setAmbient(new ColorRGBA(0.0f, 0.5f, 0.5f, .5f));
			light.setDirection(new Vector3f(1, -1, 0));
			light.setShadowCaster(true);
			light.setEnabled(true);

			/** Attach the light to a lightState and the lightState to rootNode. */
			LightState lightState = display.getRenderer().createLightState();
			lightState.setEnabled(true);
			lightState.setGlobalAmbient(new ColorRGBA(.2f, .2f, .2f, 1f));
			lightState.attach(light);
			node.setRenderState(lightState);
		}

		if (USE_MATERIAL_STATES) {
			MaterialState ms = display.getRenderer().createMaterialState();
			ms.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
			// ms.setEmissive(new ColorRGBA(0,0.75f,0,0.5f));
			ms.setDiffuse(new ColorRGBA(0, 1, 0, 0.5f));
			ms.setSpecular(new ColorRGBA(1, 0, 0, 0.5f));
			ms.setShininess(100);
			node.setRenderState(ms);
		}

		// display.getRenderer().setBackgroundColor(ColorRGBA.lightGray);

	}

	public static void main(String[] args) {
		LoggingSystem.getLogger().setLevel(Level.WARNING);
			new gPhyJme("groovy.gphyjme").start();
	}

	
	
	
	public void addTestStuffB(){


//		lightState.setEnabled(false);
		Node tmp = new Node("tmp");
	    AlphaState as1 = display.getRenderer().createAlphaState();
	    as1.setBlendEnabled(true);
	    as1.setSrcFunction(AlphaState.SB_SRC_ALPHA);
	    as1.setDstFunction(AlphaState.DB_ONE);
	    as1.setTestEnabled(true);
	    as1.setTestFunction(AlphaState.TF_GREATER);
	    as1.setEnabled(true);

	    TextureState ts = display.getRenderer().createTextureState();
	    ts.setTexture(
	        TextureManager.loadTexture(
	        gPhyJme.class.getClassLoader().getResource(
	        "jmetest/data/texture/flaresmall.jpg"),
	        Texture.MM_LINEAR_LINEAR,
	        Texture.FM_LINEAR));
	    ts.setEnabled(true);

	    pMesh = ParticleFactory.buildParticles("particles", 300);
	    pMesh.setEmissionDirection(new Vector3f(0,1,0));
	    pMesh.setInitialVelocity(.006f);
	    pMesh.setStartSize(2.5f);
	    pMesh.setEndSize(.5f);
	    pMesh.setMinimumLifeTime(1200f);
	    pMesh.setMaximumLifeTime(1400f);
	    pMesh.setStartColor(new ColorRGBA(1, 0, 0, 1));
	    pMesh.setEndColor(new ColorRGBA(0, 1, 0, 0));
	    pMesh.setMaximumAngle(360f * FastMath.DEG_TO_RAD);
	    pMesh.getParticleController().setControlFlow(false);
	    pMesh.warmUp(60);

	    tmp.setRenderState(ts);
	    tmp.setRenderState(as1);
			ZBufferState zstate = display.getRenderer().createZBufferState();
			zstate.setEnabled(false);
			pMesh.setRenderState(zstate);
	    pMesh.setModelBound(new BoundingSphere());
	    pMesh.updateModelBound();

	    tmp.attachChild(pMesh);
	    rootNode.attachChild(tmp);
	    tmp.updateRenderState();
	}
	
	 protected void simpleUpdate() {
		    if (tpf > 1f) tpf = 1.0f; // do this to prevent a long pause at start

		    if ( (int) currentPos.x == (int) newPos.x
		        && (int) currentPos.y == (int) newPos.y
		        && (int) currentPos.z == (int) newPos.z) {
		      newPos.x = (float) Math.random() * 50 - 25;
		      newPos.y = (float) Math.random() * 50 - 25;
		      newPos.z = (float) Math.random() * 50 - 150;
		    }

		    frameRate = timer.getFrameRate() / 2;
		    currentPos.x -= (currentPos.x - newPos.x)
		        / frameRate;
		    currentPos.y -= (currentPos.y - newPos.y)
		        / frameRate;
		    currentPos.z -= (currentPos.z - newPos.z)
		        / frameRate;

		    if (pMesh != null) 
		    	pMesh.setOriginOffset(currentPos);

		  }
}
