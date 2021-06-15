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

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import appeng.api.definitions.IItemDefinition;
import appeng.entity.ChargedQuartzEntity;
import appeng.entity.SingularityEntity;
import appeng.items.materials.MaterialItem;
import appeng.items.materials.MaterialType;

/**
 * Internal implementation for the API materials
 */
public final class ApiMaterials {
    private static final IItemDefinition cell2SpatialPart;
    private static final IItemDefinition cell16SpatialPart;
    private static final IItemDefinition cell128SpatialPart;

    private static final IItemDefinition silicon;
    private static final IItemDefinition skyDust;

    private static final IItemDefinition calcProcessorPress;
    private static final IItemDefinition engProcessorPress;
    private static final IItemDefinition logicProcessorPress;

    private static final IItemDefinition calcProcessorPrint;
    private static final IItemDefinition engProcessorPrint;
    private static final IItemDefinition logicProcessorPrint;

    private static final IItemDefinition siliconPress;
    private static final IItemDefinition siliconPrint;

    private static final IItemDefinition namePress;

    private static final IItemDefinition logicProcessor;
    private static final IItemDefinition calcProcessor;
    private static final IItemDefinition engProcessor;

    private static final IItemDefinition basicCard;
    private static final IItemDefinition advCard;

    private static final IItemDefinition purifiedCertusQuartzCrystal;
    private static final IItemDefinition purifiedNetherQuartzCrystal;
    private static final IItemDefinition purifiedFluixCrystal;

    private static final IItemDefinition cell1kPart;
    private static final IItemDefinition cell4kPart;
    private static final IItemDefinition cell16kPart;
    private static final IItemDefinition cell64kPart;
    private static final IItemDefinition emptyStorageCell;

    private static final IItemDefinition cardRedstone;
    private static final IItemDefinition cardSpeed;
    private static final IItemDefinition cardCapacity;
    private static final IItemDefinition cardFuzzy;
    private static final IItemDefinition cardInverter;
    private static final IItemDefinition cardCrafting;

    private static final IItemDefinition enderDust;
    private static final IItemDefinition flour;
    private static final IItemDefinition goldDust;
    private static final IItemDefinition ironDust;
    private static final IItemDefinition fluixDust;
    private static final IItemDefinition certusQuartzDust;
    private static final IItemDefinition netherQuartzDust;

    private static final IItemDefinition matterBall;

    private static final IItemDefinition certusQuartzCrystal;
    private static final IItemDefinition certusQuartzCrystalCharged;
    private static final IItemDefinition fluixCrystal;
    private static final IItemDefinition fluixPearl;

    private static final IItemDefinition woodenGear;

    private static final IItemDefinition wirelessReceiver;
    private static final IItemDefinition wirelessBooster;

    private static final IItemDefinition annihilationCore;
    private static final IItemDefinition formationCore;

    private static final IItemDefinition singularity;
    private static final IItemDefinition qESingularity;
    private static final IItemDefinition blankPattern;

    private static final IItemDefinition fluidCell1kPart;
    private static final IItemDefinition fluidCell4kPart;
    private static final IItemDefinition fluidCell16kPart;
    private static final IItemDefinition fluidCell64kPart;

