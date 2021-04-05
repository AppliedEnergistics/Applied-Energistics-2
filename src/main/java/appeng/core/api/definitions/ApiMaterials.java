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

package appeng.core.api.definitions;

import com.google.common.base.Preconditions;

import net.minecraft.entity.EntityClassification;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.bootstrap.FeatureFactory;
import appeng.core.AEConfig;
import appeng.entity.ChargedQuartzEntity;
import appeng.entity.SingularityEntity;
import appeng.items.materials.MaterialItem;
import appeng.items.materials.MaterialType;

/**
 * Internal implementation for the API materials
 */
public final class ApiMaterials implements IMaterials {
    private final IItemDefinition cell2SpatialPart;
    private final IItemDefinition cell16SpatialPart;
    private final IItemDefinition cell128SpatialPart;

    private final IItemDefinition silicon;
    private final IItemDefinition skyDust;

    private final IItemDefinition calcProcessorPress;
    private final IItemDefinition engProcessorPress;
    private final IItemDefinition logicProcessorPress;

    private final IItemDefinition calcProcessorPrint;
    private final IItemDefinition engProcessorPrint;
    private final IItemDefinition logicProcessorPrint;

    private final IItemDefinition siliconPress;
    private final IItemDefinition siliconPrint;

    private final IItemDefinition namePress;

    private final IItemDefinition logicProcessor;
    private final IItemDefinition calcProcessor;
    private final IItemDefinition engProcessor;

    private final IItemDefinition basicCard;
    private final IItemDefinition advCard;

    private final IItemDefinition purifiedCertusQuartzCrystal;
    private final IItemDefinition purifiedNetherQuartzCrystal;
    private final IItemDefinition purifiedFluixCrystal;

    private final IItemDefinition cell1kPart;
    private final IItemDefinition cell4kPart;
    private final IItemDefinition cell16kPart;
    private final IItemDefinition cell64kPart;
    private final IItemDefinition emptyStorageCell;

    private final IItemDefinition cardRedstone;
    private final IItemDefinition cardSpeed;
    private final IItemDefinition cardCapacity;
    private final IItemDefinition cardFuzzy;
    private final IItemDefinition cardInverter;
    private final IItemDefinition cardCrafting;

    private final IItemDefinition enderDust;
    private final IItemDefinition flour;
    private final IItemDefinition goldDust;
    private final IItemDefinition ironDust;
    private final IItemDefinition fluixDust;
    private final IItemDefinition certusQuartzDust;
    private final IItemDefinition netherQuartzDust;

    private final IItemDefinition matterBall;

    private final IItemDefinition certusQuartzCrystal;
    private final IItemDefinition certusQuartzCrystalCharged;
    private final IItemDefinition fluixCrystal;
    private final IItemDefinition fluixPearl;

    private final IItemDefinition woodenGear;

    private final IItemDefinition wirelessReceiver;
    private final IItemDefinition wirelessBooster;

    private final IItemDefinition annihilationCore;
    private final IItemDefinition formationCore;

    private final IItemDefinition singularity;
    private final IItemDefinition qESingularity;
    private final IItemDefinition blankPattern;

    private final IItemDefinition fluidCell1kPart;
    private final IItemDefinition fluidCell4kPart;
    private final IItemDefinition fluidCell16kPart;
    private final IItemDefinition fluidCell64kPart;

    private final FeatureFactory registry;

