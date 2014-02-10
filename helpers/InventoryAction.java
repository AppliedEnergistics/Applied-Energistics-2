package appeng.helpers;

public enum InventoryAction
{
	// standard vanilla mechanics.
	PICKUP_OR_SETDOWN, SPLIT_OR_PLACESINGLE, CREATIVE_DUPLICATE, SHIFT_CLICK,

	// crafting term
	CRAFT_STACK, CRAFT_ITEM, CRAFT_SHIFT,

	// extra...
	MOVE_REGION, PICKUP_SINGLE, UPDATE_HAND
}
