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

package appeng.items.tools.powered;


import appeng.api.config.Actionable;
import appeng.api.util.DimensionalCoord;
import appeng.block.misc.BlockTinyTNT;
import appeng.core.AEConfig;
import appeng.hooks.IBlockTool;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.InWorldToolOperationResult;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ToolEntropyManipulator extends AEBasePoweredItem implements IBlockTool {
    private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> heatUp;
    private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> coolDown;

    public ToolEntropyManipulator() {
        super(AEConfig.instance().getEntropyManipulatorBattery());

        this.heatUp = new HashMap<>();
        this.coolDown = new HashMap<>();

        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.STONE.getDefaultState()),
                new InWorldToolOperationResult(Blocks.COBBLESTONE.getDefaultState()));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.STONEBRICK.getDefaultState()),
                new InWorldToolOperationResult(Blocks.STONEBRICK.getStateFromMeta(2)));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.LAVA, true), new InWorldToolOperationResult(Blocks.OBSIDIAN.getDefaultState()));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.FLOWING_LAVA, true),
                new InWorldToolOperationResult(Blocks.OBSIDIAN.getDefaultState()));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.GRASS, true), new InWorldToolOperationResult(Blocks.DIRT.getDefaultState()));

        final List<ItemStack> snowBalls = new ArrayList<>();
        snowBalls.add(new ItemStack(Items.SNOWBALL));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.FLOWING_WATER, true), new InWorldToolOperationResult(null, snowBalls));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.WATER, true), new InWorldToolOperationResult(Blocks.ICE.getDefaultState()));

        this.heatUp.put(new InWorldToolOperationIngredient(Blocks.ICE.getDefaultState()), new InWorldToolOperationResult(Blocks.WATER.getDefaultState()));
        this.heatUp.put(new InWorldToolOperationIngredient(Blocks.FLOWING_WATER, true), new InWorldToolOperationResult());
        this.heatUp.put(new InWorldToolOperationIngredient(Blocks.WATER, true), new InWorldToolOperationResult());
        this.heatUp.put(new InWorldToolOperationIngredient(Blocks.SNOW, true),
                new InWorldToolOperationResult(Blocks.FLOWING_WATER.getStateFromMeta(7)));
    }

    private static class InWorldToolOperationIngredient {
        private final IBlockState state;
        private final boolean blockOnly;

        public InWorldToolOperationIngredient(final IBlockState state) {
            this.state = state;
            this.blockOnly = false;
        }

        public InWorldToolOperationIngredient(final Block blk, final boolean b) {
            this.state = blk.getDefaultState();
            this.blockOnly = b;
        }

        @Override
        public int hashCode() {
            return this.state.getBlock().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final InWorldToolOperationIngredient other = (InWorldToolOperationIngredient) obj;
            return this.state == other.state && (this.blockOnly && this.state.getBlock() == other.state.getBlock());
        }
    }

    private void heat(final IBlockState state, final World w, final BlockPos pos) {
        InWorldToolOperationResult r = this.heatUp.get(new InWorldToolOperationIngredient(state));

        if (r == null) {
            r = this.heatUp.get(new InWorldToolOperationIngredient(state.getBlock(), true));
        }

        if (r.getBlockState() != null) {
            w.setBlockState(pos, r.getBlockState(), 3);
        } else {
            w.setBlockToAir(pos);
        }

        if (r.getDrops() != null) {
            Platform.spawnDrops(w, pos, r.getDrops());
        }
    }

    private boolean canHeat(final IBlockState state) {
        InWorldToolOperationResult r = this.heatUp.get(new InWorldToolOperationIngredient(state));

        if (r == null) {
            r = this.heatUp.get(new InWorldToolOperationIngredient(state.getBlock(), true));
        }

        return r != null;
    }

    private void cool(final IBlockState state, final World w, final BlockPos pos) {
        InWorldToolOperationResult r = this.coolDown.get(new InWorldToolOperationIngredient(state));

        if (r == null) {
            r = this.coolDown.get(new InWorldToolOperationIngredient(state.getBlock(), true));
        }

        if (r.getBlockState() != null) {
            w.setBlockState(pos, r.getBlockState(), 3);
        } else {
            w.setBlockToAir(pos);
        }

        if (r.getDrops() != null) {
            Platform.spawnDrops(w, pos, r.getDrops());
        }
    }

    private boolean canCool(final IBlockState state) {
        InWorldToolOperationResult r = this.coolDown.get(new InWorldToolOperationIngredient(state));

        if (r == null) {
            r = this.coolDown.get(new InWorldToolOperationIngredient(state.getBlock(), true));
        }

        return r != null;
    }

    @Override
    public boolean hitEntity(final ItemStack item, final EntityLivingBase target, final EntityLivingBase hitter) {
        if (this.getAECurrentPower(item) > 1600) {
            this.extractAEPower(item, 1600, Actionable.MODULATE);
            target.setFire(8);
        }

        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final EntityPlayer p, final EnumHand hand) {
        final RayTraceResult target = this.rayTrace(w, p, true);

        if (target == null) {
            return new ActionResult<>(EnumActionResult.FAIL, p.getHeldItem(hand));
        } else {
            if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
                final IBlockState state = w.getBlockState(target.getBlockPos());
                if (state.getMaterial() == Material.LAVA || state.getMaterial() == Material.WATER) {
                    if (Platform.hasPermissions(new DimensionalCoord(w, target.getBlockPos()), p)) {
                        this.onItemUse(p, w, target.getBlockPos(), hand, EnumFacing.UP, 0.0F, 0.0F, 0.0F);
                    }
                }
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer p, World w, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return this.onItemUse(p.getHeldItem(hand), p, w, pos, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack item, EntityPlayer p, World w, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (this.getAECurrentPower(item) > 1600) {
            if (!p.canPlayerEdit(pos, side, item)) {
                return EnumActionResult.FAIL;
            }

            final IBlockState state = w.getBlockState(pos);
            final Block blockID = state.getBlock();

            if (p.isSneaking()) {
                if (this.canCool(state)) {
                    this.extractAEPower(item, 1600, Actionable.MODULATE);
                    this.cool(state, w, pos);
                    return EnumActionResult.SUCCESS;
                }
            } else {
                if (blockID instanceof BlockTNT) {
                    w.setBlockToAir(pos);
                    ((BlockTNT) blockID).explode(w, pos, state, p);
                    return EnumActionResult.SUCCESS;
                }

                if (blockID instanceof BlockTinyTNT) {
                    w.setBlockToAir(pos);
                    ((BlockTinyTNT) blockID).startFuse(w, pos, p);
                    return EnumActionResult.SUCCESS;
                }

                if (this.canHeat(state)) {
                    this.extractAEPower(item, 1600, Actionable.MODULATE);
                    this.heat(state, w, pos);
                    return EnumActionResult.SUCCESS;
                }

                final ItemStack[] stack = Platform.getBlockDrops(w, pos);
                final List<ItemStack> out = new ArrayList<>();
                boolean hasFurnaceable = false;
                boolean canFurnaceable = true;

                for (final ItemStack i : stack) {
                    final ItemStack result = FurnaceRecipes.instance().getSmeltingResult(i);

                    if (!result.isEmpty()) {
                        if (result.getItem() instanceof ItemBlock) {
                            if (Block.getBlockFromItem(result.getItem()) == blockID && result.getItem().getDamage(result) == blockID
                                    .getMetaFromState(state)) {
                                canFurnaceable = false;
                            }
                        }
                        hasFurnaceable = true;
                        out.add(result);
                    } else {
                        canFurnaceable = false;
                        out.add(i);
                    }
                }

                if (hasFurnaceable && canFurnaceable) {
                    this.extractAEPower(item, 1600, Actionable.MODULATE);
                    final InWorldToolOperationResult or = InWorldToolOperationResult.getBlockOperationResult(out.toArray(new ItemStack[out.size()]));
                    w.playSound(p, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0F,
                            itemRand.nextFloat() * 0.4F + 0.8F);

                    if (or.getBlockState() == null) {
                        w.setBlockState(pos, Platform.AIR_BLOCK.getDefaultState(), 3);
                    } else {
                        w.setBlockState(pos, or.getBlockState(), 3);
                    }

                    if (or.getDrops() != null) {
                        Platform.spawnDrops(w, pos, or.getDrops());
                    }

                    return EnumActionResult.SUCCESS;
                } else {
                    final BlockPos offsetPos = pos.offset(side);

                    if (!p.canPlayerEdit(offsetPos, side, item)) {
                        return EnumActionResult.FAIL;
                    }

                    if (w.isAirBlock(offsetPos)) {
                        this.extractAEPower(item, 1600, Actionable.MODULATE);
                        w.playSound(p, offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D, SoundEvents.ITEM_FLINTANDSTEEL_USE,
                                SoundCategory.PLAYERS, 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
                        w.setBlockState(offsetPos, Blocks.FIRE.getDefaultState());
                    }

                    return EnumActionResult.SUCCESS;
                }
            }
        }

        return EnumActionResult.PASS;
    }
}
