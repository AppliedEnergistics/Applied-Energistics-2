/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.gui.widgets;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import appeng.api.config.*;
import appeng.core.localization.ButtonToolTips;
import appeng.util.EnumCycler;

public class SettingToggleButton<T extends Enum<T>> extends IconButton {
    private static final Pattern COMPILE = Pattern.compile("%s");
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);
    private static Map<EnumPair, ButtonAppearance> appearances;
    private final Settings buttonSetting;
    private final IHandler<SettingToggleButton<T>> onPress;
    private final EnumSet<T> validValues;
    private String fillVar;
    private T currentValue;

    @FunctionalInterface
    public interface IHandler<T extends SettingToggleButton<?>> {
        void handle(T button, boolean backwards);
    }

    public SettingToggleButton(final int x, final int y, final Settings setting, final T val,
            IHandler<SettingToggleButton<T>> onPress) {
        this(x, y, setting, val, t -> true, onPress);
    }

    public SettingToggleButton(final int x, final int y, final Settings setting, final T val, Predicate<T> isValidValue,
            IHandler<SettingToggleButton<T>> onPress) {
        super(x, y, SettingToggleButton::onPress);
        this.onPress = onPress;

        // Build a list of values (in order) that are valid w.r.t. the given predicate
        EnumSet<T> validValues = EnumSet.allOf(val.getDeclaringClass());
        validValues.removeIf(isValidValue.negate());
        validValues.removeIf(s -> !setting.getPossibleValues().contains(s));
        this.validValues = validValues;

        this.buttonSetting = setting;
        this.currentValue = val;

        if (appearances == null) {
            appearances = new HashMap<>();
            registerApp(16 * 7, Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH, ButtonToolTips.CondenserOutput,
                    ButtonToolTips.Trash);
            registerApp(16 * 7 + 1, Settings.CONDENSER_OUTPUT, CondenserOutput.MATTER_BALLS,
                    ButtonToolTips.CondenserOutput, ButtonToolTips.MatterBalls);
            registerApp(16 * 7 + 2, Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY,
                    ButtonToolTips.CondenserOutput, ButtonToolTips.Singularity);

            registerApp(16 * 9 + 1, Settings.ACCESS, AccessRestriction.READ, ButtonToolTips.IOMode,
                    ButtonToolTips.Read);
            registerApp(16 * 9, Settings.ACCESS, AccessRestriction.WRITE, ButtonToolTips.IOMode, ButtonToolTips.Write);
            registerApp(16 * 9 + 2, Settings.ACCESS, AccessRestriction.READ_WRITE, ButtonToolTips.IOMode,
                    ButtonToolTips.ReadWrite);

            registerApp(16 * 10, Settings.POWER_UNITS, PowerUnits.AE, ButtonToolTips.PowerUnits,
                    PowerUnits.AE.textComponent());
            registerApp(16 * 10 + 1, Settings.POWER_UNITS, PowerUnits.EU, ButtonToolTips.PowerUnits,
                    PowerUnits.EU.textComponent());
            registerApp(16 * 10 + 4, Settings.POWER_UNITS, PowerUnits.RF, ButtonToolTips.PowerUnits,
                    PowerUnits.RF.textComponent());

            registerApp(3, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.AlwaysActive);
            registerApp(0, Settings.REDSTONE_CONTROLLED, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.ActiveWithoutSignal);
            registerApp(1, Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.ActiveWithSignal);
            registerApp(2, Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.ActiveOnPulse);

            registerApp(0, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.EmitLevelsBelow);
            registerApp(1, Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.EmitLevelAbove);

            registerApp(51, Settings.OPERATION_MODE, OperationMode.FILL, ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToStorageCell);
            registerApp(50, Settings.OPERATION_MODE, OperationMode.EMPTY, ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToNetwork);

            registerApp(51, Settings.IO_DIRECTION, RelativeDirection.LEFT, ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToStorageCell);
            registerApp(50, Settings.IO_DIRECTION, RelativeDirection.RIGHT, ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToNetwork);

            registerApp(48, Settings.SORT_DIRECTION, SortDir.ASCENDING, ButtonToolTips.SortOrder,
                    ButtonToolTips.ToggleSortDirection);
            registerApp(49, Settings.SORT_DIRECTION, SortDir.DESCENDING, ButtonToolTips.SortOrder,
                    ButtonToolTips.ToggleSortDirection);

            registerApp(16 * 2 + 3, Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_Auto);
            registerApp(16 * 2 + 4, Settings.SEARCH_MODE, SearchBoxMode.MANUAL_SEARCH, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_Standard);
            registerApp(16 * 2 + 5, Settings.SEARCH_MODE, SearchBoxMode.JEI_AUTOSEARCH, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_JEIAuto);
            registerApp(16 * 2 + 6, Settings.SEARCH_MODE, SearchBoxMode.JEI_MANUAL_SEARCH, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_JEIStandard);
            registerApp(16 * 2 + 7, Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH_KEEP, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_AutoKeep);
            registerApp(16 * 2 + 8, Settings.SEARCH_MODE, SearchBoxMode.MANUAL_SEARCH_KEEP, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_StandardKeep);
            registerApp(16 * 2 + 9, Settings.SEARCH_MODE, SearchBoxMode.JEI_AUTOSEARCH_KEEP, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_JEIAutoKeep);
            registerApp(16 * 2 + 10, Settings.SEARCH_MODE, SearchBoxMode.JEI_MANUAL_SEARCH_KEEP,
                    ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_JEIStandardKeep);

            registerApp(16 * 5 + 3, Settings.LEVEL_TYPE, LevelType.ENERGY_LEVEL, ButtonToolTips.LevelType,
                    ButtonToolTips.LevelType_Energy);
            registerApp(16 * 4 + 3, Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL, ButtonToolTips.LevelType,
                    ButtonToolTips.LevelType_Item);

            registerApp(16 * 13, Settings.TERMINAL_STYLE, TerminalStyle.TALL, ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Tall);
            registerApp(16 * 13 + 1, Settings.TERMINAL_STYLE, TerminalStyle.SMALL, ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Small);
            registerApp(16 * 13 + 2, Settings.TERMINAL_STYLE, TerminalStyle.FULL, ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Full);

            registerApp(64, Settings.SORT_BY, SortOrder.NAME, ButtonToolTips.SortBy, ButtonToolTips.ItemName);
            registerApp(65, Settings.SORT_BY, SortOrder.AMOUNT, ButtonToolTips.SortBy, ButtonToolTips.NumberOfItems);
            // 68: Formerly sort by inventory tweaks
            registerApp(69, Settings.SORT_BY, SortOrder.MOD, ButtonToolTips.SortBy, ButtonToolTips.Mod);

            registerApp(16, Settings.VIEW_MODE, ViewItems.STORED, ButtonToolTips.View, ButtonToolTips.StoredItems);
            registerApp(18, Settings.VIEW_MODE, ViewItems.ALL, ButtonToolTips.View, ButtonToolTips.StoredCraftable);
            registerApp(19, Settings.VIEW_MODE, ViewItems.CRAFTABLE, ButtonToolTips.View, ButtonToolTips.Craftable);

            registerApp(16 * 6, Settings.FUZZY_MODE, FuzzyMode.PERCENT_25, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_25);
            registerApp(16 * 6 + 1, Settings.FUZZY_MODE, FuzzyMode.PERCENT_50, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_50);
            registerApp(16 * 6 + 2, Settings.FUZZY_MODE, FuzzyMode.PERCENT_75, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_75);
            registerApp(16 * 6 + 3, Settings.FUZZY_MODE, FuzzyMode.PERCENT_99, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_99);
            registerApp(16 * 6 + 4, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZIgnoreAll);

            registerApp(80, Settings.FULLNESS_MODE, FullnessMode.EMPTY, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenEmpty);
            registerApp(81, Settings.FULLNESS_MODE, FullnessMode.HALF, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenWorkIsDone);
            registerApp(82, Settings.FULLNESS_MODE, FullnessMode.FULL, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenFull);

            registerApp(16 + 5, Settings.BLOCK, YesNo.YES, ButtonToolTips.InterfaceBlockingMode,
                    ButtonToolTips.Blocking);
            registerApp(16 + 4, Settings.BLOCK, YesNo.NO, ButtonToolTips.InterfaceBlockingMode,
                    ButtonToolTips.NonBlocking);

            registerApp(16 + 3, Settings.CRAFT_ONLY, YesNo.YES, ButtonToolTips.Craft, ButtonToolTips.CraftOnly);
            registerApp(16 + 2, Settings.CRAFT_ONLY, YesNo.NO, ButtonToolTips.Craft, ButtonToolTips.CraftEither);

            registerApp(16 * 11 + 2, Settings.CRAFT_VIA_REDSTONE, YesNo.YES, ButtonToolTips.EmitterMode,
                    ButtonToolTips.CraftViaRedstone);
            registerApp(16 * 11 + 1, Settings.CRAFT_VIA_REDSTONE, YesNo.NO, ButtonToolTips.EmitterMode,
                    ButtonToolTips.EmitWhenCrafting);

            registerApp(16 * 3 + 5, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY,
                    ButtonToolTips.ReportInaccessibleItems, ButtonToolTips.ReportInaccessibleItemsNo);
            registerApp(16 * 3 + 6, Settings.STORAGE_FILTER, StorageFilter.NONE, ButtonToolTips.ReportInaccessibleItems,
                    ButtonToolTips.ReportInaccessibleItemsYes);

            registerApp(16 * 14, Settings.PLACE_BLOCK, YesNo.YES, ButtonToolTips.BlockPlacement,
                    ButtonToolTips.BlockPlacementYes);
            registerApp(16 * 14 + 1, Settings.PLACE_BLOCK, YesNo.NO, ButtonToolTips.BlockPlacement,
                    ButtonToolTips.BlockPlacementNo);

            registerApp(16 * 15, Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT, ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeDefault);
            registerApp(16 * 15 + 1, Settings.SCHEDULING_MODE, SchedulingMode.ROUNDROBIN, ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeRoundRobin);
            registerApp(16 * 15 + 2, Settings.SCHEDULING_MODE, SchedulingMode.RANDOM, ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeRandom);
        }
    }

    private static void onPress(ButtonWidget btn) {
        if (btn instanceof SettingToggleButton) {
            ((SettingToggleButton<?>) btn).triggerPress();
        }
    }

    private void triggerPress() {
        boolean backwards = MinecraftClient.getInstance().mouse.wasRightButtonClicked();
        onPress.handle(this, backwards);
    }

    private static void registerApp(final int iconIndex, final Settings setting, final Enum<?> val,
            final ButtonToolTips title, final Text hint) {
        final ButtonAppearance a = new ButtonAppearance();
        a.displayName = title.text();
        a.displayValue = hint;
        a.index = iconIndex;
        appearances.put(new EnumPair(setting, val), a);
    }

    private static void registerApp(final int iconIndex, final Settings setting, final Enum<?> val,
            final ButtonToolTips title, final ButtonToolTips hint) {
        registerApp(iconIndex, setting, val, title, hint.text());
    }

    @Override
    protected int getIconIndex() {
        if (this.buttonSetting != null && this.currentValue != null) {
            final ButtonAppearance app = appearances.get(new EnumPair(this.buttonSetting, this.currentValue));
            if (app == null) {
                return 256 - 1;
            }
            return app.index;
        }
        return 256 - 1;
    }

    public Settings getSetting() {
        return this.buttonSetting;
    }

    public T getCurrentValue() {
        return this.currentValue;
    }

    public void set(final T e) {
        if (this.currentValue != e) {
            this.currentValue = e;
        }
    }

    public T getNextValue(boolean backwards) {
        return EnumCycler.rotateEnum(currentValue, backwards, validValues);
    }

    @Override
    public Text getTooltipMessage() {
        Text displayName = null;
        Text displayValue = null;

        if (this.buttonSetting != null && this.currentValue != null) {
            final ButtonAppearance buttonAppearance = appearances
                    .get(new EnumPair(this.buttonSetting, this.currentValue));
            if (buttonAppearance == null) {
                return new LiteralText("No Such Message");
            }

            displayName = buttonAppearance.displayName;
            displayValue = buttonAppearance.displayValue;
        }

        if (displayName != null) {
            String name = displayName.getString();
            String value = displayValue.getString();

            if (this.fillVar != null) {
                value = COMPILE.matcher(value).replaceFirst(this.fillVar);
            }

            value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n");
            final StringBuilder sb = new StringBuilder(value);

            int i = sb.lastIndexOf("\n");
            if (i <= 0) {
                i = 0;
            }
            while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
                sb.replace(i, i + 1, "\n");
            }

            return new LiteralText(name + '\n' + sb);
        }
        return LiteralText.EMPTY;
    }

    public String getFillVar() {
        return this.fillVar;
    }

    public void setFillVar(final String fillVar) {
        this.fillVar = fillVar;
    }

    private static final class EnumPair {

        final Settings setting;
        final Enum<?> value;

        EnumPair(final Settings a, final Enum<?> b) {
            this.setting = a;
            this.value = b;
        }

        @Override
        public int hashCode() {
            return this.setting.hashCode() ^ this.value.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final EnumPair other = (EnumPair) obj;
            return other.setting == this.setting && other.value == this.value;
        }
    }

    private static class ButtonAppearance {
        public int index;
        public Text displayName;
        public Text displayValue;
    }
}
