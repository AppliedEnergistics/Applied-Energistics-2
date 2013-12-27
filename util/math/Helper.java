package appeng.util.math;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public class Helper
{

	public static final Helper instance = new Helper();

	public Quaternion getRotation(Vector3f vForward, Vector3f vUp)
	{
		Vector3f vRight = new Vector3f();
		Vector3f.cross( vUp, vForward, vRight );

		/*
		 * vRight.x vRight.y, vRight.z, vUp.x, vUp.y, vUp.z, vForward.x,
		 * vForward.y, vForward.z
		 */

		Quaternion qrot = new Quaternion();
		qrot.w = (float) Math.sqrt( 1.0f + vRight.x + vUp.y + vForward.z ) / 2.0f;
		double dfWScale = qrot.w * 4.0;
		qrot.x = (float) ((vForward.y - vUp.z) / dfWScale);
		qrot.y = (float) ((vRight.z - vForward.x) / dfWScale);
		qrot.z = (float) ((vUp.x - vRight.y) / dfWScale);

		return qrot;
	}
}
