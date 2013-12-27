package appeng.server;

public enum AccessType
{
	/**
	 * allows basic access to manipulate the block via gui, or other.
	 */
	BLOCK_ACCESS,

	/**
	 * Can player deposit items into the network.
	 */
	NETWORK_DEPOSIT,

	/**
	 * can player withdraw items from the network.
	 */
	NETWORK_WITHDRAW,

	/**
	 * can player issue crafting requests?
	 */
	NETWORK_CRAFT,

	/**
	 * can player add new blocks to the network.
	 */
	NETWORK_BUILD,

	/**
	 * can player manipulate security settings.
	 */
	NETWORK_SECURITY
}
