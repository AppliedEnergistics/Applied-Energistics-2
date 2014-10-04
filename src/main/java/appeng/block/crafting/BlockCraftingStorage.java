package appeng.block.crafting;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingStorageTile;

public class BlockCraftingStorage extends BlockCraftingUnit
{

	public BlockCraftingStorage() {
		super( BlockCraftingStorage.class );
		setTileEntity( TileCraftingStorageTile.class );
	}

	@Override
	public Class getItemBlockClass()
	{
		return ItemCraftingStorage.class;
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		if ( is.getItemDamage() == 1 )
			return "tile.appliedenergistics2.BlockCraftingStorage4k";

		if ( is.getItemDamage() == 2 )
			return "tile.appliedenergistics2.BlockCraftingStorage16k";

		if ( is.getItemDamage() == 3 )
			return "tile.appliedenergistics2.BlockCraftingStorage64k";

		return getItemUnlocalizedName( is );
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		switch (metadata & (~4))
		{
		default:

		case 0:
			return super.getIcon( 0, 0 );
		case 1:
			return ExtraBlockTextures.BlockCraftingStorage4k.getIcon();
		case 2:
			return ExtraBlockTextures.BlockCraftingStorage16k.getIcon();
		case 3:
			return ExtraBlockTextures.BlockCraftingStorage64k.getIcon();

		case 0 | FLAG_FORMED:
			return ExtraBlockTextures.BlockCraftingStorage1kFit.getIcon();
		case 1 | FLAG_FORMED:
			return ExtraBlockTextures.BlockCraftingStorage4kFit.getIcon();
		case 2 | FLAG_FORMED:
			return ExtraBlockTextures.BlockCraftingStorage16kFit.getIcon();
		case 3 | FLAG_FORMED:
			return ExtraBlockTextures.BlockCraftingStorage64kFit.getIcon();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getCheckedSubBlocks(Item item, CreativeTabs tabs, List<ItemStack> itemStacks)
	{
		itemStacks.add( new ItemStack( this, 1, 0 ) );
		itemStacks.add( new ItemStack( this, 1, 1 ) );
		itemStacks.add( new ItemStack( this, 1, 2 ) );
		itemStacks.add( new ItemStack( this, 1, 3 ) );
	}
}
