package appeng.entity;

import java.util.Date;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.util.Platform;

final public class EntitySingularity extends AEBaseEntityItem
{

	static private int randTickSeed = 0;

	public EntitySingularity(World w, double x, double y, double z, ItemStack is) {
		super( w, x, y, z, is );
	}

	@Override
	public boolean attackEntityFrom(DamageSource src, float dmg)
	{
		if ( src.isExplosion() )
		{
			doExplosion();
			return false;
		}

		return super.attackEntityFrom( src, dmg );
	}

	public void doExplosion()
	{
		if ( Platform.isClient() )
			return;

		if ( !AEConfig.instance.isFeatureEnabled( AEFeature.inWorldSingularity ) )
			return;

		ItemStack item = getEntityItem();
		if ( AEApi.instance().materials().materialSingularity.sameAsStack( item ) )
		{
			AxisAlignedBB region = AxisAlignedBB.getBoundingBox( posX - 4, posY - 4, posZ - 4, posX + 4, posY + 4, posZ + 4 );
			List<Entity> l = this.getCheckedEntitiesWithinAABBExcludingEntity( region );

			for (Entity e : l)
			{
				if ( e instanceof EntityItem )
				{
					ItemStack other = ((EntityItem) e).getEntityItem();
					if ( other != null )
					{
						boolean matches = false;
						for (ItemStack is : OreDictionary.getOres( "dustEnder" ))
						{
							if ( OreDictionary.itemMatches( other, is, false ) )
							{
								matches = true;
								break;
							}
						}

						// check... other name.
						if ( !matches )
						{
							for (ItemStack is : OreDictionary.getOres( "dustEnderPearl" ))
							{
								if ( OreDictionary.itemMatches( other, is, false ) )
								{
									matches = true;
									break;
								}
							}
						}

						if ( matches )
						{
							while (item.stackSize > 0 && other.stackSize > 0)
							{
								other.stackSize--;
								if ( other.stackSize == 0 )
									e.setDead();

								ItemStack Output = AEApi.instance().materials().materialQESingularity.stack( 2 );
								NBTTagCompound cmp = Platform.openNbtData( Output );
								cmp.setLong( "freq", (new Date()).getTime() * 100 + (randTickSeed++) % 100 );
								item.stackSize--;

								worldObj.spawnEntityInWorld( new EntitySingularity( worldObj, posX, posY, posZ, Output ) );
							}

							if ( item.stackSize <= 0 )
								setDead();
						}
					}
				}
			}
		}
	}

}
