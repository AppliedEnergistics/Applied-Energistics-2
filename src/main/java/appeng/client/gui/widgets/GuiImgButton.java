package appeng.client.gui.widgets;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.CondenserOutput;
import appeng.api.config.FullnessMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.OperationMode;
import appeng.api.config.PowerUnits;
import appeng.api.config.RedstoneMode;
import appeng.api.config.RelativeDirection;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.StorageFilter;
import appeng.api.config.TerminalStyle;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.localization.ButtonToolTips;

public class GuiImgButton extends GuiButton implements ITooltip
{

	static class EnumPair
	{

		final Enum setting;
		final Enum value;

		EnumPair(Enum a, Enum b) {
			setting = a;
			value = b;
		}

		@Override
		public int hashCode()
		{
			return setting.hashCode() ^ value.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if ( obj == null )
				return false;
			if ( getClass() != obj.getClass() )
				return false;
			EnumPair other = (EnumPair) obj;
			return other.setting.equals( setting ) && other.value.equals( value );
		}

	}

	static class BtnAppearance
	{

		public int index;
		public String DisplayName;
		public String DisplayValue;
	}

	public boolean halfSize = false;
	public String FillVar;

	private final Enum buttonSetting;
	private Enum currentValue;

	static private Map<EnumPair, BtnAppearance> Appearances;

	private void registerApp(int IIcon, Settings setting, Enum val, ButtonToolTips title, Object hint)
	{
		BtnAppearance a = new BtnAppearance();
		a.DisplayName = title.getUnlocalized();
		a.DisplayValue = (String) (hint instanceof String ? hint : ((ButtonToolTips) hint).getUnlocalized());
		a.index = IIcon;
		Appearances.put( new EnumPair( setting, val ), a );
	}

	public void setVisibility(boolean vis)
	{
		visible = vis;
		enabled = vis;
	}

