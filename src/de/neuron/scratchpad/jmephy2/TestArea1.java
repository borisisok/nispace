/*Copyright*/
package de.neuron.scratchpad.jmephy2;

import java.util.logging.Level;
import de.neuron.scratchpad.wiimote.*;

import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.NodeHandler;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.math.Vector3f;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.FastMath;

import com.jmex.model.XMLparser.Converters.FormatConverter;
import com.jmex.model.XMLparser.Converters.ObjToJme;

import com.jme.util.LoggingSystem;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.Joint;
import com.jmex.physics.JointAxis;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.material.Material;
import com.jmex.physics.util.SimplePhysicsGame;
import com.jmex.physics.PhysicsNode;
import com.jmex.physics.geometry.PhysicsMesh;
import com.jmex.physics.geometry.PhysicsBox;

import com.jme.util.export.binary.BinaryImporter;

import com.jme.image.Texture;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ShadeState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.CullState;

import com.jme.scene.shape.Torus;
import com.jme.scene.shape.Box;
import com.jme.scene.Skybox;

import com.jme.scene.state.lwjgl.LWJGLShadeState;
import com.jme.util.TextureManager;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.scene.Spatial;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import com.jme.scene.Geometry;
import com.jme.scene.TriMesh;

import com.jme.scene.state.MaterialState;
import com.jme.renderer.ColorRGBA;

import com.jme.scene.state.LightState;
import com.jme.light.DirectionalLight;
import com.jme.light.SpotLight;
import com.jme.light.PointLight;

import com.jme.bounding.BoundingBox;

import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jmex.terrain.TerrainPage;
import com.jmex.terrain.util.FaultFractalHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;
import com.jme.system.DisplaySystem;
import javax.swing.ImageIcon;

import com.jme.scene.state.FogState;

import com.jme.renderer.pass.BasicPassManager;
import com.jme.renderer.pass.RenderPass;
import com.jme.renderer.pass.ShadowedRenderPass;

import com.jmex.terrain.util.MidPointHeightMap;
import com.jmex.terrain.TerrainBlock;

// groovy related

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

/**
 * @author Boris
 */
public class TestArea1 extends SimplePhysicsGame implements WiiAccHandler {

	private static final ShadowedRenderPass shadowPass = new ShadowedRenderPass();

	private static GroovyScriptEngine gse;

	static {
	}

	protected BasicPassManager passManager;

	Skybox sky;

	DynamicPhysicsNode pball;

	StaticPhysicsNode pground;

	Node gun;

	CameraNode camNode;

	Vector3f camLockAxis = new Vector3f();

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

		LoggingSystem.getLogger().warning("Starting testscene");

