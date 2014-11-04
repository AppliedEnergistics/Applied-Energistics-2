package appeng.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.util.Platform;

final public class EntityChargedQuartz extends AEBaseEntityItem
{

	int delay = 0;
	int transformTime = 0;

	public EntityChargedQuartz(World w, double x, double y, double z, ItemStack is) {
		super( w, x, y, z, is );
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if ( !AEConfig.instance.isFeatureEnabled( AEFeature.inWorldFluix ) )
			return;

		if ( Platform.isClient() && delay++ > 30 && AEConfig.instance.enableEffects )
		{
			CommonHelper.proxy.spawnEffect( EffectType.Lightning, worldObj, posX, posY, posZ, null );
			delay = 0;
		}

		int j = MathHelper.floor_double( this.posX );
		int i = MathHelper.floor_double( this.posY );
		int k = MathHelper.floor_double( this.posZ );

		Material mat = worldObj.getBlock( j, i, k ).getMaterial();
		if ( Platform.isServer() && mat.isLiquid() )
		{
			transformTime++;
			if ( transformTime > 60 )
			{
				if ( !transform() )
					transformTime = 0;
			}
		}
		else
			transformTime = 0;
	}

	public boolean transform()
	{
		ItemStack item = getEntityItem();
		if ( AEApi.instance().materials().materialCertusQuartzCrystalCharged.sameAsStack( item ) )
		{
			AxisAlignedBB region = AxisAlignedBB.getBoundingBox( posX - 1, posY - 1, posZ - 1, posX + 1, posY + 1, posZ + 1 );
			List<Entity> l = this.getCheckedEntitiesWithinAABBExcludingEntity( region );

			EntityItem redstone = null;
			EntityItem netherQuartz = null;

			for (Entity e : l)
			{
				if ( e instanceof EntityItem && !e.isDead )
				{
					ItemStack other = ((EntityItem) e).getEntityItem();
					if ( other != null && other.stackSize > 0 )
					{
						if ( Platform.isSameItem( other, new ItemStack( Items.redstone ) ) )
							redstone = (EntityItem) e;

						if ( Platform.isSameItem( other, new ItemStack( Items.quartz ) ) )
							netherQuartz = (EntityItem) e;
					}
				}
			}

			if ( redstone != null && netherQuartz != null )
			{
				getEntityItem().stackSize--;
				redstone.getEntityItem().stackSize--;
				netherQuartz.getEntityItem().stackSize--;

				if ( getEntityItem().stackSize <= 0 )
					setDead();

				if ( redstone.getEntityItem().stackSize <= 0 )
					redstone.setDead();

				if ( netherQuartz.getEntityItem().stackSize <= 0 )
					netherQuartz.setDead();

				List<ItemStack> i = new ArrayList<ItemStack>();
				i.add( AEApi.instance().materials().materialFluixCrystal.stack( 1 ) );

				ItemStack Output = AEApi.instance().materials().materialFluixCrystal.stack( 2 );
				worldObj.spawnEntityInWorld( new EntityItem( worldObj, posX, posY, posZ, Output ) );

				return true;
			}
		}
		return false;
	}
}
