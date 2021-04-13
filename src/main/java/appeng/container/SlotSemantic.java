package appeng.container;

public enum SlotSemantic {
    NONE(false),
    STORAGE(false),
    PLAYER_INVENTORY(true),
    PLAYER_HOTBAR(true),
    TOOLBOX(true),
    /**
     * Used for configuration slots that configure a filter, such as on planes, import/export busses, etc.
     */
    CONFIG(false),
    /**
     * An upgrade slot on a machine, cell workbench, etc.
     */
    UPGRADE(false),
    /**
     * One or more slots for storage cells, i.e. on drives, cell workbench or chest.
     */
    STORAGE_CELL(false),

    INSCRIBER_PLATE_TOP(false),

    INSCRIBER_PLATE_BOTTOM(false),

    MACHINE_INPUT(false),

    MACHINE_PROCESSING(false),

    MACHINE_OUTPUT(false),

    MACHINE_CRAFTING_GRID(false),

    BLANK_PATTERN(false),

    ENCODED_PATTERN(false),

    VIEW_CELL(false),

    CRAFTING_GRID(false),

    CRAFTING_RESULT(false),

    PROCESSING_RESULT(false),

    BIOMETRIC_CARD(false);

    private final boolean playerSide;

    SlotSemantic(boolean playerSide) {
        this.playerSide = playerSide;
    }

    /**
     * @return Indicates whether a slot is considered to be part of the items that a player carries.
     */
    public boolean isPlayerSide() {
        return playerSide;
    }
}
