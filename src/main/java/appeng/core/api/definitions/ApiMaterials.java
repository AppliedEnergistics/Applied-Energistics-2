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


import appeng.core.CreativeTab;
import appeng.items.materials.ItemMaterial;
import net.minecraft.item.Item;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.bootstrap.FeatureFactory;
import appeng.items.materials.MaterialType;


/**
 * Internal implementation for the API materials
 */
public final class ApiMaterials implements IMaterials
{
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

	public ApiMaterials( FeatureFactory registry )
	{
//				.rendering( new ItemRenderingCustomizer()
//				{
//					@Override
//					@OnlyIn( Dist.CLIENT )
//					public void customize( IItemRendering rendering )
//					{
//						rendering.meshDefinition( is -> materials.getTypeByStack( is ).getModel() );
//						// Register a resource location for every material type
//						rendering.variants( Arrays.stream( MaterialType.values() )
//								.map( MaterialType::getModel )
//								.collect( Collectors.toList() ) );
//					}
//				} )
//				.bootstrap( item -> (IEntityRegistrationComponent) r ->
//				{
//					r.register( EntityEntryBuilder.create()
//							.entity( EntitySingularity.class )
//							.id( new ResourceLocation( "appliedenergistics2", EntitySingularity.class.getName() ), EntityIds.get( EntitySingularity.class ) )
//							.name( EntitySingularity.class.getSimpleName() )
//							.tracker( 16, 4, true )
//							.build() );
//					r.register( EntityEntryBuilder.create()
//							.entity( EntityChargedQuartz.class )
//							.id( new ResourceLocation( "appliedenergistics2", EntityChargedQuartz.class.getName() ),
//									EntityIds.get( EntityChargedQuartz.class ) )
//							.name( EntityChargedQuartz.class.getSimpleName() )
//							.tracker( 16, 4, true )
//							.build() );
//				} )

		Item.Properties properties = new Item.Properties()
				.group( CreativeTab.instance );

		this.cell2SpatialPart = ItemMaterial.item( registry, properties, MaterialType.CELL2_SPATIAL_PART ) ;
		this.cell16SpatialPart = ItemMaterial.item( registry, properties, MaterialType.CELL16_SPATIAL_PART ) ;
		this.cell128SpatialPart = ItemMaterial.item( registry, properties, MaterialType.CELL128_SPATIAL_PART ) ;

		this.silicon = ItemMaterial.item( registry, properties, MaterialType.SILICON ) ;
		this.skyDust = ItemMaterial.item( registry, properties, MaterialType.SKY_DUST ) ;

		this.calcProcessorPress = ItemMaterial.item( registry, properties, MaterialType.CALCULATION_PROCESSOR_PRESS ) ;
		this.engProcessorPress = ItemMaterial.item( registry, properties, MaterialType.ENGINEERING_PROCESSOR_PRESS ) ;
		this.logicProcessorPress = ItemMaterial.item( registry, properties, MaterialType.LOGIC_PROCESSOR_PRESS ) ;
		this.siliconPress = ItemMaterial.item( registry, properties, MaterialType.SILICON_PRESS ) ;
		this.namePress = ItemMaterial.item( registry, properties, MaterialType.NAME_PRESS ) ;

		this.calcProcessorPrint = ItemMaterial.item( registry, properties, MaterialType.CALCULATION_PROCESSOR_PRINT ) ;
		this.engProcessorPrint = ItemMaterial.item( registry, properties, MaterialType.ENGINEERING_PROCESSOR_PRINT ) ;
		this.logicProcessorPrint = ItemMaterial.item( registry, properties, MaterialType.LOGIC_PROCESSOR_PRINT ) ;
		this.siliconPrint = ItemMaterial.item( registry, properties, MaterialType.SILICON_PRINT ) ;

		this.logicProcessor = ItemMaterial.item( registry, properties, MaterialType.LOGIC_PROCESSOR ) ;
		this.calcProcessor = ItemMaterial.item( registry, properties, MaterialType.CALCULATION_PROCESSOR ) ;
		this.engProcessor = ItemMaterial.item( registry, properties, MaterialType.ENGINEERING_PROCESSOR ) ;

		this.basicCard = ItemMaterial.item( registry, properties, MaterialType.BASIC_CARD ) ;
		this.advCard = ItemMaterial.item( registry, properties, MaterialType.ADVANCED_CARD ) ;

		this.purifiedCertusQuartzCrystal = ItemMaterial.item( registry, properties, MaterialType.PURIFIED_CERTUS_QUARTZ_CRYSTAL ) ;
		this.purifiedNetherQuartzCrystal = ItemMaterial.item( registry, properties, MaterialType.PURIFIED_NETHER_QUARTZ_CRYSTAL ) ;
		this.purifiedFluixCrystal = ItemMaterial.item( registry, properties, MaterialType.PURIFIED_FLUIX_CRYSTAL ) ;

		this.cell1kPart = ItemMaterial.item( registry, properties, MaterialType.CELL1K_PART ) ;
		this.cell4kPart = ItemMaterial.item( registry, properties, MaterialType.CELL4K_PART ) ;
		this.cell16kPart = ItemMaterial.item( registry, properties, MaterialType.CELL16K_PART ) ;
		this.cell64kPart = ItemMaterial.item( registry, properties, MaterialType.CELL64K_PART ) ;
		this.emptyStorageCell = ItemMaterial.item( registry, properties, MaterialType.EMPTY_STORAGE_CELL ) ;

		this.cardRedstone = ItemMaterial.item( registry, properties, MaterialType.CARD_REDSTONE ) ;
		this.cardSpeed = ItemMaterial.item( registry, properties, MaterialType.CARD_SPEED ) ;
		this.cardCapacity = ItemMaterial.item( registry, properties, MaterialType.CARD_CAPACITY ) ;
		this.cardFuzzy = ItemMaterial.item( registry, properties, MaterialType.CARD_FUZZY ) ;
		this.cardInverter = ItemMaterial.item( registry, properties, MaterialType.CARD_INVERTER ) ;
		this.cardCrafting = ItemMaterial.item( registry, properties, MaterialType.CARD_CRAFTING ) ;

		this.enderDust = ItemMaterial.item( registry, properties, MaterialType.ENDER_DUST ) ;
		this.flour = ItemMaterial.item( registry, properties, MaterialType.FLOUR ) ;
		this.goldDust = ItemMaterial.item( registry, properties, MaterialType.GOLD_DUST ) ;
		this.ironDust = ItemMaterial.item( registry, properties, MaterialType.IRON_DUST ) ;
		this.fluixDust = ItemMaterial.item( registry, properties, MaterialType.FLUIX_DUST ) ;
		this.certusQuartzDust = ItemMaterial.item( registry, properties, MaterialType.CERTUS_QUARTZ_DUST ) ;
		this.netherQuartzDust = ItemMaterial.item( registry, properties, MaterialType.NETHER_QUARTZ_DUST ) ;

		this.matterBall = ItemMaterial.item( registry, properties, MaterialType.MATTER_BALL ) ;

		this.certusQuartzCrystal = ItemMaterial.item( registry, properties, MaterialType.CERTUS_QUARTZ_CRYSTAL ) ;
		this.certusQuartzCrystalCharged = ItemMaterial.item( registry, properties, MaterialType.CERTUS_QUARTZ_CRYSTAL_CHARGED ) ;
		this.fluixCrystal = ItemMaterial.item( registry, properties, MaterialType.FLUIX_CRYSTAL ) ;
		this.fluixPearl = ItemMaterial.item( registry, properties, MaterialType.FLUIX_PEARL ) ;

		this.woodenGear = ItemMaterial.item( registry, properties, MaterialType.WOODEN_GEAR ) ;

		this.wirelessReceiver = ItemMaterial.item( registry, properties, MaterialType.WIRELESS ) ;
		this.wirelessBooster = ItemMaterial.item( registry, properties, MaterialType.WIRELESS_BOOSTER ) ;

		this.annihilationCore = ItemMaterial.item( registry, properties, MaterialType.ANNIHILATION_CORE ) ;
		this.formationCore = ItemMaterial.item( registry, properties, MaterialType.FORMATION_CORE ) ;

		this.singularity = ItemMaterial.item( registry, properties, MaterialType.SINGULARITY ) ;
		this.qESingularity = ItemMaterial.item( registry, properties, MaterialType.QUANTUM_ENTANGLED_SINGULARITY ) ;
		this.blankPattern = ItemMaterial.item( registry, properties, MaterialType.BLANK_PATTERN ) ;

		this.fluidCell1kPart = ItemMaterial.item( registry, properties, MaterialType.FLUID_CELL1K_PART ) ;
		this.fluidCell4kPart = ItemMaterial.item( registry, properties, MaterialType.FLUID_CELL4K_PART ) ;
		this.fluidCell16kPart = ItemMaterial.item( registry, properties, MaterialType.FLUID_CELL16K_PART ) ;
		this.fluidCell64kPart = ItemMaterial.item( registry, properties, MaterialType.FLUID_CELL64K_PART ) ;
	}

