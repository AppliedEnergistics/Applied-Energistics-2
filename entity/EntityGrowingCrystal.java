package appeng.entity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import appeng.api.implementations.items.IGrowableCrystal;
import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TileQuartzGrowthAccelerator;
import appeng.util.Platform;

final public class EntityGrowingCrystal extends EntityItem
{

	private int progress_1000 = 0;

	public float getProgress()
	{
		return (float) progress_1000 / 1000.0f;
	}

	public EntityGrowingCrystal(World w) {
		super( w );
	}

	public EntityGrowingCrystal(World w, double x, double y, double z, ItemStack is) {
		super( w, x, y, z, is );
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if ( !AEConfig.instance.isFeatureEnabled( AEFeature.inWorldPurification ) )
			return;

		age = 0;

		ItemStack is = this.getEntityItem();
		Item gc = is.getItem();

		if ( gc instanceof IGrowableCrystal ) // if it changes this just stops being an issue...
		{
			int j = MathHelper.floor_double( this.posX );
			int i = MathHelper.floor_double( this.posY );
			int k = MathHelper.floor_double( this.posZ );

			Block blk = worldObj.getBlock( j, i, k );
			Material mat = blk.getMaterial();
			IGrowableCrystal cry = (IGrowableCrystal) is.getItem();

			float multiplier = cry.getMultiplier( blk, mat );
			int speed = (int) Math.max( 1, getSpeed( j, i, k ) * multiplier );

			boolean isClient = Platform.isClient();

			if ( mat.isLiquid() )
			{
				if ( isClient )
					progress_1000++;
				else
					progress_1000 += speed;

			}
			else
				progress_1000 = 0;

			if ( isClient )
			{
				int len = 40;

				if ( speed > 2 )
					len = 20;

				if ( speed > 90 )
					len = 15;

				if ( speed > 150 )
					len = 10;

				if ( speed > 240 )
					len = 7;

				if ( speed > 360 )
					len = 3;

				if ( speed > 500 )
					len = 1;

				if ( progress_1000 >= len )
				{
					progress_1000 = 0;
					CommonHelper.proxy.spawnEffect( EffectType.Vibrant, worldObj, posX, posY + 0.2, posZ, null );
				}
				return;
			}
			else
			{
				if ( progress_1000 > 1000 )
				{
					progress_1000 -= 1000;
					setEntityItemStack( cry.triggerGrowth( is ) );
				}
			}
		}
	}

	private int getSpeed(int x, int y, int z)
	{
		final int per = 80;
		final float mul = 0.3f;

		int qty = 0;

		if ( isAccel( x + 1, y, z ) )
			qty += per + qty * mul;

		if ( isAccel( x, y + 1, z ) )
			qty += per + qty * mul;

		if ( isAccel( x, y, z + 1 ) )
			qty += per + qty * mul;

		if ( isAccel( x - 1, y, z ) )
			qty += per + qty * mul;

		if ( isAccel( x, y - 1, z ) )
			qty += per + qty * mul;

		if ( isAccel( x, y, z - 1 ) )
			qty += per + qty * mul;

		return qty;
	}

	private boolean isAccel(int x, int y, int z)
	{
		TileEntity te = worldObj.getTileEntity( x, y, z );
		if ( te instanceof TileQuartzGrowthAccelerator )
			return ((TileQuartzGrowthAccelerator) te).isPowered();
		return false;
	}

}
