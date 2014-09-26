package powercrystals.minefactoryreloaded.api;

import net.minecraft.entity.EntityLivingBase;

/**
 * @author skyboy
 *
 * Defines a handler for mob spawns from the autospawner.
 *  Added primarily to solve item duping on exact spawn & entity inventories
 */
public interface IMobSpawnHandler
{
	/**
	 * @return The class that this instance is handling.
	 */
	public Class<? extends EntityLivingBase> getMobClass();
	
	/**
	 * @param entity The entity instance being spawned. Typically your regular spawn code 100% handles this
	 */
	public void onMobSpawn(EntityLivingBase entity);

	/**
	 * @param entity The entity instance being exact-copied. Clear your inventories & etc. here
	 */
	public void onMobExactSpawn(EntityLivingBase entity);
}
