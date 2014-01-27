package appeng.parts.p2p;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.config.TunnelType;
import appeng.core.AppEng;
import appeng.integration.abstraction.IBC;
import appeng.integration.abstraction.ITE;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.tile.inventory.AppEngNullInventory;
import appeng.util.Platform;
import appeng.util.inv.WrapperBCPipe;
import appeng.util.inv.WrapperChainedInventory;
import appeng.util.inv.WrapperMCISidedInventory;
import appeng.util.inv.WrapperTEPipe;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
			TileEntity te = tile.worldObj.getBlockTileEntity( tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ );

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

			if ( AppEng.instance.isIntegrationEnabled( "TE" ) )
			{
				ITE thermal = (ITE) AppEng.instance.getIntegration( "TE" );
				if ( thermal != null )
				{
					if ( thermal.isPipe( te, side.getOpposite() ) )
					{
						try
						{
							output = new WrapperTEPipe( te, side.getOpposite() );
						}
						catch (Throwable _)
						{
						}
					}
				}
			}

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
		if ( input != null )
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

	@Override
	public void onChange()
	{
		cachedInv = null;
		int olderSize = oldSize;
		oldSize = getDest().getSizeInventory();
		if ( olderSize != oldSize )
		{
			getHost().PartChanged();
			tile.worldObj.notifyBlocksOfNeighborChange( tile.xCoord, tile.yCoord, tile.zCoord, 0 );
		}
	}

	@SideOnly(Side.CLIENT)
	public Icon getTypeTexture()
	{
		return Block.hopperBlock.getBlockTextureFromSide( 0 );
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
	public String getInvName()
	{
		return null;
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return getDest().getInventoryStackLimit();
	}

	@Override
	public void openChest()
	{

	}

	@Override
	public void closeChest()
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
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with)
	{
		return side.equals( with ) && type == PipeType.ITEM ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
	}

	@Override
	public void onInventoryChanged()
	{
		// eh?
	}

}
