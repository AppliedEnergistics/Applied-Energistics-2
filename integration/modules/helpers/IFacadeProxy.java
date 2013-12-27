package appeng.integration.modules.helpers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IFacadeProxy
{

	@SideOnly(Side.CLIENT)
	void facadeRender(RenderBlocks renderblocks, Block block, IFacadeTile ft, int x, int y, int z, float tubeThickness);

	List<ItemStack> getDrops(IFacadeTile ft);

	boolean addFacade(IFacadeTile ft, ForgeDirection direction, int blockid, int meta);

	boolean hasFacade(IFacadeTile ft, ForgeDirection direction);

	void dropFacade(IFacadeTile ft, ForgeDirection direction);

	void writeToNBT(NBTTagCompound tc);

	void writeToStream(DataOutputStream out) throws IOException;

	boolean readFromStream(DataInputStream out) throws IOException;

	void readFromNBT(NBTTagCompound tc);

	boolean addFacade(TileEntity tileEntity, int side, ItemStack hand);

}
