package appeng.parts.reporting;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PartConversionMonitor extends PartStorageMonitor
{

	public PartConversionMonitor(ItemStack is) {
		super( PartConversionMonitor.class, is );
		frontBright = CableBusTextures.PartConvMonitor_Bright;
		frontColored = CableBusTextures.PartConvMonitor_Colored;
		frontDark = CableBusTextures.PartConvMonitor_Dark;
		frontSolid = CableBusTextures.PartConvMonitor_Solid;
	}

	@Override
	public boolean onShiftActivate(EntityPlayer player, Vec3 pos)
	{
		if ( Platform.isClient() )
			return true;

		boolean ModeB = false;

		ItemStack item = player.getCurrentEquippedItem();
		if ( item == null && getDisplayed() != null )
		{
			ModeB = true;
			item = ((IAEItemStack) getDisplayed()).getItemStack();
		}

		if ( item != null )
		{
			try
			{
				if ( !proxy.isActive() )
					return false;

				IEnergySource energy = proxy.getEnergy();
				IMEMonitor<IAEItemStack> cell = proxy.getStorage().getItemInventory();
				IAEItemStack input = AEItemStack.create( item );

				if ( ModeB )
				{
					for (int x = 0; x < player.inventory.getSizeInventory(); x++)
					{
						ItemStack targetStack = player.inventory.getStackInSlot( x );
						if ( input.equals( targetStack ) )
						{
							IAEItemStack insertItem = input.copy();
							insertItem.setStackSize( targetStack.stackSize );
							IAEItemStack failedToInsert = Platform.poweredInsert( energy, cell, insertItem, new PlayerSource( player, this ) );
							player.inventory.setInventorySlotContents( x, failedToInsert == null ? null : failedToInsert.getItemStack() );
						}
					}
				}
				else
				{
					IAEItemStack failedToInsert = Platform.poweredInsert( energy, cell, input, new PlayerSource( player, this ) );
					player.inventory.setInventorySlotContents( player.inventory.currentItem, failedToInsert == null ? null : failedToInsert.getItemStack() );
				}
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
		return true;
	}

	protected void extractItem(EntityPlayer player)
	{
		IAEItemStack input = (IAEItemStack) getDisplayed();
		if ( input != null )
		{
			try
			{
				if ( !proxy.isActive() )
					return;

				IEnergySource energy = proxy.getEnergy();
				IMEMonitor<IAEItemStack> cell = proxy.getStorage().getItemInventory();

				ItemStack is = input.getItemStack();
				input.setStackSize( is.getMaxStackSize() );

				IAEItemStack retrieved = Platform.poweredExtraction( energy, cell, input, new PlayerSource( player, this ) );
				if ( retrieved != null )
				{
					ItemStack newItems = retrieved.getItemStack();
					InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
					newItems = adaptor.addItems( newItems );
					if ( newItems != null )
					{
						TileEntity te = tile;
						List<ItemStack> list = Arrays.asList( new ItemStack[] { newItems } );
						Platform.spawnDrops( player.worldObj, te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ, list );
					}

					if ( player.openContainer != null )
						player.openContainer.detectAndSendChanges();
				}
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
	}

}
