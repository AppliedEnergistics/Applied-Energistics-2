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

package appeng.core.localization;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public enum ButtonToolTips {
    PowerUnits, IOMode, CondenserOutput, RedstoneMode, MatchingFuzzy,

    MatchingMode, TransferDirection, SortOrder, SortBy, View,

    PartitionStorage, Clear, FuzzyMode, OperationMode, TrashController,

    InterfaceBlockingMode, InterfaceCraftingMode, Trash, MatterBalls,

    Singularity, Read, Write, ReadWrite, AlwaysActive,

    ActiveWithoutSignal, ActiveWithSignal, ActiveOnPulse,

    EmitLevelsBelow, EmitLevelAbove, MatchingExact, TransferToNetwork,

    TransferToStorageCell, ToggleSortDirection,

    SearchMode_Auto, SearchMode_Standard, SearchMode_JEIAuto, SearchMode_JEIStandard, SearchMode_AutoKeep,
    SearchMode_StandardKeep, SearchMode_JEIAutoKeep, SearchMode_JEIStandardKeep,

    SearchMode, ItemName, NumberOfItems, PartitionStorageHint,

    ClearSettings, StoredItems, StoredCraftable, Craftable,

    FZPercent_25, FZPercent_50, FZPercent_75, FZPercent_99, FZIgnoreAll,

    MoveWhenEmpty, MoveWhenWorkIsDone, MoveWhenFull, Disabled, Enable,

    Blocking, NonBlocking,

    LevelType, LevelType_Energy, LevelType_Item, TerminalStyle, TerminalStyle_Full, TerminalStyle_Tall,
    TerminalStyle_Small,

    Stash, StashDesc, Encode, EncodeDescription, Substitutions, SubstitutionsOn, SubstitutionsOff,
    SubstitutionsDescEnabled, SubstitutionsDescDisabled, CraftOnly, CraftEither,

    Craft, Mod, DoesntDespawn, EmitterMode, CraftViaRedstone, EmitWhenCrafting, ReportInaccessibleItems,
    ReportInaccessibleItemsYes, ReportInaccessibleItemsNo, ReportInaccessibleFluids, ReportInaccessibleFluidsYes,
    ReportInaccessibleFluidsNo,

    BlockPlacement, BlockPlacementYes, BlockPlacementNo,

    // Used in the tooltips of the items in the terminal, when moused over
    ItemsStored, ItemsRequestable,

    SchedulingMode, SchedulingModeDefault, SchedulingModeRoundRobin, SchedulingModeRandom,

    FilterMode, FilterModeKeep, FilterModeClear,

    OverlayMode, OverlayModeYes, OverlayModeNo;

    private final TranslatableComponent text;

    ButtonToolTips() {
        this.text = new TranslatableComponent("gui.tooltips.appliedenergistics2." + this.name());
    }

    public Component text() {
        return text;
    }

    public MutableComponent text(Object... args) {
        return new TranslatableComponent(text.getKey(), args);
    }

}
