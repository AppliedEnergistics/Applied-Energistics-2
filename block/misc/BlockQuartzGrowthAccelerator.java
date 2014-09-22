package appeng.block.misc;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockQuartzAccelerator;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.helpers.MetaRotation;
import appeng.tile.misc.TileQuartzGrowthAccelerator;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockQuartzGrowthAccelerator extends AEBaseBlock implements IOrientableBlock
{

	public BlockQuartzGrowthAccelerator() {
		super( BlockQuartzGrowthAccelerator.class, Material.rock );
		setFeature( EnumSet.of( AEFeature.Core ) );
		setTileEntity( TileQuartzGrowthAccelerator.class );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockQuartzAccelerator.class;
	}

	@Override
	public IOrientable getOrientable(final IBlockAccess w, final int x, final int y, final int z)
	{
		return new MetaRotation( w, x, y, z );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random r)
	{
		if ( !AEConfig.instance.enableEffects )
			return;

		TileQuartzGrowthAccelerator tqga = getTileEntity( w, x, y, z );

		if ( tqga != null && tqga.hasPower && CommonHelper.proxy.shouldAddParticles( r ) )
		{
			double d0 = (double) (r.nextFloat() - 0.5F);
			double d1 = (double) (r.nextFloat() - 0.5F);

			ForgeDirection up = tqga.getUp();
			ForgeDirection forward = tqga.getForward();
			ForgeDirection west = Platform.crossProduct( forward, up );

			double rx = 0.5 + x;
			double ry = 0.5 + y;
			double rz = 0.5 + z;

			double dx = 0;
			double dz = 0;

			rx += up.offsetX * d0;
			ry += up.offsetY * d0;
			rz += up.offsetZ * d0;

			switch (r.nextInt( 4 ))
			{
			case 0:
				dx = 0.6;
				dz = d1;
				if ( !w.getBlock( x + west.offsetX, y + west.offsetY, z + west.offsetZ ).isAir( w, x + west.offsetX, y + west.offsetY, z + west.offsetZ ) )
					return;
				break;
			case 1:
				dx = d1;
				dz += 0.6;
				if ( !w.getBlock( x + forward.offsetX, y + forward.offsetY, z + forward.offsetZ ).isAir( w, x + forward.offsetX, y + forward.offsetY,
						z + forward.offsetZ ) )
					return;
				break;
			case 2:
				dx = d1;
				dz = -0.6;
				if ( !w.getBlock( x - forward.offsetX, y - forward.offsetY, z - forward.offsetZ ).isAir( w, x - forward.offsetX, y - forward.offsetY,
						z - forward.offsetZ ) )
					return;
				break;
			case 3:
				dx = -0.6;
				dz = d1;
				if ( !w.getBlock( x - west.offsetX, y - west.offsetY, z - west.offsetZ ).isAir( w, x - west.offsetX, y - west.offsetY, z - west.offsetZ ) )
					return;
				break;
			}

			rx += dx * west.offsetX;
			ry += dx * west.offsetY;
			rz += dx * west.offsetZ;

			rx += dz * forward.offsetX;
			ry += dz * forward.offsetY;
			rz += dz * forward.offsetZ;

			LightningFX fx = new LightningFX( w, rx, ry, rz, 0.0D, 0.0D, 0.0D );
			Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
		}
	}

	@Override
	public boolean usesMetadata()
	{
		return true;
	}
}
