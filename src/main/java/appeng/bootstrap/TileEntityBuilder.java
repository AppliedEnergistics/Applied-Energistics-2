/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import appeng.api.features.AEFeature;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.ITileEntityRegistrationComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.AppEng;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTileEntity;
import appeng.util.Platform;

/**
 * Used to define our tile entities and all of their properties that are relevant to registering them.
 *
 * @param <T>
 */
public class TileEntityBuilder<T extends AEBaseTileEntity> {

    private final FeatureFactory factory;

    private final String registryName;

    // The tile entity class
    private final Class<T> tileClass;

    private TileEntityType<T> type;

    // The factory for creating tile entity objects
    private final Function<TileEntityType<T>, T> supplier;

    @OnlyIn(Dist.CLIENT)
    private TileEntityRendering<T> tileEntityRendering;

    private final List<Block> blocks = new ArrayList<>();

    private final EnumSet<AEFeature> features = EnumSet.noneOf(AEFeature.class);

    public TileEntityBuilder(FeatureFactory factory, String registryName, Class<T> tileClass,
            Function<TileEntityType<T>, T> supplier) {
        this.factory = factory;
        this.registryName = registryName;
        this.tileClass = tileClass;
        this.supplier = supplier;

        if (Platform.hasClientClasses()) {
            this.tileEntityRendering = new TileEntityRendering<>();
        }
    }

    public TileEntityBuilder<T> features(AEFeature... features) {
        this.features.clear();
        this.addFeatures(features);
        return this;
    }

    public TileEntityBuilder<T> addFeatures(AEFeature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    public TileEntityBuilder<T> rendering(TileEntityRenderingCustomizer<T> customizer) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> customizer.customize(tileEntityRendering));
        return this;
    }

    @SuppressWarnings("unchecked")
    public TileEntityDefinition build() {

        this.factory.addBootstrapComponent((ITileEntityRegistrationComponent) registry -> {
            if (blocks.isEmpty()) {
                throw new IllegalStateException("No blocks make use of this tile entity: " + tileClass);
            }

            Supplier<T> factory = () -> supplier.apply(type);
            type = TileEntityType.Builder.create(factory, blocks.toArray(new Block[0])).build(null);
            type.setRegistryName(AppEng.MOD_ID, registryName);
            registry.register(type);

            AEBaseTileEntity.registerTileItem(tileClass, new BlockStackSrc(blocks.get(0), ActivityState.Enabled));

            for (Block block : blocks) {
                if (block instanceof AEBaseTileBlock) {
                    AEBaseTileBlock<T> baseTileBlock = (AEBaseTileBlock<T>) block;
                    baseTileBlock.setTileEntity(tileClass, factory);
                }
            }

        });
        DistExecutor.runWhenOn(Dist.CLIENT, () -> this::buildClient);

        return new TileEntityDefinition(this::addBlock);

    }

    @OnlyIn(Dist.CLIENT)
    private void buildClient() {
        this.factory.addBootstrapComponent((IClientSetupComponent) () -> {
            if (tileEntityRendering.tileEntityRenderer != null) {
                ClientRegistry.bindTileEntityRenderer(type, tileEntityRendering.tileEntityRenderer);
            }
        });
    }

    private void addBlock(Block block) {
        Preconditions.checkState(type == null, "No more blocks can be added after registration completed.");
        this.blocks.add(block);
    }

}
