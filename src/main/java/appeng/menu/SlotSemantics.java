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

package appeng.menu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link SlotSemantic}.
 * <p/>
 * This is not an enum to allow addons registration of new slot semantics for their screens.
 */
public final class SlotSemantics {
    private SlotSemantics() {
    }

    private static final Map<String, SlotSemantic> REGISTRY = new ConcurrentHashMap<>();

    /**
     * NOTE: If you use this in an addon, use an Addon-Specific Prefix for your semantic id (i.e. your mod id).
     */
    private static SlotSemantic add(String id, boolean playerSide) {
        var semantic = new SlotSemantic(id, playerSide);
        var existing = REGISTRY.putIfAbsent(semantic.id(), semantic);
        if (existing != null) {
            throw new IllegalArgumentException("Semantic with id " + semantic.id() + "was already registered");
        }

        return semantic;
    }

    public static SlotSemantic getOrThrow(String key) {
        var semantic = REGISTRY.get(key);
        if (semantic == null) {
            throw new IllegalArgumentException("Unknown slot semantic: " + key);
        }
        return semantic;
    }

    public static final SlotSemantic STORAGE = add("STORAGE", false);

    public static final SlotSemantic PLAYER_INVENTORY = add("PLAYER_INVENTORY", true);
    public static final SlotSemantic PLAYER_HOTBAR = add("PLAYER_HOTBAR", true);
    public static final SlotSemantic TOOLBOX = add("TOOLBOX", true);
    /**
     * Used for configuration slots that configure a filter, such as on planes, import/export busses, etc.
     */
    public static final SlotSemantic CONFIG = add("CONFIG", false);
    /**
     * An upgrade slot on a machine, cell workbench, etc.
     */
    public static final SlotSemantic UPGRADE = add("UPGRADE", false);
    /**
     * One or more slots for storage cells, i.e. on drives, cell workbench or chest.
     */
    public static final SlotSemantic STORAGE_CELL = add("STORAGE_CELL", false);

    public static final SlotSemantic INSCRIBER_PLATE_TOP = add("INSCRIBER_PLATE_TOP", false);

    public static final SlotSemantic INSCRIBER_PLATE_BOTTOM = add("INSCRIBER_PLATE_BOTTOM", false);

    public static final SlotSemantic MACHINE_INPUT = add("MACHINE_INPUT", false);

    public static final SlotSemantic MACHINE_OUTPUT = add("MACHINE_OUTPUT", false);

    public static final SlotSemantic MACHINE_CRAFTING_GRID = add("MACHINE_CRAFTING_GRID", false);

    public static final SlotSemantic BLANK_PATTERN = add("BLANK_PATTERN", false);

    public static final SlotSemantic ENCODED_PATTERN = add("ENCODED_PATTERN", false);

    public static final SlotSemantic VIEW_CELL = add("VIEW_CELL", false);

    public static final SlotSemantic CRAFTING_GRID = add("CRAFTING_GRID", false);

    public static final SlotSemantic CRAFTING_RESULT = add("CRAFTING_RESULT", false);

    public static final SlotSemantic PROCESSING_PRIMARY_RESULT = add("PROCESSING_PRIMARY_RESULT", false);

    public static final SlotSemantic PROCESSING_FIRST_OPTIONAL_RESULT = add("PROCESSING_FIRST_OPTIONAL_RESULT", false);

    public static final SlotSemantic PROCESSING_SECOND_OPTIONAL_RESULT = add("PROCESSING_SECOND_OPTIONAL_RESULT",
            false);

    public static final SlotSemantic BIOMETRIC_CARD = add("BIOMETRIC_CARD", false);
}
