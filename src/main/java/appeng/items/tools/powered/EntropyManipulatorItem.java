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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import appeng.api.config.Actionable;
import appeng.api.util.DimensionalCoord;
import appeng.block.misc.TinyTNTBlock;
import appeng.container.ContainerNull;
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
    private static final int ENERGY_PER_USE = 1600;

    public EntropyManipulatorItem(Item.Properties props) {
        super(AEConfig.instance().getEntropyManipulatorBattery(), props);
    }

    @Override
    public boolean hitEntity(final ItemStack item, final LivingEntity target, final LivingEntity hitter) {
        if (this.getAECurrentPower(item) > ENERGY_PER_USE) {
            this.extractAEPower(item, ENERGY_PER_USE, Actionable.MODULATE);
            target.setFire(8);
        }

        return false;
    }

    // Overridden to allow use of the item on WATER and LAVA which are otherwise not considered for onItemUse
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final PlayerEntity p, final Hand hand) {
        final BlockRayTraceResult target = rayTrace(w, p, RayTraceContext.FluidMode.ANY);

        if (target.getType() != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(ActionResultType.FAIL, p.getHeldItem(hand));
        } else {
            BlockPos pos = target.getPos();
            final BlockState state = w.getBlockState(pos);
            if (!state.getFluidState().isEmpty()) {
                if (Platform.hasPermissions(new DimensionalCoord(w, pos), p)) {
                    ItemUseContext context = new ItemUseContext(p, hand, target);
                    this.onItemUse(context);
                }
            }
        }

        return new ActionResult<>(ActionResultType.func_233537_a_(w.isRemote()), p.getHeldItem(hand));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World w = context.getWorld();
        ItemStack item = context.getItem();
        BlockPos pos = context.getPos();
        Direction side = context.getFace();
        PlayerEntity p = context.getPlayer();

        boolean tryBoth = false;
        if (p == null) {
            if (w.isRemote) {
                return ActionResultType.FAIL;
            }
            p = Platform.getPlayer((ServerWorld) w);
            // Fake players cannot crouch and we cannot communicate whether they want to heat or cool
            tryBoth = true;
        }

        // Correct pos for fluids as these are normally not taken into account.
        final BlockRayTraceResult target = rayTrace(w, p, RayTraceContext.FluidMode.ANY);
        if (target.getType() == RayTraceResult.Type.BLOCK) {
            pos = target.getPos();
        }

        if (this.getAECurrentPower(item) > ENERGY_PER_USE) {
            if (!p.canPlayerEdit(pos, side, item)) {
                return ActionResultType.FAIL;
            }

            // Delegate to the server from here on
            if (!w.isRemote() && !tryApplyEffect(w, item, pos, side, p, tryBoth)) {
                return ActionResultType.FAIL;
            }

            return ActionResultType.func_233537_a_(w.isRemote());
        }

        return ActionResultType.PASS;
    }

    private boolean tryApplyEffect(World w, ItemStack item, BlockPos pos, Direction side, PlayerEntity p,
            boolean tryBoth) {
        final BlockState blockState = w.getBlockState(pos);
        final Block block = blockState.getBlock();
        final FluidState fluidState = w.getFluidState(pos);

        if (tryBoth || InteractionUtil.isInAlternateUseMode(p)) {
            EntropyRecipe coolRecipe = findRecipe(w, EntropyMode.COOL, blockState, fluidState);
            if (coolRecipe != null) {
                this.extractAEPower(item, 1600, Actionable.MODULATE);
                applyRecipe(coolRecipe, w, pos, blockState, fluidState);
                return true;
            }
        }

        if (tryBoth || !InteractionUtil.isInAlternateUseMode(p)) {
            if (block instanceof TNTBlock) {
                w.removeBlock(pos, false);
                block.catchFire(w.getBlockState(pos), w, pos, side, p);
                return true;
            }

            if (block instanceof TinyTNTBlock) {
                w.removeBlock(pos, false);
                ((TinyTNTBlock) block).startFuse(w, pos, p);
                return true;
            }

            EntropyRecipe heatRecipe = findRecipe(w, EntropyMode.HEAT, blockState, fluidState);
            if (heatRecipe != null) {
                this.extractAEPower(item, 1600, Actionable.MODULATE);
                applyRecipe(heatRecipe, w, pos, blockState, fluidState);
                return true;
            }

            if (performInWorldSmelting(item, w, p, pos, block)) {
                return true;
            }

            if (applyFlintAndSteelEffect(w, item, pos, side, p)) {
                return true;
            }
        }

        return false;
    }

    private boolean applyFlintAndSteelEffect(World w, ItemStack item, BlockPos pos, Direction side, PlayerEntity p) {
        final BlockPos offsetPos = pos.offset(side);
        if (!p.canPlayerEdit(offsetPos, side, item)) {
            return false;
        }

        if (w.isAirBlock(offsetPos)) {
            this.extractAEPower(item, ENERGY_PER_USE, Actionable.MODULATE);
            w.playSound(p, offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D,
                    SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0F,
                    random.nextFloat() * 0.4F + 0.8F);
            w.setBlockState(offsetPos, Blocks.FIRE.getDefaultState());
        }
        return true;
    }

    /**
     * The entropy manipulator in heat-mode can directly smelt in-world blocks and drop the smelted results, but only if
     * all drops of the block have smelting recipes.
     */
    private boolean performInWorldSmelting(ItemStack item, World w, PlayerEntity p, BlockPos pos, Block block) {
        ItemStack[] stack = Platform.getBlockDrops(w, pos);

        // Results of the operation
        BlockState smeltedBlockState = null;
        List<ItemStack> smeltedDrops = new ArrayList<>();

        CraftingInventory tempInv = new CraftingInventory(new ContainerNull(), 1, 1);
        for (final ItemStack i : stack) {
            tempInv.setInventorySlotContents(0, i);
            Optional<FurnaceRecipe> recipe = w.getRecipeManager().getRecipe(IRecipeType.SMELTING, tempInv, w);

            if (!recipe.isPresent()) {
                return false;
            }

            ItemStack result = recipe.get().getCraftingResult(tempInv);
            if (result.getItem() instanceof BlockItem) {
                Block smeltedBlock = Block.getBlockFromItem(result.getItem());
                if (smeltedBlock == block) {
                    // Prevent auto-smelting if we wouldn't actually change the blockstate of the block at all,
                    // but still could drop additional items
                    return false;
                }
                // The first smelted drop that could be placed as a block itself will not be dropped, but
                // rather replace the current block.
                if (smeltedBlockState == null
                        && smeltedBlock != Blocks.AIR
                        && smeltedBlock.getDefaultState().getMaterial() != Material.AIR) {
                    smeltedBlockState = smeltedBlock.getDefaultState();
                    continue;
                }
            }

            smeltedDrops.add(result);
        }

        if (smeltedBlockState == null && smeltedDrops.isEmpty()) {
            return false; // Block has no drops
        }

        this.extractAEPower(item, ENERGY_PER_USE, Actionable.MODULATE);

        w.playSound(p, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0F,
                random.nextFloat() * 0.4F + 0.8F);

        if (smeltedBlockState == null) {
            w.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        } else {
            w.setBlockState(pos, smeltedBlockState, 3);
        }

        Platform.spawnDrops(w, pos, smeltedDrops);
        return true;
    }

    @Nullable
    private static EntropyRecipe findRecipe(World world, EntropyMode mode, BlockState blockState,
            FluidState fluidState) {
        for (IRecipe<IInventory> recipe : world.getRecipeManager().getRecipes(EntropyRecipe.TYPE).values()) {
            EntropyRecipe entropyRecipe = (EntropyRecipe) recipe;

            if (entropyRecipe.matches(mode, blockState, fluidState)) {
                return entropyRecipe;
            }
        }
        return null;
    }

    private static void applyRecipe(EntropyRecipe recipe, World w, BlockPos pos, BlockState blockState,
            FluidState fluidState) {
        BlockState outputBlockState = recipe.getOutputBlockState(blockState);
        if (outputBlockState != null) {
            w.setBlockState(pos, outputBlockState, 3);
        } else {
            FluidState outputFluidState = recipe.getOutputFluidState(fluidState);
            if (outputFluidState != null) {
                w.setBlockState(pos, outputFluidState.getBlockState(), 3);
            } else {
                w.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }

        if (!recipe.getDrops().isEmpty()) {
            Platform.spawnDrops(w, pos, recipe.getDrops());
        }

        if (recipe.getMode() == EntropyMode.HEAT && !w.isRemote()) {
            // Same effect as emptying a water bucket in the nether (see BucketItem)
            w.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                    2.6F + (w.rand.nextFloat() - w.rand.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; ++l) {
                w.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(),
                        pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