    public ApiMaterials(FeatureFactory registry) {
        this.registry = registry;

        SingularityEntity.TYPE = registry
                .<SingularityEntity>entity("singularity", SingularityEntity::new, EntityClassification.MISC)
                .customize(builder -> builder.size(0.2f, 0.2f).setTrackingRange(16).setUpdateInterval(4)
                        .setShouldReceiveVelocityUpdates(true))
                .build();

        ChargedQuartzEntity.TYPE = registry
                .<ChargedQuartzEntity>entity("charged_quartz", ChargedQuartzEntity::new, EntityClassification.MISC)
                .customize(builder -> builder.size(0.2f, 0.2f).setTrackingRange(16).setUpdateInterval(4)
                        .setShouldReceiveVelocityUpdates(true))
                .build();

        this.cell2SpatialPart = createMaterial(MaterialType.SPATIAL_2_CELL_COMPONENT);
        this.cell16SpatialPart = createMaterial(MaterialType.SPATIAL_16_CELL_COMPONENT);
        this.cell128SpatialPart = createMaterial(MaterialType.SPATIAL_128_CELL_COMPONENT);
        this.silicon = createMaterial(MaterialType.SILICON);
        this.skyDust = createMaterial(MaterialType.SKY_DUST);
        this.calcProcessorPress = createMaterial(MaterialType.CALCULATION_PROCESSOR_PRESS);
        this.engProcessorPress = createMaterial(MaterialType.ENGINEERING_PROCESSOR_PRESS);
        this.logicProcessorPress = createMaterial(MaterialType.LOGIC_PROCESSOR_PRESS);
        this.siliconPress = createMaterial(MaterialType.SILICON_PRESS);
        this.namePress = createMaterial(MaterialType.NAME_PRESS);
        this.calcProcessorPrint = createMaterial(MaterialType.CALCULATION_PROCESSOR_PRINT);
        this.engProcessorPrint = createMaterial(MaterialType.ENGINEERING_PROCESSOR_PRINT);
        this.logicProcessorPrint = createMaterial(MaterialType.LOGIC_PROCESSOR_PRINT);
        this.siliconPrint = createMaterial(MaterialType.SILICON_PRINT);
        this.logicProcessor = createMaterial(MaterialType.LOGIC_PROCESSOR);
        this.calcProcessor = createMaterial(MaterialType.CALCULATION_PROCESSOR);
        this.engProcessor = createMaterial(MaterialType.ENGINEERING_PROCESSOR);
        this.basicCard = createMaterial(MaterialType.BASIC_CARD);
        this.advCard = createMaterial(MaterialType.ADVANCED_CARD);
        this.purifiedCertusQuartzCrystal = createMaterial(MaterialType.PURIFIED_CERTUS_QUARTZ_CRYSTAL);
        this.purifiedNetherQuartzCrystal = createMaterial(MaterialType.PURIFIED_NETHER_QUARTZ_CRYSTAL);
        this.purifiedFluixCrystal = createMaterial(MaterialType.PURIFIED_FLUIX_CRYSTAL);
        this.cell1kPart = createMaterial(MaterialType.ITEM_1K_CELL_COMPONENT);
        this.cell4kPart = createMaterial(MaterialType.ITEM_4K_CELL_COMPONENT);
        this.cell16kPart = createMaterial(MaterialType.ITEM_16K_CELL_COMPONENT);
        this.cell64kPart = createMaterial(MaterialType.ITEM_64K_CELL_COMPONENT);
        this.emptyStorageCell = createMaterial(MaterialType.EMPTY_STORAGE_CELL);
        this.cardRedstone = createMaterial(MaterialType.CARD_REDSTONE);
        this.cardSpeed = createMaterial(MaterialType.CARD_SPEED);
        this.cardCapacity = createMaterial(MaterialType.CARD_CAPACITY);
        this.cardFuzzy = createMaterial(MaterialType.CARD_FUZZY);
        this.cardInverter = createMaterial(MaterialType.CARD_INVERTER);
        this.cardCrafting = createMaterial(MaterialType.CARD_CRAFTING);
        this.enderDust = createMaterial(MaterialType.ENDER_DUST);
        this.flour = createMaterial(MaterialType.FLOUR);
        this.goldDust = createMaterial(MaterialType.GOLD_DUST);
        this.ironDust = createMaterial(MaterialType.IRON_DUST);
        this.fluixDust = createMaterial(MaterialType.FLUIX_DUST);
        this.certusQuartzDust = createMaterial(MaterialType.CERTUS_QUARTZ_DUST);
        this.netherQuartzDust = createMaterial(MaterialType.NETHER_QUARTZ_DUST);
        this.matterBall = createMaterial(MaterialType.MATTER_BALL);
        this.certusQuartzCrystal = createMaterial(MaterialType.CERTUS_QUARTZ_CRYSTAL);
        this.certusQuartzCrystalCharged = createMaterial(MaterialType.CERTUS_QUARTZ_CRYSTAL_CHARGED);
        this.fluixCrystal = createMaterial(MaterialType.FLUIX_CRYSTAL);
        this.fluixPearl = createMaterial(MaterialType.FLUIX_PEARL);
        this.woodenGear = createMaterial(MaterialType.WOODEN_GEAR);
        this.wirelessReceiver = createMaterial(MaterialType.WIRELESS_RECEIVER);
        this.wirelessBooster = createMaterial(MaterialType.WIRELESS_BOOSTER);
        this.annihilationCore = createMaterial(MaterialType.ANNIHILATION_CORE);
        this.formationCore = createMaterial(MaterialType.FORMATION_CORE);
        this.singularity = createMaterial(MaterialType.SINGULARITY);
        this.qESingularity = createMaterial(MaterialType.QUANTUM_ENTANGLED_SINGULARITY);
        this.blankPattern = createMaterial(MaterialType.BLANK_PATTERN);
        this.fluidCell1kPart = createMaterial(MaterialType.FLUID_1K_CELL_COMPONENT);
        this.fluidCell4kPart = createMaterial(MaterialType.FLUID_4K_CELL_COMPONENT);
        this.fluidCell16kPart = createMaterial(MaterialType.FLUID_16K_CELL_COMPONENT);
        this.fluidCell64kPart = createMaterial(MaterialType.FLUID_64K_CELL_COMPONENT);
    }

