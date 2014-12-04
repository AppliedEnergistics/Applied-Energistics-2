package appeng.api.features;

import net.minecraft.item.ItemStack;

public interface IMatterCannonAmmoRegistry
{

	/**
	 * register a new ammo, generally speaking this is based off of atomic weight to make it easier to guess at
	 * 
	 * @param ammo new ammo
	 * @param weight atomic weight
	 */
	void registerAmmo(ItemStack ammo, double weight);

	/**
	 * get the penetration value for a particular ammo, 0 indicates a non-ammo.
	 * 
	 * @param is ammo
	 * @return 0 or a valid penetration value.
	 */
	float getPenetration(ItemStack is);

}
