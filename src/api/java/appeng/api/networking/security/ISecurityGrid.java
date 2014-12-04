package appeng.api.networking.security;

import net.minecraft.entity.player.EntityPlayer;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridCache;

public interface ISecurityGrid extends IGridCache
{

	/**
	 * @return true if a security provider is in the network ( and only 1 )
	 */
	boolean isAvailable();

	/**
	 * Check if a player has permissions.
	 * 
	 * @param player to be checked player
	 * @param perm checked permissions
	 * 
	 * @return true if the player has permissions.
	 */
	boolean hasPermission(EntityPlayer player, SecurityPermissions perm);

	/**
	 * Check if a player has permissions.
	 * 
	 * @param playerID id of player
	 * @param perm checked permissions
	 * 
	 * @return true if the player has permissions.
	 */
	boolean hasPermission(int playerID, SecurityPermissions perm);

	/**
	 * @return PlayerID of the admin, or owner, this is the person who placed the security block.
	 */
	int getOwner();

}
