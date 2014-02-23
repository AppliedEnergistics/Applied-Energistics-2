package appeng.entity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import appeng.api.implementations.items.IGrowableCrystal;
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

		if ( Platform.isClient() )
			return;

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
			if ( Platform.isServer() && mat.isLiquid() )
			{
				progress_1000 += Math.min( 1, getSpeed( j, i, k ) * multiplier );
				if ( progress_1000 > 1000 )
				{
					setEntityItemStack( cry.triggerGrowth( is ) );
				}
			}
			else
				progress_1000 = 0;
		}
	}

	private int getSpeed(int x, int y, int z)
	{
		return 10;
	}

}
