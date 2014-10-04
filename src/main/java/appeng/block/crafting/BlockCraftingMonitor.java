package appeng.block.crafting;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockCraftingCPUMonitor;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingMonitorTile;

public class BlockCraftingMonitor extends BlockCraftingUnit
{

	public BlockCraftingMonitor() {
		super( BlockCraftingMonitor.class );

		this.setTileEntity( TileCraftingMonitorTile.class );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockCraftingCPUMonitor.class;
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		if ( direction != ForgeDirection.SOUTH.ordinal() )
			return AEApi.instance().blocks().blockCraftingUnit.block().getIcon( direction, metadata );

		switch (metadata)
		{
		default:
		case 0:
			return super.getIcon( 0, 0 );
		case FLAG_FORMED:
			return ExtraBlockTextures.BlockCraftingMonitorFit_Light.getIcon();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getCheckedSubBlocks(Item item, CreativeTabs tabs, List<ItemStack> itemStacks)
	{
		itemStacks.add( new ItemStack( this, 1, 0 ) );
	}
}
