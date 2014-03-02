package appeng.block.misc;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderTinyTNT;
import appeng.client.texture.FullIcon;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.entity.EntityIds;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.helpers.ICustomCollision;
import appeng.hooks.DispenserBehaviorTinyTNT;
import cpw.mods.fml.common.registry.EntityRegistry;

public class BlockTinyTNT extends AEBaseBlock implements ICustomCollision
{

	public BlockTinyTNT() {
		super( BlockTinyTNT.class, Material.tnt );
		setfeature( EnumSet.of( AEFeature.TinyTNT ) );
		setLightOpacity( 3 );
		setBlockBounds( 0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f );
		isFullSize = isOpaque = false;

		EntityRegistry.registerModEntity( EntityTinyTNTPrimed.class, "EntityTinyTNTPrimed", EntityIds.TINY_TNT, AppEng.instance, 16, 4, true );
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( Item.getItemFromBlock( this ), new DispenserBehaviorTinyTNT() );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderTinyTNT.class;
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{
		// no images required.
	}

	@Override
	public IIcon getIcon(int direction, int metadata)
	{
		return new FullIcon( Blocks.tnt.getIcon( direction, metadata ) );
	}

	@Override
	public void onEntityCollidedWithBlock(World w, int x, int y, int z, Entity entity)
	{
		if ( entity instanceof EntityArrow && !w.isRemote )
		{
			EntityArrow entityarrow = (EntityArrow) entity;

			if ( entityarrow.isBurning() )
			{
				this.startFuse( w, x, y, z, entityarrow.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase) entityarrow.shootingEntity : null );
				w.setBlockToAir( x, y, z );
			}
		}
	}

	@Override
	public void onBlockAdded(World w, int x, int y, int z)
	{
		super.onBlockAdded( w, x, y, z );

		if ( w.isBlockIndirectlyGettingPowered( x, y, z ) )
		{
			this.startFuse( w, x, y, z, null );
			w.setBlockToAir( x, y, z );
		}
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block id)
	{
		if ( w.isBlockIndirectlyGettingPowered( x, y, z ) )
		{
			this.startFuse( w, x, y, z, null );
			w.setBlockToAir( x, y, z );
		}
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ( player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.flint_and_steel )
		{
			this.startFuse( w, x, y, z, player );
			w.setBlockToAir( x, y, z );
			player.getCurrentEquippedItem().damageItem( 1, player );
			return true;
		}
		else
		{
			return super.onActivated( w, x, y, z, player, side, hitX, hitY, hitZ );
		}
	}

	@Override
	public void onBlockDestroyedByExplosion(World w, int x, int y, int z, Explosion exp)
	{
		if ( !w.isRemote )
		{
			EntityTinyTNTPrimed entitytntprimed = new EntityTinyTNTPrimed( w, x + 0.5F, y + 0.5F, z + 0.5F, exp.getExplosivePlacedBy() );
			entitytntprimed.fuse = w.rand.nextInt( entitytntprimed.fuse / 4 ) + entitytntprimed.fuse / 8;
			w.spawnEntityInWorld( entitytntprimed );
		}
	}

	public void startFuse(World w, int x, int y, int z, EntityLivingBase ignitor)
	{
		if ( !w.isRemote )
		{
			EntityTinyTNTPrimed entitytntprimed = new EntityTinyTNTPrimed( w, x + 0.5F, y + 0.5F, z + 0.5F, ignitor );
			w.spawnEntityInWorld( entitytntprimed );
			w.playSoundAtEntity( entitytntprimed, "game.tnt.primed", 1.0F, 1.0F );
		}
	}

	@Override
	public boolean canDropFromExplosion(Explosion exp)
	{
		return false;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxsFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		return Arrays.asList( new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox( 0.25, 0, 0.25, 0.75, 0.5, 0.75 ) } );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{
		out.add( AxisAlignedBB.getAABBPool().getAABB( 0.25, 0, 0.25, 0.75, 0.5, 0.75 ) );

	}

}
