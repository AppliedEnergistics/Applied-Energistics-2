package appeng.helpers;

public enum InventoryAction
{
	// standard vanilla mechanics.
	PICKUP_OR_SET_DOWN, SPLIT_OR_PLACE_SINGLE, CREATIVE_DUPLICATE, SHIFT_CLICK,

	// crafting term
	CRAFT_STACK, CRAFT_ITEM, CRAFT_SHIFT,

	// extra...
	MOVE_REGION, PICKUP_SINGLE, UPDATE_HAND, ROLL_UP, ROLL_DOWN, AUTO_CRAFT, PLACE_SINGLE
}
