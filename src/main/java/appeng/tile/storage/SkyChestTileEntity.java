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

package appeng.tile.storage;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.block.storage.SkyChestBlock;
import appeng.tile.AEBaseInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;

@OnlyIn(value = Dist.CLIENT, _interface = IChestLid.class)
public class SkyChestTileEntity extends AEBaseInvTileEntity implements ITickableTileEntity, IChestLid {

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 9 * 4);

    // server
    private int numPlayersUsing;
    // client..
    private long lastEvent;
    private float lidAngle;
    private float prevLidAngle;

    public SkyChestTileEntity(TileEntityType<? extends SkyChestTileEntity> type) {
        super(type);
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.getPlayerOpen() > 0);
    }

    @Override
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int wasOpen = this.getPlayerOpen();
        this.setPlayerOpen(data.readBoolean() ? 1 : 0);

        if (wasOpen != this.getPlayerOpen()) {
            this.setLastEvent(System.currentTimeMillis());
        }

        return c; // TESR yo!
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    public void openInventory(final PlayerEntity player) {
        if (!player.isSpectator()) {
            this.setPlayerOpen(this.getPlayerOpen() + 1);
            onOpenOrClose();

            if (this.getPlayerOpen() == 1) {
                this.getLevel().playSound(player, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
                        this.worldPosition.getZ() + 0.5D, SoundEvents.CHEST_OPEN, SoundCategory.BLOCKS, 0.5F,
                        this.getLevel().random.nextFloat() * 0.1F + 0.9F);
                this.markForUpdate();
            }
        }
    }

    public void closeInventory(final PlayerEntity player) {
        if (!player.isSpectator()) {
            this.setPlayerOpen(this.getPlayerOpen() - 1);
            onOpenOrClose();

            if (this.getPlayerOpen() < 0) {
                this.setPlayerOpen(0);
            }

            if (this.getPlayerOpen() == 0) {
                this.getLevel().playSound(player, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
                        this.worldPosition.getZ() + 0.5D, SoundEvents.CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F,
                        this.getLevel().random.nextFloat() * 0.1F + 0.9F);
                this.markForUpdate();
            }
        }
    }

    // See ChestTileEntity
    private void onOpenOrClose() {
        Block block = this.getBlockState().getBlock();
        if (block instanceof SkyChestBlock) {
            this.level.blockEvent(this.worldPosition, block, 1, this.numPlayersUsing);
            this.level.updateNeighborsAt(this.worldPosition, block);
            // FIXME: Uhm, we are we doing this?
            this.level.updateNeighborsAt(this.worldPosition.below(), block);
        }
    }

    @Override
    public void tick() {
        this.prevLidAngle = this.lidAngle;
        if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            if (this.numPlayersUsing > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }

            this.lidAngle = MathHelper.clamp(this.lidAngle, 0, 1);
        }
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {

    }

    public int getPlayerOpen() {
        return this.numPlayersUsing;
    }

    private void setPlayerOpen(final int playerOpen) {
        this.numPlayersUsing = playerOpen;
    }

    public long getLastEvent() {
        return this.lastEvent;
    }

    private void setLastEvent(final long lastEvent) {
        this.lastEvent = lastEvent;
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return MathHelper.lerp(partialTicks, this.prevLidAngle, this.lidAngle);
    }

}
