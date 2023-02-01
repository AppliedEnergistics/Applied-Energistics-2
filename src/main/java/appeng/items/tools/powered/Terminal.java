package appeng.items.tools.powered;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.core.sync.GuiBridge;

public enum Terminal {
    WIRELESS_TERMINAL(AEApi.instance().definitions().items().wirelessTerminal(), GuiBridge.GUI_WIRELESS_TERM),
    WIRELESS_CRAFTING_TERMINAL(AEApi.instance().definitions().items().wirelessCraftingTerminal(), GuiBridge.GUI_WIRELESS_CRAFTING_TERMINAL),
    WIRELESS_PATTERN_TERMINAL(AEApi.instance().definitions().items().wirelessPatternTerminal(), GuiBridge.GUI_WIRELESS_PATTERN_TERMINAL),
    WIRELESS_FLUID_TERMINAL(AEApi.instance().definitions().items().wirelessFluidTerminal(), GuiBridge.GUI_WIRELESS_FLUID_TERMINAL);

    final GuiBridge bridge;
    final IItemDefinition itemDefinition;

    Terminal(IItemDefinition itemDefinition, GuiBridge guiBridge) {
        this.itemDefinition = itemDefinition;
        this.bridge = guiBridge;
    }

    public GuiBridge getBridge() {
        return bridge;
    }

    public IItemDefinition getItemDefinition() {
        return itemDefinition;
    }
}
