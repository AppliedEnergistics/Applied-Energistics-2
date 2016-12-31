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

package appeng.items.parts;


import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.parts.automation.PartAnnihilationPlane;
import appeng.parts.automation.PartExportBus;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.automation.PartIdentityAnnihilationPlane;
import appeng.parts.automation.PartImportBus;
import appeng.parts.automation.PartLevelEmitter;
import appeng.parts.misc.PartCableAnchor;
import appeng.parts.misc.PartInterface;
import appeng.parts.misc.PartInvertedToggleBus;
import appeng.parts.misc.PartStorageBus;
import appeng.parts.misc.PartToggleBus;
import appeng.parts.networking.PartCableCovered;
import appeng.parts.networking.PartCableGlass;
import appeng.parts.networking.PartCableSmart;
import appeng.parts.networking.PartDenseCable;
import appeng.parts.networking.PartQuartzFiber;
import appeng.parts.p2p.PartP2PFluids;
import appeng.parts.p2p.PartP2PIC2Power;
import appeng.parts.p2p.PartP2PItems;
import appeng.parts.p2p.PartP2PLight;
import appeng.parts.p2p.PartP2PRedstone;
import appeng.parts.p2p.PartP2PTunnelME;
import appeng.parts.reporting.PartConversionMonitor;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartDarkPanel;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.parts.reporting.PartPanel;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartSemiDarkPanel;
import appeng.parts.reporting.PartStorageMonitor;
import appeng.parts.reporting.PartTerminal;
import appeng.util.Platform;


public enum PartType
{
	InvalidType( -1, "invalid", EnumSet.of( AEFeature.CORE ), EnumSet.noneOf( IntegrationType.class ), null ),

	CableGlass( 0, "cable_glass", EnumSet.of( AEFeature.GLASS_CABLES ), EnumSet.noneOf( IntegrationType.class ), PartCableGlass.class )
	{
		@Override
		public boolean isCable()
		{
			return true;
		}

		@Override
		@SideOnly( Side.CLIENT )
		protected List<ModelResourceLocation> createItemModels( String baseName )
		{
			return Arrays.stream( AEColor.values() )
					.map( color -> modelFromBaseName( baseName + "_" + color.name().toLowerCase() ) )
					.collect( Collectors.toList() );
		}
	},

	CableCovered( 20, "cable_covered", EnumSet.of( AEFeature.COVERED_CABLES ), EnumSet.noneOf( IntegrationType.class ), PartCableCovered.class )
	{
		@Override
		public boolean isCable()
		{
			return true;
		}

		@Override
		@SideOnly( Side.CLIENT )
		protected List<ModelResourceLocation> createItemModels( String baseName )
		{
			return Arrays.stream( AEColor.values() )
					.map( color -> modelFromBaseName( baseName + "_" + color.name().toLowerCase() ) )
					.collect( Collectors.toList() );
		}
	},

	CableSmart( 40, "cable_smart", EnumSet.of( AEFeature.CHANNELS, AEFeature.SMART_CABLES ), EnumSet.noneOf( IntegrationType.class ), PartCableSmart.class )
	{
		@Override
		public boolean isCable()
		{
			return true;
		}

		@Override
		@SideOnly( Side.CLIENT )
		protected List<ModelResourceLocation> createItemModels( String baseName )
		{
			return Arrays.stream( AEColor.values() )
					.map( color -> modelFromBaseName( baseName + "_" + color.name().toLowerCase() ) )
					.collect( Collectors.toList() );
		}
	},

	CableDense( 60, "cable_dense", EnumSet.of( AEFeature.CHANNELS, AEFeature.DENSE_CABLES ), EnumSet.noneOf( IntegrationType.class ), PartDenseCable.class )
	{
		@Override
		public boolean isCable()
		{
			return true;
		}

		@Override
		@SideOnly( Side.CLIENT )
		protected List<ModelResourceLocation> createItemModels( String baseName )
		{
			return Arrays.stream( AEColor.values() )
					.map( color -> modelFromBaseName( baseName + "_" + color.name().toLowerCase() ) )
					.collect( Collectors.toList() );
		}
	},

	ToggleBus( 80, "toggle_bus", EnumSet.of( AEFeature.TOGGLE_BUS ), EnumSet.noneOf( IntegrationType.class ), PartToggleBus.class ),

	InvertedToggleBus( 100, "inverted_toggle_bus", EnumSet.of( AEFeature.TOGGLE_BUS ), EnumSet.noneOf( IntegrationType.class ), PartInvertedToggleBus.class ),

	CableAnchor( 120, "cable_anchor", EnumSet.of( AEFeature.CABLE_ANCHOR ), EnumSet.noneOf( IntegrationType.class ), PartCableAnchor.class ),

