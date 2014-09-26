package net.mcft.copy.betterstorage.api.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface ICraftingSource {
	
	/** Returns the player currently crafting, null if none. */
	EntityPlayer getPlayer();
	
	/** Returns the world the recipe is being crafted in. */
	World getWorld();
	
	double getX();
	double getY();
	double getZ();
	
}
