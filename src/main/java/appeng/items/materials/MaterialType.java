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

package appeng.items.materials;


import java.util.EnumSet;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.features.MaterialStackSrc;
import appeng.entity.EntityChargedQuartz;
import appeng.entity.EntityIds;
import appeng.entity.EntitySingularity;


public enum MaterialType
{
	InvalidType( -1, AEFeature.Core ),

	CertusQuartzCrystal( 0, AEFeature.Core, "crystalCertusQuartz" ),
	CertusQuartzCrystalCharged( 1, AEFeature.Core, EntityChargedQuartz.class ),

	CertusQuartzDust( 2, AEFeature.Core, "dustCertusQuartz" ),
	NetherQuartzDust( 3, AEFeature.Core, "dustNetherQuartz" ),
	Flour( 4, AEFeature.Flour, "dustWheat" ),
	GoldDust( 51, AEFeature.Core, "dustGold" ),
	IronDust( 49, AEFeature.Core, "dustIron" ),
	IronNugget( 50, AEFeature.Core, "nuggetIron" ),

	Silicon( 5, AEFeature.Core, "itemSilicon" ),
	MatterBall( 6 ),

	FluixCrystal( 7, AEFeature.Core, "crystalFluix" ),
	FluixDust( 8, AEFeature.Core, "dustFluix" ),
	FluixPearl( 9, AEFeature.Core, "pearlFluix" ),

	PurifiedCertusQuartzCrystal( 10 ),
	PurifiedNetherQuartzCrystal( 11 ),
	PurifiedFluixCrystal( 12 ),

	CalcProcessorPress( 13 ),
	EngProcessorPress( 14 ),
	LogicProcessorPress( 15 ),

	CalcProcessorPrint( 16 ),
	EngProcessorPrint( 17 ),
	LogicProcessorPrint( 18 ),

	SiliconPress( 19 ),
	SiliconPrint( 20 ),

	NamePress( 21 ),

	LogicProcessor( 22 ),
	CalcProcessor( 23 ),
	EngProcessor( 24 ),

	// Basic Cards
	BasicCard( 25 ),
	CardRedstone( 26 ),
	CardCapacity( 27 ),

	// Adv Cards
	AdvCard( 28 ),
	CardFuzzy( 29 ),
	CardSpeed( 30 ),
	CardInverter( 31 ),

	Cell2SpatialPart( 32, AEFeature.SpatialIO ),
	Cell16SpatialPart( 33, AEFeature.SpatialIO ),
	Cell128SpatialPart( 34, AEFeature.SpatialIO ),

	Cell1kPart( 35, AEFeature.StorageCells ),
	Cell4kPart( 36, AEFeature.StorageCells ),
	Cell16kPart( 37, AEFeature.StorageCells ),
	Cell64kPart( 38, AEFeature.StorageCells ),
	EmptyStorageCell( 39, AEFeature.StorageCells ),

	WoodenGear( 40, AEFeature.GrindStone, "gearWood" ),

	Wireless( 41, AEFeature.WirelessAccessTerminal ),
	WirelessBooster( 42, AEFeature.WirelessAccessTerminal ),

	FormationCore( 43 ),
	AnnihilationCore( 44 ),

	SkyDust( 45, AEFeature.Core ),

	EnderDust( 46, AEFeature.QuantumNetworkBridge, "dustEnder,dustEnderPearl", EntitySingularity.class ),
	Singularity( 47, AEFeature.QuantumNetworkBridge, EntitySingularity.class ),
	QESingularity( 48, AEFeature.QuantumNetworkBridge, EntitySingularity.class ),

	BlankPattern( 52 ),
	CardCrafting( 53 );

	private final EnumSet<AEFeature> features;
	private final ModelResourceLocation model = new ModelResourceLocation( "appliedenergistics2:ItemMaterial." + name() );
	private Item itemInstance;
	private int damageValue;
	// stack!
	private MaterialStackSrc stackSrc;
	private String oreName;
	private Class<? extends Entity> droppedEntity;
	private boolean isRegistered = false;

	MaterialType( final int metaValue )
	{
		this.setDamageValue( metaValue );
		this.features = EnumSet.of( AEFeature.Core );
	}

	MaterialType( final int metaValue, final AEFeature part )
	{
		this.setDamageValue( metaValue );
		this.features = EnumSet.of( part );
	}

	MaterialType( final int metaValue, final AEFeature part, final Class<? extends Entity> c )
	{
		this.features = EnumSet.of( part );
		this.setDamageValue( metaValue );
		this.droppedEntity = c;

		EntityRegistry.registerModEntity( this.droppedEntity, this.droppedEntity.getSimpleName(), EntityIds.get( this.droppedEntity ), AppEng.instance(), 16, 4, true );
	}

	MaterialType( final int metaValue, final AEFeature part, final String oreDictionary, final Class<? extends Entity> c )
	{
		this.features = EnumSet.of( part );
		this.setDamageValue( metaValue );
		this.oreName = oreDictionary;
		this.droppedEntity = c;
		EntityRegistry.registerModEntity( this.droppedEntity, this.droppedEntity.getSimpleName(), EntityIds.get( this.droppedEntity ), AppEng.instance(), 16, 4, true );
	}

	MaterialType( final int metaValue, final AEFeature part, final String oreDictionary )
	{
		this.features = EnumSet.of( part );
		this.setDamageValue( metaValue );
		this.oreName = oreDictionary;
	}

	public ItemStack stack( final int size )
	{
		return new ItemStack( this.getItemInstance(), size, this.getDamageValue() );
	}

	EnumSet<AEFeature> getFeature()
	{
		return this.features;
	}

	public String getOreName()
	{
		return this.oreName;
	}

	boolean hasCustomEntity()
	{
		return this.droppedEntity != null;
	}

	Class<? extends Entity> getCustomEntityClass()
	{
		return this.droppedEntity;
	}

	public boolean isRegistered()
	{
		return this.isRegistered;
	}

	void markReady()
	{
		this.isRegistered = true;
	}

	public int getDamageValue()
	{
		return this.damageValue;
	}

	void setDamageValue( final int damageValue )
	{
		this.damageValue = damageValue;
	}

	public Item getItemInstance()
	{
		return this.itemInstance;
	}

	void setItemInstance( final Item itemInstance )
	{
		this.itemInstance = itemInstance;
	}

	MaterialStackSrc getStackSrc()
	{
		return this.stackSrc;
	}

	void setStackSrc( final MaterialStackSrc stackSrc )
	{
		this.stackSrc = stackSrc;
	}

	public ModelResourceLocation getModel() {
		return model;
	}

}
