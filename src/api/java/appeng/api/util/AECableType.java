package appeng.api.util;

public enum AECableType
{
	/**
	 * No Cable present.
	 */
	NONE,

	/**
	 * Connections to this block should render as glass.
	 */
	GLASS,

	/**
	 * Connections to this block should render as covered.
	 */
	COVERED,

	/**
	 * Connections to this block should render as smart.
	 */
	SMART,

	/**
	 * Dense Cable, represents a tier 2 block that can carry 32 channels.
	 */
	DENSE,

}
