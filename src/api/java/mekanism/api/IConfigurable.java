package mekanism.api;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Implement this in your TileEntity class if your block can be modified by a Configurator.
 * @author aidancbrady
 *
 */
public interface IConfigurable
{
	/**
	 * Called when a player shift-right clicks this block with a Configurator.
	 * @param player - the player who clicked the block
	 * @param side - the side the block was clicked on
	 * @return whether or not an action was performed
	 */
	public boolean onSneakRightClick(EntityPlayer player, int side);

	/**
	 * Called when a player right clicks this block with a Configurator.
	 * @param player - the player who clicked the block
	 * @param side - the side the block was clicked on
	 * @return whether or not an action was performed
	 */
	public boolean onRightClick(EntityPlayer player, int side);
}
