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

import java.util.*;

import appeng.mixins.TntAccessor;
import appeng.util.FakePlayer;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.*;
import net.minecraft.recipe.RecipeType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RayTraceContext;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

import appeng.api.config.Actionable;
import appeng.api.util.DimensionalCoord;
import appeng.block.misc.TinyTNTBlock;
import appeng.container.ContainerNull;
import appeng.core.AEConfig;
import appeng.hooks.IBlockTool;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.InWorldToolOperationResult;
import appeng.util.Platform;

public class EntropyManipulatorItem extends AEBasePoweredItem implements IBlockTool {
    private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> heatUp;
    private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> coolDown;

    public EntropyManipulatorItem(Item.Settings props) {
        super(AEConfig.instance().getEntropyManipulatorBattery(), props);

        this.heatUp = new HashMap<>();
        this.coolDown = new HashMap<>();

        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.STONE),
                new InWorldToolOperationResult(Blocks.COBBLESTONE.getDefaultState()));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.STONE_BRICKS),
                new InWorldToolOperationResult(Blocks.CRACKED_STONE_BRICKS.getDefaultState()));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.LAVA, Fluids.LAVA),
                new InWorldToolOperationResult(Blocks.OBSIDIAN.getDefaultState()));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.LAVA, Fluids.FLOWING_LAVA),
                new InWorldToolOperationResult(Blocks.OBSIDIAN.getDefaultState()));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.GRASS_BLOCK),
                new InWorldToolOperationResult(Blocks.DIRT.getDefaultState()));

        final List<ItemStack> snowBalls = new ArrayList<>();
        snowBalls.add(new ItemStack(Items.SNOWBALL));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.WATER, Fluids.FLOWING_WATER),
                new InWorldToolOperationResult(null, snowBalls));
        this.coolDown.put(new InWorldToolOperationIngredient(Blocks.WATER, Fluids.WATER),
                new InWorldToolOperationResult(Blocks.ICE.getDefaultState()));

        this.heatUp.put(new InWorldToolOperationIngredient(Blocks.ICE),
                new InWorldToolOperationResult(Blocks.WATER.getDefaultState()));
        this.heatUp.put(new InWorldToolOperationIngredient(Blocks.WATER, Fluids.WATER),
                new InWorldToolOperationResult());
        this.heatUp.put(new InWorldToolOperationIngredient(Blocks.WATER, Fluids.FLOWING_WATER),
                new InWorldToolOperationResult());
        this.heatUp.put(new InWorldToolOperationIngredient(Blocks.SNOW), new InWorldToolOperationResult(
                Blocks.WATER.getDefaultState().with(Properties.LEVEL_15, 7), Fluids.FLOWING_WATER));
    }

    private static class InWorldToolOperationIngredient {
        private final Block block;
        private final Fluid fluid;

        public InWorldToolOperationIngredient(Block block) {
            this(block, Fluids.EMPTY);
        }

        public InWorldToolOperationIngredient(Block block, Fluid fluid) {
            this.block = block;
            this.fluid = fluid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            InWorldToolOperationIngredient that = (InWorldToolOperationIngredient) o;
            return block.equals(that.block) && fluid.equals(that.fluid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(block, fluid);
        }
    }

    private void heat(final Block block, Fluid fluid, final World w, final BlockPos pos) {
        InWorldToolOperationResult r = this.heatUp.get(new InWorldToolOperationIngredient(block, fluid));

        if (r == null) {
            // Try with "don't care" fluid
            r = this.heatUp.get(new InWorldToolOperationIngredient(block, Fluids.EMPTY));
        }

        if (r.getBlockState() != null) {
            w.setBlockState(pos, r.getBlockState(), 3);
        } else {
            w.setBlockState(pos, Fluids.EMPTY.getDefaultState().getBlockState(), 3);
        }

        if (r.getDrops() != null) {
            Platform.spawnDrops(w, pos, r.getDrops());
        }

        if (!w.isClient) {
            // Same effect as emptying a water bucket in the nether (see BucketItem)
            w.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                    2.6F + (w.random.nextFloat() - w.random.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; ++l) {
                w.addParticle(ParticleTypes.LARGE_SMOKE, (double) pos.getX() + Math.random(),
                        (double) pos.getY() + Math.random(), (double) pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
            }
        }

    }

    private boolean canHeat(final Block block, Fluid fluid) {
        InWorldToolOperationResult r = this.heatUp.get(new InWorldToolOperationIngredient(block, fluid));

        if (r == null) {
            // Also try with "don't care" fluid
            r = this.heatUp.get(new InWorldToolOperationIngredient(block, Fluids.EMPTY));
        }

        return r != null;
    }

    private void cool(final Block block, Fluid fluid, final World w, final BlockPos pos) {
        InWorldToolOperationResult r = this.coolDown.get(new InWorldToolOperationIngredient(block, fluid));

        if (r == null) {
            r = this.coolDown.get(new InWorldToolOperationIngredient(block, Fluids.EMPTY));
        }

        if (r.getBlockState() != null) {
            w.setBlockState(pos, r.getBlockState(), 3);
        } else {
            w.removeBlock(pos, false);
        }

        if (r.getDrops() != null) {
            Platform.spawnDrops(w, pos, r.getDrops());
        }
    }

    private boolean canCool(Block block, Fluid fluid) {
        InWorldToolOperationResult r = this.coolDown.get(new InWorldToolOperationIngredient(block, fluid));

        if (r == null) {
            r = this.coolDown.get(new InWorldToolOperationIngredient(block, Fluids.EMPTY));
        }

        return r != null;
    }

    @Override
    public boolean postHit(final ItemStack item, final LivingEntity target, final LivingEntity hitter) {
        if (this.getAECurrentPower(item) > 1600) {
            this.extractAEPower(item, 1600, Actionable.MODULATE);
            target.setFireTicks(8);
        }

        return false;
    }

    // Overridden to allow use of the item on WATER and LAVA which are otherwise not
    // considered for onItemUse
    @Override
    public TypedActionResult<ItemStack> use(final World w, final PlayerEntity p, final Hand hand) {
        final BlockHitResult target = rayTrace(w, p, RayTraceContext.FluidHandling.ANY);

        if (target.getType() != HitResult.Type.BLOCK) {
            BlockPos pos = target.getBlockPos();
            final BlockState state = w.getBlockState(pos);
            if (state.getMaterial() == Material.LAVA || state.getMaterial() == Material.WATER) {
                if (Platform.hasPermissions(new DimensionalCoord(w, pos), p)) {
                    ItemUsageContext context = new ItemUsageContext(p, hand, target);
                    this.onItemUse(context);
                }
            }
        }

        return new TypedActionResult<>(ActionResult.SUCCESS, p.getStackInHand(hand));
    }

    @Override
    public ActionResult onItemUse(ItemUsageContext context) {
        World w = context.getWorld();
        ItemStack item = context.getStack();
        BlockPos pos = context.getBlockPos();
        Direction side = context.getSide();
        PlayerEntity p = context.getPlayer();
        boolean tryBoth = false;
        if (p == null) {
            if (w.isClient) {
                return ActionResult.FAIL;
            }
            p = FakePlayer.getOrCreate((ServerWorld) w);
            // Fake players cannot crouch and we cannot communicate whether they want to
            // heat or cool
            tryBoth = true;
        }

        if (this.getAECurrentPower(item) > 1600) {
            if (!p.canPlaceOn(pos, side, item)) {
                return ActionResult.FAIL;
            }

            final Block block = w.getBlockState(pos).getBlock();
            final Fluid fluid = w.getFluidState(pos).getFluid();

            if (tryBoth || p.isInSneakingPose()) {
                if (this.canCool(block, fluid)) {
                    this.extractAEPower(item, 1600, Actionable.MODULATE);
                    this.cool(block, fluid, w, pos);
                    return ActionResult.SUCCESS;
                }
            }
            if (tryBoth || !p.isInSneakingPose()) {
                if (block instanceof TntBlock) {
                    TntAccessor.callPrimeTnt(w, pos, p);
                    w.removeBlock(pos, false);
                    return ActionResult.SUCCESS;
                }

                if (block instanceof TinyTNTBlock) {
                    w.removeBlock(pos, false);
                    ((TinyTNTBlock) block).startFuse(w, pos, p);
                    return ActionResult.SUCCESS;
                }

                if (this.canHeat(block, fluid)) {
                    this.extractAEPower(item, 1600, Actionable.MODULATE);
                    this.heat(block, fluid, w, pos);
                    return ActionResult.SUCCESS;
                }

                final ItemStack[] stack = Platform.getBlockDrops(w, pos);
                final List<ItemStack> out = new ArrayList<>();
                boolean hasFurnaceable = false;
                boolean canFurnaceable = true;

                for (final ItemStack i : stack) {
                    CraftingInventory tempInv = new CraftingInventory(new ContainerNull(), 1, 1);
                    tempInv.setStack(0, i);
                    Optional<SmeltingRecipe> recipe = w.getRecipeManager().getFirstMatch(RecipeType.SMELTING, tempInv, w);

                    if (recipe.isPresent()) {
                        ItemStack result = recipe.get().craft(tempInv);
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
                            RANDOM.nextFloat() * 0.4F + 0.8F);

                    if (or.getBlockState() == null) {
                        w.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    } else {
                        w.setBlockState(pos, or.getBlockState(), 3);
                    }

                    if (or.getDrops() != null) {
                        Platform.spawnDrops(w, pos, or.getDrops());
                    }

                    return ActionResult.SUCCESS;
                } else {
                    final BlockPos offsetPos = pos.offset(side);

                    if (!p.canPlaceOn(offsetPos, side, item)) {
                        return ActionResult.FAIL;
                    }

                    if (w.isAir(offsetPos)) {
                        this.extractAEPower(item, 1600, Actionable.MODULATE);
                        w.playSound(p, offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D,
                                SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1.0F,
                                RANDOM.nextFloat() * 0.4F + 0.8F);
                        w.setBlockState(offsetPos, Blocks.FIRE.getDefaultState());
                    }

                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }
}
