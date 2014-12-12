package appeng.api.networking.security;

import appeng.api.config.SecurityPermissions;

import java.util.EnumSet;

/**
 * Used by vanilla Security Terminal to post biometric data into the security cache.
 */
public interface ISecurityRegistry
{

	/**
	 * Submit Permissions into the register.
	 * 
	 * @param PlayerID player id
	 * @param permissions permissions of player
	 */
	void addPlayer(int PlayerID, EnumSet<SecurityPermissions> permissions);

}
