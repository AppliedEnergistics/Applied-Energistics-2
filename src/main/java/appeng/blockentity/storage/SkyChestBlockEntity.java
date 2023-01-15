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

import static net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity.LOOT_TABLE_SEED_TAG;
import static net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity.LOOT_TABLE_TAG;

import java.util.List;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.menu.implementations.SkyChestMenu;
import appeng.util.inv.AppEngInternalInventory;

@SuppressWarnings("JavadocReference")
@EnvironmentInterface(value = EnvType.CLIENT, itf = LidBlockEntity.class)
public class SkyChestBlockEntity extends AEBaseInvBlockEntity implements ClientTickingBlockEntity, LidBlockEntity {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 9 * 4);

    private final ChestLidController chestLidController = new ChestLidController();

    // This reimplements RandomizableContainerBlockEntity, because we cannot inherit from it here
    @Nullable
    protected ResourceLocation lootTable;
    protected long lootTableSeed;

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
                    0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int p_155364_, int x) {
            level.blockEvent(pos, state.getBlock(), 1, x);
        }

        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof SkyChestMenu menu) {
                return menu.getChest() == SkyChestBlockEntity.this;
            } else {
                return false;
            }
        }
    };

    public SkyChestBlockEntity(BlockEntityType<? extends SkyChestBlockEntity> type, BlockPos pos,
            BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
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
    public void onChangeInventory(InternalInventory inv, int slot) {

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

    /**
     * @see RandomizableContainerBlockEntity#tryLoadLootTable(CompoundTag)
     */
    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);

        if (data.contains(LOOT_TABLE_TAG, Tag.TAG_STRING)) {
            this.lootTable = new ResourceLocation(data.getString(LOOT_TABLE_TAG));
            this.lootTableSeed = data.getLong(LOOT_TABLE_SEED_TAG);
        }
    }

    /**
     * @see RandomizableContainerBlockEntity#trySaveLootTable(CompoundTag)
     */
    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);

        if (this.lootTable != null) {
            data.putString(LOOT_TABLE_TAG, this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                data.putLong(LOOT_TABLE_SEED_TAG, this.lootTableSeed);
            }
        }
    }

    /**
     * @see RandomizableContainerBlockEntity#unpackLootTable(Player)
     */
    public void unpackLootTable(@Nullable Player openingPlayer) {
        if (this.lootTable != null && this.level instanceof ServerLevel serverLevel) {
            var loottable = serverLevel.getServer().getLootTables().get(this.lootTable);
            if (openingPlayer instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger(serverPlayer, this.lootTable);
            }

            this.lootTable = null; // Can only generate once
            var lootBuilder = new LootContext.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                    .withOptionalRandomSeed(this.lootTableSeed);
            if (openingPlayer != null) {
                lootBuilder.withLuck(openingPlayer.getLuck())
                        .withParameter(LootContextParams.THIS_ENTITY, openingPlayer);
            }

            loottable.fill(this.inv.toContainer(), lootBuilder.create(LootContextParamSets.CHEST));
        }
    }

    /**
     * @see RandomizableContainerBlockEntity#setLootTable(ResourceLocation, long)
     */
    public void setLootTable(ResourceLocation lootTable, long lootTableSeed) {
        this.lootTable = lootTable;
        this.lootTableSeed = lootTableSeed;
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops, boolean remove) {
        unpackLootTable(null);
        super.addAdditionalDrops(level, pos, drops, remove);
    }

    @Override
    public InteractionResult disassembleWithWrench(Player player, Level level, BlockHitResult hitResult,
            ItemStack wrench) {
        return InteractionResult.FAIL;
    }
}
