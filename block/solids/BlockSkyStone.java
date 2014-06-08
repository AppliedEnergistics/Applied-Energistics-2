package appeng.block.solids;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import rblocks.api.RotatableBlockEnable;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.core.AppEng;
import appeng.core.WorldSettings;
import appeng.core.features.AEFeature;
import appeng.helpers.LocationRotation;
import appeng.helpers.NullRotation;
import appeng.integration.abstraction.IRB;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@RotatableBlockEnable
public class BlockSkyStone extends AEBaseBlock implements IOrientableBlock
{

	@SideOnly(Side.CLIENT)
	IIcon Block;

	@SideOnly(Side.CLIENT)
	IIcon Brick;

	@SideOnly(Side.CLIENT)
	IIcon SmallBrick;

	@SubscribeEvent
	public void breakFaster(PlayerEvent.BreakSpeed Ev)
	{
		if ( Ev.block == this && Ev.entityPlayer != null )
		{
			ItemStack is = Ev.entityPlayer.inventory.getCurrentItem();
			int level = -1;

			if ( is != null )
				level = is.getItem().getHarvestLevel( is, "pickaxe" );

			if ( Ev.metadata > 0 || level >= 3 || Ev.originalSpeed > 7.0 )
				Ev.newSpeed /= 0.1;
		}
	}

	public BlockSkyStone() {
		super( BlockSkyStone.class, Material.rock );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setHardness( 50 );
		hasSubtypes = true;
		blockResistance = 150.0f;
		setHarvestLevel( "pickaxe", 3, 0 );
		MinecraftForge.EVENT_BUS.register( this );
	}

	@Override
	public int damageDropped(int meta)
	{
		return meta;
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
		if ( AppEng.instance.isIntegrationEnabled( "RB" ) )
		{
			TileEntity te = w.getTileEntity( x, y, z );
			if ( te != null )
			{
				IOrientable out = ((IRB) AppEng.instance.getIntegration( "RB" )).getOrientable( te );
				if ( out != null )
					return out;
			}
		}

		if ( w.getBlockMetadata( x, y, z ) == 0 )
			return new LocationRotation( w, x, y, z );

		return new NullRotation();
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

	@Override
	public void onBlockAdded(World w, int x, int y, int z)
	{
		super.onBlockAdded( w, x, y, z );
		WorldSettings.getInstance().getCompass().updateArea( w, x, y, z );
	}

	@Override
	public void breakBlock(World w, int x, int y, int z, Block b, int WTF)
	{
		super.breakBlock( w, x, y, z, b, WTF );
		WorldSettings.getInstance().getCompass().updateArea( w, x, y, z );
	}

	// use AE2's enderer, no rotatable blocks.
	int getRealRenderType()
	{
		return getRenderType();
	}

	@Override
	public boolean usesMetadata()
	{
		return false;
	}

}
