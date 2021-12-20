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

public enum GuiText {
    inventory("container"), // mc's default Inventory localization.

    TankAmount, TankCapacity,

    Chest, StoredEnergy, Of, Condenser, Drive, SkyChest,

    VibrationChamber, SpatialIOPort, SpatialAnchor, LevelEmitter, Terminal, TerminalViewCellsTooltip,

    Interface, Config, StoredItems, StoredFluids, Patterns, ImportBus, ExportBus,

    CompatibleUpgrades, CompatibleUpgrade, UpgradeToolbelt,

    CellWorkbench, NetworkDetails, StorageCells, Interfaces, IOBuses,

    IOPort, BytesUsed, Types, QuantumLinkChamber, PortableCell,

    NetworkTool, PowerUsageRate, PowerInputRate, Installed, EnergyDrain, ChannelEnergyDrain,

    StorageBus, StorageBusFluids, Priority, Security, Encoded, Blank, Unlinked, Linked,

    SecurityCardEditor, NoPermissions, WirelessTerminal, Wireless,

    CraftingTerminal, FormationPlane, FluidFormationPlane, Inscriber, QuartzCuttingKnife,

    // spatial
    SpatialCapacity, StoredSize, Unformatted, SerialNumber,

    // spatial anchor

    SpatialAnchorUsedPower, SpatialAnchorLoadedChunks,
    SpatialAnchorStatistics, SpatialAnchorAll, SpatialAnchorAllLoaded,

    CopyMode, CopyModeDesc, PatternEncoding,

    // Pattern tooltips
    CraftingPattern, ProcessingPattern, Crafts, Produces, And, With, Substitute, Yes, No,

    MolecularAssembler,

    StoredPower, MaxPower, RequiredPower, Efficiency, SCSSize, SCSInvalid, InWorldCrafting,

    inWorldSingularity, ChargedQuartz,

    NoSecondOutput, OfSecondOutput, MultipleOutputs,

    Stores, Next, Set, SelectAmount, Lumen, Empty,

    ConfirmCrafting, Stored, Crafting, Scheduled, CraftingStatus, Cancel, ETA, ETAFormat,

    FromStorage, ToCraft, CraftingPlan, CalculatingWait, Start,

    SelectedCraftingCPU, Automatic, PartialPlan, Missing, ConfirmCraftCpuStatus, ConfirmCraftNoCpu,

    InterfaceTerminal, NoCraftingCPUs, Clean, InvalidPattern,

    InterfaceTerminalHint, WirelessRange, TransparentFacades, TransparentFacadesHint,

    NoCraftingJobs, CPUs, FacadeCrafting, inWorldCraftingPresses,

    Included, Excluded, Partitioned, Precise, Fuzzy,

    // Used in a terminal to indicate that an item is craftable
    SmallFontCraft, LargeFontCraft,

    // Used by storage bus for attached storage types
    AttachedTo, Unattached, MENetworkStorage, ExternalStorage, Items, Fluids,

    // Used in a ME Interface when no appropriate block entity was detected near it
    Nothing;

    private final String root;

    private final Component text;

    GuiText() {
        this.root = "gui.ae2";
        this.text = new TranslatableComponent(getTranslationKey());
    }

    GuiText(final String r) {
        this.root = r;
        this.text = new TranslatableComponent(getTranslationKey());
    }

    public String getLocal() {
        return text.getString();
    }

    public String getTranslationKey() {
        return this.root + '.' + this.toString();
    }

    public Component text() {
        return text;
    }

    public MutableComponent withSuffix(String text) {
        return text().copy().append(text);
    }

    public MutableComponent withSuffix(Component text) {
        return text().copy().append(text);
    }

    public MutableComponent text(Object... args) {
        return new TranslatableComponent(getTranslationKey(), args);
    }

}