	QuartzFiber( 140, "quartz_fiber", EnumSet.of( AEFeature.QUARTZ_FIBER ), EnumSet.noneOf( IntegrationType.class ), PartQuartzFiber.class ),

	Monitor( 160, "monitor", EnumSet.of( AEFeature.PANELS ), EnumSet.noneOf( IntegrationType.class ), PartPanel.class ),

	SemiDarkMonitor( 180, "semi_dark_monitor", EnumSet.of( AEFeature.PANELS ), EnumSet.noneOf( IntegrationType.class ), PartSemiDarkPanel.class ),

	DarkMonitor( 200, "dark_monitor", EnumSet.of( AEFeature.PANELS ), EnumSet.noneOf( IntegrationType.class ), PartDarkPanel.class ),

	StorageBus( 220, "storage_bus", EnumSet.of( AEFeature.STORAGE_BUS ), EnumSet.noneOf( IntegrationType.class ), PartStorageBus.class ),

	ImportBus( 240, "import_bus", EnumSet.of( AEFeature.IMPORT_BUS ), EnumSet.noneOf( IntegrationType.class ), PartImportBus.class ),

	ExportBus( 260, "export_bus", EnumSet.of( AEFeature.EXPORT_BUS ), EnumSet.noneOf( IntegrationType.class ), PartExportBus.class ),

	LevelEmitter( 280, "level_emitter", EnumSet.of( AEFeature.LEVEL_EMITTER ), EnumSet.noneOf( IntegrationType.class ), PartLevelEmitter.class ),

	AnnihilationPlane( 300, "annihilation_plane", EnumSet.of( AEFeature.ANNIHILATION_PLANE ), EnumSet
			.noneOf( IntegrationType.class ), PartAnnihilationPlane.class ),

	IdentityAnnihilationPlane( 301, "identity_annihilation_plane", EnumSet.of( AEFeature.ANNIHILATION_PLANE, AEFeature.IDENTITY_ANNIHILATION_PLANE ), EnumSet
			.noneOf( IntegrationType.class ), PartIdentityAnnihilationPlane.class ),

	FormationPlane( 320, "formation_plane", EnumSet.of( AEFeature.FORMATION_PLANE ), EnumSet.noneOf( IntegrationType.class ), PartFormationPlane.class ),

	PatternTerminal( 340, "pattern_terminal", EnumSet.of( AEFeature.PATTERNS ), EnumSet.noneOf( IntegrationType.class ), PartPatternTerminal.class ),

	CraftingTerminal( 360, "crafting_terminal", EnumSet.of( AEFeature.CRAFTING_TERMINAL ), EnumSet.noneOf( IntegrationType.class ), PartCraftingTerminal.class ),

	Terminal( 380, "terminal", EnumSet.of( AEFeature.TERMINAL ), EnumSet.noneOf( IntegrationType.class ), PartTerminal.class ),

	StorageMonitor( 400, "storage_monitor", EnumSet.of( AEFeature.STORAGE_MONITOR ), EnumSet.noneOf( IntegrationType.class ), PartStorageMonitor.class ),

	ConversionMonitor( 420, "conversion_monitor", EnumSet.of( AEFeature.PART_CONVERSION_MONITOR ), EnumSet
			.noneOf( IntegrationType.class ), PartConversionMonitor.class ),

	Interface( 440, "interface", EnumSet.of( AEFeature.INTERFACE ), EnumSet.noneOf( IntegrationType.class ), PartInterface.class ),

	P2PTunnelME( 460, "p2p_tunnel_me", EnumSet.of( AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_ME ), EnumSet
			.noneOf( IntegrationType.class ), PartP2PTunnelME.class, GuiText.METunnel )
	{
		@Override
		String getUnlocalizedName()
		{
			return "P2PTunnel";
		}
	},

	P2PTunnelRedstone( 461, "p2p_tunnel_redstone", EnumSet.of( AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_REDSTONE ), EnumSet
			.noneOf( IntegrationType.class ), PartP2PRedstone.class, GuiText.RedstoneTunnel )
	{
		@Override
		String getUnlocalizedName()
		{
			return "P2PTunnel";
		}
	},

	P2PTunnelItems( 462, "p2p_tunnel_items", EnumSet.of( AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_ITEMS ), EnumSet
			.noneOf( IntegrationType.class ), PartP2PItems.class, GuiText.ItemTunnel )
	{
		@Override
		String getUnlocalizedName()
		{
			return "P2PTunnel";
		}
	},

