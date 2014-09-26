package rblocks.api;

/**
 * These methods are added to the Block Class.
 */
public interface IRBMethods
{

	/**
	 * @returns true or false based on previously set value, or null if a value has not been set.
	 */
	Boolean isRotableBlockSupported();

	/**
	 * updates the field for the supported flag.
	 * 
	 * @param supported
	 * @return supported
	 */
	boolean setRotableBlockSupported(boolean supported);

	/**
	 * Override this method if you wish to render your rotatable block instead of using the default renderer.
	 * 
	 * @return the normal renderType value, or Rotablable Blocks render type depending on if the block is supported.
	 */
	int getRealRenderType();

}
