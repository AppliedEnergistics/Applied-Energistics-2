package appeng.api.config;

/**
 * Represent the security systems basic permissions, these are not for anti-griefing, they are part of the mod as a
 * gameplay feature.
 */
public enum SecurityPermissions
{
	/**
	 * required to insert items into the network via terminal ( also used for machines based on the owner of the
	 * network, which is determined by its Security Block. )
	 */
	INJECT,

	/**
	 * required to extract items from the network via terminal ( also used for machines based on the owner of the
	 * network, which is determined by its Security Block. )
	 */
	EXTRACT,

	/**
	 * required to request crafting from the network via terminal.
	 */
	CRAFT,

	/**
	 * required to modify automation, and make modifications to the networks physical layout.
	 */
	BUILD,

	/**
	 * required to modify the security blocks settings.
	 */
	SECURITY;

	final private String unlocalizedName = "gui.appliedenergistics2.security." + name().toLowerCase();

	public String getUnlocalizedName()
	{
		return unlocalizedName + ".name";
	}

	public String getUnlocalizedTip()
	{
		return unlocalizedName + ".tip";
	}
}