	public GuiImgButton(int x, int y, Enum idx, Enum val) {
		super( 0, 0, 16, "" );
		buttonSetting = idx;
		currentValue = val;
		xPosition = x;
		yPosition = y;
		width = 16;
		height = 16;

		if ( Appearances == null )
		{
			Appearances = new HashMap<EnumPair, BtnAppearance>();
			registerApp( 16 * 7 + 0, Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH, ButtonToolTips.CondenserOutput, ButtonToolTips.Trash );
			registerApp( 16 * 7 + 1, Settings.CONDENSER_OUTPUT, CondenserOutput.MATTER_BALLS, ButtonToolTips.CondenserOutput, ButtonToolTips.MatterBalls );
			registerApp( 16 * 7 + 2, Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY, ButtonToolTips.CondenserOutput, ButtonToolTips.Singularity );

			registerApp( 16 * 9 + 1, Settings.ACCESS, AccessRestriction.READ, ButtonToolTips.IOMode, ButtonToolTips.Read );
			registerApp( 16 * 9 + 0, Settings.ACCESS, AccessRestriction.WRITE, ButtonToolTips.IOMode, ButtonToolTips.Write );
			registerApp( 16 * 9 + 2, Settings.ACCESS, AccessRestriction.READ_WRITE, ButtonToolTips.IOMode, ButtonToolTips.ReadWrite );

			registerApp( 16 * 10 + 0, Settings.POWER_UNITS, PowerUnits.AE, ButtonToolTips.PowerUnits, PowerUnits.AE.unlocalizedName );
			registerApp( 16 * 10 + 1, Settings.POWER_UNITS, PowerUnits.EU, ButtonToolTips.PowerUnits, PowerUnits.EU.unlocalizedName );
			registerApp( 16 * 10 + 2, Settings.POWER_UNITS, PowerUnits.MJ, ButtonToolTips.PowerUnits, PowerUnits.MJ.unlocalizedName );
			registerApp( 16 * 10 + 3, Settings.POWER_UNITS, PowerUnits.MK, ButtonToolTips.PowerUnits, PowerUnits.MK.unlocalizedName );
			registerApp( 16 * 10 + 4, Settings.POWER_UNITS, PowerUnits.WA, ButtonToolTips.PowerUnits, PowerUnits.WA.unlocalizedName );
			registerApp( 16 * 10 + 5, Settings.POWER_UNITS, PowerUnits.RF, ButtonToolTips.PowerUnits, PowerUnits.RF.unlocalizedName );

			registerApp( 3, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE, ButtonToolTips.RedstoneMode, ButtonToolTips.AlwaysActive );
			registerApp( 0, Settings.REDSTONE_CONTROLLED, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.ActiveWithoutSignal );
			registerApp( 1, Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.ActiveWithSignal );
			registerApp( 2, Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE, ButtonToolTips.RedstoneMode, ButtonToolTips.ActiveOnPulse );

			registerApp( 0, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.EmitLevelsBelow );
			registerApp( 1, Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL, ButtonToolTips.RedstoneMode, ButtonToolTips.EmitLevelAbove );

			registerApp( 51, Settings.OPERATION_MODE, OperationMode.FILL, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToStorageCell );
			registerApp( 50, Settings.OPERATION_MODE, OperationMode.EMPTY, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToNetwork );

			registerApp( 51, Settings.IO_DIRECTION, RelativeDirection.LEFT, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToStorageCell );
			registerApp( 50, Settings.IO_DIRECTION, RelativeDirection.RIGHT, ButtonToolTips.TransferDirection, ButtonToolTips.TransferToNetwork );

			registerApp( 48, Settings.SORT_DIRECTION, SortDir.ASCENDING, ButtonToolTips.SortOrder, ButtonToolTips.ToggleSortDirection );
			registerApp( 49, Settings.SORT_DIRECTION, SortDir.DESCENDING, ButtonToolTips.SortOrder, ButtonToolTips.ToggleSortDirection );

			registerApp( 16 * 2 + 3, Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH, ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_Auto );
			registerApp( 16 * 2 + 4, Settings.SEARCH_MODE, SearchBoxMode.MANUAL_SEARCH, ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_Standard );
			registerApp( 16 * 2 + 5, Settings.SEARCH_MODE, SearchBoxMode.NEI_AUTOSEARCH, ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_NEIAuto );
			registerApp( 16 * 2 + 6, Settings.SEARCH_MODE, SearchBoxMode.NEI_MANUAL_SEARCH, ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_NEIStandard );

			registerApp( 16 * 5 + 3, Settings.LEVEL_TYPE, LevelType.ENERGY_LEVEL, ButtonToolTips.LevelType, ButtonToolTips.LevelType_Energy );
			registerApp( 16 * 4 + 3, Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL, ButtonToolTips.LevelType, ButtonToolTips.LevelType_Item );

			registerApp( 16 * 13 + 0, Settings.TERMINAL_STYLE, TerminalStyle.TALL, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Tall );
			registerApp( 16 * 13 + 1, Settings.TERMINAL_STYLE, TerminalStyle.SMALL, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Small );
			registerApp( 16 * 13 + 2, Settings.TERMINAL_STYLE, TerminalStyle.FULL, ButtonToolTips.TerminalStyle, ButtonToolTips.TerminalStyle_Full );

			registerApp( 64, Settings.SORT_BY, SortOrder.NAME, ButtonToolTips.SortBy, ButtonToolTips.ItemName );
			registerApp( 65, Settings.SORT_BY, SortOrder.AMOUNT, ButtonToolTips.SortBy, ButtonToolTips.NumberOfItems );
			registerApp( 68, Settings.SORT_BY, SortOrder.INVTWEAKS, ButtonToolTips.SortBy, ButtonToolTips.InventoryTweaks );
			registerApp( 69, Settings.SORT_BY, SortOrder.MOD, ButtonToolTips.SortBy, ButtonToolTips.Mod );

			registerApp( 66, Settings.ACTIONS, ActionItems.WRENCH, ButtonToolTips.PartitionStorage, ButtonToolTips.PartitionStorageHint );
			registerApp( 6, Settings.ACTIONS, ActionItems.CLOSE, ButtonToolTips.Clear, ButtonToolTips.ClearSettings );
			registerApp( 6, Settings.ACTIONS, ActionItems.STASH, ButtonToolTips.Stash, ButtonToolTips.StashDesc );

			registerApp( 8, Settings.ACTIONS, ActionItems.ENCODE, ButtonToolTips.Encode, ButtonToolTips.EncodeDescription );
			registerApp( 4 + 3 * 16, Settings.ACTIONS, ActionItems.SUBSTITUTION, ButtonToolTips.Substitutions, ButtonToolTips.SubstitutionsDesc );

			registerApp( 16, Settings.VIEW_MODE, ViewItems.STORED, ButtonToolTips.View, ButtonToolTips.StoredItems );
			registerApp( 18, Settings.VIEW_MODE, ViewItems.ALL, ButtonToolTips.View, ButtonToolTips.StoredCraftable );
			registerApp( 19, Settings.VIEW_MODE, ViewItems.CRAFTABLE, ButtonToolTips.View, ButtonToolTips.Craftable );

			registerApp( 16 * 6 + 0, Settings.FUZZY_MODE, FuzzyMode.PERCENT_25, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_25 );
			registerApp( 16 * 6 + 1, Settings.FUZZY_MODE, FuzzyMode.PERCENT_50, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_50 );
			registerApp( 16 * 6 + 2, Settings.FUZZY_MODE, FuzzyMode.PERCENT_75, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_75 );
			registerApp( 16 * 6 + 3, Settings.FUZZY_MODE, FuzzyMode.PERCENT_99, ButtonToolTips.FuzzyMode, ButtonToolTips.FZPercent_99 );
			registerApp( 16 * 6 + 4, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, ButtonToolTips.FuzzyMode, ButtonToolTips.FZIgnoreAll );

			registerApp( 80, Settings.FULLNESS_MODE, FullnessMode.EMPTY, ButtonToolTips.OperationMode, ButtonToolTips.MoveWhenEmpty );
			registerApp( 81, Settings.FULLNESS_MODE, FullnessMode.HALF, ButtonToolTips.OperationMode, ButtonToolTips.MoveWhenWorkIsDone );
			registerApp( 82, Settings.FULLNESS_MODE, FullnessMode.FULL, ButtonToolTips.OperationMode, ButtonToolTips.MoveWhenFull );

			registerApp( 16 * 1 + 5, Settings.BLOCK, YesNo.YES, ButtonToolTips.InterfaceBlockingMode, ButtonToolTips.Blocking );
			registerApp( 16 * 1 + 4, Settings.BLOCK, YesNo.NO, ButtonToolTips.InterfaceBlockingMode, ButtonToolTips.NonBlocking );

			registerApp( 16 * 1 + 3, Settings.CRAFT_ONLY, YesNo.YES, ButtonToolTips.Craft, ButtonToolTips.CraftOnly );
			registerApp( 16 * 1 + 2, Settings.CRAFT_ONLY, YesNo.NO, ButtonToolTips.Craft, ButtonToolTips.CraftEither );

			registerApp( 16 * 11 + 2, Settings.CRAFT_VIA_REDSTONE, YesNo.YES, ButtonToolTips.EmitterMode, ButtonToolTips.CraftViaRedstone );
			registerApp( 16 * 11 + 1, Settings.CRAFT_VIA_REDSTONE, YesNo.NO, ButtonToolTips.EmitterMode, ButtonToolTips.EmitWhenCrafting );

			registerApp( 16 * 3 + 5, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY, ButtonToolTips.ReportInaccessibleItems,
					ButtonToolTips.ReportInaccessibleItemsNo );
			registerApp( 16 * 3 + 6, Settings.STORAGE_FILTER, StorageFilter.NONE, ButtonToolTips.ReportInaccessibleItems,
					ButtonToolTips.ReportInaccessibleItemsYes );
		}
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
	{
		if ( this.visible )
		{
			int iconIndex = getIconIndex();

			if ( halfSize )
			{
				width = 8;
				height = 8;

				GL11.glPushMatrix();
				GL11.glTranslatef( this.xPosition, this.yPosition, 0.0F );
				GL11.glScalef( 0.5f, 0.5f, 0.5f );

				if ( enabled )
					GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
				else
					GL11.glColor4f( 0.5f, 0.5f, 0.5f, 1.0f );

				par1Minecraft.renderEngine.bindTexture( ExtraBlockTextures.GuiTexture( "guis/states.png" ) );
				this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width
						&& par3 < this.yPosition + this.height;

				int uv_y = (int) Math.floor( iconIndex / 16 );
				int uv_x = iconIndex - uv_y * 16;

				this.drawTexturedModalRect( 0, 0, 256 - 16, 256 - 16, 16, 16 );
				this.drawTexturedModalRect( 0, 0, uv_x * 16, uv_y * 16, 16, 16 );
				this.mouseDragged( par1Minecraft, par2, par3 );

				GL11.glPopMatrix();
			}
			else
			{
				if ( enabled )
					GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
				else
					GL11.glColor4f( 0.5f, 0.5f, 0.5f, 1.0f );

				par1Minecraft.renderEngine.bindTexture( ExtraBlockTextures.GuiTexture( "guis/states.png" ) );
				this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width
						&& par3 < this.yPosition + this.height;

				int uv_y = (int) Math.floor( iconIndex / 16 );
				int uv_x = iconIndex - uv_y * 16;

				this.drawTexturedModalRect( this.xPosition, this.yPosition, 256 - 16, 256 - 16, 16, 16 );
				this.drawTexturedModalRect( this.xPosition, this.yPosition, uv_x * 16, uv_y * 16, 16, 16 );
				this.mouseDragged( par1Minecraft, par2, par3 );
			}
		}
		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
	}

	private int getIconIndex()
	{
		if ( buttonSetting != null && currentValue != null )
		{
			BtnAppearance app = Appearances.get( new EnumPair( buttonSetting, currentValue ) );
			if ( app == null )
				return 256 - 1;
			return app.index;
		}
		return 256 - 1;
	}

	public Settings getSetting()
	{
		return (Settings) buttonSetting;
	}

	public Enum getCurrentValue()
	{
		return currentValue;
	}

	@Override
	public String getMsg()
	{
		String DisplayName = null;
		String DisplayValue = null;

		if ( buttonSetting != null && currentValue != null )
		{
			BtnAppearance ba = Appearances.get( new EnumPair( buttonSetting, currentValue ) );
			if ( ba == null )
				return "No Such Message";

			DisplayName = ba.DisplayName;
			DisplayValue = ba.DisplayValue;
		}

		if ( DisplayName != null )
		{
			String Name = StatCollector.translateToLocal( DisplayName );
			String Value = StatCollector.translateToLocal( DisplayValue );

			if ( Name == null || Name.equals( "" ) )
				Name = DisplayName;
			if ( Value == null || Value.equals( "" ) )
				Value = DisplayValue;

			if ( FillVar != null )
				Value = Value.replaceFirst( "%s", FillVar );

			Value = Value.replace( "\\n", "\n" );
			StringBuilder sb = new StringBuilder( Value );

			int i = sb.lastIndexOf( "\n" );
			if ( i <= 0 )
				i = 0;
			while (i + 30 < sb.length() && (i = sb.lastIndexOf( " ", i + 30 )) != -1)
			{
				sb.replace( i, i + 1, "\n" );
			}

			return Name + "\n" + sb.toString();
		}
		return null;
	}

	@Override
	public int xPos()
	{
		return xPosition;
	}

	@Override
	public int yPos()
	{
		return yPosition;
	}

	@Override
	public int getWidth()
	{
		return halfSize ? 8 : 16;
	}

	@Override
	public int getHeight()
	{
		return halfSize ? 8 : 16;
	}

	public void set(Enum e)
	{
		if ( currentValue != e )
		{
			currentValue = e;
		}
	}

}
