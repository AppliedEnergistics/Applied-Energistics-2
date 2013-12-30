package appeng.parts;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IBusCommon;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.networking.PartCable;

public class AEBasePart implements IPart, IGridProxyable, IGridHost, IBusCommon
{

	protected AENetworkProxy proxy = new AENetworkProxy( this, "part", this instanceof PartCable );
	protected TileEntity tile = null;
	protected IPartHost host = null;
	protected ForgeDirection side = null;

	protected final ItemStack is;

	public AEBasePart(Class c, ItemStack is) {
		this.is = is;
		proxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setBounds( 1, 1, 1, 15, 15, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 1, 1, 1, 15, 15, 15 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setBounds( 1, 1, 1, 15, 15, 15 );
		rh.renderBlock( x, y, z, renderer );
	}

	@Override
	public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{

	}

	@Override
	public ItemStack getItemStack(boolean wrenched)
	{
		return is;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public void onNeighborChanged()
	{

	}

	@Override
	public boolean canConnectRedstone()
	{
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound extra)
	{
		proxy.readFromNBT( extra );
	}

	@Override
	public void writeToNBT(NBTTagCompound extra)
	{
		proxy.writeToNBT( extra );
	}

	@Override
	public int isProvidingStrongPower()
	{
		return 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return 0;
	}

	@Override
	public void writeToStream(DataOutputStream data) throws IOException
	{

	}

	@Override
	public boolean readFromStream(DataInputStream data) throws IOException
	{
		return false;
	}

	@Override
	public IGridNode getGridNode()
	{
		return proxy.getNode();
	}

	@Override
	public void onEntityCollision(Entity entity)
	{

	}

	@Override
	public void removeFromWorld()
	{
		proxy.invalidate();
	}

	@Override
	public void addToWorld()
	{
		proxy.onReady();
	}

	@Override
	public void setPartHostInfo(ForgeDirection side, IPartHost host, TileEntity tile)
	{
		this.side = side;
		this.tile = tile;
		this.host = host;
	}

	public IPartHost getHost()
	{
		return host;
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return null;
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir)
	{
		return proxy.getNode();
	}

	protected AEColor getColor()
	{
		if ( getHost() == null )
			return AEColor.Transparent;
		return getHost().getColor();
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( tile );
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{

	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{

	}

	@Override
	public int getLightLevel()
	{
		return 0;
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		return false;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.GLASS;
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched)
	{

	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	@Override
	public void gridChanged()
	{

	}

	@Override
	public boolean isLadder(EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return null;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		return null;
	}

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return 0;
	}

}