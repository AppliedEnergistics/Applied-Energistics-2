package powercrystals.minefactoryreloaded.api;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;

/**
 * @author PowerCrystals
 * 
 * Defines a ranchable entity for use in the Rancher.
 */
public interface IFactoryRanchable
{
	/**
	 * @return The entity being ranched. Must be a subtype of EntityLivingBase.
	 */
	public Class<? extends EntityLivingBase> getRanchableEntity();
	
	/**
	 * @param world The world this entity is in.
	 * @param entity The entity instance being ranched.
	 * @param rancher The rancher instance doing the ranching. Used to access the Rancher's inventory when milking cows, for example.
	 * @return A list of drops.
	 */
	public List<RanchedItem> ranch(World world, EntityLivingBase entity, IInventory rancher);
}
