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

package appeng.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import appeng.api.orientation.IOrientableBlock;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.hooks.WrenchHook;

public abstract class AEBaseBlock extends Block implements IOrientableBlock {

    protected AEBaseBlock(Properties props) {
        super(props);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.none();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        for (var property : getOrientationStrategy().getProperties()) {
            builder.add(property);
        }
    }

    /**
     * Utility function to create block properties with some sensible defaults for AE blocks.
     */
    public static Properties defaultProps(MapColor mapColor, SoundType soundType) {
        return Properties.of()
                // These values previously were encoded in AEBaseBlock
                .strength(2.2f, 11.f)
                .mapColor(mapColor)
                .sound(soundType);
    }

    public static Properties stoneProps() {
        return defaultProps(MapColor.STONE, SoundType.STONE).forceSolidOn();
    }

    public static Properties metalProps() {
        return defaultProps(MapColor.METAL, SoundType.METAL).forceSolidOn();
    }

    public static Properties glassProps() {
        return defaultProps(MapColor.NONE, SoundType.GLASS);
    }

    public static Properties fixtureProps() {
        return defaultProps(MapColor.METAL, SoundType.GLASS)
                .noCollission()
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY);
    }

    public void addToMainCreativeTab(CreativeModeTab.Output output) {
        output.accept(this);
    }

    @Override
    public String toString() {
        String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        var id = BuiltInRegistries.BLOCK.getKey(this);
        return id != BuiltInRegistries.BLOCK.getDefaultKey() ? id : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = this.defaultBlockState();
        return getOrientationStrategy().getStateForPlacement(state, context);
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        // Suppress break particles & sound when being disassembled by a wrench
        if (!WrenchHook.isDisassembling()) {
            super.spawnDestroyParticles(level, player, pos, state);
        }
    }
}