    private IItemDefinition createMaterial(final MaterialType mat) {
        Preconditions.checkState(!mat.isRegistered(), "Cannot create the same material twice.");

        IItemDefinition def = registry.item(mat.getId(), props -> new MaterialItem(props, mat))
                .features(mat.getFeature().toArray(new AEFeature[0])).build();

        boolean enabled = true;

        for (final AEFeature f : mat.getFeature()) {
            enabled = enabled && AEConfig.instance().isFeatureEnabled(f);
        }

        mat.setItemInstance(def.item());
        mat.markReady();
        return def;
    }

    @Override
    public IItemDefinition cell2SpatialPart() {
        return this.cell2SpatialPart;
    }

    @Override
    public IItemDefinition cell16SpatialPart() {
        return this.cell16SpatialPart;
    }

    @Override
    public IItemDefinition cell128SpatialPart() {
        return this.cell128SpatialPart;
    }

    @Override
    public IItemDefinition silicon() {
        return this.silicon;
    }

    @Override
    public IItemDefinition skyDust() {
        return this.skyDust;
    }

    @Override
    public IItemDefinition calcProcessorPress() {
        return this.calcProcessorPress;
    }

    @Override
    public IItemDefinition engProcessorPress() {
        return this.engProcessorPress;
    }

    @Override
    public IItemDefinition logicProcessorPress() {
        return this.logicProcessorPress;
    }

    @Override
    public IItemDefinition calcProcessorPrint() {
        return this.calcProcessorPrint;
    }

    @Override
    public IItemDefinition engProcessorPrint() {
        return this.engProcessorPrint;
    }

    @Override
    public IItemDefinition logicProcessorPrint() {
        return this.logicProcessorPrint;
    }

    @Override
    public IItemDefinition siliconPress() {
        return this.siliconPress;
    }

    @Override
    public IItemDefinition siliconPrint() {
        return this.siliconPrint;
    }

    @Override
    public IItemDefinition namePress() {
        return this.namePress;
    }

    @Override
    public IItemDefinition logicProcessor() {
        return this.logicProcessor;
    }

    @Override
    public IItemDefinition calcProcessor() {
        return this.calcProcessor;
    }

    @Override
    public IItemDefinition engProcessor() {
        return this.engProcessor;
    }

