package appeng.api.implementations.items;

import appeng.api.config.SecurityPermissions;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.security.ISecurityRegistry;
import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;

public interface IBiometricCard
{

	/**
	 * Set the  {@link GameProfile} to null, to clear it.
	 */
	void setProfile(ItemStack itemStack, GameProfile username);

	/**
	 * @return {@link GameProfile} of the player encoded on this card, or a blank string.
	 */
	GameProfile getProfile(ItemStack is);

	/**
	 * @param itemStack card
	 * @return the full list of permissions encoded on the card.
	 */
	EnumSet<SecurityPermissions> getPermissions(ItemStack itemStack);

	/**
	 * Check if a permission is encoded on the card.
	 * 
	 * @param permission card
	 * @return true if this permission is set on the card.
	 */
	boolean hasPermission(ItemStack is, SecurityPermissions permission);

	/**
	 * remove a permission from the item stack.
	 * 
	 * @param itemStack card
	 * @param permission to be removed permission
	 */
	void removePermission(ItemStack itemStack, SecurityPermissions permission);

	/**
	 * add a permission to the item stack.
	 * 
	 * @param itemStack card
	 * @param permission to be added permission
	 */
	void addPermission(ItemStack itemStack, SecurityPermissions permission);

	/**
	 * lets you handle submission of security values on the card for custom behavior.
	 * 
	 * @param registry security registry
	 * @param pr player registry
	 * @param is card
	 */
	void registerPermissions(ISecurityRegistry registry, IPlayerRegistry pr, ItemStack is);

}
