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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

import appeng.api.config.Actionable;
import appeng.api.util.DimensionalBlockPos;
import appeng.block.misc.TinyTNTBlock;
import appeng.core.AEConfig;
import appeng.hooks.IBlockTool;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.recipes.entropy.EntropyMode;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class EntropyManipulatorItem extends AEBasePoweredItem implements IBlockTool {

    /**
     * The amount of AE energy consumed per use.
     */
    public static final int ENERGY_PER_USE = 1600;

    public EntropyManipulatorItem(Item.Properties props) {
        super(AEConfig.instance().getEntropyManipulatorBattery(), props);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 800d;
    }

    @Override
    public boolean hurtEnemy(ItemStack item, LivingEntity target, LivingEntity hitter) {
        if (this.getAECurrentPower(item) > ENERGY_PER_USE) {
            this.extractAEPower(item, ENERGY_PER_USE, Actionable.MODULATE);
            target.setSecondsOnFire(8);
        }

        return false;
    }

    // Overridden to allow use of the item on WATER and LAVA which are otherwise not considered for onItemUse
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        final BlockHitResult target = getPlayerPOVHitResult(level, p, Fluid.ANY);

        if (target.getType() != Type.BLOCK) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, p.getItemInHand(hand));
        } else {
            BlockPos pos = target.getBlockPos();
            final BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty() && Platform.hasPermissions(new DimensionalBlockPos(level, pos), p)) {
                UseOnContext context = new UseOnContext(p, hand, target);
                this.useOn(context);
            }
        }

        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                p.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack item = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player p = context.getPlayer();

        boolean tryBoth = false;
        if (p == null) {
            if (level.isClientSide) {
                return InteractionResult.FAIL;
            }
            p = Platform.getPlayer((ServerLevel) level);
            // Fake players cannot crouch and we cannot communicate whether they want to heat or cool
            tryBoth = true;
        }

        // Correct pos for fluids as these are normally not taken into account.
        final BlockHitResult target = getPlayerPOVHitResult(level, p, Fluid.ANY);
        if (target.getType() == Type.BLOCK) {
            pos = target.getBlockPos();
        }

        if (this.getAECurrentPower(item) > ENERGY_PER_USE) {
            if (!p.mayUseItemAt(pos, side, item)) {
                return InteractionResult.FAIL;
            }

            // Delegate to the server from here on
            if (!level.isClientSide() && !tryApplyEffect(level, item, pos, side, p, tryBoth)) {
                return InteractionResult.FAIL;
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    private boolean tryApplyEffect(Level level, ItemStack item, BlockPos pos, Direction side, Player p,
            boolean tryBoth) {
        final BlockState blockState = level.getBlockState(pos);
        final Block block = blockState.getBlock();
        final FluidState fluidState = level.getFluidState(pos);

        if (tryBoth || InteractionUtil.isInAlternateUseMode(p)) {
            EntropyRecipe coolRecipe = findRecipe(level, EntropyMode.COOL, blockState, fluidState);
            if (coolRecipe != null) {
                this.extractAEPower(item, 1600, Actionable.MODULATE);
                applyRecipe(coolRecipe, level, pos, blockState, fluidState);
                return true;
            }
        }

        if (tryBoth || !InteractionUtil.isInAlternateUseMode(p)) {
            if (block instanceof TntBlock) {
                level.removeBlock(pos, false);
                block.onCaughtFire(level.getBlockState(pos), level, pos, side, p);
                return true;
            }

            if (block instanceof TinyTNTBlock) {
                level.removeBlock(pos, false);
                ((TinyTNTBlock) block).startFuse(level, pos, p);
                return true;
            }

            EntropyRecipe heatRecipe = findRecipe(level, EntropyMode.HEAT, blockState, fluidState);
            if (heatRecipe != null) {
                this.extractAEPower(item, 1600, Actionable.MODULATE);
                applyRecipe(heatRecipe, level, pos, blockState, fluidState);
                return true;
            }

            if (performInWorldSmelting(item, level, p, pos, block)) {
                return true;
            }

            if (applyFlintAndSteelEffect(level, item, pos, side, p)) {
                return true;
            }
        }

        return false;
    }

    private boolean applyFlintAndSteelEffect(Level level, ItemStack item, BlockPos pos, Direction side, Player p) {
        final BlockPos offsetPos = pos.relative(side);
        if (!p.mayUseItemAt(offsetPos, side, item)) {
            return false;
        }

        if (level.isEmptyBlock(offsetPos)) {
            this.extractAEPower(item, ENERGY_PER_USE, Actionable.MODULATE);
            level.playSound(p, offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D,
                    SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F,
                    level.random.nextFloat() * 0.4F + 0.8F);
            level.setBlockAndUpdate(offsetPos, Blocks.FIRE.defaultBlockState());
        }
        return true;
    }

    /**
     * The entropy manipulator in heat-mode can directly smelt in-level blocks and drop the smelted results, but only if
     * all drops of the block have smelting recipes.
     */
    private boolean performInWorldSmelting(ItemStack item, Level level, Player p, BlockPos pos, Block block) {
        ItemStack[] stack = Platform.getBlockDrops(level, pos);

        // Results of the operation
        BlockState smeltedBlockState = null;
        List<ItemStack> smeltedDrops = new ArrayList<>();

        var tempInv = new SimpleContainer(1);
        for (ItemStack i : stack) {
            tempInv.setItem(0, i);
            Optional<SmeltingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, tempInv,
                    level);

            if (!recipe.isPresent()) {
                return false;
            }

            ItemStack result = recipe.get().assemble(tempInv);
            if (result.getItem() instanceof BlockItem) {
                Block smeltedBlock = Block.byItem(result.getItem());
                if (smeltedBlock == block) {
                    // Prevent auto-smelting if we wouldn't actually change the blockstate of the block at all,
                    // but still could drop additional items
                    return false;
                }
                // The first smelted drop that could be placed as a block itself will not be dropped, but
                // rather replace the current block.
                if (smeltedBlockState == null
                        && smeltedBlock != Blocks.AIR
                        && smeltedBlock.defaultBlockState().getMaterial() != Material.AIR) {
                    smeltedBlockState = smeltedBlock.defaultBlockState();
                    continue;
                }
            }

            smeltedDrops.add(result);
        }

        if (smeltedBlockState == null && smeltedDrops.isEmpty()) {
            return false; // Block has no drops
        }

        this.extractAEPower(item, ENERGY_PER_USE, Actionable.MODULATE);

        level.playSound(p, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F,
                level.random.nextFloat() * 0.4F + 0.8F);

        if (smeltedBlockState == null) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            level.setBlock(pos, smeltedBlockState, 3);
        }

        Platform.spawnDrops(level, pos, smeltedDrops);
        return true;
    }

    @Nullable
    private static EntropyRecipe findRecipe(Level level, EntropyMode mode, BlockState blockState,
            FluidState fluidState) {
        for (Recipe<Container> recipe : level.getRecipeManager().byType(EntropyRecipe.TYPE).values()) {
            EntropyRecipe entropyRecipe = (EntropyRecipe) recipe;

            if (entropyRecipe.matches(mode, blockState, fluidState)) {
                return entropyRecipe;
            }
        }
        return null;
    }

    private static void applyRecipe(EntropyRecipe recipe, Level level, BlockPos pos, BlockState blockState,
            FluidState fluidState) {
        BlockState outputBlockState = recipe.getOutputBlockState(blockState);
        if (outputBlockState != null) {
            level.setBlock(pos, outputBlockState, 3);
        } else {
            FluidState outputFluidState = recipe.getOutputFluidState(fluidState);
            if (outputFluidState != null) {
                level.setBlock(pos, outputFluidState.createLegacyBlock(), 3);
            } else {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }

        if (!recipe.getDrops().isEmpty()) {
            Platform.spawnDrops(level, pos, recipe.getDrops());
        }

        if (recipe.getMode() == EntropyMode.HEAT && !level.isClientSide()) {
            // Same effect as emptying a water bucket in the nether (see BucketItem)
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                    2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; ++l) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(),
                        pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