    static {
        cell2SpatialPart = createMaterial(MaterialType.SPATIAL_2_CELL_COMPONENT);
        cell16SpatialPart = createMaterial(MaterialType.SPATIAL_16_CELL_COMPONENT);
        cell128SpatialPart = createMaterial(MaterialType.SPATIAL_128_CELL_COMPONENT);
        silicon = createMaterial(MaterialType.SILICON);
        skyDust = createMaterial(MaterialType.SKY_DUST);
        calcProcessorPress = createMaterial(MaterialType.CALCULATION_PROCESSOR_PRESS);
        engProcessorPress = createMaterial(MaterialType.ENGINEERING_PROCESSOR_PRESS);
        logicProcessorPress = createMaterial(MaterialType.LOGIC_PROCESSOR_PRESS);
        siliconPress = createMaterial(MaterialType.SILICON_PRESS);
        namePress = createMaterial(MaterialType.NAME_PRESS);
        calcProcessorPrint = createMaterial(MaterialType.CALCULATION_PROCESSOR_PRINT);
        engProcessorPrint = createMaterial(MaterialType.ENGINEERING_PROCESSOR_PRINT);
        logicProcessorPrint = createMaterial(MaterialType.LOGIC_PROCESSOR_PRINT);
        siliconPrint = createMaterial(MaterialType.SILICON_PRINT);
        logicProcessor = createMaterial(MaterialType.LOGIC_PROCESSOR);
        calcProcessor = createMaterial(MaterialType.CALCULATION_PROCESSOR);
        engProcessor = createMaterial(MaterialType.ENGINEERING_PROCESSOR);
        basicCard = createMaterial(MaterialType.BASIC_CARD);
        advCard = createMaterial(MaterialType.ADVANCED_CARD);
        purifiedCertusQuartzCrystal = createMaterial(MaterialType.PURIFIED_CERTUS_QUARTZ_CRYSTAL);
        purifiedNetherQuartzCrystal = createMaterial(MaterialType.PURIFIED_NETHER_QUARTZ_CRYSTAL);
        purifiedFluixCrystal = createMaterial(MaterialType.PURIFIED_FLUIX_CRYSTAL);
        cell1kPart = createMaterial(MaterialType.ITEM_1K_CELL_COMPONENT);
        cell4kPart = createMaterial(MaterialType.ITEM_4K_CELL_COMPONENT);
        cell16kPart = createMaterial(MaterialType.ITEM_16K_CELL_COMPONENT);
        cell64kPart = createMaterial(MaterialType.ITEM_64K_CELL_COMPONENT);
        emptyStorageCell = createMaterial(MaterialType.EMPTY_STORAGE_CELL);
        cardRedstone = createMaterial(MaterialType.CARD_REDSTONE);
        cardSpeed = createMaterial(MaterialType.CARD_SPEED);
        cardCapacity = createMaterial(MaterialType.CARD_CAPACITY);
        cardFuzzy = createMaterial(MaterialType.CARD_FUZZY);
        cardInverter = createMaterial(MaterialType.CARD_INVERTER);
        cardCrafting = createMaterial(MaterialType.CARD_CRAFTING);
        enderDust = createMaterial(MaterialType.ENDER_DUST, SingularityEntity::new);
        flour = createMaterial(MaterialType.FLOUR);
        goldDust = createMaterial(MaterialType.GOLD_DUST);
        ironDust = createMaterial(MaterialType.IRON_DUST);
        fluixDust = createMaterial(MaterialType.FLUIX_DUST);
        certusQuartzDust = createMaterial(MaterialType.CERTUS_QUARTZ_DUST);
        netherQuartzDust = createMaterial(MaterialType.NETHER_QUARTZ_DUST);
        matterBall = createMaterial(MaterialType.MATTER_BALL);
        certusQuartzCrystal = createMaterial(MaterialType.CERTUS_QUARTZ_CRYSTAL);
        certusQuartzCrystalCharged = createMaterial(MaterialType.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                ChargedQuartzEntity::new);
        fluixCrystal = createMaterial(MaterialType.FLUIX_CRYSTAL);
        fluixPearl = createMaterial(MaterialType.FLUIX_PEARL);
        woodenGear = createMaterial(MaterialType.WOODEN_GEAR);
        wirelessReceiver = createMaterial(MaterialType.WIRELESS_RECEIVER);
        wirelessBooster = createMaterial(MaterialType.WIRELESS_BOOSTER);
        annihilationCore = createMaterial(MaterialType.ANNIHILATION_CORE);
        formationCore = createMaterial(MaterialType.FORMATION_CORE);
        singularity = createMaterial(MaterialType.SINGULARITY, SingularityEntity::new);
        qESingularity = createMaterial(MaterialType.QUANTUM_ENTANGLED_SINGULARITY, SingularityEntity::new);
        blankPattern = createMaterial(MaterialType.BLANK_PATTERN);
        fluidCell1kPart = createMaterial(MaterialType.FLUID_1K_CELL_COMPONENT);
        fluidCell4kPart = createMaterial(MaterialType.FLUID_4K_CELL_COMPONENT);
        fluidCell16kPart = createMaterial(MaterialType.FLUID_16K_CELL_COMPONENT);
        fluidCell64kPart = createMaterial(MaterialType.FLUID_64K_CELL_COMPONENT);
    }

    private static IItemDefinition createMaterial(final MaterialType mat) {
        return createMaterial(mat, null);
    }

    private static IItemDefinition createMaterial(final MaterialType mat,
            @Nullable MaterialItem.EntityFactory entityFactory) {
        Preconditions.checkState(!mat.isRegistered(), "Cannot create the same material twice.");
        IItemDefinition def = ApiItems.item(mat.getId(), props -> new MaterialItem(props, mat, entityFactory))
                .build();

        mat.setItemInstance(def.item());
        mat.markReady();
        return def;
    }

    public static IItemDefinition cell2SpatialPart() {
        return cell2SpatialPart;
    }

    public static IItemDefinition cell16SpatialPart() {
        return cell16SpatialPart;
    }

