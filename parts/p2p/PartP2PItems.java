package appeng.parts.p2p;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.TunnelType;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.core.AppEng;
import appeng.integration.abstraction.IBC;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.tile.inventory.AppEngNullInventory;
import appeng.util.Platform;
import appeng.util.inv.WrapperBCPipe;
import appeng.util.inv.WrapperChainedInventory;
import appeng.util.inv.WrapperMCISidedInventory;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Interface(iface = "buildcraft.api.transport.IPipeConnection", modid = "BuildCraftAPI|transport")
public class PartP2PItems extends PartP2PTunnel<PartP2PItems> implements IPipeConnection, IInventory, ISidedInventory
{

	public TunnelType getTunnelType()
	{
		return TunnelType.ITEM;
	}

	public PartP2PItems(ItemStack is) {
		super( is );
	}

	int oldSize = 0;
	IInventory cachedInv;

	LinkedList<IInventory> which = new LinkedList<IInventory>();

	IInventory getOutputInv()
	{
		IInventory output = null;

		if ( proxy.isActive() )
		{
			TileEntity te = tile.getWorldObj().getTileEntity( tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ );

			if ( which.contains( this ) )
				return null;

			which.add( this );

			if ( AppEng.instance.isIntegrationEnabled( "BC" ) )
			{
				IBC buildcraft = (IBC) AppEng.instance.getIntegration( "BC" );
				if ( buildcraft != null )
				{
					if ( buildcraft.isPipe( te, side.getOpposite() ) )
					{
						try
						{
							output = new WrapperBCPipe( te, side.getOpposite() );
						}
						catch (Throwable _)
						{
						}
					}
				}
			}

			/*
			 * if ( AppEng.instance.isIntegrationEnabled( "TE" ) ) { ITE thermal = (ITE) AppEng.instance.getIntegration(
			 * "TE" ); if ( thermal != null ) { if ( thermal.isPipe( te, side.getOpposite() ) ) { try { output = new
			 * WrapperTEPipe( te, side.getOpposite() ); } catch (Throwable _) { } } } }
			 */

			if ( output == null )
			{
				if ( te instanceof TileEntityChest )
				{
					output = Platform.GetChestInv( te );
				}
				else if ( te instanceof ISidedInventory )
				{
					output = new WrapperMCISidedInventory( (ISidedInventory) te, side.getOpposite() );
				}
				else if ( te instanceof IInventory )
				{
					output = (IInventory) te;
				}
			}

			which.pop();
		}

		return output;
	}

	@Override
	public void onNeighborChanged()
	{
		cachedInv = null;
		PartP2PItems input = getInput();
		if ( input != null && output )
			input.onChange();
	}

	IInventory getDest()
	{
		if ( cachedInv != null )
			return cachedInv;

		List<IInventory> outs = new LinkedList<IInventory>();
		TunnelCollection<PartP2PItems> itemTunnels;

		try
		{
			itemTunnels = getOutputs();
		}
		catch (GridAccessException e)
		{
			return new AppEngNullInventory();
		}

		for (PartP2PItems t : itemTunnels)
		{
			IInventory inv = t.getOutputInv();
			if ( inv != null )
			{
				if ( Platform.getRandomInt() % 2 == 0 )
					outs.add( inv );
				else
					outs.add( 0, inv );
			}
		}

		return cachedInv = new WrapperChainedInventory( outs );
	}

	@MENetworkEventSubscribe
	public void changeStateA(MENetworkBootingStatusChange bs)
	{
		if ( !output )
		{
			cachedInv = null;
			int olderSize = oldSize;
			oldSize = getDest().getSizeInventory();
			if ( olderSize != oldSize )
			{
				getHost().partChanged();
				tile.getWorldObj().notifyBlocksOfNeighborChange( tile.xCoord, tile.yCoord, tile.zCoord, Platform.air );
			}
		}
	}

	@MENetworkEventSubscribe
	public void changeStateB(MENetworkChannelsChanged bs)
	{
		if ( !output )
		{
			cachedInv = null;
			int olderSize = oldSize;
			oldSize = getDest().getSizeInventory();
			if ( olderSize != oldSize )
			{
				getHost().partChanged();
				tile.getWorldObj().notifyBlocksOfNeighborChange( tile.xCoord, tile.yCoord, tile.zCoord, Platform.air );
			}
		}
	}

	@MENetworkEventSubscribe
	public void changeStateC(MENetworkPowerStatusChange bs)
	{
		if ( !output )
		{
			cachedInv = null;
			int olderSize = oldSize;
			oldSize = getDest().getSizeInventory();
			if ( olderSize != oldSize )
			{
				getHost().partChanged();
				tile.getWorldObj().notifyBlocksOfNeighborChange( tile.xCoord, tile.yCoord, tile.zCoord, Platform.air );
			}
		}
	}

	@Override
	public void onChange()
	{
		if ( !output )
		{
			cachedInv = null;
			int olderSize = oldSize;
			oldSize = getDest().getSizeInventory();
			if ( olderSize != oldSize )
			{
				getHost().partChanged();
				tile.getWorldObj().notifyBlocksOfNeighborChange( tile.xCoord, tile.yCoord, tile.zCoord, Platform.air );
			}
		}
		else
		{
			PartP2PItems input = getInput();
			if ( input != null )
				input.onChange();
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.hopper.getBlockTextureFromSide( 0 );
	}

	@Override
	public int getSizeInventory()
	{
		return getDest().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return getDest().getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return getDest().decrStackSize( i, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		getDest().setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getInventoryName()
	{
		return null;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return getDest().getInventoryStackLimit();
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, net.minecraft.item.ItemStack itemstack)
	{
		return getDest().isItemValidForSlot( i, itemstack );
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		int[] slots = new int[getSizeInventory()];
		for (int x = 0; x < getSizeInventory(); x++)
			slots[x] = x;
		return slots;
	}

	public float getPowerDrainPerTick()
	{
		return 2.0f;
	};

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return getDest().isItemValidForSlot( i, itemstack );
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return false;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return false;
	}

	@Override
	@Method(modid = "BuildCraftAPI|transport")
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with)
	{
		return side.equals( with ) && type == PipeType.ITEM ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
	}

	@Override
	public void markDirty()
	{
		// eh?
	}

}