	@Override
	public IItemDefinition cell2SpatialPart()
	{
		return this.cell2SpatialPart;
	}

	@Override
	public IItemDefinition cell16SpatialPart()
	{
		return this.cell16SpatialPart;
	}

	@Override
	public IItemDefinition cell128SpatialPart()
	{
		return this.cell128SpatialPart;
	}

	@Override
	public IItemDefinition silicon()
	{
		return this.silicon;
	}

	@Override
	public IItemDefinition skyDust()
	{
		return this.skyDust;
	}

	@Override
	public IItemDefinition calcProcessorPress()
	{
		return this.calcProcessorPress;
	}

	@Override
	public IItemDefinition engProcessorPress()
	{
		return this.engProcessorPress;
	}

	@Override
	public IItemDefinition logicProcessorPress()
	{
		return this.logicProcessorPress;
	}

	@Override
	public IItemDefinition calcProcessorPrint()
	{
		return this.calcProcessorPrint;
	}

	@Override
	public IItemDefinition engProcessorPrint()
	{
		return this.engProcessorPrint;
	}

	@Override
	public IItemDefinition logicProcessorPrint()
	{
		return this.logicProcessorPrint;
	}

	@Override
	public IItemDefinition siliconPress()
	{
		return this.siliconPress;
	}

