package appeng.client.gui;

public enum NumberEntryType {
    CRAFT_ITEM_COUNT(Long.class),
    PRIORITY(Long.class),
    LEVEL_ITEM_COUNT(Long.class),
    LEVEL_FLUID_VOLUME(Long.class);

    private final Class<? extends Number> inputType;

    NumberEntryType(Class<? extends Number> inputType) {
        this.inputType = inputType;
    }

    public Class<? extends Number> getInputType() {
        return inputType;
    }

}
