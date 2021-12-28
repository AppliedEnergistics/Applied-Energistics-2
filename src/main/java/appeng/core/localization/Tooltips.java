package appeng.core.localization;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import org.jetbrains.annotations.NotNull;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.core.AEConfig;
import appeng.menu.me.interaction.EmptyingAction;

/**
 * Static utilities for constructing tooltips in various places.
 */
public final class Tooltips {

    public static final ChatFormatting MUTED_COLOR = ChatFormatting.DARK_GRAY;
    public static final ChatFormatting NORMAL_TOOLTIP_TEXT = ChatFormatting.GRAY;

    private Tooltips() {
    }

    public static List<Component> inputSlot(Side... sides) {
        var sidesText = Arrays.stream(sides).map(Tooltips::side).toList();

        return List.of(
                ButtonToolTips.CanInsertFrom.text(conjunction(sidesText))
                        .withStyle(MUTED_COLOR));
    }

    public static List<Component> outputSlot(Side... sides) {
        var sidesText = Arrays.stream(sides).map(Tooltips::side).toList();

        return List.of(
                ButtonToolTips.CanExtractFrom.text(conjunction(sidesText))
                        .withStyle(MUTED_COLOR));
    }

    public static Component side(Side side) {
        return switch (side) {
            case BOTTOM -> ButtonToolTips.SideBottom.text();
            case TOP -> ButtonToolTips.SideTop.text();
            case LEFT -> ButtonToolTips.SideLeft.text();
            case RIGHT -> ButtonToolTips.SideRight.text();
            case FRONT -> ButtonToolTips.SideFront.text();
            case BACK -> ButtonToolTips.SideBack.text();
        };
    }

    public static Component conjunction(List<Component> components) {
        return list(components, GuiText.And);
    }

    public static Component disjunction(List<Component> components) {
        return list(components, GuiText.Or);
    }

    @NotNull
    private static Component list(List<Component> components, GuiText lastJoiner) {
        if (components.isEmpty()) {
            return TextComponent.EMPTY;
        }

        if (components.size() == 2) {
            return components.get(0)
                    .copy()
                    .append(lastJoiner.text())
                    .append(components.get(1));
        }

        var current = components.get(0);
        for (int i = 1; i < components.size(); i++) {
            if (i + 1 < components.size()) {
                current = current.copy().append(", ").append(components.get(i));
            } else {
                current = current.copy().append(", ").append(lastJoiner.text()).append(" ").append(components.get(i));
            }
        }

        return current;
    }

    public static List<Component> getEmptyingTooltip(ButtonToolTips baseAction, ItemStack carried,
            EmptyingAction emptyingAction) {
        return List.of(
                baseAction.text(
                        getMouseButtonText(InputConstants.MOUSE_BUTTON_LEFT),
                        carried.getHoverName().copy().withStyle(NORMAL_TOOLTIP_TEXT)).withStyle(MUTED_COLOR),
                baseAction.text(
                        getMouseButtonText(InputConstants.MOUSE_BUTTON_RIGHT),
                        emptyingAction.description().copy().withStyle(NORMAL_TOOLTIP_TEXT)).withStyle(MUTED_COLOR));
    }

    public static Component getSetAmountTooltip() {
        return ButtonToolTips.ModifyAmountAction.text(Tooltips.getMouseButtonText(InputConstants.MOUSE_BUTTON_MIDDLE))
                .withStyle(MUTED_COLOR);
    }

    public static Component getMouseButtonText(int button) {
        return switch (button) {
            case InputConstants.MOUSE_BUTTON_LEFT -> ButtonToolTips.LeftClick.text();
            case InputConstants.MOUSE_BUTTON_RIGHT -> ButtonToolTips.RightClick.text();
            case InputConstants.MOUSE_BUTTON_MIDDLE -> ButtonToolTips.MiddleClick.text();
            default -> new TextComponent("Mouse " + button);
        };
    }

    /**
     * Should the amount be shown in the tooltip of the given stack.
     */
    public static boolean shouldShowAmountTooltip(AEKey what, long amount) {
        // TODO: Now that we can show fractional numbers, this approach of detecting whether the formatted amount has
        // been abbreviated or rounded no longer works
        var bigNumber = AEConfig.instance().isUseLargeFonts() ? 999L : 9999L;
        return amount > bigNumber * what.getAmountPerUnit()
                // Unit symbols are never shown in slots and must be shown in the tooltip instead
                || what.getUnitSymbol() != null
                // Damaged items always get their amount shown in the tooltip because
                // the amount is sometimes hard to read superimposed on the damage bar
                || what instanceof AEItemKey itemKey && itemKey.getItem().isBarVisible(itemKey.toStack());
    }

    public static Component getAmountTooltip(ButtonToolTips baseText, GenericStack stack) {
        return getAmountTooltip(baseText, stack.what(), stack.amount());
    }

    public static Component getAmountTooltip(ButtonToolTips baseText, AEKey what, long amount) {
        var amountText = what.formatAmount(amount, AmountFormat.FULL);
        return baseText.text(amountText).withStyle(MUTED_COLOR);
    }
}