	P2PTunnelFluids( 463, "p2p_tunnel_fluids", EnumSet.of( AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_FLUIDS ), EnumSet
			.noneOf( IntegrationType.class ), PartP2PFluids.class, GuiText.FluidTunnel )
	{
		@Override
		String getUnlocalizedName()
		{
			return "P2PTunnel";
		}
	},

	P2PTunnelEU( 465, "p2p_tunnel_ic2", EnumSet.of( AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_EU ), EnumSet
			.of( IntegrationType.IC2 ), PartP2PIC2Power.class, GuiText.EUTunnel )
	{
		@Override
		String getUnlocalizedName()
		{
			return "P2PTunnel";
		}
	},

	// P2PTunnelRF( 466, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelRF ), EnumSet.of( IntegrationType.RF ),
	// PartP2PRFPower.class, GuiText.RFTunnel ),

	P2PTunnelLight( 467, "p2p_tunnel_light", EnumSet.of( AEFeature.P2P_TUNNEL, AEFeature.P2P_TUNNEL_LIGHT ), EnumSet
			.noneOf( IntegrationType.class ), PartP2PLight.class, GuiText.LightTunnel )
	{
		@Override
		String getUnlocalizedName()
		{
			return "P2PTunnel";
		}
	},

	// P2PTunnelOpenComputers( 468, EnumSet.of( AEFeature.P2PTunnel, AEFeature.P2PTunnelOpenComputers ), EnumSet.of(
	// IntegrationType.OpenComputers ), PartP2POpenComputers.class, GuiText.OCTunnel ),

	InterfaceTerminal( 480, "interface_terminal", EnumSet.of( AEFeature.INTERFACE_TERMINAL ), EnumSet
			.noneOf( IntegrationType.class ), PartInterfaceTerminal.class );

	private final int baseDamage;
	private final Set<AEFeature> features;
	private final Set<IntegrationType> integrations;
	private final Class<? extends IPart> myPart;
	private final GuiText extraName;
	@SideOnly( Side.CLIENT )
	private List<ModelResourceLocation> itemModels;
	private final Set<ResourceLocation> models;
	private final boolean enabled;
	private Constructor<? extends IPart> constructor;

	PartType( final int baseMetaValue, final String itemModel, final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c )
	{
		this( baseMetaValue, itemModel, features, integrations, c, null );
	}

	PartType( final int baseMetaValue, final String itemModel, final Set<AEFeature> features, final Set<IntegrationType> integrations, final Class<? extends IPart> c, final GuiText en )
	{
		this.baseDamage = baseMetaValue;
		this.features = Collections.unmodifiableSet( features );
		this.integrations = Collections.unmodifiableSet( integrations );
		this.myPart = c;
		this.extraName = en;

		// The part is enabled if all features + integrations it needs are enabled
		this.enabled = features.stream().allMatch( AEConfig.instance()::isFeatureEnabled ) && integrations.stream()
				.allMatch( IntegrationRegistry.INSTANCE::isEnabled );

		if( enabled )
		{
			// Only load models if the part is enabled, otherwise we also run into class-loading issues while
			// scanning for annotations
			if( Platform.isClientInstall() )
			{
				this.itemModels = createItemModels( itemModel );
			}
			if( c != null )
			{
				this.models = new HashSet<>( PartModelsHelper.createModels( c ) );
			}
			else
			{
				this.models = Collections.emptySet();
			}
		}
		else
		{
			if( Platform.isClientInstall() )
			{
				this.itemModels = Collections.emptyList();
			}
			this.models = Collections.emptySet();
		}
	}

	@SideOnly( Side.CLIENT )
	protected List<ModelResourceLocation> createItemModels( String baseName )
	{
		return ImmutableList.of( modelFromBaseName( baseName ) );
	}

	@SideOnly( Side.CLIENT )
	private static ModelResourceLocation modelFromBaseName( String baseName )
	{
		return new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, "part/" + baseName ), "inventory" );
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	int getBaseDamage()
	{
		return this.baseDamage;
	}

	public boolean isCable()
	{
		return false;
	}

	Set<AEFeature> getFeature()
	{
		return this.features;
	}

	Set<IntegrationType> getIntegrations()
	{
		return this.integrations;
	}

	Class<? extends IPart> getPart()
	{
		return this.myPart;
	}

	String getUnlocalizedName()
	{
		return name();
	}

	GuiText getExtraName()
	{
		return this.extraName;
	}

	Constructor<? extends IPart> getConstructor()
	{
		return this.constructor;
	}

	void setConstructor( final Constructor<? extends IPart> constructor )
	{
		this.constructor = constructor;
	}

	@SideOnly( Side.CLIENT )
	public List<ModelResourceLocation> getItemModels()
	{
		return itemModels;
	}

	public Set<ResourceLocation> getModels()
	{
		return models;
	}

}
