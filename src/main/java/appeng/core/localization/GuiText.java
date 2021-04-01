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

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum GuiText {
    inventory("container"), // mc's default Inventory localization.

    Chest, StoredEnergy, Of, Condenser, Drive, GrindStone, SkyChest,

    VibrationChamber, SpatialIOPort, SpatialAnchor, LevelEmitter, FluidLevelEmitter, FluidLevelEmitterUnit, Terminal,

    Interface, FluidInterface, Config, StoredItems, StoredFluids, Patterns, ImportBus, ImportBusFluids, ExportBus,
    ExportBusFluids,

    CellWorkbench, NetworkDetails, StorageCells, IOBuses, IOBusesFluids,

    IOPort, BytesUsed, Types, QuantumLinkChamber, PortableCell,

    NetworkTool, PowerUsageRate, PowerInputRate, Installed, EnergyDrain,

    StorageBus, StorageBusFluids, Priority, Security, Encoded, Blank, Unlinked, Linked,

    SecurityCardEditor, NoPermissions, WirelessTerminal, Wireless,

    CraftingTerminal, FormationPlane, FluidFormationPlane, Inscriber, QuartzCuttingKnife,

    // spatial
    SpatialCapacity, StoredSize, Unformatted, SerialNumber,

    // spatial anchor

    SpatialAnchorUsedPower, SpatialAnchorLoadedChunks,
    SpatialAnchorStatistics, SpatialAnchorAll, SpatialAnchorAllLoaded,

    CopyMode, CopyModeDesc, PatternTerminal, FluidTerminal,

    // Pattern tooltips
    CraftingPattern, ProcessingPattern, Crafts, Creates, And, With, Substitute, Yes, No,

    MolecularAssembler,

    StoredPower, MaxPower, RequiredPower, Efficiency, SCSSize, SCSInvalid, InWorldCrafting,

    inWorldFluix, inWorldPurificationCertus, inWorldPurificationNether,

    inWorldPurificationFluix, inWorldSingularity, ChargedQuartz,

    NoSecondOutput, OfSecondOutput, MultipleOutputs,

    Stores, Next, SelectAmount, Lumen, Empty,

    ConfirmCrafting, Stored, Crafting, Scheduled, CraftingStatus, Cancel, ETA, ETAFormat,

    FromStorage, ToCraft, CraftingPlan, CalculatingWait, Start, Bytes,

    CraftingCPU, Automatic, CoProcessors, Simulation, Missing,

    InterfaceTerminal, NoCraftingCPUs, Clean, InvalidPattern,

    InterfaceTerminalHint, Range, TransparentFacades, TransparentFacadesHint,

    NoCraftingJobs, CPUs, FacadeCrafting, inWorldCraftingPresses, ChargedQuartzFind,

    Included, Excluded, Partitioned, Precise, Fuzzy,

    // Used in a terminal to indicate that an item is craftable
    SmallFontCraft, LargeFontCraft,

    // Used in a ME Interface when no appropriate TileEntity was detected near it
    Nothing;

    private final String root;

    private final ITextComponent text;

    GuiText() {
        this.root = "gui.appliedenergistics2";
        this.text = new TranslationTextComponent(getTranslationKey());
    }

    GuiText(final String r) {
        this.root = r;
        this.text = new TranslationTextComponent(getTranslationKey());
    }

    public String getLocal() {
        return text.getString();
    }

    public String getTranslationKey() {
        return this.root + '.' + this.toString();
    }

    public ITextComponent text() {
        return text;
    }

    public IFormattableTextComponent withSuffix(String text) {
        return text().deepCopy().appendString(text);
    }

    public IFormattableTextComponent withSuffix(ITextComponent text) {
        return text().deepCopy().append(text);
    }

    public IFormattableTextComponent text(Object... args) {
        return new TranslationTextComponent(getTranslationKey(), args);
    }

}
