package appeng.menu;

/**
 * Used to group {@link net.minecraft.world.inventory.Slot slots} in a menu into specific semantics, which are then
 * positioned on the screen from a {@link appeng.client.gui.style.ScreenStyle}.
 *
 * @param playerSide Indicates whether a slot is considered to be part of the items that a player carries.
 * @see SlotSemantics For a registry of slot semantics.
 */
public record SlotSemantic(String id, boolean playerSide) {
}
