package appeng.parts.p2p;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import appeng.api.config.TunnelType;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartP2PLight extends PartP2PTunnel<PartP2PLight> implements IGridTickable
{

	public PartP2PLight(ItemStack is) {
		super( is );
	}

	public TunnelType getTunnelType()
	{
		return TunnelType.LIGHT;
	}

	int lastValue = 0;

	public void setLightLevel(int out)
	{
		lastValue = out;
		getHost().markForUpdate();
	}

	@Override
	public int getLightLevel()
	{
		if ( output && isPowered() )
			return blockLight( lastValue );

		return 0;
	}

	private int blockLight(int emit)
	{
		TileEntity te = this.getTile();
		float opacity = 255 - te.getWorldObj().getBlockLightOpacity( te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ );
		return (int) (emit * (opacity / 255.0f));
	}

	@Override
	public void chanRender(MENetworkChannelsChanged c)
	{
		onTunnelNetworkChange();
		super.chanRender( c );
	}

	@Override
	public void powerRender(MENetworkPowerStatusChange c)
	{
		onTunnelNetworkChange();
		super.powerRender( c );
	}

	@Override
	public void onTunnelNetworkChange()
	{
		if ( output )
		{
			PartP2PLight src = getInput();
			if ( src != null && src.proxy.isActive() )
				setLightLevel( src.lastValue );
			else
				getHost().markForUpdate();
		}
		else
			doWork();
	}

	@Override
	public void onTunnelConfigChange()
	{
		onTunnelNetworkChange();
	}

	@Override
	public void onNeighborChanged()
	{
		doWork();
		if ( output )
			getHost().markForUpdate();
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT( tag );
		tag.setInteger( "lastValue", lastValue );
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT( tag );
		lastValue = tag.getInteger( "lastValue" );
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException
	{
		super.readFromStream( data );
		lastValue = data.readInt();
		output = lastValue > 0;
		return false;
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException
	{
		super.writeToStream( data );
		data.writeInt( output ? lastValue : 0 );
	}

	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.quartz_block.getBlockTextureFromSide( 0 );
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.LightTunnel.min, TickRates.LightTunnel.max, false, false );
	}

	private boolean doWork()
	{
		if ( output )
			return false;

		TileEntity te = getTile();
		World w = te.getWorldObj();

		int newLevel = w.getBlockLightValue( te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ );

		if ( lastValue != newLevel && proxy.isActive() )
		{
			lastValue = newLevel;
			try
			{
				for (PartP2PLight out : getOutputs())
					out.setLightLevel( lastValue );
			}
			catch (GridAccessException e)
			{
				// :P
			}
			return true;
		}
		return false;
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		return doWork() ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}
}
