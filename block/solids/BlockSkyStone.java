package appeng.block.solids;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.helpers.LocationRotation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSkyStone extends AEBaseBlock implements IOrientableBlock
{

	@SideOnly(Side.CLIENT)
	IIcon Block;

	@SideOnly(Side.CLIENT)
	IIcon Brick;

	@SideOnly(Side.CLIENT)
	IIcon SmallBrick;

	public BlockSkyStone() {
		super( BlockSkyStone.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setHardness( 50 );
		hasSubtypes = true;
		blockResistance = 150.0f;
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		if ( is.getItemDamage() == 1 )
			return getUnlocalizedName() + ".Block";

		if ( is.getItemDamage() == 2 )
			return getUnlocalizedName() + ".Brick";

		if ( is.getItemDamage() == 3 )
			return getUnlocalizedName() + ".SmallBrick";

		return getUnlocalizedName();
	}

	@Override
	public IOrientable getOrientable(final IBlockAccess w, final int x, final int y, final int z)
	{
		if ( w.getBlockMetadata( x, y, z ) == 0 )
			return new LocationRotation( w, x, y, z );
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		super.registerBlockIcons( ir );
		Block = ir.registerIcon( getTextureName() + ".Block" );
		Brick = ir.registerIcon( getTextureName() + ".Brick" );
		SmallBrick = ir.registerIcon( getTextureName() + ".SmallBrick" );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int direction, int metadata)
	{
		if ( metadata == 1 )
			return Block;
		if ( metadata == 2 )
			return Brick;
		if ( metadata == 3 )
			return SmallBrick;
		return super.getIcon( direction, metadata );
	}

	@Override
	public void setRenderStateByMeta(int metadata)
	{
		getRendererInstance().setTemporaryRenderIcon( getIcon( 0, metadata ) );
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		ItemStack is = super.getPickBlock( target, world, x, y, z );
		is.setItemDamage( world.getBlockMetadata( x, y, z ) );
		return is;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item i, CreativeTabs ct, List l)
	{
		super.getSubBlocks( i, ct, l );
		l.add( new ItemStack( i, 1, 1 ) );
		l.add( new ItemStack( i, 1, 2 ) );
		l.add( new ItemStack( i, 1, 3 ) );
	}

}
