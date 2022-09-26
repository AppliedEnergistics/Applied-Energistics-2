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
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.implementations.items.IGrowableCrystal;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.Tooltips;
import appeng.entity.GrowingCrystalEntity;
import appeng.items.AEBaseItem;

/**
 * This item represents one of the seeds used to grow various forms of quartz by throwing them into water (for that
 * behavior, see the linked entity)
 */
public class CrystalSeedItem extends AEBaseItem implements IGrowableCrystal {

    /**
     * Name of NBT tag used to store the growth progress value.
     */
    public static final String TAG_GROWTH_TICKS = "p";

    public static final String TAG_PREVENT_MAGNET = "PreventRemoteMovement";

    /**
     * The number of growth ticks required to finish growing in minecraft ticks 24000 ticks equals one minecraft day or
     * 20 minutes.
     */
    public static final int GROWTH_TICKS_REQUIRED = 24000;

    /**
     * The item to convert to, when growth finishes.
     */
    private final ItemLike grownItem;

    public CrystalSeedItem(Item.Properties properties, ItemLike grownItem) {
        super(properties);
        this.grownItem = Objects.requireNonNull(grownItem);
    }

    @Nullable
    @Override
    public ItemStack triggerGrowth(ItemStack is) {
        final int growthTicks = getGrowthTicks(is) + 1;
        if (growthTicks >= GROWTH_TICKS_REQUIRED) {
            return new ItemStack(grownItem, is.getCount());
        } else {
            setGrowthTicks(is, growthTicks);
            return is;
        }
    }

    public static int getGrowthTicks(ItemStack is) {
        CompoundTag tag = is.getTag();
        return tag != null ? tag.getInt(TAG_GROWTH_TICKS) : 0;
    }

    public static void setGrowthTicks(ItemStack is, int ticks) {
        ticks = Mth.clamp(ticks, 0, GROWTH_TICKS_REQUIRED);
        is.getOrCreateTag().putInt(TAG_GROWTH_TICKS, ticks);
    }

    @Override
    public float getMultiplier(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {

        // Check for the improved fluid tag and return the improved multiplier
        var improvedFluidTag = AEConfig.instance().getImprovedFluidTag();
        if (improvedFluidTag != null) {
            if (state.getFluidState().is(improvedFluidTag)) {
                return AEConfig.instance().getImprovedFluidMultiplier();
            }
        }

        // Check for the normal supported fluid
        if (level != null && level.dimension() == Level.NETHER) {
            // In the nether, use Lava as the "normal" fluid
            return state.getFluidState().is(FluidTags.LAVA) ? 1 : 0;
        } else {
            return state.getFluidState().is(FluidTags.WATER) ? 1 : 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {

        lines.add(Tooltips.of(ButtonToolTips.DoesntDespawn));
        lines.add(Tooltips.ofPercent((double) getGrowthTicks(stack) / GROWTH_TICKS_REQUIRED));

        super.appendHoverText(stack, level, lines, advancedTooltips);
    }

    @Override
    public int getEntityLifespan(final ItemStack itemStack, final Level level) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean hasCustomEntity(final ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(final Level level, final Entity location, final ItemStack itemstack) {
        final GrowingCrystalEntity egc = new GrowingCrystalEntity(level, location.getX(), location.getY(),
                location.getZ(), itemstack);

        egc.setDeltaMovement(location.getDeltaMovement());

        // Cannot read the pickup delay of the original item, so we
        // use the pickup delay used for items dropped by a player instead
        egc.setPickUpDelay(40);
        egc.getPersistentData().putBoolean(TAG_PREVENT_MAGNET, true);

        return egc;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            // lvl 0
            items.add(new ItemStack(this, 1));
            // one tick before maturity
            ItemStack almostFullGrown = new ItemStack(this, 1);
            setGrowthTicks(almostFullGrown, GROWTH_TICKS_REQUIRED - 1);
            items.add(almostFullGrown);
        }
    }

}
