package appeng.core.localization;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

/**
 * Static utilities for constructing tooltips in various places.
 */
public final class Tooltips {

    private Tooltips() {
    }

    public static List<Component> inputSlot(Side... sides) {
        var sidesText = Arrays.stream(sides).map(Tooltips::side).toList();

        return List.of(
                ButtonToolTips.CanInsertFrom.text(conjunction(sidesText))
                        .withStyle(ChatFormatting.GRAY));
    }

    public static List<Component> outputSlot(Side... sides) {
        var sidesText = Arrays.stream(sides).map(Tooltips::side).toList();

        return List.of(
                ButtonToolTips.CanExtractFrom.text(conjunction(sidesText))
                        .withStyle(ChatFormatting.GRAY));
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

}
