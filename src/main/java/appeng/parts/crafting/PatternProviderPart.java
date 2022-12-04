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

package appeng.parts.crafting;

import java.util.EnumSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.helpers.iface.PatternProviderLogic;
import appeng.helpers.iface.PatternProviderLogicHost;
import appeng.items.parts.PartModels;
import appeng.menu.locator.MenuLocators;
import appeng.parts.BasicStatePart;
import appeng.parts.PartModel;
import appeng.util.SettingsFrom;

public class PatternProviderPart extends BasicStatePart implements PatternProviderLogicHost {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID,
            "part/pattern_provider_base");

    // TODO: unify the following between the 3 interface parts?
    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/interface_has_channel"));

    private final PatternProviderLogic logic;

    public PatternProviderPart(IPartItem<?> partItem) {
        super(partItem);
        this.logic = new PatternProviderLogic(this.getMainNode(), this);
    }

    @Override
    public void saveChanges() {
        getHost().markForSave();
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(getPartItem());
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.logic.onMainNodeStateChanged();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.logic.readFromNBT(data);
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        this.logic.writeToNBT(data);
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.logic.updatePatterns();
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        this.logic.addDrops(drops);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag output) {
        super.exportSettings(mode, output);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.exportSettings(output);
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.importSettings(input, player);
        }
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        logic.updateRedstoneState();
    }

    @Override
    public boolean onPartActivate(Player p, InteractionHand hand, Vec3 pos) {
        if (!p.getCommandSenderWorld().isClientSide()) {
            openMenu(p, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public PatternProviderLogic getLogic() {
        return logic;
    }

    @Override
    public EnumSet<Direction> getTargets() {
        return EnumSet.of(this.getSide());
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEParts.PATTERN_PROVIDER.stack();
    }
}
