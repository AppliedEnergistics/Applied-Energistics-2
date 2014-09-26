package uristqwerty.CraftGuide.api;

/**
 * SlotType is used to declare the purpose of a {@link Slot}, used
 * by searches (both in-game and through the API).
 */
public enum SlotType
{
	/**
	 * For searches; matches anything except 
	 * {@link HIDDEN_SLOT} and {@link DISPLAY_SLOT}
	 */
	ANY_SLOT,

	/** Resources required for the recipe */
	INPUT_SLOT,
	/** Products produced by the recipe */
	OUTPUT_SLOT,
	/**	Machine(s) that processes the recipe */
	MACHINE_SLOT,

	/**	
	 * Just in case {@link INPUT_SLOT}, {@link OUTPUT_SLOT},
	 * and {@link MACHINE_SLOT} aren't adequate descriptions
	 * of this slot's purpose.
	 */
	OTHER_SLOT,
	
	/**
	 * Does not show up in searches for {@link ANY_SLOT}.
	 * Use for visual effects and such.
	 */
	DISPLAY_SLOT,
	/**
	 * Does not show up in searches for {@link ANY_SLOT}.
	 */
	HIDDEN_SLOT,
}
