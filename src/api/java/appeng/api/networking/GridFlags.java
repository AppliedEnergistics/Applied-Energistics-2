package appeng.api.networking;

/**
 * Various flags to determine network node behavior.
 */
public enum GridFlags
{
	/**
	 * import/export buses, terminals, and other devices that use network features, will use this setting.
	 */
	REQUIRE_CHANNEL,

	/**
	 * P2P ME tunnels use this setting.
	 */
	COMPRESSED_CHANNEL,

	/**
	 * cannot carry channels over this node.
	 */
	CANNOT_CARRY,

	/**
	 * Used by P2P Tunnels to prevent tunnels from tunneling recursively.
	 */
	CANNOT_CARRY_COMPRESSED,

	/**
	 * This node can transmit 32 signals, this should only apply to Tier2 Cable, P2P Tunnels, and Quantum Network
	 * Bridges.
	 */
	DENSE_CAPACITY,

	/**
	 * This block is part of a multiblock, used in conjunction with REQUIRE_CHANNEL, and {@link IGridMultiblock} see this
	 * interface for details.
	 */
	MULTIBLOCK,

	/**
	 * Indicates which path might be preferred, this only matters if two routes of equal length exist, ad only changes
	 * the order they are processed in.
	 */
	PREFERRED
}
