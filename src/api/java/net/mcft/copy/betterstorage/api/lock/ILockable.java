package net.mcft.copy.betterstorage.api.lock;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ILockable {
	
	/** Returns the lock of this container, or null if there is none. */
	public ItemStack getLock();
	
	/** Returns if this container can be locked with this lock. */
	public boolean isLockValid(ItemStack lock);
	
	/** Sets the lock of this container. <br>
	 *  Has no effect if canSetLock() returns false. */
	public void setLock(ItemStack lock);
	
	/** Returns if this container can be used by the player without using a key,
	 *  for example, while the container is being held open by another player. */
	public boolean canUse(EntityPlayer player);
	
	/** Called when a lock gets unlocked by a key and the container
	 *  should be used/opened by the player as if it wasn't locked. */
	public void useUnlocked(EntityPlayer player);
	
	/** Makes the container emit a redstone signal for 10 ticks. */
	public void applyTrigger();
	
}
