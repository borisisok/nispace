package de.neuron.scratchpad.wiimote;

import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;

public class WiiGunAPosAction implements WiiIrAPosHandler, Runnable{
	
	CameraNode camNode;
	Node gun;
    Vector3f camLockAxis = new Vector3f();
    Matrix3f incr = new Matrix3f();
    Matrix3f tempMa = new Matrix3f();
    Matrix3f tempMb = new Matrix3f();
	Quaternion xRot = new Quaternion();
	Quaternion yRot = new Quaternion();
	Quaternion cxRot = new Quaternion();
	Quaternion gunRot;
	Vector3f gunTrans = new Vector3f();
	float cy = 0;
	float cx = 0;

    public WiiGunAPosAction (CameraNode iCamNode, Node iGun){
    	camNode = iCamNode;
    	gun = iGun;
    	camLockAxis = camNode.getLocalRotation().getRotationColumn( 1 );
    }
    
	public void run() {
		// infinit cam view mover thread
		try {
		while (true){
			if (cy != 0f || cx != 0) {

		        incr.loadIdentity();
			    incr.fromAngleNormalAxis(cy, camLockAxis);
			    camNode.getLocalRotation().fromRotationMatrix(
		                incr.mult(camNode.getLocalRotation().toRotationMatrix(tempMa),
		                        tempMb));
			    camNode.getLocalRotation().normalize();
				cxRot.fromAngleAxis(cx, Vector3f.UNIT_X);
				camNode.getLocalRotation().multLocal(cxRot);
			}
			Thread.sleep(20l);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}    
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.neuron.scratchpad.wiimote.WiiIrAPosHandler#handleWiiAPos(float,
	 *      float)
	 */
	public void handleWiiAPos(float x, float y) {

		// set gun rotation
		yRot.fromAngleAxis((y - 0.5f) * -4, Vector3f.UNIT_Y);
		gunRot = gun.getLocalRotation();
		gunRot.set(yRot);
		xRot.fromAngleAxis((x - 0.5f) * 2, Vector3f.UNIT_X);
		gunRot.multLocal(xRot);
		gunTrans.set(((y - 0.5f) * -5),
				((x - 0.5f) * -1) - 5, 3f);
		gun.setLocalTranslation(gunTrans);

		// set cam movement vars 
		cy = 0;
		cx = 0;

		if (y < 0.3) {
			cy = 0.3f - y;
		} else if (y > 0.7) {
			cy = (y - 0.7f)*-1;
		}
		if (x < 0.3) {
			cx =  (0.3f - x ) * -1;
		} else if (x > 0.7) {
			cx = (x - 0.7f);
		}
	}


}
