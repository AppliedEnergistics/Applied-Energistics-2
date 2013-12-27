package appeng.integration.modules.helpers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FacadeProxyNull implements IFacadeProxy
{

	@Override
	@SideOnly(Side.CLIENT)
	public void facadeRender(RenderBlocks renderblocks, Block block, IFacadeTile ft, int x, int y, int z, float th)
	{

	}

	@Override
	public List<ItemStack> getDrops(IFacadeTile ft)
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	public boolean addFacade(IFacadeTile ft, ForgeDirection direction, int blockid, int meta)
	{

		return false;
	}

	@Override
	public boolean hasFacade(IFacadeTile ft, ForgeDirection direction)
	{
		return false;
	}

	@Override
	public void dropFacade(IFacadeTile ft, ForgeDirection direction)
	{

	}

	@Override
	public void writeToNBT(NBTTagCompound tc)
	{

	}

	@Override
	public void writeToStream(DataOutputStream out) throws IOException
	{

	}

	@Override
	public boolean readFromStream(DataInputStream out) throws IOException
	{
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound tc)
	{

	}

	@Override
	public boolean addFacade(TileEntity tileEntity, int side, ItemStack hand)
	{
		return false;
	}

}
