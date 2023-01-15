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

package appeng.blockentity.crafting;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.crafting.PushDirection;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.util.SettingsFrom;

public class PatternProviderBlockEntity extends AENetworkBlockEntity implements PatternProviderLogicHost {
    protected final PatternProviderLogic logic = createLogic();

    @Nullable
    private PushDirection pendingPushDirectionChange;

    public PatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(this.getMainNode(), this);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.logic.onMainNodeStateChanged();
    }

    private PushDirection getPushDirection() {
        return getBlockState().getValue(PatternProviderBlock.PUSH_DIRECTION);
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        // In omnidirectional mode, every side is grid-connectable
        var pushDirection = getPushDirection().getDirection();
        if (pushDirection == null) {
            return EnumSet.allOf(Direction.class);
        }

        // Otherwise all sides *except* the target side are connectable
        return EnumSet.complementOf(EnumSet.of(pushDirection));
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops, boolean remove) {
        this.logic.addDrops(drops, remove);
    }

    @Override
    public void onReady() {
        if (pendingPushDirectionChange != null) {
            level.setBlockAndUpdate(
                    getBlockPos(),
                    getBlockState().setValue(PatternProviderBlock.PUSH_DIRECTION, pendingPushDirectionChange));
            pendingPushDirectionChange = null;
            onGridConnectableSidesChanged();
        }

        super.onReady();
        this.logic.updatePatterns();
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.logic.writeToNBT(data);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);

        // Remove in 1.20.1+: Convert legacy NBT orientation to blockstate
        if (data.getBoolean("omniDirectional")) {
            pendingPushDirectionChange = PushDirection.ALL;
        } else if (data.contains("forward", Tag.TAG_STRING)) {
            try {
                var forward = Direction.valueOf(data.getString("forward").toUpperCase(Locale.ROOT));
                pendingPushDirectionChange = PushDirection.fromDirection(forward);
            } catch (IllegalArgumentException ignored) {
            }
        }

        this.logic.readFromNBT(data);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public PatternProviderLogic getLogic() {
        return logic;
    }

    @Override
    public EnumSet<Direction> getTargets() {
        var pushDirection = getPushDirection();
        if (pushDirection.getDirection() == null) {
            return EnumSet.allOf(Direction.class);
        } else {
            return EnumSet.of(pushDirection.getDirection());
        }
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(AEBlocks.PATTERN_PROVIDER.stack());
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag output,
            @org.jetbrains.annotations.Nullable Player player) {
        super.exportSettings(mode, output, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.exportSettings(output);

            var pushDirection = getPushDirection();
            output.putByte("push_direction", (byte) pushDirection.ordinal());
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input,
            @org.jetbrains.annotations.Nullable Player player) {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.importSettings(input, player);

            // Restore push direction blockstate
            if (input.contains(PatternProviderBlock.PUSH_DIRECTION.getName(), Tag.TAG_BYTE)) {
                var pushDirection = input.getByte(PatternProviderBlock.PUSH_DIRECTION.getName());
                if (pushDirection >= 0 && pushDirection < PushDirection.values().length) {
                    var level = getLevel();
                    if (level != null) {
                        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(
                                PatternProviderBlock.PUSH_DIRECTION,
                                PushDirection.values()[pushDirection]));
                    }
                }
            }
        }
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.PATTERN_PROVIDER.stack();
    }

    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        onGridConnectableSidesChanged();
    }
}
