package appeng.parts.misc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;

public class PartCableAnchor implements IPart
{

	ItemStack is = null;
	ForgeDirection mySide = ForgeDirection.UP;

	public PartCableAnchor(ItemStack is) {
		this.is = is;
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		Icon myIcon = is.getIconIndex();
		rh.setTexture( myIcon );
		rh.setBounds( 7, 7, 10, 9, 9, 16 );
		rh.renderBlock( x, y, z, renderer );
		rh.setTexture( null );
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{

	}

	@Override
	public void renderInventory(IPartRenderHelper instance, RenderBlocks renderer)
	{
		instance.setTexture( is.getIconIndex() );
		instance.setBounds( 7, 7, 4, 9, 9, 14 );
		instance.renderInventoryBox( renderer );
		instance.setTexture( null );
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 7, 7, 10, 9, 9, 16 );
	}

	@Override
	public ItemStack getItemStack(PartItemStack wrenched)
	{
		return is;
	}

	@Override
	public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{

	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public boolean canConnectRedstone()
	{
		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{

	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{

	}

	@Override
	public int getLightLevel()
	{
		return 0;
	}

	@Override
	public void onNeighborChanged()
	{

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
		return null;
	}

	@Override
	public void onEntityCollision(Entity entity)
	{

	}

	@Override
	public void removeFromWorld()
	{

	}

	@Override
	public void addToWorld()
	{

	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return null;
	}

	@Override
	public void setPartHostInfo(ForgeDirection side, IPartHost host, TileEntity tile)
	{
		mySide = side;
	}

	@Override
	public boolean onActivate(EntityPlayer player, Vec3 pos)
	{
		return false;
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched)
	{

	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 0;
	}

	@Override
	public boolean isLadder(EntityLivingBase entity)
	{
		return mySide.offsetY == 0;
	}

	@Override
	public boolean onShiftActivate(EntityPlayer player, Vec3 pos)
	{
		return false;
	}

	@Override
	public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side)
	{

	}

	@Override
	public boolean canBePlacedOn(BusSupport what)
	{
		return what == BusSupport.CABLE || what == BusSupport.DENSE_CABLE;
	}
}
