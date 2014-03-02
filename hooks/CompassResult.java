package appeng.hooks;

public class CompassResult
{

	public final boolean hasResult;
	public final boolean spin;
	public final double rad;
	public final long time;

	public boolean requested = false;

	public CompassResult(boolean hasResult, boolean spin, double rad) {
		this.hasResult = hasResult;
		this.spin = spin;
		this.rad = rad;
		this.time = System.currentTimeMillis();
	}

}
