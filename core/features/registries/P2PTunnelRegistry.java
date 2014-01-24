package appeng.core.features.registries;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.Blocks;
import appeng.api.definitions.Parts;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.util.Platform;

public class P2PTunnelRegistry implements IP2PTunnelRegistry
{

	HashMap<ItemStack, TunnelType> Tunnels = new HashMap();

	public void configure()
	{
		/**
		 * attune based on most redstone base items.
		 */
		addNewAttunement( new ItemStack( Item.redstone ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Item.redstoneRepeater ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Block.redstoneLampActive ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Block.redstoneLampIdle ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Block.redstoneComparatorActive ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Block.redstoneComparatorIdle ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Block.redstoneRepeaterActive ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Block.redstoneRepeaterIdle ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Block.redstoneWire ), TunnelType.REDSTONE );
		addNewAttunement( new ItemStack( Block.blockRedstone ), TunnelType.REDSTONE );

		/**
		 * attune based on lots of random item related stuff
		 */
		Blocks Blocks = AEApi.instance().blocks();
		Parts Parts = AEApi.instance().parts();

		addNewAttunement( Blocks.blockInterface.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partInterface.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partStorageBus.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partImportBus.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( Parts.partExportBus.stack( 1 ), TunnelType.ITEM );
		addNewAttunement( new ItemStack( Block.hopperBlock ), TunnelType.ITEM );
		addNewAttunement( new ItemStack( Block.chest ), TunnelType.ITEM );
		addNewAttunement( new ItemStack( Block.chestTrapped ), TunnelType.ITEM );

		/**
		 * attune based on lots of random item related stuff
		 */
		addNewAttunement( new ItemStack( Item.bucketEmpty ), TunnelType.FLUID );
		addNewAttunement( new ItemStack( Item.bucketLava ), TunnelType.FLUID );
		addNewAttunement( new ItemStack( Item.bucketMilk ), TunnelType.FLUID );
		addNewAttunement( new ItemStack( Item.bucketWater ), TunnelType.FLUID );
	}

	@Override
	public void addNewAttunement(ItemStack trigger, TunnelType type)
	{
		if ( type == null )
			throw new RuntimeException( "Invalid Tunnel Type." );

		Tunnels.put( trigger, type );
	}

	@Override
	public TunnelType getTunnelTypeByItem(ItemStack trigger)
	{
		if ( FluidContainerRegistry.isContainer( trigger ) )
			return TunnelType.FLUID;

		for (ItemStack is : Tunnels.keySet())
		{
			if ( Platform.isSameItemType( is, trigger ) )
				return Tunnels.get( is );
		}

		return null;
	}

}