    @Override
    public IItemDefinition basicCard() {
        return this.basicCard;
    }

    @Override
    public IItemDefinition advCard() {
        return this.advCard;
    }

    @Override
    public IItemDefinition purifiedCertusQuartzCrystal() {
        return this.purifiedCertusQuartzCrystal;
    }

    @Override
    public IItemDefinition purifiedNetherQuartzCrystal() {
        return this.purifiedNetherQuartzCrystal;
    }

    @Override
    public IItemDefinition purifiedFluixCrystal() {
        return this.purifiedFluixCrystal;
    }

    @Override
    public IItemDefinition cell1kPart() {
        return this.cell1kPart;
    }

    @Override
    public IItemDefinition cell4kPart() {
        return this.cell4kPart;
    }

    @Override
    public IItemDefinition cell16kPart() {
        return this.cell16kPart;
    }

    @Override
    public IItemDefinition cell64kPart() {
        return this.cell64kPart;
    }

    @Override
    public IItemDefinition emptyStorageCell() {
        return this.emptyStorageCell;
    }

    @Override
    public IItemDefinition cardRedstone() {
        return this.cardRedstone;
    }

    @Override
    public IItemDefinition cardSpeed() {
        return this.cardSpeed;
    }

    @Override
    public IItemDefinition cardCapacity() {
        return this.cardCapacity;
    }

    @Override
    public IItemDefinition cardFuzzy() {
        return this.cardFuzzy;
    }

    @Override
    public IItemDefinition cardInverter() {
        return this.cardInverter;
    }

    @Override
    public IItemDefinition cardCrafting() {
        return this.cardCrafting;
    }

    @Override
    public IItemDefinition enderDust() {
        return this.enderDust;
    }

    @Override
    public IItemDefinition flour() {
        return this.flour;
    }

    @Override
    public IItemDefinition goldDust() {
        return this.goldDust;
    }

    @Override
    public IItemDefinition ironDust() {
        return this.ironDust;
    }

    @Override
    public IItemDefinition fluixDust() {
        return this.fluixDust;
    }

    @Override
    public IItemDefinition certusQuartzDust() {
        return this.certusQuartzDust;
    }

    @Override
    public IItemDefinition netherQuartzDust() {
        return this.netherQuartzDust;
    }

    @Override
    public IItemDefinition matterBall() {
        return this.matterBall;
    }

    @Override
    public IItemDefinition certusQuartzCrystal() {
        return this.certusQuartzCrystal;
    }

    @Override
    public IItemDefinition certusQuartzCrystalCharged() {
        return this.certusQuartzCrystalCharged;
    }

    @Override
    public IItemDefinition fluixCrystal() {
        return this.fluixCrystal;
    }

    @Override
    public IItemDefinition fluixPearl() {
        return this.fluixPearl;
    }

    @Override
    public IItemDefinition woodenGear() {
        return this.woodenGear;
    }

    @Override
    public IItemDefinition wirelessReceiver() {
        return this.wirelessReceiver;
    }

    @Override
    public IItemDefinition wirelessBooster() {
        return this.wirelessBooster;
    }

    @Override
    public IItemDefinition annihilationCore() {
        return this.annihilationCore;
    }

    @Override
    public IItemDefinition formationCore() {
        return this.formationCore;
    }

    @Override
    public IItemDefinition singularity() {
        return this.singularity;
    }

    @Override
    public IItemDefinition qESingularity() {
        return this.qESingularity;
    }

    @Override
    public IItemDefinition blankPattern() {
        return this.blankPattern;
    }

    @Override
    public IItemDefinition fluidCell1kPart() {
        return this.fluidCell1kPart;
    }

    @Override
    public IItemDefinition fluidCell4kPart() {
        return this.fluidCell4kPart;
    }

    @Override
    public IItemDefinition fluidCell16kPart() {
        return this.fluidCell16kPart;
    }

    @Override
    public IItemDefinition fluidCell64kPart() {
        return this.fluidCell64kPart;
    }
}
