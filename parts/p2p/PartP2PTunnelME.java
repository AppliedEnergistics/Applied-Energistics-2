package appeng.parts.p2p;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;
import appeng.me.helpers.AENetworkProxy;

public class PartP2PTunnelME extends PartP2PTunnel<PartP2PTunnelME>
{

	AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", true );

	public PartP2PTunnelME(ItemStack is) {
		super( is );
		proxy.setFlags( GridFlags.TIER_2_CAPACITY, GridFlags.REQURE_CHANNEL, GridFlags.DENSE_CHANNEL, GridFlags.CANNOT_CARRY_DENSE );
	}

	@Override
	public void setPartHostInfo(ForgeDirection side, IPartHost host, TileEntity tile)
	{
		super.setPartHostInfo( side, host, tile );
		outerProxy.setValidSides( EnumSet.of( side ) );
	}

	@Override
	public void readFromNBT(NBTTagCompound extra)
	{
		super.readFromNBT( extra );
		outerProxy.readFromNBT( extra );
	}

	@Override
	public void writeToNBT(NBTTagCompound extra)
	{
		super.writeToNBT( extra );
		outerProxy.writeToNBT( extra );
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		outerProxy.onReady();
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		outerProxy.invalidate();
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return outerProxy.getNode();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.DENSE;
	}

}
