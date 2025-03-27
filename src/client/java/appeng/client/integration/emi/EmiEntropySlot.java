package appeng.client.integration.emi;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEFluidKey;
import appeng.core.localization.ItemModText;

/**
 * Extended slot used in {@link EmiEntropyRecipe}.
 */
class EmiEntropySlot extends SlotWidget {
    private final boolean consumed;
    private final boolean flowing;

    public EmiEntropySlot(EmiStack stack, boolean consumed, boolean flowing, int x, int y) {
        super(stack, x, y);
        this.consumed = consumed;
        this.flowing = flowing;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        var tooltip = super.getTooltip(mouseX, mouseY);

        var fluid = ((EmiStack) stack).getKeyOfType(Fluid.class);
        if (fluid != null) {
            // We use our own tooltip composition here since it's hard to customize client tooltip components
            tooltip.clear();

            var fluidTooltip = AEKeyRendering.getTooltip(AEFluidKey.of(fluid));
            // Prepend "Flowing" to the first line if we're dealing with a non-source block
            if (!fluidTooltip.isEmpty() && flowing) {
                fluidTooltip.set(
                        0,
                        ItemModText.FLOWING_FLUID_NAME.text(fluidTooltip.get(0)));
            }

            fluidTooltip.stream()
                    .map(Component::getVisualOrderText)
                    .map(ClientTooltipComponent::create)
                    .forEach(tooltip::add);

            addSlotTooltip(tooltip);
        }

        if (consumed) {
            var text = ItemModText.CONSUMED.text().withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
            tooltip.add(ClientTooltipComponent.create(text.getVisualOrderText()));
        }

        return tooltip;
    }

    @Override
    public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
        // Draw an X over the input ingredient to represent that it is consumed
        if (consumed) {
            var bounds = getBounds();
            draw.blit(RenderType::guiTextured, AppEngEmiPlugin.TEXTURE, bounds.x() + 1, bounds.y() + 1,
                    0, 0,
                    0, 52, 16, 16);
        }
        super.drawOverlay(draw, mouseX, mouseY, delta);
    }
}
