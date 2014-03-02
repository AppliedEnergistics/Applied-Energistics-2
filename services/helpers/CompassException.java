package appeng.services.helpers;

public class CompassException extends RuntimeException
{

	private static final long serialVersionUID = 8825268683203860877L;

	public final Throwable inner;

	public CompassException(Throwable t) {
		inner = t;
	}

}
