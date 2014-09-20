package appeng.block.misc;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQuartzTorch;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.helpers.MetaRotation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockQuartzTorch extends AEBaseBlock implements IOrientableBlock, ICustomCollision
{

	protected BlockQuartzTorch(Class which) {
		super( which, Material.circuits );
		setLightOpacity( 0 );
		isFullSize = isOpaque = false;
	}

	public BlockQuartzTorch() {
		this( BlockQuartzTorch.class );
		setFeature( EnumSet.of( AEFeature.DecorativeLights ) );
		setLightLevel( 0.9375F );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQuartzTorch.class;
	}

	@Override
	public IOrientable getOrientable(final IBlockAccess w, final int x, final int y, final int z)
	{
		return new MetaRotation( w, x, y, z );
	}

	private void dropTorch(World w, int x, int y, int z)
	{
		w.func_147480_a( x, y, z, true );
		// w.destroyBlock( x, y, z, true );
		w.markBlockForUpdate( x, y, z );
	}

	@Override
	public boolean canPlaceBlockAt(World w, int x, int y, int z)
	{
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			if ( canPlaceAt( w, x, y, z, dir ) )
				return true;
		return false;
	}

	private boolean canPlaceAt(World w, int x, int y, int z, ForgeDirection dir)
	{
		return w.isSideSolid( x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.getOpposite(), false );
	}

	@Override
	public boolean isValidOrientation(World w, int x, int y, int z, ForgeDirection forward, ForgeDirection up)
	{
		return canPlaceAt( w, x, y, z, up.getOpposite() );
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block id)
	{
		ForgeDirection up = getOrientable( w, x, y, z ).getUp();
		if ( !canPlaceAt( w, x, y, z, up.getOpposite() ) )
			dropTorch( w, x, y, z );
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxsFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		ForgeDirection up = getOrientable( w, x, y, z ).getUp();
		double xOff = -0.3 * up.offsetX;
		double yOff = -0.3 * up.offsetY;
		double zOff = -0.3 * up.offsetZ;
		return Arrays.asList( new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox( xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7 ) } );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{/*
	 * double xOff = -0.15 * getUp().offsetX; double yOff = -0.15 * getUp().offsetY; double zOff = -0.15 *
	 * getUp().offsetZ; out.add( AxisAlignedBB.getBoundingBox( xOff + (double) x + 0.15, yOff + (double) y + 0.15, zOff
	 * + (double) z + 0.15,// ahh xOff + (double) x + 0.85, yOff + (double) y + 0.85, zOff + (double) z + 0.85 ) );
	 */
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random r)
	{
		if ( !AEConfig.instance.enableEffects )
			return;

		if ( r.nextFloat() < 0.98 )
			return;

		ForgeDirection up = getOrientable( w, x, y, z ).getUp();
		double xOff = -0.3 * up.offsetX;
		double yOff = -0.3 * up.offsetY;
		double zOff = -0.3 * up.offsetZ;
		for (int bolts = 0; bolts < 3; bolts++)
		{
			if ( CommonHelper.proxy.shouldAddParticles( r ) )
			{
				LightningFX fx = new LightningFX( w, xOff + 0.5 + x, yOff + 0.5 + y, zOff + 0.5 + z, 0.0D, 0.0D, 0.0D );

				Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
			}
		}
	}

	@Override
	public boolean usesMetadata()
	{
		return true;
	}

}
