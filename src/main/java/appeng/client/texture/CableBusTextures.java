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

package appeng.client.texture;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;


public enum CableBusTextures
{

	Channels00( "MECableSmart00" ), Channels01( "MECableSmart01" ), Channels02( "MECableSmart02" ), Channels03( "MECableSmart03" ), Channels10( "MECableSmart10" ), Channels11( "MECableSmart11" ), Channels12( "MECableSmart12" ), Channels13( "MECableSmart13" ), Channels14( "MECableSmart14" ), Channels04( "MECableSmart04" ),

	LevelEmitterTorchOn( "ItemPart.LevelEmitterOn" ), BlockWirelessOn( "BlockWirelessOn" ),

	BlockP2PTunnel2( "ItemPart.P2PTunnel2" ), BlockP2PTunnel3( "ItemPart.P2PTunnel3" ),

	// MEWaiting("MEWaiting"),

	PartMonitorSides( "PartMonitorSides" ), PartMonitorBack( "PartMonitorBack" ),

	Transparent( "Transparent" ), PartMonitorSidesStatus( "PartMonitorSidesStatus" ), PartMonitorSidesStatusLights( "PartMonitorSidesStatusLights" ),

	PartMonitor_Colored( "PartMonitor_Colored" ), PartMonitor_Bright( "PartMonitor_Bright" ),

	PartPatternTerm_Bright( "PartPatternTerm_Bright" ), PartPatternTerm_Colored( "PartPatternTerm_Colored" ), PartPatternTerm_Dark( "PartPatternTerm_Dark" ),

	PartConversionMonitor_Bright( "PartConversionMonitor_Bright" ), PartConversionMonitor_Colored( "PartConversionMonitor_Colored" ), PartConversionMonitor_Dark( "PartConversionMonitor_Dark" ), PartConversionMonitor_Dark_Locked( "PartConversionMonitor_Dark_Locked" ),

	PartInterfaceTerm_Bright( "PartInterfaceTerm_Bright" ), PartInterfaceTerm_Colored( "PartInterfaceTerm_Colored" ), PartInterfaceTerm_Dark( "PartInterfaceTerm_Dark" ),

	PartCraftingTerm_Bright( "PartCraftingTerm_Bright" ), PartCraftingTerm_Colored( "PartCraftingTerm_Colored" ), PartCraftingTerm_Dark( "PartCraftingTerm_Dark" ), //

	PartStorageMonitor_Bright( "PartStorageMonitor_Bright" ), PartStorageMonitor_Colored( "PartStorageMonitor_Colored" ), PartStorageMonitor_Dark( "PartStorageMonitor_Dark" ), PartStorageMonitor_Colored_Locked( "PartStorageMonitor_Colored_Locked" ),

	PartTerminal_Bright( "PartTerminal_Bright" ), PartTerminal_Colored( "PartTerminal_Colored" ), PartTerminal_Dark( "PartTerminal_Dark" ),

	MECable_Green( "MECable_Green" ), MECable_Grey( "MECable_Grey" ), MECable_LightBlue( "MECable_LightBlue" ), MECable_LightGrey( "MECable_LightGrey" ), MECable_Lime( "MECable_Lime" ), MECable_Magenta( "MECable_Magenta" ), MECable_Orange( "MECable_Orange" ), MECable_Pink( "MECable_Pink" ), MECable_Purple( "MECable_Purple" ), MECable_Red( "MECable_Red" ), MECable_White( "MECable_White" ), MECable_Yellow( "MECable_Yellow" ), MECable_Black( "MECable_Black" ), MECable_Blue( "MECable_Blue" ), MECable_Brown( "MECable_Brown" ), MECable_Cyan( "MECable_Cyan" ),

