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

package appeng.items.misc;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.implementations.items.IGrowableCrystal;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.entity.GrowingCrystalEntity;
import appeng.items.AEBaseItem;

/**
 * This item reprents one of the seeds used to grow various forms of quartz by
 * throwing them into water (for that behavior, see the linked entity)
 */
public class CrystalSeedItem extends AEBaseItem implements IGrowableCrystal {

    /**
     * Name of NBT tag used to store the growth progress value.
     */
    private static final String TAG_GROWTH_TICKS = "p";

    /**
     * The number of growth ticks required to finish growing.
     */
    public static final int GROWTH_TICKS_REQUIRED = 600;

    /**
     * The item to convert to, when growth finishes.
     */
    private final IItemProvider grownItem;

    public CrystalSeedItem(Properties properties, IItemProvider grownItem) {
        super(properties);
        this.grownItem = Preconditions.checkNotNull(grownItem);
    }

    @Nullable
    @Override
    public ItemStack triggerGrowth(final ItemStack is) {
        final int growthTicks = getGrowthTicks(is) + 1;
        if (growthTicks >= GROWTH_TICKS_REQUIRED) {
            return new ItemStack(grownItem, is.getCount());
        } else {
            this.setGrowthTicks(is, growthTicks);
            return is;
        }
    }

    public static int getGrowthTicks(final ItemStack is) {
        CompoundNBT tag = is.getTag();
        return tag != null ? tag.getInt(TAG_GROWTH_TICKS) : 0;
    }

    private void setGrowthTicks(final ItemStack is, int ticks) {
        ticks = MathHelper.clamp(ticks, 0, GROWTH_TICKS_REQUIRED);
        is.getOrCreateTag().putInt(TAG_GROWTH_TICKS, ticks);
    }

    @Override
    public float getMultiplier(BlockState state, @Nullable World world, @Nullable BlockPos pos) {

        // Check for the improved fluid tag and return the improved multiplier
        String improvedFluidTagName = AEConfig.instance().getImprovedFluidTag();
        if (improvedFluidTagName != null) {
            ITag<Fluid> tag = FluidTags.getCollection().get(new ResourceLocation(improvedFluidTagName));
            if (tag != null && state.getFluidState().isTagged(tag)) {
                return AEConfig.instance().getImprovedFluidMultiplier();
            }
        }

        // Check for the normal supported fluid
        if (world != null && world.getDimensionKey() == World.THE_NETHER) {
            // In the nether, use Lava as the "normal" fluid
            return state.getFluidState().isTagged(FluidTags.LAVA) ? 1 : 0;
        } else {
            return state.getFluidState().isTagged(FluidTags.WATER) ? 1 : 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        lines.add(ButtonToolTips.DoesntDespawn.text());
        lines.add(getGrowthTooltipItem(stack));

        super.addInformation(stack, world, lines, advancedTooltips);
    }

    public ITextComponent getGrowthTooltipItem(ItemStack stack) {
        final int progress = getGrowthTicks(stack);
        return new StringTextComponent(Math.round(100 * progress / (float) GROWTH_TICKS_REQUIRED) + "%");
    }

    @Override
    public int getEntityLifespan(final ItemStack itemStack, final World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean hasCustomEntity(final ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(final World world, final Entity location, final ItemStack itemstack) {
        final GrowingCrystalEntity egc = new GrowingCrystalEntity(world, location.getPosX(), location.getPosY(),
                location.getPosZ(), itemstack);

        egc.setMotion(location.getMotion());

        // Cannot read the pickup delay of the original item, so we
        // use the pickup delay used for items dropped by a player instead
        egc.setPickupDelay(40);

        return egc;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            // lvl 0
            items.add(new ItemStack(this, 1));
            // one tick before maturity
            ItemStack almostFullGrown = new ItemStack(this, 1);
            setGrowthTicks(almostFullGrown, GROWTH_TICKS_REQUIRED - 1);
            items.add(almostFullGrown);
        }
    }

}
