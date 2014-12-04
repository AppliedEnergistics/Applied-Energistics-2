package appeng.api.features;

import net.minecraft.entity.player.EntityPlayer;

import com.mojang.authlib.GameProfile;

/**
 * Maintains a save specific list of userids and username combinations this greatly simplifies storage internally and
 * gives a common place to look up and get IDs for the security framework.
 */
public interface IPlayerRegistry
{

	/**
	 * @param gameProfile user game profile
	 * @return user id of a username.
	 */
	int getID(GameProfile gameProfile);

	/**
	 * @param player player
	 * @return user id of a player entity.
	 */
	int getID(EntityPlayer player);

	/**
	 * @param playerID to be found player id
	 * @return PlayerEntity, or null if the player could not be found.
	 */
	EntityPlayer findPlayer(int playerID);

}
