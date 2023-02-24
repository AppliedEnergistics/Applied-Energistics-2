package appeng.client.gui.me.items;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.config.ActionItems;
import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;

public class CraftingEncodingPanel extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(0, 0, 126, 68);

    private final ActionButton clearBtn;
    private final ToggleButton substitutionsBtn;
    private final ToggleButton fluidSubstitutionsBtn;

    public CraftingEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);

        // Add buttons for the crafting mode
        clearBtn = new ActionButton(ActionItems.CLOSE, act -> menu.clear());
        clearBtn.setHalfSize(true);
        widgets.add("craftingClearPattern", clearBtn);

        this.substitutionsBtn = createCraftingSubstitutionButton(widgets);
        this.fluidSubstitutionsBtn = createCraftingFluidSubstitutionButton(widgets);
    }

    @Override
    public ItemStack getTabIconItem() {
        return Items.CRAFTING_TABLE.getDefaultInstance();
    }

    @Override
    public Component getTabTooltip() {
        return GuiText.CraftingPattern.text();
    }

    private ToggleButton createCraftingSubstitutionButton(WidgetContainer widgets) {
        var button = new ToggleButton(
                Icon.SUBSTITUTION_ENABLED,
                Icon.SUBSTITUTION_DISABLED,
                menu::setSubstitute);
        button.setHalfSize(true);
        button.setTooltipOn(List.of(
                ButtonToolTips.SubstitutionsOn.text(),
                ButtonToolTips.SubstitutionsDescEnabled.text()));
        button.setTooltipOff(List.of(
                ButtonToolTips.SubstitutionsOff.text(),
                ButtonToolTips.SubstitutionsDescDisabled.text()));
        widgets.add("craftingSubstitutions", button);
        return button;
    }

    private ToggleButton createCraftingFluidSubstitutionButton(WidgetContainer widgets) {
        var button = new ToggleButton(
                Icon.FLUID_SUBSTITUTION_ENABLED,
                Icon.FLUID_SUBSTITUTION_DISABLED,
                menu::setSubstituteFluids);
        button.setHalfSize(true);
        button.setTooltipOn(List.of(
                ButtonToolTips.FluidSubstitutions.text(),
                ButtonToolTips.FluidSubstitutionsDescEnabled.text()));
        button.setTooltipOff(List.of(
                ButtonToolTips.FluidSubstitutions.text(),
                ButtonToolTips.FluidSubstitutionsDescDisabled.text()));
        widgets.add("craftingFluidSubstitutions", button);
        return button;
    }

    @Override
    public void drawBackgroundLayer(PoseStack poseStack, Rect2i bounds, Point mouse) {
        BG.dest(bounds.getX() + 9, bounds.getY() + bounds.getHeight() - 164).blit(poseStack);

        var absMouseX = bounds.getX() + mouse.getX();
        var absMouseY = bounds.getY() + mouse.getY();
        if (menu.substituteFluids && fluidSubstitutionsBtn.isMouseOver(absMouseX, absMouseY)) {
            for (var slotIndex : menu.slotsSupportingFluidSubstitution) {
                drawSlotGreenBG(bounds, poseStack, menu.getCraftingGridSlots()[slotIndex]);
            }
        }
    }

    private void drawSlotGreenBG(Rect2i bounds, PoseStack poseStack, Slot slot) {
        int x = bounds.getX() + slot.x;
        int y = bounds.getY() + slot.y;
        GuiComponent.fill(poseStack, x, y, x + 16, y + 16, 0x7f00FF00);
    }

    @Override
    public void updateBeforeRender() {
        this.substitutionsBtn.setState(this.menu.substitute);
        this.fluidSubstitutionsBtn.setState(this.menu.substituteFluids);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        clearBtn.setVisibility(visible);
        substitutionsBtn.setVisibility(visible);
        fluidSubstitutionsBtn.setVisibility(visible);

        screen.setSlotsHidden(SlotSemantics.CRAFTING_GRID, !visible);
        screen.setSlotsHidden(SlotSemantics.CRAFTING_RESULT, !visible);
    }
}
