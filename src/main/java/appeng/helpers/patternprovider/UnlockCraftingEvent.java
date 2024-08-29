package appeng.helpers.patternprovider;

/**
 * The types of event that the pattern provider is waiting for to unlock crafting again.
 */
public enum UnlockCraftingEvent {
    REDSTONE_POWER, // Waiting for redstone to be on (pulse blocking mode)
    REDSTONE_PULSE, // Waiting for redstone to turn off and back on (pulse blocking mode, already powered when craft started)
    RESULT // Waiting for result
}
