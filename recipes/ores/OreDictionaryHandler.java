package appeng.recipes.ores;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.oredict.OreDictionary;
import appeng.core.AELog;
import appeng.recipes.game.IRecipeBakeable;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class OreDictionaryHandler
{

	public static final OreDictionaryHandler instance = new OreDictionaryHandler();

	private List<IOreListener> ol = new ArrayList<IOreListener>();

	private boolean enableRebaking = false;

	/**
	 * Just limit what items are sent to the final listeners, I got sick of strange items showing up...
	 * 
	 * @param name
	 * @return
	 */
	private boolean shouldCare(String name)
	{
		return true;
	}

	@SubscribeEvent
	public void onOreDictionaryRegister(OreDictionary.OreRegisterEvent event)
	{
		if ( event.Name == null || event.Ore == null )
			return;

		if ( shouldCare( event.Name ) )
		{
			for (IOreListener v : ol)
				v.oreRegistered( event.Name, event.Ore );
		}

		if ( enableRebaking )
			bakeRecipes();
	}

	/**
	 * Adds a new IOreListener and immediately notifies it of any previous ores, any ores added latter will be added at
	 * that point.
	 * 
	 * @param n
	 */
	public void observe(IOreListener n)
	{
		ol.add( n );

		// notify the listener of any ore already in existence.
		for (String name : OreDictionary.getOreNames())
		{
			if ( name != null && shouldCare( name ) )
			{
				for (ItemStack item : OreDictionary.getOres( name ))
				{
					if ( item != null )
						n.oreRegistered( name, item );
				}
			}
		}
	}

	public void bakeRecipes()
	{
		enableRebaking = true;

		for (Object o : CraftingManager.getInstance().getRecipeList())
		{
			if ( o instanceof IRecipeBakeable )
			{
				try
				{
					((IRecipeBakeable) o).bake();
				}
				catch (Throwable e)
				{
					AELog.error( e );
				}
			}
		}
	}

}