		// testScene001();
		// testScene002();
		viewSetup();
		stateSetup();
		// createFloor();
		// testScene003(); // the good one with physics join example with 3
		// limited rotation axis
		// testSceneFromAoi(); // the aoi exporter test scene
		testSceneFromMultiAois();
		// buildPassManager();

	}
	public void	update(float interpolation ){
		System.out.println("update: " + interpolation);
		super.update(interpolation);
	}
	
	public void testSceneFromAoi() {

		Node test = JmeMultiLoader.loadXmlModel("aoi.xml", "sphere");

		// create dummy joint data (test only)
		String jointList[] = new String[] {

				// test arm
				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-32:-32:-32,rotMax=32:32:32,from=arm,to=ell",
				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-32:-32:-32,rotMax=32:32:32,from=ell,to=hand",

				// spider bot - flex legs
				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-136:-36:-36,rotMax=136:36:36,from=z,to=a",
				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-36:-36:-36,rotMax=36:36:36,from=a,to=aa",

				// "rotDir=1_0_0:0_1_0:0_0_1,rotMin=-136:-36:-36,rotMax=136:36:36,from=z,to=b",
				// "rotDir=1_0_0:0_1_0:0_0_1,rotMin=-36:-36:-36,rotMax=36:36:36,from=b,to=bb",

				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-136:-36:-36,rotMax=136:36:36,from=z,to=c",
				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-36:-36:-36,rotMax=36:36:36,from=c,to=cc",

				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-136:-36:-36,rotMax=136:36:36,from=z,to=d",
				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-36:-36:-36,rotMax=36:36:36,from=d,to=dd",

				// "rotDir=1_0_0:0_1_0:0_0_1,rotMin=-136:-36:-36,rotMax=136:36:36,from=z,to=e",
				// "rotDir=1_0_0:0_1_0:0_0_1,rotMin=-36:-36:-36,rotMax=36:36:36,from=e,to=ee",

				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-136:-36:-36,rotMax=136:36:36,from=z,to=f",
				"rotDir=1_0_0:0_1_0:0_0_1,rotMin=-36:-36:-36,rotMax=36:36:36,from=f,to=ff",

		};

		NiWorldBuilder nwb = new NiWorldBuilder(jointList);
		nwb.setPhysicsGame(this);
		nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_MIRROR);
		nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO);
		nwb.buildFrom(test);

		BodyController spiderCtl = new BodyController(nwb.getSkeleton("z"));
		new Thread(spiderCtl).start();

		DynamicPhysicsNode sphere = (DynamicPhysicsNode) nwb
				.getPhysicsObjects().get("sphere");

		// add the ball "manipulator" to the world
		sphere.setMass(100f);
		sphere.setMaterial(Material.RUBBER);
		setupForceInput(sphere);

		// add to render graph
		rootNode.attachChild(test);

	}

	public void testSceneFromMultiAois() {

		// groovy script engine
		String[] groot = new String[] { "/my/groovy/script/path" };
		try {
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// GROUND

		Node ground = JmeMultiLoader.loadXmlModel("ground4.xml", "sphere");

		NiWorldBuilder nwb = new NiWorldBuilder();
		nwb.setPhysicsGame(this);
		nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_MIRROR);
		nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO);
		nwb.buildFrom(ground);
		rootNode.attachChild(ground);
		// Node box = rootNode.createBox("Box");

		gun = JmeMultiLoader.loadXmlModel("gun.xml", "sphere");

		NiWorldBuilder nwbGun = new NiWorldBuilder();
		nwbGun.setPhysicsGame(this);
		nwbGun.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_MIRROR);
		nwbGun.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO);
		nwbGun.buildFrom(gun);

		// DynamicPhysicsNode tmpGun = (DynamicPhysicsNode)
		// nwbGun.getPhysicsObjects().get("gun");
		// tmpGun.setActive(false);
		// tmpGun.setAffectedByGravity(false);

		camNode = new CameraNode("Camera Node", cam);
		camNode.attachChild(gun);
		rootNode.attachChild(camNode);

		// Setup the input controller and timer
		input = new NodeHandler(camNode, 100, 1);
		gun.setLocalTranslation(new Vector3f(0f, -5f, 0f));

		// MANIPULATIOR
		DynamicPhysicsNode sphere = (DynamicPhysicsNode) nwb
				.getPhysicsObjects().get("sphere");
		// add the ball "manipulator" to the world
		sphere.setMass(100f);
		sphere.setMaterial(Material.GRANITE);
		setupForceInput(sphere);
		/*
		 * // TALL BOY Node sam = JmeMultiLoader.loadXmlModel("girl.xml",
		 * "sphere");
		 * 
		 * nwb = new NiWorldBuilder(); nwb.setPhysicsGame(this); //
		 * nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_MIRROR);
		 * nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO);
		 * nwb.buildFrom(sam); rootNode.attachChild(sam);
		 * sam.setLocalTranslation(new Vector3f(0f,50f,0f));
		 */
		// BOTS
		String filename = "bot6legs";
		for (int i = 0; i < 1 + 1; i++) {
			Node bot = JmeMultiLoader.loadXmlModel(filename + ".xml", "sphere");

			URL botPhy = TestArea1.class.getClassLoader().getResource(
					filename + ".phy");

			nwb = new NiWorldBuilder(botPhy);
			nwb.setPhysicsGame(this);
			nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_MIRROR);
			nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO);
			nwb.buildFrom(bot);
			rootNode.attachChild(bot);

			BodyController spiderCtl = new BodyController(nwb.getSkeleton("z"));
			spiderCtl.setGSE(gse);
			spiderCtl.setGScriptName("groovy.test");
			new Thread(spiderCtl).start();

		}

		// BOTS
		filename = "bot4legs";
		for (int i = 0; i < 1 + 1; i++) {
			Node bot = JmeMultiLoader.loadXmlModel(filename + ".xml", "sphere");

			URL botPhy = TestArea1.class.getClassLoader().getResource(
					filename + ".phy");

			nwb = new NiWorldBuilder(botPhy);
			nwb.setPhysicsGame(this);
			nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_MIRROR);
			nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO);
			nwb.buildFrom(bot);
			bot.setLocalTranslation(new Vector3f((float) Math.random() * 100,
					(float) Math.random() * 100, (float) Math.random() * 100));

			rootNode.attachChild(bot);

			BodyController spiderCtl = new BodyController(nwb.getSkeleton("z"));

			spiderCtl.setGSE(gse);
			spiderCtl.setGScriptName("groovy.test");

			new Thread(spiderCtl).start();
		}

		try {

			WiiGunAPosAction wiiGunAPosAction = new WiiGunAPosAction(camNode,
					gun);
			new Thread(wiiGunAPosAction).start();

			wiiEventListener wii = new wiiEventListener(9911);
			wii.setIrAPosHandler(wiiGunAPosAction);
			new Thread(wii).start();

		} catch (IOException ie) {
			ie.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.neuron.scratchpad.wiimote.WiiAccHandler#handleWiiAcc(int, int,
	 *      int)
	 */
	public void handleWiiAcc(int x, int y, int z) {
		System.out.println("i can handle this: " + x + " " + y + " " + z);
	}

	public void testScene002() {
		rootNode.attachChild(JmeMultiLoader.loadObjModel(
				"model/test/testScene1.obj", "sphere", true));
	}

	public void testScene003() {

		createSkyBox();
		// Spatial terrain = createTerrain();
		// rootNode.attachChild( terrain );
		rootNode.getLocalTranslation().set(0, -150, 0);

		/*
		 * Node ramp = loadStaticPhyObjModel("testRamp2.obj", true);
		 * 
		 * rootNode.attachChild(ramp); // Node board =
		 * loadPhyObjModel("testBoard1.obj", true);
		 */
		// Node ball = JmeMultiLoader.loadPhyObjModel(this.getPhysicsSpace(),
		// "testBall.obj", false);
		Node ball = JmeMultiLoader.loadXmlModel("icoball.xml", "sphere");

		// shadowPass.addOccluder(ball);
		CullState cs = DisplaySystem.getDisplaySystem().getRenderer()
				.createCullState();
		cs.setCullMode(CullState.CS_BACK);
		cs.setEnabled(true);
		ball.setRenderState(cs);

		// Node test = loadObjModel("testNiWorldBuilder.obj", "sphere", true);

		Node test = JmeMultiLoader.loadXmlModel("test3.xml", "sphere");
		// shadowPass.addOccluder(test);
		test.setRenderState(cs);

		// get my aoi obj here

		Node test2 = JmeMultiLoader.loadXmlModel("aoi.xml", "sphere");
		// shadowPass.addOccluder(test);
		test2.setRenderState(cs);
		rootNode.attachChild(test2);

		// let worldbuilder decorate that scene (applys import fixes and adds
		// physics
		// and joints )

		// create dummy joint data (test only)
		String jointList[] = new String[] {
				// "rotDir=1_0_0:0_1_0:0_0_1,rotMin=-32:-32:-32,rotMax=32:32:32,from=arm,to=ell",
				// "rotDir=1_0_0:0_1_0:0_0_1,rotMin=-32:-32:-32,rotMax=32:32:32,from=ell,to=hand",
				// "transDir=1_0_0:0_1_0:0_0_1,transMin=-32:-32:-32,transMax=32:32:32,from=arm,to=ell",
				// "transDir=1_0_0:0_1_0:0_0_1,transMin=-32:-32:-32,transMax=32:32:32,from=ell,to=hand",
				"transDir=1_0_0,transMin=-32,transMax=32:32:32,from=arm,to=ell",
				"transDir=1_0_0,transMin=-32,transMax=32:32:32,from=ell,to=hand",

				"rotDir=0_0_1:0_0_1,rotMin=-4:-4,rotMax=4:4,from=chain0,to=chain1",
				"rotDir=0_0_1:0_0_1,rotMin=-4:-4,rotMax=4:4,from=chain1,to=chain2",
				"rotDir=0_0_1:0_0_1,rotMin=-4:-4,rotMax=4:4,from=chain2,to=chain3",

		};

		NiWorldBuilder nwb = new NiWorldBuilder(jointList);
		nwb.setPhysicsGame(this);
		nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_MIRROR);
		nwb.enable(NiWorldBuilder.FEATURE_GEO_FIX_LOAD_NAME_NOCULLINFO);
		nwb.buildFrom(test);

		// sPass.addOccluder(test);

		pground = (StaticPhysicsNode) nwb.getPhysicsObjects().get("Ground");

		rootNode.attachChild(test);

		nwb.buildFrom(ball);

		pball = (DynamicPhysicsNode) nwb.getPhysicsObjects().get("Icoball");

		rootNode.attachChild(ball);

		System.out.println("My BALL is a: "
				+ ((Node) nwb.getPhysicsObjects().get("Icoball")).getType());
		pball.setMass(50f);
		pball.setMaterial(Material.IRON);
		setupForceInput(pball);

		/*
		 * for (int i = 0; i<40;i++){ createRndBox(); }
		 */
	}

	public void testScene001() {
		try {
			// convert the input file to a jme -binary model
			FormatConverter converter = new ObjToJme();

			ByteArrayOutputStream modelData = new ByteArrayOutputStream();
			URL modelFile = TestArea1.class.getClassLoader().getResource(
					"testBoard1.obj");
			System.out.println("zzz=" + modelFile);

			// URL modelFile =
			// TestArea1.class.getClassLoader().getResource("model/test/chineseevergreen/aglaonema.obj"
			// );
			converter.setProperty("mtllib", modelFile);
			converter.convert(new BufferedInputStream(modelFile.openStream()),
					modelData);
			// get the model
			// JmeBinaryReader jbr = new JmeBinaryReader();
			// jbr.setProperty("bound","sphere");
			// Node model = jbr.loadBinaryFormat( new ByteArrayInputStream(
			// modelData. toByteArray() ) );
			// Node model = (Node) BinaryImporter.getInstance().load( new
			// ByteArrayInputStream( modelData.toByteArray() ) );
			Spatial model = (Spatial) BinaryImporter.getInstance().load(
					new ByteArrayInputStream(modelData.toByteArray()));

			// spatial.setName(modelFile.toString());
			// Node model = (Node)spatial;
			// model.setModelBound(new BoundingBox());
			// model.updateModelBound();
			// model.updateGeometricState(0f, true);
			// model.updateWorldBound();
			// carModel.setLocalTranslation( new Vector3f(0f, 300f, 0f));
			URL texLoc;
			texLoc = TestArea1.class.getClassLoader().getResource("test3.jpg");
			// System.out.println("xxx="+((Node)model).getChild(0).getName());
			// Torus torus = new Torus("Torus", 12,12, 11, 12);

			// torus.setLocalTranslation(new Vector3f(50, -5, 20));

			TextureState ts = display.getRenderer().createTextureState();
			Texture t0 = TextureManager.loadTexture(TestArea1.class
					.getClassLoader().getResource("test1.jpg"),
					Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
			Texture t1 = TextureManager.loadTexture(TestArea1.class
					.getClassLoader().getResource("westworld2.jpg"),
					Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);

			t1.setEnvironmentalMapMode(Texture.EM_SPHERE);
			ts.setTexture(t0, 0);
			// ts.setTexture(t1, 1);
			ts.setEnabled(true);

			// rootNode.setCullMode(CullMode) setEnabled(true);

			// torus.setRenderState(ts);
			// model.setRenderState(ts);
			/*
			 * TextureState tsMipMap =
			 * display.getRenderer().createTextureState(); Texture tex =
			 * TextureManager.loadTexture( texLoc , Texture.MM_LINEAR_LINEAR ,
			 * Texture.FM_LINEAR ); tex.setScale( new Vector3f(0.001f, 0.001f,
			 * 0.001f)); // tex.setEnvironmentalMapMode( tex.EM_IGNORE ); //
			 * tex.setMipmapState(tex.MM_NONE); tsMipMap.setTexture( tex );
			 * tsMipMap.setEnabled(true); tex.setTranslation(new Vector3f(0f,
			 * 0f, 0f)); // carModel.setRenderState( tsMipMap ); //
			 * carModel.updateRenderState();
			 */

			// carModel.setLocalScale( 3.5f );
			// TriMesh geo = (TriMesh) carModel.getChild(0);
			// PhysicsObject obj = new PhysicsObject(geo, 100, false);
			// PhysicsWorld.getInstance().addObject(obj);
			DynamicPhysicsNode dpNode = getPhysicsSpace().createDynamicNode();

			setupForceInput(dpNode);

			// PhysicsMesh pMesh = getPhysicsSpace().createMesh("testobj",
			// dpNode);
			// pMesh.copyFrom(geo);
			// pMesh.
			// dpNode.setRenderState( tsMipMap );
			// dpNode.attachChild((TriMesh) model.getChild(0));
			// torus.setLocalTranslation( new Vector3f(0f, 30f, 0f));
			dpNode.attachChild(model);
			((PhysicsNode) dpNode).generatePhysicsGeometry(true);
			// dpNode.createBox("Box");
			// dpNode.generatePhysicsGeometry();
			// dpNode.setLocalTranslation( new Vector3f(0f, 30f, 0f));
			// dpNode.setMass(10f);
			dpNode.setMaterial(Material.WOOD);
			// pnode.setLocalScale ( 0.1f );
			// dpNode.
			dpNode.updateRenderState();

			MaterialState ms = display.getRenderer().createMaterialState();
			ms.setColorMaterial(MaterialState.CM_DIFFUSE);
			dpNode.setRenderState(ms);
			dpNode.updateRenderState();

			rootNode.attachChild(dpNode);

			for (int i = 0; i < 20; i++) {
				createRndBox();
			}

			// Implementation of "Culling"; render only objects visible.
			// CullState cs = display.getRenderer().createCullState();
			// cs.setCullMode(CullState.CS_FRONT);
			// rootNode.setRenderState(cs);

			// ShadeState st2 = new LWJGLShadeState();
			// st2.setShade(ShadeState.RS_WIREFRAME);
			// dpNode.setRenderState( st2 );
			// dpNode.updateRenderState();

			// geo.setRenderState( tsMipMap );

			// geo.setLocalScale( 0.1f );

			// MaterialState ms = display.getRenderer().createMaterialState();
			// ms.setColorMaterial(MaterialState.CM_DIFFUSE);
			// carModel.setRenderState(ms);
			// carModel.updateRenderState();
			// this.showDepth = true;
			// this.showPhysics = true;
			// this.showBounds = true;
			// this.showNormals = true;

			// ShadeState st = new LWJGLShadeState();
			// st.setShade(ShadeState.SM_SMOOTH);
			stateSetup();
			// rootNode.setRenderState(st);
			rootNode.updateRenderState();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}

	public void stateSetup() {
		// cameraAxisVector.normalizeLocal();
		// cam.setParallelProjection(false);
		// cam.update();

		final boolean USE_TEXTURE_STATES = false;
		final boolean USE_LIGHT_STATES = false;
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
			// rootNode.clearRenderState(LightState.RS_LIGHT);
			/** Set up a basic, default light. */
			DirectionalLight light = new DirectionalLight();
			light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
			light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, .5f));
			light.setDirection(new Vector3f(1, -1, 0));
			light.setShadowCaster(true);
			light.setEnabled(true);

			/** Attach the light to a lightState and the lightState to rootNode. */
			LightState lightState = display.getRenderer().createLightState();
			lightState.setEnabled(true);
			lightState.setGlobalAmbient(new ColorRGBA(.2f, .2f, .2f, 1f));
			lightState.attach(light);
			rootNode.setRenderState(lightState);
		}

		if (USE_MATERIAL_STATES) {
			MaterialState ms = display.getRenderer().createMaterialState();
			ms.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
			// ms.setEmissive(new ColorRGBA(0,0.75f,0,0.5f));
			ms.setDiffuse(new ColorRGBA(0, 1, 0, 0.5f));
			ms.setSpecular(new ColorRGBA(1, 0, 0, 0.5f));
			ms.setShininess(100);
			rootNode.setRenderState(ms);
		}

		// display.getRenderer().setBackgroundColor(ColorRGBA.lightGray);

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

	private void buildPassManager() {
		passManager = new BasicPassManager();

		// Add skybox first to make sure it is in the background
		RenderPass rPass = new RenderPass();
		rPass.add(sky);
		passManager.add(rPass);

		shadowPass.add(rootNode);
		shadowPass.addOccluder(pball);
		// shadowPass.addOccluder(flag);
		shadowPass.setRenderShadows(true);
		shadowPass.setLightingMethod(ShadowedRenderPass.MODULATIVE);
		passManager.add(shadowPass);
	}

	private void setupForceInput(DynamicPhysicsNode iNode) {
		long factor = 100;
		// now we add an input action to move the sphere on a key event
		input.addAction(new ForceInputAction(iNode, new Vector3f(
				-250f * factor, 0f, 0f)), InputHandler.DEVICE_KEYBOARD,
				KeyInput.KEY_H, InputHandler.AXIS_NONE, false); // left
		input.addAction(new ForceInputAction(iNode, new Vector3f(250f * factor,
				0f, 0f)), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_K,
				InputHandler.AXIS_NONE, false); // right
		input.addAction(new ForceInputAction(iNode, new Vector3f(0f, 0f, -250f
				* factor)), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_U,
				InputHandler.AXIS_NONE, false); // fwd
		input.addAction(new ForceInputAction(iNode, new Vector3f(0f, 0f,
				250f * factor)), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_J,
				InputHandler.AXIS_NONE, false); // bwd
		input.addAction(new ForceInputAction(iNode, new Vector3f(0f,
				1000f * factor, 0f)), InputHandler.DEVICE_KEYBOARD,
				KeyInput.KEY_Z, InputHandler.AXIS_NONE, false); // up
		input.addAction(new ForceInputAction(iNode, new Vector3f(0f, -1000f
				* factor, 0f)), InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_I,
				InputHandler.AXIS_NONE, false); // down
	}

	/**
	 * An action that get's invoked on a keystroke (once per stroke).
	 */
	private class ForceInputAction extends InputAction {

		private Vector3f vector;

		private DynamicPhysicsNode node;

		public ForceInputAction(DynamicPhysicsNode iNode, Vector3f iVector) {
			super();
			this.node = iNode;
			this.vector = iVector;
		}

		/**
		 * This method gets invoked upon key event
		 * 
		 * @param evt
		 *            more data about the event (we don't need it)
		 */
		public void performAction(InputActionEvent evt) {
			// the only really important line: apply a force to the moved node
			node.addForce(vector);
			// note: forces are applied every physics step and accumulate until
			// then
			// to apply a constant force we would have to do it for each physics
			// step! (see next lesson)
		}
	}

	private void createFloor() {

		final StaticPhysicsNode staticNode = getPhysicsSpace()
				.createStaticNode();
		/*
		 * staticNode.createBox( "box physics" ); Box box = new Box("Box", new
		 * Vector3f(0f, 0f, 0f), 1400, 0.2f, 1400);
		 * box.getLocalTranslation().set( 0, -29.2f, 0 );
		 * staticNode.setLocalScale( new Vector3f( 2800, 0.2f, 2800 ) ); //
		 * staticNode.attachChild(box); staticNode.getLocalTranslation().set( 0,
		 * -2, 0 ); // staticNode.getLocalScale().multLocal( 1.2f );
		 * staticNode.setMaterial( Material.RUBBER ); rootNode.attachChild(
		 * staticNode );
		 * 
		 * 
		 * 
		 * 
		 * MaterialState ms = display.getRenderer().createMaterialState();
		 * ms.setColorMaterial(MaterialState.CM_DIFFUSE);
		 * staticNode.setRenderState(ms); staticNode.updateRenderState();
		 * 
		 * TextureState ts = display.getRenderer().createTextureState(); Texture
		 * t0 = TextureManager.loadTexture(
		 * TestArea1.class.getClassLoader().getResource(
		 * "texture/ground/terrain1.jpg"), Texture.MM_LINEAR_LINEAR,
		 * Texture.FM_LINEAR); Texture t1 = TextureManager.loadTexture(
		 * TestArea1.class.getClassLoader().getResource( "test2.jpg"),
		 * Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
		 * t1.setEnvironmentalMapMode(Texture.EM_SPHERE); t0.setScale(new
		 * Vector3f(100f, 100f, 100f)); ts.setTexture(t0, 0); ts.setTexture(t1,
		 * 1); ts.setEnabled(true);
		 */

		Spatial terrain = createTerrain();
		staticNode.attachChild(terrain);
		staticNode.getLocalTranslation().set(0, -200, 0);
		staticNode.getLocalScale().set(10f, 1f, 10f);

		rootNode.attachChild(staticNode);
		staticNode.generatePhysicsGeometry();

	}

	/**
	 * build the height map and terrain block.
	 */
	private void buildTerrain() {

		MidPointHeightMap heightMap = new MidPointHeightMap(64, 1f);
		// Scale the data
		Vector3f terrainScale = new Vector3f(4, 0.0575f, 4);
		// create a terrainblock
		TerrainBlock tb = new TerrainBlock("Terrain", heightMap.getSize(),
				terrainScale, heightMap.getHeightMap(), new Vector3f(0, 0, 0),
				false);

		tb.setModelBound(new BoundingBox());
		tb.updateModelBound();

		// generate a terrain texture with 2 textures
		ProceduralTextureGenerator pt = new ProceduralTextureGenerator(
				heightMap);
		pt.addTexture(new ImageIcon(TestArea1.class.getClassLoader()
				.getResource("data/texture/grassb.png")), -128, 0, 128);
		pt.addTexture(new ImageIcon(TestArea1.class.getClassLoader()
				.getResource("data/texture/dirt.jpg")), 0, 128, 255);
		pt.addTexture(new ImageIcon(TestArea1.class.getClassLoader()
				.getResource("data/texture/highest.jpg")), 128, 255, 384);
		pt.createTexture(32);

		// assign the texture to the terrain
		TextureState ts = display.getRenderer().createTextureState();
		Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR, true);
		ts.setTexture(t1, 0);

		// load a detail texture and set the combine modes for the two terrain
		// textures.
		Texture t2 = TextureManager.loadTexture(TestArea1.class
				.getClassLoader().getResource("data/texture/Detail.jpg"),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);

		ts.setTexture(t2, 1);
		t2.setWrap(Texture.WM_WRAP_S_WRAP_T);

		t1.setApply(Texture.AM_COMBINE);
		t1.setCombineFuncRGB(Texture.ACF_MODULATE);
		t1.setCombineSrc0RGB(Texture.ACS_TEXTURE);
		t1.setCombineOp0RGB(Texture.ACO_SRC_COLOR);
		t1.setCombineSrc1RGB(Texture.ACS_PRIMARY_COLOR);
		t1.setCombineOp1RGB(Texture.ACO_SRC_COLOR);
		t1.setCombineScaleRGB(1.0f);

		t2.setApply(Texture.AM_COMBINE);
		t2.setCombineFuncRGB(Texture.ACF_ADD_SIGNED);
		t2.setCombineSrc0RGB(Texture.ACS_TEXTURE);
		t2.setCombineOp0RGB(Texture.ACO_SRC_COLOR);
		t2.setCombineSrc1RGB(Texture.ACS_PREVIOUS);
		t2.setCombineOp1RGB(Texture.ACO_SRC_COLOR);
		t2.setCombineScaleRGB(1.0f);

		tb.setRenderState(ts);
		// set the detail parameters.
		tb.setDetailTexture(1, 16);
		tb.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
		rootNode.attachChild(tb);

	}

	private void createSkyBox() {

		Texture skyTex_N = TextureManager.loadTexture(TestArea1.class
				.getClassLoader().getResource(
						"texture/skybox/skyely1/rightxl.jpg"),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);

		Texture skyTex_S = TextureManager.loadTexture(TestArea1.class
				.getClassLoader().getResource(
						"texture/skybox/skyely1/leftxl.jpg"),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);

		Texture skyTex_W = TextureManager.loadTexture(TestArea1.class
				.getClassLoader().getResource(
						"texture/skybox/skyely1/frontxl.jpg"),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);

		Texture skyTex_E = TextureManager.loadTexture(TestArea1.class
				.getClassLoader().getResource(
						"texture/skybox/skyely1/backxl.jpg"),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);

		Texture skyTex_U = TextureManager.loadTexture(TestArea1.class
				.getClassLoader().getResource(
						"texture/skybox/skyely1/topxl.jpg"),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);

		Texture skyTex_D = TextureManager.loadTexture(TestArea1.class
				.getClassLoader().getResource("texture/ground/terrain1.jpg"),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);

		sky = new Skybox("Sky", 2800f, 500f, 2800f);
		sky.setTexture(Skybox.NORTH, skyTex_N);
		sky.setTexture(Skybox.SOUTH, skyTex_S);
		sky.setTexture(Skybox.WEST, skyTex_W);
		sky.setTexture(Skybox.EAST, skyTex_E);
		sky.setTexture(Skybox.UP, skyTex_U);
		// skyTex_D.setScale(new Vector3f(1000f, 1000f, 1000f));
		// skyTex_D.setWrap(1);
		sky.setTexture(Skybox.DOWN, skyTex_D);
		sky.preloadTextures();

		sky.setLocalTranslation(new Vector3f(0f, 100f, 0f));

		rootNode.attachChild(sky);
	}

	private void createRndBox() {
		float dimX = (float) Math.random() * 8 + 2;
		float dimY = (float) Math.random() * 8 + 2;
		float dimZ = (float) Math.random() * 8 + 2;
		DynamicPhysicsNode dpNodeBox = getPhysicsSpace().createDynamicNode();
		Box box = new Box("Box", new Vector3f(0f, 0f, 0f), dimX, dimY, dimZ);
		dpNodeBox.attachChild(box);
		dpNodeBox.generatePhysicsGeometry();
		PhysicsBox pn = dpNodeBox.createBox("PhyBox");
		float dDimX = dimX * 2;
		float dDimY = dimY * 2;
		float dDimZ = dimZ * 2;
		pn.setLocalScale(new Vector3f(dDimX, dDimY, dDimZ));
		dpNodeBox.setMaterial(Material.CONCRETE);
		dpNodeBox.setMass(10f);
		dpNodeBox.getLocalTranslation().set(((float) Math.random() * 150) - 75,
				((float) Math.random() * 1500) + 2000,
				((float) Math.random() * 150) - 75); // move to position

		TextureState ts = display.getRenderer().createTextureState();
		Texture t0 = TextureManager.loadTexture(TestArea1.class
				.getClassLoader().getResource("westworld2.jpg"),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
		// Texture t1 = TextureManager.loadTexture(
		// TestArea1.class.getClassLoader().getResource(
		// "test2.jpg"),
		// Texture.MM_LINEAR_LINEAR,
		// Texture.FM_LINEAR);
		// t1.setEnvironmentalMapMode(Texture.EM_SPHERE);
		// t0.setScale(new Vector3f(100f, 100f, 100f));
		ts.setTexture(t0, 0);
		ts.setEnabled(true);

		// torus.setRenderState(ts);
		dpNodeBox.setRenderState(ts);
		rootNode.attachChild(dpNodeBox); // add to scene

	}

	private Spatial createTerrain() {

		// fpsNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
		/*
		 * DirectionalLight dl = new DirectionalLight(); dl.setDiffuse(new
		 * ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f)); dl.setDirection(new Vector3f(1,
		 * -0.5f, 1)); dl.setEnabled(true); lightState.attach(dl);
		 * 
		 * DirectionalLight dr = new DirectionalLight(); dr.setEnabled(true);
		 * dr.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
		 * dr.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
		 * dr.setDirection(new Vector3f(0.5f, -0.5f, 0));
		 * 
		 * lightState.attach(dr);
		 * 
		 */
		display.getRenderer().setBackgroundColor(
				new ColorRGBA(0.5f, 0.5f, 0.5f, 1));

		FaultFractalHeightMap heightMap = new FaultFractalHeightMap(257, 32, 0,
				255, 0.75f, 3);
		Vector3f terrainScale = new Vector3f(10, 1, 10);
		heightMap.setHeightScale(0.001f);
		TerrainPage page = new TerrainPage("Terrain", 33, heightMap.getSize(),
				terrainScale, heightMap.getHeightMap(), false);
		page.setDetailTexture(1, 16);

		CullState cs = DisplaySystem.getDisplaySystem().getRenderer()
				.createCullState();
		cs.setCullMode(CullState.CS_BACK);
		cs.setEnabled(true);
		page.setRenderState(cs);

		ProceduralTextureGenerator pt = new ProceduralTextureGenerator(
				heightMap);
		pt.addTexture(new ImageIcon(TestArea1.class.getClassLoader()
				.getResource("jmetest/data/texture/grassb.png")), -128, 0, 128);
		pt.addTexture(new ImageIcon(TestArea1.class.getClassLoader()
				.getResource("jmetest/data/texture/dirt.jpg")), 0, 128, 255);
		pt.addTexture(new ImageIcon(TestArea1.class.getClassLoader()
				.getResource("jmetest/data/texture/highest.jpg")), 128, 255,
				384);

		pt.createTexture(512);

		TextureState ts = DisplaySystem.getDisplaySystem().getRenderer()
				.createTextureState();
		ts.setEnabled(true);
		Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR, true);
		ts.setTexture(t1, 0);

		Texture t2 = TextureManager.loadTexture(TestArea1.class
				.getClassLoader()
				.getResource("jmetest/data/texture/Detail.jpg"),
				Texture.MM_LINEAR_LINEAR, Texture.FM_LINEAR);
		ts.setTexture(t2, 1);
		t2.setWrap(Texture.WM_WRAP_S_WRAP_T);

		t1.setApply(Texture.AM_COMBINE);
		t1.setCombineFuncRGB(Texture.ACF_MODULATE);
		t1.setCombineSrc0RGB(Texture.ACS_TEXTURE);
		t1.setCombineOp0RGB(Texture.ACO_SRC_COLOR);
		t1.setCombineSrc1RGB(Texture.ACS_PRIMARY_COLOR);
		t1.setCombineOp1RGB(Texture.ACO_SRC_COLOR);
		t1.setCombineScaleRGB(1.0f);

		t2.setApply(Texture.AM_COMBINE);
		t2.setCombineFuncRGB(Texture.ACF_ADD_SIGNED);
		t2.setCombineSrc0RGB(Texture.ACS_TEXTURE);
		t2.setCombineOp0RGB(Texture.ACO_SRC_COLOR);
		t2.setCombineSrc1RGB(Texture.ACS_PREVIOUS);
		t2.setCombineOp1RGB(Texture.ACO_SRC_COLOR);
		t2.setCombineScaleRGB(1.0f);
		page.setRenderState(ts);

		/*
		 * FogState fs = DisplaySystem.getDisplaySystem().getRenderer()
		 * .createFogState(); fs.setDensity(0.5f); fs.setEnabled(true);
		 * fs.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f)); fs.setEnd(1000);
		 * fs.setStart(500); fs.setDensityFunction(FogState.DF_LINEAR);
		 * fs.setApplyFunction(FogState.AF_PER_VERTEX); page.setRenderState(fs);
		 */
		return page;
	}

	public static void main(String[] args) {
		LoggingSystem.getLogger().setLevel(Level.WARNING);
		new TestArea1().start();
	}

	private static class SteerAction extends InputAction {
		private final JointAxis axis1;

		private float velocity;

		public SteerAction(JointAxis axis1, float velocity) {
			this.axis1 = axis1;
			this.velocity = velocity;
		}

		public void performAction(InputActionEvent evt) {
			if (evt.getTriggerPressed()) {
				axis1.setDesiredVelocity(velocity);
			} else {
				axis1.setDesiredVelocity(0);
			}
		}
	}
}

/*
 * $log$
 */

