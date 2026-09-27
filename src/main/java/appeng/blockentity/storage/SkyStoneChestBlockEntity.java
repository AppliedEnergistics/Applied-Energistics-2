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

package appeng.blockentity.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.menu.implementations.SkyStonechestMenu;
import appeng.util.inv.AppEngInternalInventory;

@SuppressWarnings("JavadocReference")
public class SkyStoneChestBlockEntity extends AEBaseInvBlockEntity implements ClientTickingBlockEntity, LidBlockEntity {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 9 * 4);

    private final ChestLidController chestLidController = new ChestLidController();

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_OPEN);
        }

        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_CLOSE);
        }

        private void playSound(Level level, BlockPos pos, SoundEvent event) {
            var x = pos.getX() + 0.5D;
            var y = pos.getY() + 0.5D;
            var z = pos.getZ() + 0.5D;
            level.playSound(null, x, y, z, event, SoundSource.BLOCKS,
                    0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int p_155364_, int x) {
            level.blockEvent(pos, state.getBlock(), 1, x);
        }

        public boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof SkyStonechestMenu menu) {
                return menu.getChest() == SkyStoneChestBlockEntity.this;
            } else {
                return false;
            }
        }
    };

    public SkyStoneChestBlockEntity(BlockEntityType<? extends SkyStoneChestBlockEntity> type, BlockPos pos,
            BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState(),
                    player.getContainerInteractionRange());
        }
    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void clientTick() {
        chestLidController.tickLid();
    }

    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.chestLidController.getOpenness(partialTicks);
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public InteractionResult disassembleWithWrench(Player player, Level level, BlockHitResult hitResult,
            ItemStack wrench) {
        return InteractionResult.FAIL;
    }
}
