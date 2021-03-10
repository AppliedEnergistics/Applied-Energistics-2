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

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
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
import appeng.util.Platform;

public class EntropyManipulatorItem extends AEBasePoweredItem implements IBlockTool {

    public EntropyManipulatorItem(Item.Properties props) {
        super(AEConfig.instance().getEntropyManipulatorBattery(), props);
    }

    public static EntropyRecipe findForInput(World world, EntropyMode mode, BlockState block, FluidState fluid) {
        for (IRecipe<IInventory> recipe : world.getRecipeManager().getRecipes(EntropyRecipe.TYPE).values()) {
            EntropyRecipe entropyRecipe = (EntropyRecipe) recipe;

            if (entropyRecipe.matches(mode, block, fluid)) {
                return entropyRecipe;
            }
        }
        return null;
    }

    private void heat(final BlockState block, FluidState fluid, final World w, final BlockPos pos) {
        EntropyRecipe recipe = findForInput(w, EntropyMode.HEAT, block, fluid);

        if (recipe.getOutputBlockState(block) != null) {
            w.setBlockState(pos, recipe.getOutputBlockState(block), 3);
        } else if (recipe.getOutputFluidState() != null) {
            w.setBlockState(pos, recipe.getOutputFluidState().getBlockState(), 3);
        } else {
            w.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        if (!recipe.getDrops().isEmpty()) {
            Platform.spawnDrops(w, pos, recipe.getDrops());
        }

        if (!w.isRemote()) {
            // Same effect as emptying a water bucket in the nether (see BucketItem)
            w.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                    2.6F + (w.rand.nextFloat() - w.rand.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; ++l) {
                w.addParticle(ParticleTypes.LARGE_SMOKE, (double) pos.getX() + Math.random(),
                        (double) pos.getY() + Math.random(), (double) pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    private boolean canHeat(World world, BlockState block, FluidState fluid) {
        return findForInput(world, EntropyMode.HEAT, block, fluid) != null;
    }

    private void cool(final BlockState block, FluidState fluid, final World w, final BlockPos pos) {
        EntropyRecipe recipe = findForInput(w, EntropyMode.COOL, block, fluid);

        if (recipe.getOutputBlockState(block) != null) {
            w.setBlockState(pos, recipe.getOutputBlockState(block), 3);
        } else if (recipe.getOutputFluidState() != null) {
            w.setBlockState(pos, recipe.getOutputFluidState().getBlockState(), 3);
        } else {
            w.setBlockState(pos, Blocks.AIR.getDefaultState());
        }

        if (!recipe.getDrops().isEmpty()) {
            Platform.spawnDrops(w, pos, recipe.getDrops());
        }
    }

    private boolean canCool(World world, BlockState block, FluidState fluid) {
        return findForInput(world, EntropyMode.COOL, block, fluid) != null;
    }

    @Override
    public boolean hitEntity(final ItemStack item, final LivingEntity target, final LivingEntity hitter) {
        if (this.getAECurrentPower(item) > 1600) {
            this.extractAEPower(item, 1600, Actionable.MODULATE);
            target.setFire(8);
        }

        return false;
    }

    // Overridden to allow use of the item on WATER and LAVA which are otherwise not considered for onItemUse
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final PlayerEntity p, final Hand hand) {
        final RayTraceResult target = rayTrace(w, p, RayTraceContext.FluidMode.ANY);

        if (target.getType() != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(ActionResultType.FAIL, p.getHeldItem(hand));
        } else {
            BlockPos pos = ((BlockRayTraceResult) target).getPos();
            final BlockState state = w.getBlockState(pos);
            if (!state.getFluidState().isEmpty()) {
                if (Platform.hasPermissions(new DimensionalCoord(w, pos), p)) {
                    ItemUseContext context = new ItemUseContext(p, hand, (BlockRayTraceResult) target);
                    this.onItemUse(context);
                }
            }
        }

        return new ActionResult<>(ActionResultType.SUCCESS, p.getHeldItem(hand));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World w = context.getWorld();
        ItemStack item = context.getItem();
        BlockPos pos = context.getPos();
        Direction side = context.getFace();
        PlayerEntity p = context.getPlayer();

        // Correct pos for fluids as these are normally not taken into account.
        final RayTraceResult target = rayTrace(w, p, RayTraceContext.FluidMode.ANY);
        if (target.getType() == RayTraceResult.Type.BLOCK) {
            pos = ((BlockRayTraceResult) target).getPos();
        }

        boolean tryBoth = false;
        if (p == null) {
            if (w.isRemote) {
                return ActionResultType.FAIL;
            }
            p = Platform.getPlayer((ServerWorld) w);
            // Fake players cannot crouch and we cannot communicate whether they want to heat or cool
            tryBoth = true;
        }

        if (this.getAECurrentPower(item) > 1600) {
            if (!p.canPlayerEdit(pos, side, item)) {
                return ActionResultType.FAIL;
            }

            final BlockState blockState = w.getBlockState(pos);
            final Block block = blockState.getBlock();
            final FluidState fluidState = w.getFluidState(pos);
            final Fluid fluid = fluidState.getFluid();

            if (tryBoth || p.isCrouching()) {
                if (this.canCool(w, blockState, fluidState)) {
                    this.extractAEPower(item, 1600, Actionable.MODULATE);
                    this.cool(blockState, fluidState, w, pos);
                    return ActionResultType.SUCCESS;
                }
            }
            if (tryBoth || !p.isCrouching()) {
                if (block instanceof TNTBlock) {
                    w.removeBlock(pos, false);
                    block.catchFire(w.getBlockState(pos), w, pos, context.getFace(), p);
                    return ActionResultType.SUCCESS;
                }

                if (block instanceof TinyTNTBlock) {
                    w.removeBlock(pos, false);
                    ((TinyTNTBlock) block).startFuse(w, pos, p);
                    return ActionResultType.SUCCESS;
                }

                if (this.canHeat(w, blockState, fluidState)) {
                    this.extractAEPower(item, 1600, Actionable.MODULATE);
                    this.heat(blockState, fluidState, w, pos);
                    return ActionResultType.SUCCESS;
                }

                final ItemStack[] stack = Platform.getBlockDrops(w, pos);
                final List<ItemStack> out = new ArrayList<>();
                boolean hasFurnaceable = false;
                boolean canFurnaceable = true;

                for (final ItemStack i : stack) {
                    CraftingInventory tempInv = new CraftingInventory(new ContainerNull(), 1, 1);
                    tempInv.setInventorySlotContents(0, i);
                    Optional<FurnaceRecipe> recipe = w.getRecipeManager().getRecipe(IRecipeType.SMELTING, tempInv, w);

                    if (recipe.isPresent()) {
                        ItemStack result = recipe.get().getCraftingResult(tempInv);
                        if (result.getItem() instanceof BlockItem) {
                            // Anti-Dupe-Bug I presume...
                            if (Block.getBlockFromItem(result.getItem()) == block) {
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
                    final InWorldToolOperationResult or = InWorldToolOperationResult
                            .getBlockOperationResult(out.toArray(new ItemStack[0]));

                    w.playSound(p, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                            SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0F,
                            random.nextFloat() * 0.4F + 0.8F);

                    if (or.getBlockState() == null) {
                        w.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    } else {
                        w.setBlockState(pos, or.getBlockState(), 3);
                    }

                    if (or.getDrops() != null) {
                        Platform.spawnDrops(w, pos, or.getDrops());
                    }

                    return ActionResultType.SUCCESS;
                } else {
                    final BlockPos offsetPos = pos.offset(side);

                    if (!p.canPlayerEdit(offsetPos, side, item)) {
                        return ActionResultType.FAIL;
                    }

                    if (w.isAirBlock(offsetPos)) {
                        this.extractAEPower(item, 1600, Actionable.MODULATE);
                        w.playSound(p, offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D,
                                SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0F,
                                random.nextFloat() * 0.4F + 0.8F);
                        w.setBlockState(offsetPos, Blocks.FIRE.getDefaultState());
                    }

                    return ActionResultType.SUCCESS;
                }
            }
        }

        return ActionResultType.PASS;
    }

    /**
     * A helper class to handle in world operations when meltable blocks are involved.
     */
    private static class InWorldToolOperationResult {

        private final BlockState blockState;
        private final List<ItemStack> drops;

        public InWorldToolOperationResult(final BlockState block, final List<ItemStack> drops) {
            this.blockState = block;
            this.drops = drops;
        }

        public static InWorldToolOperationResult getBlockOperationResult(final ItemStack[] items) {
            final List<ItemStack> temp = new ArrayList<>();
            BlockState b = null;

            for (final ItemStack l : items) {
                if (b == null) {
                    final Block bl = Block.getBlockFromItem(l.getItem());

                    if (bl != null && !(bl instanceof AirBlock)) {
                        b = bl.getDefaultState();
                        continue;
                    }
                }

                temp.add(l);
            }

            return new InWorldToolOperationResult(b, temp);
        }

        public BlockState getBlockState() {
            return this.blockState;
        }

        public List<ItemStack> getDrops() {
            return this.drops;
        }
    }
}
