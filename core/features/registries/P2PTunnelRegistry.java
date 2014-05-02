package appeng.core.features.registries;

import java.util.HashMap;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.Parts;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.util.AEColor;
import appeng.util.Platform;

public class P2PTunnelRegistry implements IP2PTunnelRegistry
{

	HashMap<ItemStack, TunnelType> Tunnels = new HashMap();

	public void configure()
	{
		/**
		 * attune based on most redstone base items.
		 */
		addNewAttunement( new ItemStack( Items.redstone ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Items.repeater ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.redstone_lamp ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.unpowered_comparator ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.powered_comparator ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.powered_repeater ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.unpowered_repeater ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.daylight_detector ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.redstone_wire ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.redstone_block ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Blocks.lever ), TunnelType.REDSTONE );

		/**
		 * attune based on lots of random item related stuff
		 */
		appeng.api.definitions.Blocks AEBlocks = AEApi.instance().blocks();
		Parts Parts = AEApi.instance().parts();

		addNewAttunement( AEBlocks.blockInterface.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partInterface.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partStorageBus.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partImportBus.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partExportBus.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( new ItemStack( Blocks.hopper ), TunnelType.ITEM );
		addNewAttunement( new ItemStack( Blocks.chest ), TunnelType.ITEM );
		addNewAttunement( new ItemStack( Blocks.trapped_chest ), TunnelType.ITEM );

		/**
		 * attune based on lots of random item related stuff
		 */
		addNewAttunement( new ItemStack( Items.bucket ), TunnelType.FLUID );
		addNewAttunement( new ItemStack( Items.lava_bucket ), TunnelType.FLUID );
		addNewAttunement( new ItemStack( Items.milk_bucket ), TunnelType.FLUID );
		addNewAttunement( new ItemStack( Items.water_bucket ), TunnelType.FLUID );

		for (AEColor c : AEColor.values())
		{
			addNewAttunement( Parts.partCableGlass.stack( c, 1 ), TunnelType.ME );
			addNewAttunement( Parts.partCableCovered.stack( c, 1 ), TunnelType.ME );
			addNewAttunement( Parts.partCableSmart.stack( c, 1 ), TunnelType.ME );
			addNewAttunement( Parts.partCableDense.stack( c, 1 ), TunnelType.ME );
		}
	}

	@Override
	public void addNewAttunement(ItemStack trigger, TunnelType type)
	{
		if ( type == null )
			return;

		Tunnels.put( trigger, type );
	}

	@Override
	public TunnelType getTunnelTypeByItem(ItemStack trigger)
	{
		if ( FluidContainerRegistry.isContainer( trigger ) )
			return TunnelType.FLUID;

		for (ItemStack is : Tunnels.keySet())
		{
			if ( is.getItem() == trigger.getItem() && is.getItemDamage() == OreDictionary.WILDCARD_VALUE )
				return Tunnels.get( is );

			if ( Platform.isSameItem( is, trigger ) )
				return Tunnels.get( is );
		}

		return null;
	}

}
