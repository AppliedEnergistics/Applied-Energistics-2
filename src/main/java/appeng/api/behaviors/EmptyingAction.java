package appeng.api.behaviors;

import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEKey;

/**
 * Describes the action of emptying an item into the storage network.
 */
public record EmptyingAction(Component description, AEKey what, long maxAmount) {
}
