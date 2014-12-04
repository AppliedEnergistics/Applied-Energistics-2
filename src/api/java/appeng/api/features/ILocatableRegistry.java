package appeng.api.features;

/**
 * A Registry for locatable items, works based on serial numbers.
 */
public interface ILocatableRegistry
{

	/**
	 * Attempts to find the object with the serial specified, if it can it
	 * returns the object.
	 * 
	 * @param serial serial
	 * @return requestedObject, or null
	 */
	public abstract Object findLocatableBySerial(long serial);

}