	MEDense_Black( "MEDense_Black" ), MEDense_Blue( "MEDense_Blue" ), MEDense_Brown( "MEDense_Brown" ), MEDense_Cyan( "MEDense_Cyan" ), MEDense_Gray( "MEDense_Gray" ), MEDense_Green( "MEDense_Green" ), MEDense_LightBlue( "MEDense_LightBlue" ), MEDense_LightGrey( "MEDense_LightGrey" ), MEDense_Lime( "MEDense_Lime" ), MEDense_Magenta( "MEDense_Magenta" ), MEDense_Orange( "MEDense_Orange" ), MEDense_Pink( "MEDense_Pink" ), MEDense_Purple( "MEDense_Purple" ), MEDense_Red( "MEDense_Red" ), MEDense_White( "MEDense_White" ), MEDense_Yellow( "MEDense_Yellow" ),

	MESmart_Black( "MESmart_Black" ), MESmart_Blue( "MESmart_Blue" ), MESmart_Brown( "MESmart_Brown" ), MESmart_Cyan( "MESmart_Cyan" ), MESmart_Gray( "MESmart_Gray" ), MESmart_Green( "MESmart_Green" ), MESmart_LightBlue( "MESmart_LightBlue" ), MESmart_LightGrey( "MESmart_LightGrey" ), MESmart_Lime( "MESmart_Lime" ), MESmart_Magenta( "MESmart_Magenta" ), MESmart_Orange( "MESmart_Orange" ), MESmart_Pink( "MESmart_Pink" ), MESmart_Purple( "MESmart_Purple" ), MESmart_Red( "MESmart_Red" ), MESmart_White( "MESmart_White" ), MESmart_Yellow( "MESmart_Yellow" ),

	MECovered_Black( "MECovered_Black" ), MECovered_Blue( "MECovered_Blue" ), MECovered_Brown( "MECovered_Brown" ), MECovered_Cyan( "MECovered_Cyan" ), MECovered_Gray( "MECovered_Gray" ), MECovered_Green( "MECovered_Green" ), MECovered_LightBlue( "MECovered_LightBlue" ), MECovered_LightGrey( "MECovered_LightGrey" ), MECovered_Lime( "MECovered_Lime" ), MECovered_Magenta( "MECovered_Magenta" ), MECovered_Orange( "MECovered_Orange" ), MECovered_Pink( "MECovered_Pink" ), MECovered_Purple( "MECovered_Purple" ), MECovered_Red( "MECovered_Red" ), MECovered_White( "MECovered_White" ), MECovered_Yellow( "MECovered_Yellow" ),

	BlockAnnihilationPlaneOn( "BlockAnnihilationPlaneOn" ),

	BlockFormPlaneOn( "BlockFormPlaneOn" ),

	BlockIdentityAnnihilationPlaneOn( "BlockIdentityAnnihilationPlaneOn" ),

	ItemPartLevelEmitterOn( "ItemPart.LevelEmitterOn" ), PartTransitionPlaneBack( "PartTransitionPlaneBack" ),

	PartTunnelSides( "PartTunnelSides" ), PartPlaneSides( "PartPlaneSides" ), PartExportSides( "PartExportSides" ), PartImportSides( "PartImportSides" ),

	PartWirelessSides( "PartWirelessSides" ), PartStorageSides( "PartStorageSides" ), PartStorageBack( "PartStorageBack" );

	private final String name;
	public IIcon IIcon;

	CableBusTextures( final String name )
	{
		this.name = name;
	}

	public static ResourceLocation GuiTexture( final String string )
	{
		return null;
	}

	@SideOnly( Side.CLIENT )
	public static IIcon getMissing()
	{
		return ( (TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture( TextureMap.locationBlocksTexture ) ).getAtlasSprite( "missingno" );
	}

	public String getName()
	{
		return this.name;
	}

	public IIcon getIcon()
	{
		return this.IIcon;
	}

	public void registerIcon( final TextureMap map )
	{
		this.IIcon = map.registerIcon( "appliedenergistics2:" + this.name );
	}
}