    public static IItemDefinition cell128SpatialPart() {
        return cell128SpatialPart;
    }

    public static IItemDefinition silicon() {
        return silicon;
    }

    public static IItemDefinition skyDust() {
        return skyDust;
    }

    public static IItemDefinition calcProcessorPress() {
        return calcProcessorPress;
    }

    public static IItemDefinition engProcessorPress() {
        return engProcessorPress;
    }

    public static IItemDefinition logicProcessorPress() {
        return logicProcessorPress;
    }

    public static IItemDefinition calcProcessorPrint() {
        return calcProcessorPrint;
    }

    public static IItemDefinition engProcessorPrint() {
        return engProcessorPrint;
    }

    public static IItemDefinition logicProcessorPrint() {
        return logicProcessorPrint;
    }

    public static IItemDefinition siliconPress() {
        return siliconPress;
    }

    public static IItemDefinition siliconPrint() {
        return siliconPrint;
    }

    public static IItemDefinition namePress() {
        return namePress;
    }

    public static IItemDefinition logicProcessor() {
        return logicProcessor;
    }

    public static IItemDefinition calcProcessor() {
        return calcProcessor;
    }

    public static IItemDefinition engProcessor() {
        return engProcessor;
    }

    public static IItemDefinition basicCard() {
        return basicCard;
    }

    public static IItemDefinition advCard() {
        return advCard;
    }

    public static IItemDefinition purifiedCertusQuartzCrystal() {
        return purifiedCertusQuartzCrystal;
    }

    public static IItemDefinition purifiedNetherQuartzCrystal() {
        return purifiedNetherQuartzCrystal;
    }

    public static IItemDefinition purifiedFluixCrystal() {
        return purifiedFluixCrystal;
    }

    public static IItemDefinition cell1kPart() {
        return cell1kPart;
    }

    public static IItemDefinition cell4kPart() {
        return cell4kPart;
    }

    public static IItemDefinition cell16kPart() {
        return cell16kPart;
    }

    public static IItemDefinition cell64kPart() {
        return cell64kPart;
    }

    public static IItemDefinition emptyStorageCell() {
        return emptyStorageCell;
    }

    public static IItemDefinition cardRedstone() {
        return cardRedstone;
    }

    public static IItemDefinition cardSpeed() {
        return cardSpeed;
    }

    public static IItemDefinition cardCapacity() {
        return cardCapacity;
    }

    public static IItemDefinition cardFuzzy() {
        return cardFuzzy;
    }

    public static IItemDefinition cardInverter() {
        return cardInverter;
    }

    public static IItemDefinition cardCrafting() {
        return cardCrafting;
    }

    public static IItemDefinition enderDust() {
        return enderDust;
    }

    public static IItemDefinition flour() {
        return flour;
    }

    public static IItemDefinition goldDust() {
        return goldDust;
    }

    public static IItemDefinition ironDust() {
        return ironDust;
    }

    public static IItemDefinition fluixDust() {
        return fluixDust;
    }

    public static IItemDefinition certusQuartzDust() {
        return certusQuartzDust;
    }

    public static IItemDefinition netherQuartzDust() {
        return netherQuartzDust;
    }

    public static IItemDefinition matterBall() {
        return matterBall;
    }

    public static IItemDefinition certusQuartzCrystal() {
        return certusQuartzCrystal;
    }

    public static IItemDefinition certusQuartzCrystalCharged() {
        return certusQuartzCrystalCharged;
    }

    public static IItemDefinition fluixCrystal() {
        return fluixCrystal;
    }

    public static IItemDefinition fluixPearl() {
        return fluixPearl;
    }

    public static IItemDefinition woodenGear() {
        return woodenGear;
    }

    public static IItemDefinition wirelessReceiver() {
        return wirelessReceiver;
    }

    public static IItemDefinition wirelessBooster() {
        return wirelessBooster;
    }

    public static IItemDefinition annihilationCore() {
        return annihilationCore;
    }

    public static IItemDefinition formationCore() {
        return formationCore;
    }

    public static IItemDefinition singularity() {
        return singularity;
    }

    public static IItemDefinition qESingularity() {
        return qESingularity;
    }

    public static IItemDefinition blankPattern() {
        return blankPattern;
    }

    public static IItemDefinition fluidCell1kPart() {
        return fluidCell1kPart;
    }

    public static IItemDefinition fluidCell4kPart() {
        return fluidCell4kPart;
    }

    public static IItemDefinition fluidCell16kPart() {
        return fluidCell16kPart;
    }

    public static IItemDefinition fluidCell64kPart() {
        return fluidCell64kPart;
    }

    // Used to control in which order static constructors are called
    public static void init() {
    }

}
