package appeng.api.storage;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;

public interface IStorageHelper
{

	/**
	 * load a crafting link from nbt data.
	 * 
	 * @param data to be loaded data
	 * @return crafting link
	 */
	ICraftingLink loadCraftingLink(NBTTagCompound data, ICraftingRequester req);

	/**
	 * @param is
	 *            An ItemStack
	 * 
	 * @return a new instance of {@link IAEItemStack} from a MC {@link ItemStack}
	 */
	IAEItemStack createItemStack(ItemStack is);

	/**
	 * @param is
	 *            A FluidStack
	 * 
	 * @return a new instance of {@link IAEFluidStack} from a Forge {@link FluidStack}
	 */
	IAEFluidStack createFluidStack(FluidStack is);

	/**
	 * @return a new instance of {@link IItemList} for items
	 */
	IItemList<IAEItemStack> createItemList();

	/**
	 * @return a new instance of {@link IItemList} for fluids
	 */
	IItemList<IAEFluidStack> createFluidList();

	/**
	 * Read a AE Item Stack from a byte stream, returns a AE item stack or null.
	 * 
	 * @param input to be loaded data
	 * @return item based of data
	 * @throws IOException if file could not be read
	 */
	IAEItemStack readItemFromPacket(ByteBuf input) throws IOException;

	/**
	 * Read a AE Fluid Stack from a byte stream, returns a AE fluid stack or null.
	 * 
	 * @param input to be loaded data
	 * @return fluid based on data
	 * @throws IOException if file could not be written
	 */
	IAEFluidStack readFluidFromPacket(ByteBuf input) throws IOException;

	/**
	 * use energy from energy, to remove request items from cell, at the request of src.
	 * 
	 * @param energy to be drained energy source
	 * @param cell cell of requested items
	 * @param request requested items
	 * @param src action source
	 * @return items that successfully extracted.
	 */
	IAEItemStack poweredExtraction(IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack request, BaseActionSource src);

	/**
	 * use energy from energy, to inject input items into cell, at the request of src
	 * 
	 * @param energy to be added energy source
	 * @param cell injected cell
	 * @param input to be injected items
	 * @param src action source
	 * @return items that failed to insert.
	 */
	IAEItemStack poweredInsert(IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack input, BaseActionSource src);

}
