package appeng.parts.p2p;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import appeng.api.config.TunnelType;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.me.GridAccessException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartP2PRedstone extends PartP2PTunnel<PartP2PRedstone>
{

	public TunnelType getTunnelType()
	{
		return TunnelType.REDSTONE;
	}

	public PartP2PRedstone(ItemStack is) {
		super( is );
	}

	int power;

	@Override
	public boolean canConnectRedstone()
	{
		return true;
	}

	@Override
	public int isProvidingStrongPower()
	{
		return output ? power : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return output ? power : 0;
	}

	@Override
	public void onChange()
	{
		setNetworkReady();
	}

	@MENetworkEventSubscribe
	public void changeStateA(MENetworkBootingStatusChange bs)
	{
		setNetworkReady();
	}

	@MENetworkEventSubscribe
	public void changeStateB(MENetworkChannelsChanged bs)
	{
		setNetworkReady();
	}

	@MENetworkEventSubscribe
	public void changeStateC(MENetworkPowerStatusChange bs)
	{
		setNetworkReady();
	}

	public void setNetworkReady()
	{
		if ( output )
		{
			PartP2PRedstone in = getInput();
			if ( in != null )
				putInput( ((PartP2PRedstone) in).power );
		}
	}

	boolean recursive = false;

	protected void putInput(Object o)
	{
		if ( recursive )
			return;

		recursive = true;
		if ( output && proxy.isActive() )
		{
			int newPower = (Integer) o;
			if ( power != newPower )
			{
				power = newPower;
				notifyNeightbors();
			}
		}
		recursive = false;

	}

	public void notifyNeightbors()
	{
		World worldObj = tile.worldObj;

		int xCoord = tile.xCoord;
		int yCoord = tile.yCoord;
		int zCoord = tile.zCoord;

		worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, worldObj.getBlockId( xCoord, yCoord, zCoord ) );

		// and this cause somtimes it can go thought walls.
		worldObj.notifyBlocksOfNeighborChange( xCoord - 1, yCoord, zCoord, worldObj.getBlockId( xCoord, yCoord, zCoord ) );
		worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord - 1, zCoord, worldObj.getBlockId( xCoord, yCoord, zCoord ) );
		worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord - 1, worldObj.getBlockId( xCoord, yCoord, zCoord ) );
		worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord + 1, worldObj.getBlockId( xCoord, yCoord, zCoord ) );
		worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord + 1, zCoord, worldObj.getBlockId( xCoord, yCoord, zCoord ) );
		worldObj.notifyBlocksOfNeighborChange( xCoord + 1, yCoord, zCoord, worldObj.getBlockId( xCoord, yCoord, zCoord ) );
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT( tag );
		tag.setInteger( "power", power );
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT( tag );
		power = tag.getInteger( "power" );
	}

	@SideOnly(Side.CLIENT)
	public Icon getTypeTexture()
	{
		return Block.blockRedstone.getBlockTextureFromSide( 0 );
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	};

	@Override
	public void onNeighborChanged()
	{
		if ( !output )
		{
			int x = tile.xCoord + side.offsetX;
			int y = tile.yCoord + side.offsetY;
			int z = tile.zCoord + side.offsetZ;

			Block b = Block.blocksList[tile.worldObj.getBlockId( x, y, z )];
			if ( b != null && !output )
			{
				int srcSide = side.ordinal();
				if ( b instanceof BlockRedstoneWire )
					srcSide = 1;
				power = b.isProvidingStrongPower( tile.worldObj, x, y, z, srcSide );
				power = Math.max( power, b.isProvidingWeakPower( tile.worldObj, x, y, z, srcSide ) );
				sendToOutput( power );
			}
			else
				sendToOutput( 0 );
		}
	}

	private void sendToOutput(int power)
	{
		try
		{
			for (PartP2PRedstone rs : getOutputs())
			{
				rs.putInput( power );
			}
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

}
