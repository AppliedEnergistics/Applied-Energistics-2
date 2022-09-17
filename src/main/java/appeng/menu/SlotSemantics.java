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

import org.jetbrains.annotations.Nullable;

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
    public static SlotSemantic register(String id, boolean playerSide) {
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

    @Nullable
    public static SlotSemantic get(String key) {
        return REGISTRY.get(key);
    }

    public static final SlotSemantic STORAGE = register("STORAGE", false);

    public static final SlotSemantic PLAYER_INVENTORY = register("PLAYER_INVENTORY", true);
    public static final SlotSemantic PLAYER_HOTBAR = register("PLAYER_HOTBAR", true);
    public static final SlotSemantic TOOLBOX = register("TOOLBOX", true);
    /**
     * Used for configuration slots that configure a filter, such as on planes, import/export busses, etc.
     */
    public static final SlotSemantic CONFIG = register("CONFIG", false);
    /**
     * An upgrade slot on a machine, cell workbench, etc.
     */
    public static final SlotSemantic UPGRADE = register("UPGRADE", false);
    /**
     * One or more slots for storage cells, i.e. on drives, cell workbench or chest.
     */
    public static final SlotSemantic STORAGE_CELL = register("STORAGE_CELL", false);

    public static final SlotSemantic INSCRIBER_PLATE_TOP = register("INSCRIBER_PLATE_TOP", false);

    public static final SlotSemantic INSCRIBER_PLATE_BOTTOM = register("INSCRIBER_PLATE_BOTTOM", false);

    public static final SlotSemantic MACHINE_INPUT = register("MACHINE_INPUT", false);

    public static final SlotSemantic MACHINE_OUTPUT = register("MACHINE_OUTPUT", false);

    public static final SlotSemantic MACHINE_CRAFTING_GRID = register("MACHINE_CRAFTING_GRID", false);

    public static final SlotSemantic BLANK_PATTERN = register("BLANK_PATTERN", false);

    public static final SlotSemantic ENCODED_PATTERN = register("ENCODED_PATTERN", false);

    public static final SlotSemantic VIEW_CELL = register("VIEW_CELL", false);

    public static final SlotSemantic CRAFTING_GRID = register("CRAFTING_GRID", false);

    public static final SlotSemantic PROCESSING_INPUTS = register("PROCESSING_INPUTS", false);

    public static final SlotSemantic PROCESSING_OUTPUTS = register("PROCESSING_OUTPUTS", false);

    public static final SlotSemantic CRAFTING_RESULT = register("CRAFTING_RESULT", false);

    public static final SlotSemantic BIOMETRIC_CARD = register("BIOMETRIC_CARD", false);

    public static final SlotSemantic MISSING_INGREDIENT = register("MISSING_INGREDIENT", true);
}
