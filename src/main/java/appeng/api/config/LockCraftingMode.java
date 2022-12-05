package appeng.api.config;

/**
 * The circumstances under which a pattern provider will lock further crafting.
 */
public enum LockCraftingMode {
    /**
     * Crafting is never locked.
     */
    NONE,
    /**
     * After pushing a pattern to an adjacent machine, the pattern provider will not accept further crafts until a
     * redstone pulse is received.
     */
    LOCK_UNTIL_PULSE,
    /**
     * Crafting is locked while the pattern provider is receiving a redstone signal.
     */
    LOCK_WHILE_HIGH,
    /**
     * Crafting is locked while the pattern provider is not receiving a redstone signal.
     */
    LOCK_WHILE_LOW,
    /**
     * After pushing a pattern to an adjacent machine, the pattern provider will not accept further crafts until the
     * primary pattern result is returned to the network through the pattern provider.
     */
    LOCK_UNTIL_RESULT
}
