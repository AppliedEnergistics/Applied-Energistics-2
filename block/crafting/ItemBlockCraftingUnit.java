package appeng.block.crafting;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.block.AEBaseItemBlock;
import appeng.core.localization.GuiText;

public class ItemBlockCraftingUnit extends AEBaseItemBlock
{

	public ItemBlockCraftingUnit(Block id) {
		super( id );
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		switch (is.getItemDamage())
		{
		case BlockCraftingUnit.BASE_STORAGE:
			return "tile.appliedenergistics2.BlockCraftingStorage";
		case BlockCraftingUnit.BASE_ACCELERATOR:
			return "tile.appliedenergistics2.BlockCraftingAccelerator";
		case BlockCraftingUnit.BASE_MONITOR:
			return "tile.appliedenergistics2.BlockCraftingMonitor";
		}

		return super.getUnlocalizedName( is );
	}

	@Override
	public String getItemStackDisplayName(ItemStack is)
	{
		String name = super.getItemStackDisplayName( is );

		long storageBytes = getStorageBytes( is );
		if ( storageBytes > 0 )
			return name + " - " + GuiText.Stores.getLocal() + " " + (storageBytes / 1024) + "k";

		return name;
	}

	public long getStorageBytes(ItemStack is)
	{
		NBTTagCompound tag = is.getTagCompound();

		if ( tag != null )
		{
			return tag.getLong( "bytes" );
		}

		return 0;
	}

}
