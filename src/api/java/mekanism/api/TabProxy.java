package mekanism.api;

import net.minecraft.creativetab.CreativeTabs;

/**
 * Class used to indirectly reference the Mekanism creative tab.
 * @author AidanBrady
 *
 */
public final class TabProxy
{
	/** The 'Mekanism' class where the tab instance is stored. */
	public static Class Mekanism;

	/**
	 * Attempts to get the Mekanism creative tab instance from the 'Mekanism' class. Will return
	 * the tab if the mod is loaded, but otherwise will return the defined 'preferred' creative tab. This way
	 * you don't need to worry about NPEs!
	 * @return Mekanism creative tab if can, otherwise preferred tab
	 */
	public static CreativeTabs tabMekanism(CreativeTabs preferred)
	{
		try {
			if(Mekanism == null)
			{
				Mekanism = Class.forName("mekanism.common.Mekanism");
			}

			Object ret = Mekanism.getField("tabMekanism").get(null);

			if(ret instanceof CreativeTabs)
			{
				return (CreativeTabs)ret;
			}

			return preferred;
		} catch(Exception e) {
			System.err.println("Error retrieving Mekanism creative tab.");
			return preferred;
		}
	}
}
