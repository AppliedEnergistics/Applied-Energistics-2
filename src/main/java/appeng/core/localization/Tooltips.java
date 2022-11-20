package appeng.core.localization;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.mojang.blaze3d.platform.InputConstants;

import org.jetbrains.annotations.NotNull;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.PowerUnits;
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

    private static final char SEP;
    static {
        var format = (DecimalFormat) DecimalFormat.getInstance();
        var symbols = format.getDecimalFormatSymbols();
        SEP = symbols.getDecimalSeparator();
    }

    public static final ChatFormatting MUTED_COLOR = ChatFormatting.DARK_GRAY;

    public static final Style NORMAL_TOOLTIP_TEXT = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false);
    public static final Style QUOTE_TEXT = NORMAL_TOOLTIP_TEXT.withItalic(true);
    public static final Style NUMBER_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0x886eff)).withItalic(false);
    public static final Style UNIT_TEXT = Style.EMPTY.withColor(TextColor.fromRgb(0xffde7d)).withItalic(false);

    public static final Style RED = Style.EMPTY.withColor(ChatFormatting.RED);
    public static final Style GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);

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
            return Component.empty();
        }

        if (components.size() == 2) {
            return components.get(0)
                    .copy()
                    .append(" ")
                    .append(lastJoiner.text())
                    .append(" ")
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
            default -> ButtonToolTips.MouseButton.text(button);
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

    public record Amount(String digit, String unit) {
    }

    public record MaxedAmount(String digit, String maxDigit, String unit) {
    }

    public static Component ofDuration(long number, TimeUnit unit) {
        long seconds = TimeUnit.SECONDS.convert(number, unit);
        // We don't have 1s, but not 0 either
        if (seconds == 0) {
            if (number > 0) {
                return Component.literal("~")
                        .withStyle(NUMBER_TEXT)
                        .append(ButtonToolTips.DurationFormatSeconds.text(0));
            } else {
                return ButtonToolTips.DurationFormatSeconds.text(0)
                        .withStyle(NUMBER_TEXT);
            }
        }

        MutableComponent durationStr = Component.literal("");
        var hours = TimeUnit.HOURS.convert(seconds, TimeUnit.SECONDS);
        if (hours > 0) {
            durationStr.append(Long.toString(hours)).append("h");
            seconds -= hours * 60 * 60;
        }
        var minutes = TimeUnit.MINUTES.convert(seconds, TimeUnit.SECONDS);
        if (minutes > 0) {
            durationStr.append(Long.toString(minutes)).append("m");
            seconds -= minutes * 60;
        }
        if (seconds > 0) {
            durationStr.append(Long.toString(seconds)).append("s");
        }

        return durationStr.withStyle(NUMBER_TEXT);
    }

    public static final String[] units = new String[] { "k", "M", "G", "T", "P", "E" };
    public static final long[] DECIMAL_NUMS = new long[] { 1000L, 1000_000L, 1000_000_000L, 1000_000_000_000L,
            1000_000_000_000_000L,
            1000_000_000_000_000_000L };
    public static final long[] BYTE_NUMS = new long[] { 1024L, 1024 * 1024L, 1024 * 1024 * 1024L, 1024 * 1024 * 1024L };

    public static Component ofAmount(GenericStack stack) {
        return Component.literal(stack.what().formatAmount(stack.amount(), AmountFormat.FULL))
                .withStyle(NUMBER_TEXT);
    }

    public static String getAmount(double amount, long num) {
        double fract = amount / num;
        String returned;
        if (fract < 10) {
            returned = String.format("%.3f", fract);
        } else if (fract < 100) {
            returned = String.format("%.2f", fract);
        } else {
            returned = String.format("%.1f", fract);
        }
        while (returned.endsWith("0")) {
            returned = returned.substring(0, returned.length() - 1);
        }
        if (returned.endsWith(String.valueOf(SEP))) {
            returned = returned.substring(0, returned.length() - 1);
        }
        return returned;

    }

    public static Amount getAmount(double amount) {
        if (amount < 10000) {
            return new Amount(getAmount(amount, 1), "");
        } else {
            int i = 0;
            while (amount / DECIMAL_NUMS[i] >= 1000) {
                i++;
            }
            return new Amount(getAmount(amount, DECIMAL_NUMS[i]), units[i]);
        }
    }

    public static MaxedAmount getMaxedAmount(double amount, double max) {
        if (max < 10000) {
            return new MaxedAmount(getAmount(amount, 1), getAmount(max, 1), "");
        } else {
            int i = 0;
            while (max / DECIMAL_NUMS[i] >= 1000) {
                i++;
            }
            return new MaxedAmount(getAmount(amount, DECIMAL_NUMS[i]), getAmount(max, DECIMAL_NUMS[i]), units[i]);
        }
    }

    public static Amount getByteAmount(long amount) {
        if (amount < BYTE_NUMS[0]) {
            return new Amount(String.valueOf(amount), "");
        } else {
            int i = 0;
            while (i < BYTE_NUMS.length && amount / BYTE_NUMS[i] >= 1000) {
                i++;
            }
            return new Amount(getAmount(amount, BYTE_NUMS[i]), units[i]);
        }
    }

    public static Amount getAmount(long amount) {
        if (amount < 10000) {
            return new Amount(String.valueOf(amount), "");
        } else {
            int i = 0;
            while (i < DECIMAL_NUMS.length && amount / DECIMAL_NUMS[i] >= 1000) {
                i++;
            }
            return new Amount(getAmount(amount, DECIMAL_NUMS[i]), units[i]);
        }
    }

    public static MaxedAmount getMaxedAmount(long amount, long max) {
        if (max < 10000) {
            return new MaxedAmount(String.valueOf(amount), String.valueOf(max), "");
        } else {
            int i = 0;
            while (max / DECIMAL_NUMS[i] >= 1000) {
                i++;
            }
            return new MaxedAmount(getAmount(amount, DECIMAL_NUMS[i]), getAmount(max, DECIMAL_NUMS[i]), units[i]);
        }
    }

    public static MutableComponent of(Component component) {
        return component.copy().withStyle(NORMAL_TOOLTIP_TEXT);
    }

    public static MutableComponent of(ButtonToolTips buttonToolTips, Object... args) {
        return of(buttonToolTips, NORMAL_TOOLTIP_TEXT, args);
    }

    public static MutableComponent of(ButtonToolTips buttonToolTips, Style style, Object... args) {
        return buttonToolTips.text(args).copy().withStyle(style);
    }

    public static MutableComponent of(GuiText guiText, Object... args) {
        return of(guiText, NORMAL_TOOLTIP_TEXT, args);
    }

    public static MutableComponent of(GuiText guiText, Style style, Object... args) {

        if (args.length > 0 && args[0] instanceof Integer) {
            return guiText.text(Arrays.stream(args).map((o) -> ofUnformattedNumber((Integer) o)).toArray()).copy()
                    .withStyle(style);
        } else if (args.length > 0 && args[0] instanceof Long) {
            return guiText.text(Arrays.stream(args).map((o) -> ofUnformattedNumber((Long) o)).toArray()).copy()
                    .withStyle(style);
        }
        return guiText.text(args).copy().withStyle(style);

    }

    public static MutableComponent of(String s) {
        return Component.literal(s).withStyle(NORMAL_TOOLTIP_TEXT);
    }

    public static MutableComponent of(PowerUnits pU) {
        return pU.textComponent().copy().withStyle(UNIT_TEXT);
    }

    public static MutableComponent ofPercent(double percent, boolean oneIsGreen) {
        return Component.literal(MessageFormat.format("{0,number,#.##%}", percent))
                .withStyle(colorFromRatio(percent, oneIsGreen));
    }

    public static Style colorFromRatio(double ratio, boolean oneIsGreen) {
        double p = ratio;

        if (!oneIsGreen) {
            p = 1 - p;
        }

        int r = (int) (255d * (Math.max(0, Math.min(2 - 2 * p, 1))));
        int g = (int) (255d * (Math.max(0, Math.min(2 * p, 1))));
        int rgb = 0xFF000000 + (r << 16) + (g << 8);

        return Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(rgb));
    }

    public static MutableComponent ofPercent(double percent) {
        return ofPercent(percent, true);

    }

    public static MutableComponent ofUnformattedNumber(long number) {
        return Component.literal(String.valueOf(number)).withStyle(NUMBER_TEXT);
    }

    public static MutableComponent ofUnformattedNumberWithRatioColor(long number, double ratio, boolean oneIsGreen) {
        return Component.literal(String.valueOf(number)).withStyle(colorFromRatio(ratio, oneIsGreen));
    }

    public static MutableComponent ofBytes(long number) {
        Amount amount = getByteAmount(number);
        return ofNumber(amount);
    }

    public static MutableComponent ofNumber(long number) {
        Amount amount = getAmount(number);
        return ofNumber(amount);
    }

    public static MutableComponent ofNumber(double number) {
        Amount amount = getAmount(number);
        return ofNumber(amount);
    }

    private static MutableComponent ofNumber(Amount number) {
        return Component.literal(number.digit() + number.unit()).withStyle(NUMBER_TEXT);
    }

    public static MutableComponent ofNumber(long number, long max) {
        MaxedAmount amount = getMaxedAmount(number, max);
        return ofNumber(amount);
    }

    public static MutableComponent ofNumber(double number, double max) {
        MaxedAmount amount = getMaxedAmount(number, max);
        return ofNumber(amount);
    }

    private static MutableComponent ofNumber(MaxedAmount number) {
        boolean numberUnit = !number.digit().equals("0");
        return Component.literal(number.digit() + (numberUnit ? number.unit() : "")).withStyle(NUMBER_TEXT)
                .append(Component.literal("/")
                        .withStyle(NORMAL_TOOLTIP_TEXT))
                .append(number.maxDigit() + number.unit()).withStyle(NUMBER_TEXT);
    }

    public static MutableComponent of(Component... components) {
        MutableComponent s = Component.literal("");
        for (var c : components) {
            s = s.append(c);
        }
        return s;
    }

    public static Component energyStorageComponent(double energy, double max) {
        return Tooltips.of(
                Tooltips.of(GuiText.StoredEnergy),
                Tooltips.of(": "),
                Tooltips.ofNumber(energy, max),
                Tooltips.of(" "),
                Tooltips.of(PowerUnits.AE),
                Tooltips.of(" ("),
                Tooltips.ofPercent(energy / max),
                Tooltips.of(")"));
    }

    public static Component bytesUsed(long bytes, long max) {
        return of(
                GuiText.BytesUsed,
                Tooltips.of(
                        ofUnformattedNumberWithRatioColor(bytes, (double) bytes / max, false),
                        of(" "),
                        of(GuiText.Of),
                        of(" "),
                        ofUnformattedNumber(max)));
    }

    public static Component typesUsed(long types, long max) {
        return Tooltips.of(
                ofUnformattedNumberWithRatioColor(types, (double) types / max, false),
                of(" "),
                of(GuiText.Of),
                of(" "),
                ofUnformattedNumber(max),
                of(" "),
                of(GuiText.Types));
    }

}
