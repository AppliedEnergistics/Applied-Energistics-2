/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.decorative.solid;


import appeng.block.AEBaseBlock;
import appeng.core.worlddata.WorldData;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class BlockSkyStone extends AEBaseBlock {
    private static final float BLOCK_RESISTANCE = 150.0f;
    private static final float BREAK_SPEAK_SCALAR = 0.1f;
    private static final double BREAK_SPEAK_THRESHOLD = 7.0;
    private final SkystoneType type;

    public BlockSkyStone(final SkystoneType type) {
        super(Material.ROCK);
        this.setHardness(50);
        this.blockResistance = BLOCK_RESISTANCE;
        if (type == SkystoneType.STONE) {
            this.setHarvestLevel("pickaxe", 3);
        }

        this.type = type;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void breakFaster(final PlayerEvent.BreakSpeed event) {
        if (event.getState().getBlock() == this && event.getEntityPlayer() != null) {
            final ItemStack is = event.getEntityPlayer().getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            int level = -1;

            if (!is.isEmpty()) {
                level = is.getItem().getHarvestLevel(is, "pickaxe", event.getEntityPlayer(), event.getState());
            }

            if (this.type != SkystoneType.STONE || level >= 3 || event.getOriginalSpeed() > BREAK_SPEAK_THRESHOLD) {
                event.setNewSpeed(event.getNewSpeed() / BREAK_SPEAK_SCALAR);
            }
        }
    }

    @Override
    public void onBlockAdded(final World w, final BlockPos pos, final IBlockState state) {
        super.onBlockAdded(w, pos, state);
        if (Platform.isServer()) {
            WorldData.instance().compassData().service().updateArea(w, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public void breakBlock(final World w, final BlockPos pos, final IBlockState state) {
        super.breakBlock(w, pos, state);

        if (Platform.isServer()) {
            WorldData.instance().compassData().service().updateArea(w, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public enum SkystoneType {
        STONE, BLOCK, BRICK, SMALL_BRICK
    }
}
