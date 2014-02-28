package appeng.block.solids;

import java.util.EnumSet;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.helpers.LocationRotation;

public class BlockSkyStone extends AEBaseBlock implements IOrientableBlock
{

	@SideOnly(Side.CLIENT)
	IIcon Smooth;
	
	public BlockSkyStone() {
		super( BlockSkyStone.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setHardness( 50 );
		blockResistance = 150.0f;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack is) {
		
		if ( is.getItemDamage() == 1 )
			return getUnlocalizedName()+".smooth";
		
		return getUnlocalizedName();
	}
	
	@Override
	public IOrientable getOrientable(final IBlockAccess w, final int x, final int y, final int z)
	{
		if ( w.getBlockMetadata(x, y, z) == 0 )
			return new LocationRotation( w, x, y, z );
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) {
		super.registerBlockIcons(ir);
		Smooth = ir.registerIcon(getTextureName()+".Smooth");
	}
		
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int direction, int metadata) {
		if ( metadata == 1 )
			return Smooth;
		return super.getIcon(direction, metadata);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item i, CreativeTabs ct, List l) {
		super.getSubBlocks(i, ct, l);
		l.add( new ItemStack( i, 1, 1 ) );
	}
		
}
