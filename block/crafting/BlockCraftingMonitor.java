package appeng.block.crafting;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingMonitorTile;

public class BlockCraftingMonitor extends BlockCraftingUnit
{

	public BlockCraftingMonitor() {
		super( BlockCraftingMonitor.class );
		setTileEntiy( TileCraftingMonitorTile.class );
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		switch (metadata)
		{
		default:
		case 0:
			return super.getIcon( 0, 0 );
		case 0 | FLAG_FORMED:
			return ExtraBlockTextures.BlockCraftingMonitorFit.getIcon();
		}
	}

	@Override
	public void getSubBlocks(Item i, CreativeTabs c, List l)
	{
		l.add( new ItemStack( this, 1, 0 ) );
	}

}
