/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.client.gui;

import net.minecraft.resources.ResourceLocation;

import appeng.client.gui.style.Blitter;
import appeng.core.AppEng;

/**
 * Edit in {@code assets/ae2/textures/guis/states.png}.
 */
public enum Icon {

    // ROW 0
    REDSTONE_LOW(0, 0),
    REDSTONE_HIGH(16, 0),
    REDSTONE_PULSE(32, 0),
    REDSTONE_IGNORE(48, 0),
    // CLEAR, STASH
    CLEAR(96, 0),
    ENTER(112, 0),
    // ENCODE
    WHITE_ARROW_DOWN(128, 0),
    BACKGROUND_PRIMARY_OUTPUT(224, 0),
    BACKGROUND_STORAGE_CELL(240, 0),

    // ROW 1
    VIEW_MODE_STORED(0, 16),
    VIEW_MODE_ALL(32, 16),
    VIEW_MODE_CRAFTING(48, 16),
    BLOCKING_MODE_NO(64, 16),
    BLOCKING_MODE_YES(80, 16),
    TRANSPARENT_FACADES_OFF(96, 16),
    TRANSPARENT_FACADES_ON(112, 16),
    TYPE_FILTER_ITEMS(128, 16),
    TYPE_FILTER_FLUIDS(144, 16),
    TYPE_FILTER_ALL(160, 16),
    BACKGROUND_ORE(240, 16),

    // ROW 2
    SEARCH_AUTO_FOCUS(48, 32),
    SEARCH_DEFAULT(64, 32),
    SEARCH_JEI(80, 32),
    SEARCH_REI(96, 32),
    SEARCH_AUTO_FOCUS_REMEMBER(112, 32),
    SEARCH_REMEMBER(128, 32),
    UNUSED_02_09(144, 32),
    UNUSED_02_10(160, 32),
    BACKGROUND_PLATE(224, 32),
    BACKGROUND_DUST(240, 32),

    // ROW 3
    ARROW_UP(0, 48),
    ARROW_DOWN(16, 48),
    ARROW_RIGHT(32, 48),
    ARROW_LEFT(48, 48),
    SUBSTITUTION_ENABLED(64, 48),
    STORAGE_FILTER_EXTRACTABLE_ONLY(80, 48),
    STORAGE_FILTER_EXTRACTABLE_NONE(96, 48),
    SUBSTITUTION_DISABLED(112, 48),
    FLUID_SUBSTITUTION_ENABLED(128, 48),
    FLUID_SUBSTITUTION_DISABLED(144, 48),
    FILTER_ON_EXTRACT_ENABLED(160, 48),
    FILTER_ON_EXTRACT_DISABLED(176, 48),
    BACKGROUND_INGOT(224, 48),
    BACKGROUND_STORAGE_COMPONENT(240, 48),

    // ROW 4
    SORT_BY_NAME(0, 64),
    SORT_BY_AMOUNT(16, 64),
    // WRENCH | PARTITION STORAGE
    WRENCH(32, 64),
    LEVEL_ITEM(48, 64),
    SORT_BY_INVENTORY_TWEAKS(64, 64),
    SORT_BY_MOD(80, 64),
    BACKGROUND_VIEW_CELL(224, 64),
    BACKGROUND_WIRELESS_TERM(240, 64),

    // ROW 5
    FULLNESS_EMPTY(0, 80),
    FULLNESS_HALF(16, 80),
    FULLNESS_FULL(32, 80),
    LEVEL_ENERGY(48, 80),
    PATTERN_ACCESS_SHOW(64, 80),
    PATTERN_ACCESS_HIDE(80, 80),
    PATTERN_TERMINAL_VISIBLE(96, 80),
    PATTERN_TERMINAL_ALL(112, 80),
    PATTERN_TERMINAL_NOT_FULL(128, 80),
    BACKGROUND_TRASH(240, 80),

