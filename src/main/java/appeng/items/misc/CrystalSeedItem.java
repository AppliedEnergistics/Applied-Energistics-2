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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import appeng.api.implementations.items.IGrowableCrystal;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.entity.GrowingCrystalEntity;
import appeng.hooks.AECustomEntityItem;
import appeng.items.AEBaseItem;

/**
 * This item reprents one of the seeds used to grow various forms of quartz by throwing them into water (for that
 * behavior, see the linked entity)
 */
public class CrystalSeedItem extends AEBaseItem implements IGrowableCrystal, AECustomEntityItem {

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
    private final ItemLike grownItem;

    public CrystalSeedItem(Item.Properties properties, ItemLike grownItem) {
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
        CompoundTag tag = is.getTag();
        return tag != null ? tag.getInt(TAG_GROWTH_TICKS) : 0;
    }

    private void setGrowthTicks(final ItemStack is, int ticks) {
        ticks = Mth.clamp(ticks, 0, GROWTH_TICKS_REQUIRED);
        is.getOrCreateTag().putInt(TAG_GROWTH_TICKS, ticks);
    }

    @Override
    public float getMultiplier(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {

        // Check for the improved fluid tag and return the improved multiplier
        String improvedFluidTagName = AEConfig.instance().getImprovedFluidTag();
        if (improvedFluidTagName != null) {
            Tag<Fluid> tag = FluidTags.getAllTags().getTag(new ResourceLocation(improvedFluidTagName));
            if (tag != null && state.getFluidState().is(tag)) {
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
    @Environment(EnvType.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        lines.add(ButtonToolTips.DoesntDespawn.text());
        lines.add(getGrowthTooltipItem(stack));

        super.appendHoverText(stack, level, lines, advancedTooltips);
    }

    public Component getGrowthTooltipItem(ItemStack stack) {
        final int progress = getGrowthTicks(stack);
        return new TextComponent(Math.round(100 * progress / (float) GROWTH_TICKS_REQUIRED) + "%");
    }

    @Override
    public Entity replaceItemEntity(ServerLevel level, ItemEntity location, ItemStack itemStack) {
        var egc = new GrowingCrystalEntity(level, location.getX(), location.getY(),
                location.getZ(), itemStack);

        egc.setDeltaMovement(location.getDeltaMovement());

        // Cannot read the pickup delay of the original item, so we
        // use the pickup delay used for items dropped by a player instead
        egc.setPickUpDelay(40);

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
