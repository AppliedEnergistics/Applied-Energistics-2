package appeng.integration.modules.emi;

import net.minecraft.network.chat.Component;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import appeng.core.definitions.AEParts;
import appeng.core.localization.ItemModText;

/**
 * Represents a specific source of attuning a subtype of {@link appeng.parts.p2p.P2PTunnelPart} to EMI.
 */
class EmiP2PAttunementRecipe extends BasicEmiRecipe {
    public static final EmiRecipeCategory CATEGORY = new AppEngRecipeCategory("p2p_attunement",
            EmiStack.of(AEParts.ME_P2P_TUNNEL), ItemModText.P2P_TUNNEL_ATTUNEMENT);
    private final EmiIngredient input;
    private final EmiStack p2pTunnel;
    private final Component description;

    public EmiP2PAttunementRecipe(EmiIngredient input, EmiStack p2pTunnel, Component description) {
        super(CATEGORY, null, 150, 36);
        this.input = input;
        this.p2pTunnel = p2pTunnel;
        this.description = description;
        inputs.add(input);
        outputs.add(p2pTunnel);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var originX = width / 2 - 41;
        var originY = height / 2 - 13;

        widgets.addSlot(input, originX + 3, originY + 4)
                .appendTooltip(description);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, originX + 27, originY + 4);
        widgets.addSlot(p2pTunnel, originX + 60, originY + 4);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }
}