    // ROW 6
    FUZZY_PERCENT_25(0, 96),
    FUZZY_PERCENT_50(16, 96),
    FUZZY_PERCENT_75(32, 96),
    FUZZY_PERCENT_99(48, 96),
    FUZZY_IGNORE(64, 96),
    FIND_CONTAINED_FLUID(80, 96),
    BACKGROUND_WIRELESS_BOOSTER(240, 96),

    // ROW 7
    CONDENSER_OUTPUT_TRASH(0, 112),
    CONDENSER_OUTPUT_MATTER_BALL(16, 112),
    CONDENSER_OUTPUT_SINGULARITY(32, 112),
    BACKGROUND_ENCODED_PATTERN(240, 112),

    // ROW 8
    INVALID(0, 128),
    VALID(16, 128),
    WHITELIST(32, 128),
    BLACKLIST(48, 128),
    HORIZONTAL_TAB(128, 128, 20, 22),
    HORIZONTAL_TAB_SELECTED(128, 150, 20, 22),
    HORIZONTAL_TAB_FOCUS(148, 128, 20, 22),
    BACKGROUND_BLANK_PATTERN(240, 128),

    // ROW 9
    ACCESS_WRITE(0, 144),
    ACCESS_READ(16, 144),
    ACCESS_READ_WRITE(32, 144),
    BACKGROUND_CHARGABLE(240, 144),

    // ROW 10
    POWER_UNIT_AE(0, 160),
    POWER_UNIT_EU(16, 160),
    POWER_UNIT_J(32, 160),
    POWER_UNIT_W(48, 160),
    POWER_UNIT_RF(64, 160),
    POWER_UNIT_TR(80, 160),
    BACKGROUND_SINGULARITY(240, 160),

    // ROW 11
    PERMISSION_INJECT(0, 176),
    PERMISSION_EXTRACT(16, 176),
    PERMISSION_CRAFT(32, 176),
    PERMISSION_BUILD(48, 176),
    PERMISSION_SECURITY(64, 176),
    COPY_MODE_ON(80, 176),
    BACKGROUND_SPATIAL_CELL(240, 176),

    // ROW 12
    PERMISSION_INJECT_DISABLED(0, 192),
    PERMISSION_EXTRACT_DISABLED(16, 192),
    PERMISSION_CRAFT_DISABLED(32, 192),
    PERMISSION_BUILD_DISABLED(48, 192),
    PERMISSION_SECURITY_DISABLED(64, 192),
    COPY_MODE_OFF(80, 192),
    TAB_BUTTON_BACKGROUND_BORDERLESS(128, 192, 25, 22),
    TAB_BUTTON_BACKGROUND(160, 192, 22, 22),
    SLOT_BACKGROUND(192, 192, 18, 18),
    BACKGROUND_FUEL(240, 192),

    // ROW 13
    TERMINAL_STYLE_TALL(0, 208),
    TERMINAL_STYLE_SMALL(16, 208),
    TERMINAL_STYLE_FULL(32, 208),
    BACKGROUND_UPGRADE(240, 208),

    // ROW 14
    PLACEMENT_BLOCK(0, 224),
    PLACEMENT_ITEM(16, 224),
    TAB_BUTTON_BACKGROUND_BORDERLESS_FOCUS(128, 224, 25, 22),
    TAB_BUTTON_BACKGROUND_FOCUS(160, 224, 22, 22),
    BACKGROUND_BIOMETRIC_CARD(240, 224),

    SCHEDULING_DEFAULT(0, 240),
    SCHEDULING_ROUND_ROBIN(16, 240),
    SCHEDULING_RANDOM(32, 240),
    OVERLAY_OFF(48, 240),
    OVERLAY_ON(64, 240),
    TOOLBAR_BUTTON_BACKGROUND(240, 240);

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public static final ResourceLocation TEXTURE = new ResourceLocation(AppEng.MOD_ID, "textures/guis/states.png");
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    Icon(int x, int y) {
        this(x, y, 16, 16);
    }

    Icon(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT)
                .src(x, y, width, height);
    }

}
