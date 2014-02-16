package appeng.integration.abstraction;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IFacadePart;

public interface IBC
{

	boolean isWrench(Item eq);

	boolean canWrench(Item i, EntityPlayer p, int x, int y, int z);

	void wrenchUsed(Item i, EntityPlayer p, int x, int y, int z);

	boolean canAddItemsToPipe(TileEntity te, ItemStack is, ForgeDirection dir);

	boolean addItemsToPipe(TileEntity te, ItemStack is, ForgeDirection dir);

	boolean isFacade(ItemStack is);

	boolean isPipe(TileEntity te, ForgeDirection dir);

	void addFacade(ItemStack item);

	void registerPowerP2P();

	void registerItemP2P();

	void registerLiquidsP2P();

	IFacadePart createFacadePart(Block blk, int meta, ForgeDirection side);

	IFacadePart createFacadePart(ItemStack held, ForgeDirection side);

	ItemStack getTextureForFacade(ItemStack facade);

	IIcon getFacadeTexture();

}
