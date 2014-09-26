package powercrystals.minefactoryreloaded.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author PowerCrystals
 *
 * Defines a syringe for use in the Vet machine.
 */
public interface ISyringe
{
	/**
	 * Called when the vet is deciding if it should use this syringe.
	 * @param world The world instance.
	 * @param entity The entity being injected.
	 * @param syringe The syringe ItemStack.
	 * @return True if the entity can be injected by this syringe.
	 */
	public boolean canInject(World world, EntityLivingBase entity, ItemStack syringe);
	
	/**
	 * Called to perform an injection.
	 * @param world The world instance.
	 * @param entity The entity being injected.
	 * @param syringe The syringe ItemStack.
	 * @return True if injection was successful.
	 */
	public boolean inject(World world, EntityLivingBase entity, ItemStack syringe);
	
	/**
	 * Called to check if a syringe is empty
	 * @param syringe The syringe ItemStack.
	 * @return True if the syringe is empty
	 */
	public boolean isEmpty(ItemStack syringe);
	
	/**
	 * Called to get the empty syringe
	 * Note: this will replace the syringe, max stacksize should be 1
	 * @param syringe The syringe ItemStack.
	 * @return An empty syringe ItemStack
	 */
	public ItemStack getEmptySyringe(ItemStack syringe);
}
