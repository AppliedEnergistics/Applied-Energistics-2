package appeng.parts.networking;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergyGridProvider;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.AEBasePart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartQuartzFiber extends AEBasePart implements IEnergyGridProvider
{

	AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", proxy.getMachineRepresentation(), true );

	public PartQuartzFiber(ItemStack is) {
		super( PartQuartzFiber.class, is );
		proxy.setIdlePowerUsage( 0 );
		proxy.setFlags( GridFlags.CANNOT_CARRY );
	}

	@Override
	public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side)
	{
		super.onPlacement( player, held, side );
		outerProxy.setOwner( player );
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
		return AECableType.GLASS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		IIcon myIcon = is.getIconIndex();
		rh.setTexture( myIcon );
		rh.setBounds( 6, 6, 10, 10, 10, 16 );
		rh.renderBlock( x, y, z, renderer );
		rh.setTexture( null );
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 6, 6, 10, 10, 10, 16 );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		GL11.glTranslated( -0.2, -0.3, 0.0 );

		rh.setTexture( is.getIconIndex() );
		rh.setBounds( 6.0f, 6.0f, 5.0f, 10.0f, 10.0f, 11.0f );
		rh.renderInventoryBox( renderer );
		rh.setTexture( null );
	}

	@Override
	public double extractAEPower(double amt, Actionable mode, Set<IEnergyGrid> seen)
	{
		double aquiredPower = 0;

		try
		{
			IEnergyGrid eg = proxy.getEnergy();
			aquiredPower += eg.extractAEPower( amt - aquiredPower, mode, seen );
		}
		catch (GridAccessException e)
		{
			// :P
		}

		try
		{
			IEnergyGrid eg = outerProxy.getEnergy();
			aquiredPower += eg.extractAEPower( amt - aquiredPower, mode, seen );
		}
		catch (GridAccessException e)
		{
			// :P
		}

		return aquiredPower;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 16;
	}

}
