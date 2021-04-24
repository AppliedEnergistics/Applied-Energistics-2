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

import appeng.client.gui.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import appeng.api.config.AccessRestriction;
import appeng.api.config.CondenserOutput;
import appeng.api.config.FullnessMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.OperationMode;
import appeng.api.config.PowerUnits;
import appeng.api.config.RedstoneMode;
import appeng.api.config.RelativeDirection;
import appeng.api.config.SchedulingMode;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.StorageFilter;
import appeng.api.config.TerminalStyle;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
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

    public SettingToggleButton(final Settings setting, final T val,
            IHandler<SettingToggleButton<T>> onPress) {
        this(setting, val, t -> true, onPress);
    }

    public SettingToggleButton(final Settings setting, final T val, Predicate<T> isValidValue,
            IHandler<SettingToggleButton<T>> onPress) {
        super(SettingToggleButton::onPress);
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
            registerApp(Icon.UNUSED_07_00, Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH, ButtonToolTips.CondenserOutput,
                    ButtonToolTips.Trash);
            registerApp(Icon.UNUSED_07_01, Settings.CONDENSER_OUTPUT, CondenserOutput.MATTER_BALLS,
                    ButtonToolTips.CondenserOutput,
                    ButtonToolTips.MatterBalls.text(CondenserOutput.MATTER_BALLS.requiredPower));
            registerApp(Icon.UNUSED_07_02, Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY,
                    ButtonToolTips.CondenserOutput,
                    ButtonToolTips.Singularity.text(CondenserOutput.SINGULARITY.requiredPower));

            registerApp(Icon.UNUSED_09_01, Settings.ACCESS, AccessRestriction.READ, ButtonToolTips.IOMode,
                    ButtonToolTips.Read);
            registerApp(Icon.UNUSED_09_00, Settings.ACCESS, AccessRestriction.WRITE, ButtonToolTips.IOMode, ButtonToolTips.Write);
            registerApp(Icon.UNUSED_09_02, Settings.ACCESS, AccessRestriction.READ_WRITE, ButtonToolTips.IOMode,
                    ButtonToolTips.ReadWrite);

            registerApp(Icon.UNUSED_10_00, Settings.POWER_UNITS, PowerUnits.AE, ButtonToolTips.PowerUnits,
                    PowerUnits.AE.textComponent());
            // registerApp(Icon.UNUSED_10_01, Settings.POWER_UNITS, PowerUnits.EU, ButtonToolTips.PowerUnits,
            // PowerUnits.EU.textComponent());
            registerApp(Icon.UNUSED_10_04, Settings.POWER_UNITS, PowerUnits.RF, ButtonToolTips.PowerUnits,
                    PowerUnits.RF.textComponent());

            registerApp(Icon.UNUSED_00_03, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.AlwaysActive);
            registerApp(Icon.UNUSED_00_00, Settings.REDSTONE_CONTROLLED, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.ActiveWithoutSignal);
            registerApp(Icon.UNUSED_00_01, Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.ActiveWithSignal);
            registerApp(Icon.UNUSED_00_02, Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.ActiveOnPulse);

            registerApp(Icon.UNUSED_00_00, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.EmitLevelsBelow);
            registerApp(Icon.UNUSED_00_01, Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode,
                    ButtonToolTips.EmitLevelAbove);

            registerApp(Icon.UNUSED_03_03, Settings.OPERATION_MODE, OperationMode.FILL, ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToStorageCell);
            registerApp(Icon.UNUSED_03_02, Settings.OPERATION_MODE, OperationMode.EMPTY, ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToNetwork);

            registerApp(Icon.UNUSED_03_03, Settings.IO_DIRECTION, RelativeDirection.LEFT, ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToStorageCell);
            registerApp(Icon.UNUSED_03_02, Settings.IO_DIRECTION, RelativeDirection.RIGHT, ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToNetwork);

            registerApp(Icon.UNUSED_03_00, Settings.SORT_DIRECTION, SortDir.ASCENDING, ButtonToolTips.SortOrder,
                    ButtonToolTips.ToggleSortDirection);
            registerApp(Icon.UNUSED_03_01, Settings.SORT_DIRECTION, SortDir.DESCENDING, ButtonToolTips.SortOrder,
                    ButtonToolTips.ToggleSortDirection);

            registerApp(Icon.UNUSED_02_03, Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_Auto);
            registerApp(Icon.UNUSED_02_04, Settings.SEARCH_MODE, SearchBoxMode.MANUAL_SEARCH, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_Standard);
            registerApp(Icon.UNUSED_02_05, Settings.SEARCH_MODE, SearchBoxMode.JEI_AUTOSEARCH, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_JEIAuto);
            registerApp(Icon.UNUSED_02_06, Settings.SEARCH_MODE, SearchBoxMode.JEI_MANUAL_SEARCH, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_JEIStandard);
            registerApp(Icon.UNUSED_02_07, Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH_KEEP, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_AutoKeep);
            registerApp(Icon.UNUSED_02_08, Settings.SEARCH_MODE, SearchBoxMode.MANUAL_SEARCH_KEEP, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_StandardKeep);
            registerApp(Icon.UNUSED_02_09, Settings.SEARCH_MODE, SearchBoxMode.JEI_AUTOSEARCH_KEEP, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_JEIAutoKeep);
            registerApp(Icon.UNUSED_02_10, Settings.SEARCH_MODE, SearchBoxMode.JEI_MANUAL_SEARCH_KEEP,
                    ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_JEIStandardKeep);

            registerApp(Icon.UNUSED_05_03, Settings.LEVEL_TYPE, LevelType.ENERGY_LEVEL, ButtonToolTips.LevelType,
                    ButtonToolTips.LevelType_Energy);
            registerApp(Icon.UNUSED_04_03, Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL, ButtonToolTips.LevelType,
                    ButtonToolTips.LevelType_Item);

            registerApp(Icon.UNUSED_13_00, Settings.TERMINAL_STYLE, TerminalStyle.TALL, ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Tall);
            registerApp(Icon.UNUSED_13_01, Settings.TERMINAL_STYLE, TerminalStyle.SMALL, ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Small);
            registerApp(Icon.UNUSED_13_02, Settings.TERMINAL_STYLE, TerminalStyle.FULL, ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Full);

            registerApp(Icon.UNUSED_04_00, Settings.SORT_BY, SortOrder.NAME, ButtonToolTips.SortBy, ButtonToolTips.ItemName);
            registerApp(Icon.UNUSED_04_01, Settings.SORT_BY, SortOrder.AMOUNT, ButtonToolTips.SortBy, ButtonToolTips.NumberOfItems);
            registerApp(Icon.UNUSED_04_05, Settings.SORT_BY, SortOrder.MOD, ButtonToolTips.SortBy, ButtonToolTips.Mod);

            registerApp(Icon.UNUSED_01_00, Settings.VIEW_MODE, ViewItems.STORED, ButtonToolTips.View, ButtonToolTips.StoredItems);
            registerApp(Icon.UNUSED_01_02, Settings.VIEW_MODE, ViewItems.ALL, ButtonToolTips.View, ButtonToolTips.StoredCraftable);
            registerApp(Icon.UNUSED_01_03, Settings.VIEW_MODE, ViewItems.CRAFTABLE, ButtonToolTips.View, ButtonToolTips.Craftable);

            registerApp(Icon.UNUSED_06_00, Settings.FUZZY_MODE, FuzzyMode.PERCENT_25, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_25);
            registerApp(Icon.UNUSED_06_01, Settings.FUZZY_MODE, FuzzyMode.PERCENT_50, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_50);
            registerApp(Icon.UNUSED_06_02, Settings.FUZZY_MODE, FuzzyMode.PERCENT_75, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_75);
            registerApp(Icon.UNUSED_06_03, Settings.FUZZY_MODE, FuzzyMode.PERCENT_99, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_99);
            registerApp(Icon.UNUSED_06_04, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZIgnoreAll);

            registerApp(Icon.UNUSED_05_00, Settings.FULLNESS_MODE, FullnessMode.EMPTY, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenEmpty);
            registerApp(Icon.UNUSED_05_01, Settings.FULLNESS_MODE, FullnessMode.HALF, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenWorkIsDone);
            registerApp(Icon.UNUSED_05_02, Settings.FULLNESS_MODE, FullnessMode.FULL, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenFull);

            registerApp(Icon.UNUSED_01_05, Settings.BLOCK, YesNo.YES, ButtonToolTips.InterfaceBlockingMode,
                    ButtonToolTips.Blocking);
            registerApp(Icon.UNUSED_01_04, Settings.BLOCK, YesNo.NO, ButtonToolTips.InterfaceBlockingMode,
                    ButtonToolTips.NonBlocking);

            registerApp(Icon.UNUSED_01_03, Settings.CRAFT_ONLY, YesNo.YES, ButtonToolTips.Craft, ButtonToolTips.CraftOnly);
            registerApp(Icon.UNUSED_01_02, Settings.CRAFT_ONLY, YesNo.NO, ButtonToolTips.Craft, ButtonToolTips.CraftEither);

            registerApp(Icon.UNUSED_11_02, Settings.CRAFT_VIA_REDSTONE, YesNo.YES, ButtonToolTips.EmitterMode,
                    ButtonToolTips.CraftViaRedstone);
            registerApp(Icon.UNUSED_11_01, Settings.CRAFT_VIA_REDSTONE, YesNo.NO, ButtonToolTips.EmitterMode,
                    ButtonToolTips.EmitWhenCrafting);

            registerApp(Icon.UNUSED_03_05, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY,
                    ButtonToolTips.ReportInaccessibleItems, ButtonToolTips.ReportInaccessibleItemsNo);
            registerApp(Icon.UNUSED_03_06, Settings.STORAGE_FILTER, StorageFilter.NONE, ButtonToolTips.ReportInaccessibleItems,
                    ButtonToolTips.ReportInaccessibleItemsYes);

            registerApp(Icon.UNUSED_14_00, Settings.PLACE_BLOCK, YesNo.YES, ButtonToolTips.BlockPlacement,
                    ButtonToolTips.BlockPlacementYes);
            registerApp(Icon.UNUSED_14_01, Settings.PLACE_BLOCK, YesNo.NO, ButtonToolTips.BlockPlacement,
                    ButtonToolTips.BlockPlacementNo);

            registerApp(Icon.UNUSED_15_00, Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT, ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeDefault);
            registerApp(Icon.UNUSED_15_01, Settings.SCHEDULING_MODE, SchedulingMode.ROUNDROBIN, ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeRoundRobin);
            registerApp(Icon.UNUSED_15_02, Settings.SCHEDULING_MODE, SchedulingMode.RANDOM, ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeRandom);

            registerApp(Icon.UNUSED_15_03, Settings.OVERLAY_MODE, YesNo.NO, ButtonToolTips.OverlayMode,
                    ButtonToolTips.OverlayModeNo);
            registerApp(Icon.UNUSED_15_04, Settings.OVERLAY_MODE, YesNo.YES, ButtonToolTips.OverlayMode,
                    ButtonToolTips.OverlayModeYes);
        }
    }

    private static void onPress(Button btn) {
        if (btn instanceof SettingToggleButton) {
            ((SettingToggleButton<?>) btn).triggerPress();
        }
    }

    private void triggerPress() {
        boolean backwards = false;
        // This isn't great, but we don't get any information about right-clicks
        // otherwise
        Screen currentScreen = Minecraft.getInstance().currentScreen;
        if (currentScreen instanceof AEBaseScreen) {
            backwards = ((AEBaseScreen<?>) currentScreen).isHandlingRightClick();
        }
        onPress.handle(this, backwards);
    }

    private static void registerApp(final Icon icon, final Settings setting, final Enum<?> val,
            final ButtonToolTips title, final ITextComponent hint) {
        final ButtonAppearance a = new ButtonAppearance();
        a.displayName = title.text();
        a.displayValue = hint;
        a.icon = icon;
        appearances.put(new EnumPair(setting, val), a);
    }

    private static void registerApp(final Icon icon, final Settings setting, final Enum<?> val,
            final ButtonToolTips title, final ButtonToolTips hint) {
        registerApp(icon, setting, val, title, hint.text());
    }

    @Override
    protected Icon getIcon() {
        if (this.buttonSetting != null && this.currentValue != null) {
            final ButtonAppearance app = appearances.get(new EnumPair(this.buttonSetting, this.currentValue));
            if (app != null) {
                return app.icon;
            }
        }
        return Icon.UNUSED_15_15;
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
    public ITextComponent getTooltipMessage() {
        ITextComponent displayName = null;
        ITextComponent displayValue = null;

        if (this.buttonSetting != null && this.currentValue != null) {
            final ButtonAppearance buttonAppearance = appearances
                    .get(new EnumPair(this.buttonSetting, this.currentValue));
            if (buttonAppearance == null) {
                return new StringTextComponent("No Such Message");
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

            return new StringTextComponent(name + '\n' + sb);
        }
        return StringTextComponent.EMPTY;
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
        public Icon icon;
        public ITextComponent displayName;
        public ITextComponent displayValue;
    }
}
