package appeng.client.gui.me.items;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

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

public class SmithingTableEncodingPanel extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(128, 70, 126, 68);

    private final ActionButton clearBtn;
    private final ToggleButton substitutionsBtn;
    private final Slot resultSlot;

    public SmithingTableEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);

        clearBtn = new ActionButton(ActionItems.CLOSE, act -> menu.clear());
        clearBtn.setHalfSize(true);
        widgets.add("smithingTableClearPattern", clearBtn);

        this.substitutionsBtn = createSubstitutionButton(widgets);

        this.resultSlot = new Slot(new SimpleContainer(1), 0, 0, 0);
        menu.addClientSideSlot(resultSlot, SlotSemantics.SMITHING_TABLE_RESULT);
    }

    @Override
    public ItemStack getTabIconItem() {
        return Items.SMITHING_TABLE.getDefaultInstance();
    }

    @Override
    public Component getTabTooltip() {
        return GuiText.SmithingTablePattern.text();
    }

    private ToggleButton createSubstitutionButton(WidgetContainer widgets) {
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
        widgets.add("smithingTableSubstitutions", button);
        return button;
    }

    @Override
    public void drawBackgroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
        BG.dest(bounds.getX() + 9, bounds.getY() + bounds.getHeight() - 164).blit(poseStack, zIndex);
    }

    @Override
    public void updateBeforeRender() {
        this.substitutionsBtn.setState(this.menu.substitute);

        var container = new SimpleContainer(2);
        container.setItem(0, menu.getSmithingTableBaseSlot().getItem());
        container.setItem(1, menu.getSmithingTableAdditionSlot().getItem());

        var level = menu.getPlayer().level;
        var recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMITHING, container, level)
                .orElse(null);
        if (recipe == null) {
            resultSlot.set(ItemStack.EMPTY);
        } else {
            resultSlot.set(recipe.assemble(container));
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        clearBtn.setVisibility(visible);
        substitutionsBtn.setVisibility(visible);

        screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_BASE, !visible);
        screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_ADDITION, !visible);
        screen.setSlotsHidden(SlotSemantics.SMITHING_TABLE_RESULT, !visible);
    }
}
