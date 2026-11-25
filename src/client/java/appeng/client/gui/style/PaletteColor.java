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

package appeng.client.gui.style;

import org.jetbrains.annotations.Nullable;

/**
 * Named colors used by AE2 screens. The actual values are defined in the screen style documents (see
 * <code>screens/common/palette.json</code>), with separate values for the light and dark UI theme.
 * <p>
 * Colors that have been added after resource packs started overriding the palette carry a built-in default, which is
 * used if a palette does not define them. This prevents outdated resource packs from breaking all AE2 screens.
 */
public enum PaletteColor {
    DEFAULT_TEXT_COLOR,
    MUTED_TEXT_COLOR,
    SELECTION_COLOR,
    TEXTFIELD_PLACEHOLDER,
    TEXTFIELD_ERROR,
    TEXTFIELD_TEXT,
    ERROR,

    /**
     * Text color of the search field in terminals.
     */
    SEARCH_FIELD_TEXT(0xFFFFFFFF),
    /**
     * Text color of an enabled button.
     */
    BUTTON_TEXT(0xFFF2F2F2),
    /**
     * Text color of an enabled button while hovered.
     */
    BUTTON_TEXT_HOVER(0xFF517497),
    /**
     * Text color of a disabled button.
     */
    BUTTON_TEXT_DISABLED(0xFF413F54),
    /**
     * Border drawn around the hovered slot.
     */
    SLOT_HIGHLIGHT_BORDER(0xFFDAFFFF),
    /**
     * Fill color of the hovered slot.
     */
    SLOT_HIGHLIGHT_FILL(0x669CD3FF),
    /**
     * Overlay drawn over slots whose content is invalid.
     */
    SLOT_INVALID_OVERLAY(0x66FF6666),
    /**
     * Overlay drawn over invalid patterns in the pattern access terminal.
     */
    INVALID_PATTERN_OVERLAY(0x7FFF0000),
    /**
     * Background drawn behind crafting grid slots that support fluid substitution.
     */
    FLUID_SUBSTITUTION_SLOT_BACKGROUND(0xFF7AC25F),
    /**
     * Overlay drawn over a terminal's grid when it is disconnected from the network.
     */
    DISCONNECTED_OVERLAY(0x3F000000),
    /**
     * Background of a machine in the network status screen that is missing a channel.
     */
    MISSING_CHANNEL_BACKGROUND(0xFFFF5555),
    /**
     * Overlay for entries of a crafting plan that are missing ingredients.
     */
    CRAFT_CONFIRM_MISSING_OVERLAY(0x1AFF0000),
    /**
     * Progress bar of the currently selected crafting CPU.
     */
    SELECTED_CPU_PROGRESS(0xFF7DA9D2),
    /**
     * Status text of an unlocked pattern provider.
     */
    PATTERN_PROVIDER_UNLOCKED_TEXT(0xFF7DA9D2),
    /**
     * Status text of a locked pattern provider.
     */
    PATTERN_PROVIDER_LOCKED_TEXT(0xFFC1424B),
    /**
     * Text color used for labels in the sky stone chest.
     */
    SKY_CHEST_TEXT(0xFF575E54),
    /**
     * Text color of the unit shown next to number entry fields.
     */
    NUMBER_ENTRY_UNIT_TEXT(0xFF555555);

    @Nullable
    private final Color defaultColor;

    PaletteColor() {
        this.defaultColor = null;
    }

    PaletteColor(int defaultArgb) {
        this.defaultColor = new Color(
                defaultArgb >> 16 & 0xFF,
                defaultArgb >> 8 & 0xFF,
                defaultArgb & 0xFF,
                defaultArgb >>> 24);
    }

    /**
     * @return The color to use if a palette does not define this color, or null if every palette must define it.
     */
    @Nullable
    public Color getDefaultColor() {
        return defaultColor;
    }
}
