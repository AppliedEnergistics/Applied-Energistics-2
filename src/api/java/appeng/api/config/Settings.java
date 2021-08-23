/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.config;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

public final class Settings {
    private static final Map<String, Setting<?>> SETTINGS = new HashMap<>();

    private Settings() {
    }

    private synchronized static <T extends Enum<T>> Setting<T> register(String name, Class<T> enumClass) {
        Preconditions.checkState(!SETTINGS.containsKey(name));
        var setting = new Setting<T>(name, enumClass);
        SETTINGS.put(name, setting);
        return setting;
    }

    @SafeVarargs
    private synchronized static <T extends Enum<T>> Setting<T> register(String name, T firstOption, T... moreOptions) {
        Preconditions.checkState(!SETTINGS.containsKey(name));
        var setting = new Setting<T>(name, firstOption.getDeclaringClass(), EnumSet.of(firstOption, moreOptions));
        SETTINGS.put(name, setting);
        return setting;
    }

    public static final Setting<LevelEmitterMode> LEVEL_EMITTER_MODE = register("level_emitter_mode",
            LevelEmitterMode.class);
    public static final Setting<RedstoneMode> REDSTONE_EMITTER = register("redstone_emitter", RedstoneMode.HIGH_SIGNAL,
            RedstoneMode.LOW_SIGNAL);
    public static final Setting<RedstoneMode> REDSTONE_CONTROLLED = register("redstone_controlled", RedstoneMode.class);
    public static final Setting<CondenserOutput> CONDENSER_OUTPUT = register("condenser_output", CondenserOutput.class);
    public static final Setting<PowerUnits> POWER_UNITS = register("power_units", PowerUnits.class);
    public static final Setting<AccessRestriction> ACCESS = register("access", AccessRestriction.READ_WRITE,
            AccessRestriction.READ, AccessRestriction.WRITE);
    public static final Setting<SortDir> SORT_DIRECTION = register("sort_direction", SortDir.class);
    public static final Setting<SortOrder> SORT_BY = register("sort_by", SortOrder.class);
    public static final Setting<YesNo> SEARCH_TOOLTIPS = register("search_tooltips", YesNo.YES, YesNo.NO);
    public static final Setting<ViewItems> VIEW_MODE = register("view_mode", ViewItems.class);
    public static final Setting<SearchBoxMode> SEARCH_MODE = register("search_mode", SearchBoxMode.class);
    public static final Setting<RelativeDirection> IO_DIRECTION = register("io_direction", RelativeDirection.LEFT,
            RelativeDirection.RIGHT);
    public static final Setting<YesNo> BLOCK = register("block", YesNo.YES, YesNo.NO);
    public static final Setting<OperationMode> OPERATION_MODE = register("operation_mode", OperationMode.class);
    public static final Setting<FullnessMode> FULLNESS_MODE = register("fullness_mode", FullnessMode.class);
    public static final Setting<YesNo> CRAFT_ONLY = register("craft_only", YesNo.YES, YesNo.NO);
    public static final Setting<FuzzyMode> FUZZY_MODE = register("fuzzy_mode", FuzzyMode.class);
    public static final Setting<TerminalStyle> TERMINAL_STYLE = register("terminal_style", TerminalStyle.TALL,
            TerminalStyle.SMALL);
    public static final Setting<CopyMode> COPY_MODE = register("copy_mode", CopyMode.class);
    public static final Setting<YesNo> INTERFACE_TERMINAL = register("interface_terminal", YesNo.YES, YesNo.NO);
    public static final Setting<YesNo> CRAFT_VIA_REDSTONE = register("craft_via_redstone", YesNo.YES, YesNo.NO);
    public static final Setting<StorageFilter> STORAGE_FILTER = register("storage_filter", StorageFilter.class);
    public static final Setting<YesNo> PLACE_BLOCK = register("place_block", YesNo.YES, YesNo.NO);
    public static final Setting<SchedulingMode> SCHEDULING_MODE = register("scheduling_mode", SchedulingMode.class);
    public static final Setting<YesNo> OVERLAY_MODE = register("overlay_mode", YesNo.YES, YesNo.NO);

    public static Setting<?> getOrThrow(String name) {
        var setting = SETTINGS.get(name);
        if (setting == null) {
            throw new IllegalArgumentException("Unknown setting '" + name + "'");
        }
        return setting;
    }
}
