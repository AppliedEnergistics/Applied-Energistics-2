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

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.IGridNodeListener;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.iface.PatternProviderLogic;
import appeng.helpers.iface.PatternProviderLogicHost;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public class PatternProviderBlockEntity extends AENetworkBlockEntity implements PatternProviderLogicHost {
    protected final PatternProviderLogic logic = createLogic();
    private boolean omniDirectional = true;

    public PatternProviderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public PatternProviderLogic createLogic() {
        return new PatternProviderLogic(this.getMainNode(), this);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.logic.onMainNodeStateChanged();
    }

    public void setSide(Direction facing) {
        Direction newForward;

        if (!this.omniDirectional && this.getForward() == facing.getOpposite()) {
            newForward = facing;
        } else if (!this.omniDirectional
                && (this.getForward() == facing || this.getForward() == facing.getOpposite())) {
            newForward = null;
        } else if (this.omniDirectional) {
            newForward = facing.getOpposite();
        } else {
            newForward = Platform.rotateAround(this.getForward(), facing);
        }

        setPushDirection(newForward);
    }

    public void setPushDirection(@Nullable Direction direction) {
        this.omniDirectional = direction == null;
        if (direction == null) {
            direction = Direction.NORTH;
        }

        Direction newUp = Direction.UP;
        if (direction == Direction.UP || direction == Direction.DOWN) {
            newUp = Direction.NORTH;
        }
        this.setOrientation(direction, newUp);

        if (!isClientSide()) {
            this.configureNodeSides();
            this.markForUpdate();
            this.saveChanges();
        }
    }

    private void configureNodeSides() {
        if (this.omniDirectional) {
            this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        } else {
            this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        this.logic.addDrops(drops);
    }

    @Override
    public void onReady() {
        this.configureNodeSides();

        super.onReady();
        this.logic.updatePatterns();
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putBoolean("omniDirectional", this.omniDirectional);
        this.logic.writeToNBT(data);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.omniDirectional = data.getBoolean("omniDirectional");

        this.logic.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        boolean oldOmniDirectional = this.omniDirectional;
        this.omniDirectional = data.readBoolean();
        return oldOmniDirectional != this.omniDirectional || c;
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.omniDirectional);
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
        if (this.omniDirectional) {
            return EnumSet.allOf(Direction.class);
        }
        return EnumSet.of(this.getForward());
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
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input,
            @org.jetbrains.annotations.Nullable Player player) {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.MEMORY_CARD) {
            logic.importSettings(input, player);
        }
    }

    public boolean isOmniDirectional() {
        return this.omniDirectional;
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.PATTERN_PROVIDER.stack();
    }
}