	@Override
	public IItemDefinition siliconPrint()
	{
		return this.siliconPrint;
	}

	@Override
	public IItemDefinition namePress()
	{
		return this.namePress;
	}

	@Override
	public IItemDefinition logicProcessor()
	{
		return this.logicProcessor;
	}

	@Override
	public IItemDefinition calcProcessor()
	{
		return this.calcProcessor;
	}

	@Override
	public IItemDefinition engProcessor()
	{
		return this.engProcessor;
	}

	@Override
	public IItemDefinition basicCard()
	{
		return this.basicCard;
	}

	@Override
	public IItemDefinition advCard()
	{
		return this.advCard;
	}

	@Override
	public IItemDefinition purifiedCertusQuartzCrystal()
	{
		return this.purifiedCertusQuartzCrystal;
	}

	@Override
	public IItemDefinition purifiedNetherQuartzCrystal()
	{
		return this.purifiedNetherQuartzCrystal;
	}

	@Override
	public IItemDefinition purifiedFluixCrystal()
	{
		return this.purifiedFluixCrystal;
	}

	@Override
	public IItemDefinition cell1kPart()
	{
		return this.cell1kPart;
	}

	@Override
	public IItemDefinition cell4kPart()
	{
		return this.cell4kPart;
	}

	@Override
	public IItemDefinition cell16kPart()
	{
		return this.cell16kPart;
	}

	@Override
	public IItemDefinition cell64kPart()
	{
		return this.cell64kPart;
	}

	@Override
	public IItemDefinition emptyStorageCell()
	{
		return this.emptyStorageCell;
	}

	@Override
	public IItemDefinition cardRedstone()
	{
		return this.cardRedstone;
	}

	@Override
	public IItemDefinition cardSpeed()
	{
		return this.cardSpeed;
	}

	@Override
	public IItemDefinition cardCapacity()
	{
		return this.cardCapacity;
	}

	@Override
	public IItemDefinition cardFuzzy()
	{
		return this.cardFuzzy;
	}

	@Override
	public IItemDefinition cardInverter()
	{
		return this.cardInverter;
	}

	@Override
	public IItemDefinition cardCrafting()
	{
		return this.cardCrafting;
	}

	@Override
	public IItemDefinition enderDust()
	{
		return this.enderDust;
	}

	@Override
	public IItemDefinition flour()
	{
		return this.flour;
	}

	@Override
	public IItemDefinition goldDust()
	{
		return this.goldDust;
	}

	@Override
	public IItemDefinition ironDust()
	{
		return this.ironDust;
	}

	@Override
	public IItemDefinition fluixDust()
	{
		return this.fluixDust;
	}

	@Override
	public IItemDefinition certusQuartzDust()
	{
		return this.certusQuartzDust;
	}

	@Override
	public IItemDefinition netherQuartzDust()
	{
		return this.netherQuartzDust;
	}

	@Override
	public IItemDefinition matterBall()
	{
		return this.matterBall;
	}

	@Override
	public IItemDefinition certusQuartzCrystal()
	{
		return this.certusQuartzCrystal;
	}

	@Override
	public IItemDefinition certusQuartzCrystalCharged()
	{
		return this.certusQuartzCrystalCharged;
	}

	@Override
	public IItemDefinition fluixCrystal()
	{
		return this.fluixCrystal;
	}

	@Override
	public IItemDefinition fluixPearl()
	{
		return this.fluixPearl;
	}

	@Override
	public IItemDefinition woodenGear()
	{
		return this.woodenGear;
	}

	@Override
	public IItemDefinition wirelessReceiver()
	{
		return this.wirelessReceiver;
	}

	@Override
	public IItemDefinition wirelessBooster()
	{
		return this.wirelessBooster;
	}

	@Override
	public IItemDefinition annihilationCore()
	{
		return this.annihilationCore;
	}

	@Override
	public IItemDefinition formationCore()
	{
		return this.formationCore;
	}

	@Override
	public IItemDefinition singularity()
	{
		return this.singularity;
	}

	@Override
	public IItemDefinition qESingularity()
	{
		return this.qESingularity;
	}

	@Override
	public IItemDefinition blankPattern()
	{
		return this.blankPattern;
	}

	@Override
	public IItemDefinition fluidCell1kPart()
	{
		return this.fluidCell1kPart;
	}

	@Override
	public IItemDefinition fluidCell4kPart()
	{
		return this.fluidCell4kPart;
	}

	@Override
	public IItemDefinition fluidCell16kPart()
	{
		return this.fluidCell16kPart;
	}

	@Override
	public IItemDefinition fluidCell64kPart()
	{
		return this.fluidCell64kPart;
	}
}
