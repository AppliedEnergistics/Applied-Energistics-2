package appeng.block.crafting;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockCrafting;
import appeng.client.texture.ExtraTextures;
import appeng.core.features.AEFeature;
import appeng.tile.crafting.TileCraftingTile;

public class BlockCraftingUnit extends AEBaseBlock
{

	public static final int FLAG_FORMED = 8;
	public static final int FLAG_POWERED = 4;

	public BlockCraftingUnit(Class<? extends BlockCraftingUnit> childClass) {
		super( childClass, Material.iron );
		hasSubtypes = true;
		setfeature( EnumSet.of( AEFeature.Crafting ) );
	}

	public BlockCraftingUnit() {
		this( BlockCraftingUnit.class );
		setTileEntiy( TileCraftingTile.class );
	}

	@Override
	public int getDamageValue(World w, int x, int y, int z)
	{
		int meta = w.getBlockMetadata( x, y, z );
		return meta & 3;
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		switch (metadata)
		{
		default:
		case 0:
			return super.getIcon( 0, 0 );
		case 1:
			return ExtraTextures.BlockCraftingAccelerator.getIcon();
		case 0 | FLAG_FORMED:
			return ExtraTextures.BlockCraftingUnitFit.getIcon();
		case 1 | FLAG_FORMED:
			return ExtraTextures.BlockCraftingAcceleratorFit.getIcon();
		}
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockCrafting.class;
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block junk)
	{
		TileCraftingTile cp = getTileEntity( w, x, y, z );
		if ( cp != null )
			cp.updateMultiBlock();
	}

	@Override
	public void getSubBlocks(Item i, CreativeTabs c, List l)
	{
		l.add( new ItemStack( this, 1, 0 ) );
		l.add( new ItemStack( this, 1, 1 ) );
	}
}
