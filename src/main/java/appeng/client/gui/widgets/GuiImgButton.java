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

package appeng.client.gui.widgets;


import appeng.api.config.*;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.localization.ButtonToolTips;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class GuiImgButton extends GuiButton implements ITooltip
{
	private static final Pattern COMPILE = Pattern.compile( "%s" );
	private static final Pattern PATTERN_NEW_LINE = Pattern.compile( "\\n", Pattern.LITERAL );
	private static Map<EnumPair, ButtonAppearance> appearances;
	private final Enum buttonSetting;
	private boolean halfSize = false;
	private String fillVar;
	private Enum currentValue;

	public GuiImgButton( final int x, final int y, final Enum idx, final Enum val )
	{
		super( 0, 0, 16, "" );

		this.buttonSetting = idx;
		this.currentValue = val;
		this.xPosition = x;
		this.yPosition = y;
		this.width = 16;
		this.height = 16;

		if( appearances == null )
		{
			appearances = new HashMap<EnumPair, ButtonAppearance>();
			this.registerApp( 16 * 7, Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH, ButtonToolTips.CondenserOutput, ButtonToolTips.Trash );
			this.registerApp( 16 * 7 + 1, Settings.CONDENSER_OUTPUT, CondenserOutput.MATTER_BALLS, ButtonToolTips.CondenserOutput, ButtonToolTips.MatterBalls );
			this.registerApp( 16 * 7 + 2, Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY, ButtonToolTips.CondenserOutput, ButtonToolTips.Singularity );

			this.registerApp( 16 * 9 + 1, Settings.ACCESS, AccessRestriction.READ, ButtonToolTips.IOMode, ButtonToolTips.Read );
			this.registerApp( 16 * 9, Settings.ACCESS, AccessRestriction.WRITE, ButtonToolTips.IOMode, ButtonToolTips.Write );
			this.registerApp( 16 * 9 + 2, Settings.ACCESS, AccessRestriction.READ_WRITE, ButtonToolTips.IOMode, ButtonToolTips.ReadWrite );

			this.registerApp( 16 * 10, Settings.POWER_UNITS, PowerUnits.AE, ButtonToolTips.PowerUnits, PowerUnits.AE.unlocalizedName );
			this.registerApp( 16 * 10 + 1, Settings.POWER_UNITS, PowerUnits.EU, ButtonToolTips.PowerUnits, PowerUnits.EU.unlocalizedName );
			this.registerApp( 16 * 10 + 2, Settings.POWER_UNITS, PowerUnits.MK, ButtonToolTips.PowerUnits, PowerUnits.MK.unlocalizedName );
			this.registerApp( 16 * 10 + 3, Settings.POWER_UNITS, PowerUnits.WA, ButtonToolTips.PowerUnits, PowerUnits.WA.unlocalizedName );
			this.registerApp( 16 * 10 + 4, Settings.POWER_UNITS, PowerUnits.RF, ButtonToolTips.PowerUnits, PowerUnits.RF.unlocalizedName );

			this.registerApp( 3, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE, ButtonToolTips.RedstoneMode, ButtonToolTips.AlwaysActive );
			this.registerApp( 0, Settings.REDSTONE_CONTROLLED, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.ActiveWithoutSignal );
			this.registerApp( 1, Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.ActiveWithSignal );
			this.registerApp( 2, Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE, ButtonToolTips.RedstoneMode, ButtonToolTips.ActiveOnPulse );

			this.registerApp( 0, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.EmitLevelsBelow );
			this.registerApp( 1, Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.EmitLevelAbove );

			this.registerApp( 51, Settings.OPERATION_MODE, OperationMode.FILL, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToStorageCell );
			this.registerApp( 50, Settings.OPERATION_MODE, OperationMode.EMPTY, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToNetwork );

			this.registerApp( 51, Settings.IO_DIRECTION, RelativeDirection.LEFT, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToStorageCell );
			this.registerApp( 50, Settings.IO_DIRECTION, RelativeDirection.RIGHT, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToNetwork );

			this.registerApp( 48, Settings.SORT_DIRECTION, SortDir.ASCENDING, ButtonToolTips.SortOrder, ButtonToolTips.ToggleSortDirection );
			this.registerApp( 49, Settings.SORT_DIRECTION, SortDir.DESCENDING, ButtonToolTips.SortOrder, ButtonToolTips.ToggleSortDirection );

			this.registerApp( 16 * 2 + 3, Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH, ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_Auto );
			this.registerApp( 16 * 2 + 4, Settings.SEARCH_MODE, SearchBoxMode.MANUAL_SEARCH, ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_Standard );
			this.registerApp( 16 * 2 + 5, Settings.SEARCH_MODE, SearchBoxMode.NEI_AUTOSEARCH, ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_NEIAuto );
			this.registerApp( 16 * 2 + 6, Settings.SEARCH_MODE, SearchBoxMode.NEI_MANUAL_SEARCH, ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_NEIStandard );

			this.registerApp( 16 * 5 + 3, Settings.LEVEL_TYPE, LevelType.ENERGY_LEVEL, ButtonToolTips.LevelType, ButtonToolTips.LevelType_Energy );
			this.registerApp( 16 * 4 + 3, Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL, ButtonToolTips.LevelType, ButtonToolTips.LevelType_Item );

			this.registerApp( 16 * 13, Settings.TERMINAL_STYLE, TerminalStyle.TALL, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Tall );
			this.registerApp( 16 * 13 + 1, Settings.TERMINAL_STYLE, TerminalStyle.SMALL, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Small );
			this.registerApp( 16 * 13 + 2, Settings.TERMINAL_STYLE, TerminalStyle.FULL, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Full );

			this.registerApp( 64, Settings.SORT_BY, SortOrder.NAME, ButtonToolTips.SortBy, ButtonToolTips.ItemName );
			this.registerApp( 65, Settings.SORT_BY, SortOrder.AMOUNT, ButtonToolTips.SortBy, ButtonToolTips.NumberOfItems );
			this.registerApp( 68, Settings.SORT_BY, SortOrder.INVTWEAKS, ButtonToolTips.SortBy, ButtonToolTips.InventoryTweaks );
			this.registerApp( 69, Settings.SORT_BY, SortOrder.MOD, ButtonToolTips.SortBy, ButtonToolTips.Mod );

			this.registerApp( 66, Settings.ACTIONS, ActionItems.WRENCH, ButtonToolTips.PartitionStorage, ButtonToolTips.PartitionStorageHint );
			this.registerApp( 6, Settings.ACTIONS, ActionItems.CLOSE, ButtonToolTips.Clear, ButtonToolTips.ClearSettings );
			this.registerApp( 6, Settings.ACTIONS, ActionItems.STASH, ButtonToolTips.Stash, ButtonToolTips.StashDesc );

			this.registerApp( 8, Settings.ACTIONS, ActionItems.ENCODE, ButtonToolTips.Encode, ButtonToolTips.EncodeDescription );
			this.registerApp( 4 + 3 * 16, Settings.ACTIONS, ItemSubstitution.ENABLED, ButtonToolTips.Substitutions, ButtonToolTips.SubstitutionsDescEnabled );
			this.registerApp( 7 + 3 * 16, Settings.ACTIONS, ItemSubstitution.DISABLED, ButtonToolTips.Substitutions, ButtonToolTips.SubstitutionsDescDisabled );

			this.registerApp( 16, Settings.VIEW_MODE, ViewItems.STORED, ButtonToolTips.View, ButtonToolTips.StoredItems );
			this.registerApp( 18, Settings.VIEW_MODE, ViewItems.ALL, ButtonToolTips.View, ButtonToolTips.StoredCraftable );
			this.registerApp( 19, Settings.VIEW_MODE, ViewItems.CRAFTABLE, ButtonToolTips.View, ButtonToolTips.Craftable );

			this.registerApp( 16 * 6, Settings.FUZZY_MODE, FuzzyMode.PERCENT_25, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_25 );
			this.registerApp( 16 * 6 + 1, Settings.FUZZY_MODE, FuzzyMode.PERCENT_50, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_50 );
			this.registerApp( 16 * 6 + 2, Settings.FUZZY_MODE, FuzzyMode.PERCENT_75, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_75 );
			this.registerApp( 16 * 6 + 3, Settings.FUZZY_MODE, FuzzyMode.PERCENT_99, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_99 );
			this.registerApp( 16 * 6 + 4, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, ButtonToolTips.FuzzyMode, ButtonToolTips.FZIgnoreAll );

			this.registerApp( 80, Settings.FULLNESS_MODE, FullnessMode.EMPTY, ButtonToolTips.OperationMode, ButtonToolTips.MoveWhenEmpty );
			this.registerApp( 81, Settings.FULLNESS_MODE, FullnessMode.HALF, ButtonToolTips.OperationMode, ButtonToolTips.MoveWhenWorkIsDone );
			this.registerApp( 82, Settings.FULLNESS_MODE, FullnessMode.FULL, ButtonToolTips.OperationMode, ButtonToolTips.MoveWhenFull );

			this.registerApp( 16 + 5, Settings.BLOCK, YesNo.YES, ButtonToolTips.InterfaceBlockingMode, ButtonToolTips.Blocking );
			this.registerApp( 16 + 4, Settings.BLOCK, YesNo.NO, ButtonToolTips.InterfaceBlockingMode, ButtonToolTips.NonBlocking );

			this.registerApp( 16 + 3, Settings.CRAFT_ONLY, YesNo.YES, ButtonToolTips.Craft, ButtonToolTips.CraftOnly );
			this.registerApp( 16 + 2, Settings.CRAFT_ONLY, YesNo.NO, ButtonToolTips.Craft, ButtonToolTips.CraftEither );

			this.registerApp( 16 * 11 + 2, Settings.CRAFT_VIA_REDSTONE, YesNo.YES, ButtonToolTips.EmitterMode, ButtonToolTips.CraftViaRedstone );
			this.registerApp( 16 * 11 + 1, Settings.CRAFT_VIA_REDSTONE, YesNo.NO, ButtonToolTips.EmitterMode, ButtonToolTips.EmitWhenCrafting );

			this.registerApp( 16 * 3 + 5, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY, ButtonToolTips.ReportInaccessibleItems, ButtonToolTips.ReportInaccessibleItemsNo );
			this.registerApp( 16 * 3 + 6, Settings.STORAGE_FILTER, StorageFilter.NONE, ButtonToolTips.ReportInaccessibleItems, ButtonToolTips.ReportInaccessibleItemsYes );

			this.registerApp( 16 * 14, Settings.PLACE_BLOCK, YesNo.YES, ButtonToolTips.BlockPlacement, ButtonToolTips.BlockPlacementYes );
			this.registerApp( 16 * 14 + 1, Settings.PLACE_BLOCK, YesNo.NO, ButtonToolTips.BlockPlacement, ButtonToolTips.BlockPlacementNo );

			this.registerApp( 16 * 15, Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT, ButtonToolTips.SchedulingMode, ButtonToolTips.SchedulingModeDefault );
			this.registerApp( 16 * 15 + 1, Settings.SCHEDULING_MODE, SchedulingMode.ROUNDROBIN, ButtonToolTips.SchedulingMode, ButtonToolTips.SchedulingModeRoundRobin );
			this.registerApp( 16 * 15 + 2, Settings.SCHEDULING_MODE, SchedulingMode.RANDOM, ButtonToolTips.SchedulingMode, ButtonToolTips.SchedulingModeRandom );
		}
	}

	private void registerApp( final int iconIndex, final Settings setting, final Enum val, final ButtonToolTips title, final Object hint )
	{
		final ButtonAppearance a = new ButtonAppearance();
		a.displayName = title.getUnlocalized();
		a.displayValue = (String) ( hint instanceof String ? hint : ( (ButtonToolTips) hint ).getUnlocalized() );
		a.index = iconIndex;
		appearances.put( new EnumPair( setting, val ), a );
	}

	public void setVisibility( final boolean vis )
	{
		this.visible = vis;
		this.enabled = vis;
	}

	@Override
	public void drawButton( final Minecraft par1Minecraft, final int par2, final int par3 )
	{
		if( this.visible )
		{
			final int iconIndex = this.getIconIndex();

			if( this.halfSize )
			{
				this.width = 8;
				this.height = 8;

				GL11.glPushMatrix();
				GL11.glTranslatef( this.xPosition, this.yPosition, 0.0F );
				GL11.glScalef( 0.5f, 0.5f, 0.5f );

				if( this.enabled )
				{
					GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
				}
				else
				{
					GL11.glColor4f( 0.5f, 0.5f, 0.5f, 1.0f );
				}

				par1Minecraft.renderEngine.bindTexture( ExtraBlockTextures.GuiTexture( "guis/states.png" ) );
				this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;

				final int uv_y = (int) Math.floor( iconIndex / 16 );
				final int uv_x = iconIndex - uv_y * 16;

				this.drawTexturedModalRect( 0, 0, 256 - 16, 256 - 16, 16, 16 );
				this.drawTexturedModalRect( 0, 0, uv_x * 16, uv_y * 16, 16, 16 );
				this.mouseDragged( par1Minecraft, par2, par3 );

				GL11.glPopMatrix();
			}
			else
			{
				if( this.enabled )
				{
					GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
				}
				else
				{
					GL11.glColor4f( 0.5f, 0.5f, 0.5f, 1.0f );
				}

				par1Minecraft.renderEngine.bindTexture( ExtraBlockTextures.GuiTexture( "guis/states.png" ) );
				this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;

				final int uv_y = (int) Math.floor( iconIndex / 16 );
				final int uv_x = iconIndex - uv_y * 16;

				this.drawTexturedModalRect( this.xPosition, this.yPosition, 256 - 16, 256 - 16, 16, 16 );
				this.drawTexturedModalRect( this.xPosition, this.yPosition, uv_x * 16, uv_y * 16, 16, 16 );
				this.mouseDragged( par1Minecraft, par2, par3 );
			}
		}
		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
	}

	private int getIconIndex()
	{
		if( this.buttonSetting != null && this.currentValue != null )
		{
			final ButtonAppearance app = appearances.get( new EnumPair( this.buttonSetting, this.currentValue ) );
			if( app == null )
			{
				return 256 - 1;
			}
			return app.index;
		}
		return 256 - 1;
	}

	public Settings getSetting()
	{
		return (Settings) this.buttonSetting;
	}

	public Enum getCurrentValue()
	{
		return this.currentValue;
	}

	@Override
	public String getMessage()
	{
		String displayName = null;
		String displayValue = null;

		if( this.buttonSetting != null && this.currentValue != null )
		{
			final ButtonAppearance buttonAppearance = appearances.get( new EnumPair( this.buttonSetting, this.currentValue ) );
			if( buttonAppearance == null )
			{
				return "No Such Message";
			}

			displayName = buttonAppearance.displayName;
			displayValue = buttonAppearance.displayValue;
		}

		if( displayName != null )
		{
			String name = StatCollector.translateToLocal( displayName );
			String value = StatCollector.translateToLocal( displayValue );

			if( name == null || name.isEmpty() )
			{
				name = displayName;
			}
			if( value == null || value.isEmpty() )
			{
				value = displayValue;
			}

			if( this.fillVar != null )
			{
				value = COMPILE.matcher( value ).replaceFirst( this.fillVar );
			}

			value = PATTERN_NEW_LINE.matcher( value ).replaceAll( "\n" );
			final StringBuilder sb = new StringBuilder( value );

			int i = sb.lastIndexOf( "\n" );
			if( i <= 0 )
			{
				i = 0;
			}
			while( i + 30 < sb.length() && ( i = sb.lastIndexOf( " ", i + 30 ) ) != -1 )
			{
				sb.replace( i, i + 1, "\n" );
			}

			return name + '\n' + sb;
		}
		return null;
	}

	@Override
	public int xPos()
	{
		return this.xPosition;
	}

	@Override
	public int yPos()
	{
		return this.yPosition;
	}

	@Override
	public int getWidth()
	{
		return this.halfSize ? 8 : 16;
	}

	@Override
	public int getHeight()
	{
		return this.halfSize ? 8 : 16;
	}

	@Override
	public boolean isVisible()
	{
		return this.visible;
	}

	public void set( final Enum e )
	{
		if( this.currentValue != e )
		{
			this.currentValue = e;
		}
	}

	public boolean isHalfSize()
	{
		return this.halfSize;
	}

	public void setHalfSize( final boolean halfSize )
	{
		this.halfSize = halfSize;
	}

	public String getFillVar()
	{
		return this.fillVar;
	}

	public void setFillVar( final String fillVar )
	{
		this.fillVar = fillVar;
	}

	private static final class EnumPair
	{

		final Enum setting;
		final Enum value;

		EnumPair( final Enum a, final Enum b )
		{
			this.setting = a;
			this.value = b;
		}

		@Override
		public int hashCode()
		{
			return this.setting.hashCode() ^ this.value.hashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( obj == null )
			{
				return false;
			}
			if( this.getClass() != obj.getClass() )
			{
				return false;
			}
			final EnumPair other = (EnumPair) obj;
			return other.setting == this.setting && other.value == this.value;
		}
	}


	private static class ButtonAppearance
	{
		public int index;
		public String displayName;
		public String displayValue;
	}
}
