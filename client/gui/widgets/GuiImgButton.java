package appeng.client.gui.widgets;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.CondenserOuput;
import appeng.api.config.FullnessMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.MatchingMode;
import appeng.api.config.OperationMode;
import appeng.api.config.PowerUnits;
import appeng.api.config.RedstoneMode;
import appeng.api.config.RelativeDirection;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.client.texture.ExtraTextures;

public class GuiImgButton extends GuiButton implements ITooltip
{

	class EnumPair
	{

		Enum setting;
		Enum value;

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
			EnumPair d = (EnumPair) obj;
			return d.setting.equals( setting ) && d.value.equals( value );
		}

	};

	class BtnAppearance
	{

		public int index;
		public String DisplayName;
		public String DisplayValue;
	};

	public boolean halfSize = false;
	public String FillVar;

	private final Enum buttonSetting;
	private Enum currentValue;

	static private Map<EnumPair, BtnAppearance> Appearances;

	private void registerApp(int icon, Settings setting, Enum val, String dn, String dv)
	{
		BtnAppearance a = new BtnAppearance();
		a.DisplayName = dn;
		a.DisplayValue = dv;
		a.index = icon;
		Appearances.put( new EnumPair( setting, val ), a );
	}

	public void setVisibility(boolean vis)
	{
		drawButton = vis;
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
			Appearances = new HashMap();
			registerApp( 16 * 7 + 0, Settings.CONDENSER_OUTPUT, CondenserOuput.TRASH, "AppEng.GuiITooltip.CondenserOutput", "AppEng.GuiITooltip.Trash" );
			registerApp( 16 * 7 + 1, Settings.CONDENSER_OUTPUT, CondenserOuput.MATTER_BALLS, "AppEng.GuiITooltip.CondenserOutput",
					"AppEng.GuiITooltip.MatterBalls" );
			registerApp( 16 * 7 + 2, Settings.CONDENSER_OUTPUT, CondenserOuput.SINGULARITY, "AppEng.GuiITooltip.CondenserOutput",
					"AppEng.GuiITooltip.Singularity" );

			registerApp( 16 * 9 + 1, Settings.ACCESS, AccessRestriction.READ, "AppEng.GuiITooltip.IOMode", "AppEng.GuiITooltip.Read" );
			registerApp( 16 * 9 + 0, Settings.ACCESS, AccessRestriction.WRITE, "AppEng.GuiITooltip.IOMode", "AppEng.GuiITooltip.Write" );
			registerApp( 16 * 9 + 2, Settings.ACCESS, AccessRestriction.READ_WRITE, "AppEng.GuiITooltip.IOMode", "AppEng.GuiITooltip.ReadWrite" );

			registerApp( 16 * 10 + 0, Settings.POWER_UNITS, PowerUnits.AE, "AppEng.GuiITooltip.PowerUnits", "AppEng.GuiITooltip.AEUnits" );
			registerApp( 16 * 10 + 1, Settings.POWER_UNITS, PowerUnits.EU, "AppEng.GuiITooltip.PowerUnits", "AppEng.GuiITooltip.EUUnits" );
			registerApp( 16 * 10 + 2, Settings.POWER_UNITS, PowerUnits.MJ, "AppEng.GuiITooltip.PowerUnits", "AppEng.GuiITooltip.MJUnits" );
			registerApp( 16 * 10 + 3, Settings.POWER_UNITS, PowerUnits.KJ, "AppEng.GuiITooltip.PowerUnits", "AppEng.GuiITooltip.UEUnits" );
			registerApp( 16 * 10 + 4, Settings.POWER_UNITS, PowerUnits.WA, "AppEng.GuiITooltip.PowerUnits", "AppEng.GuiITooltip.WUnits" );

			registerApp( 3, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE, "AppEng.GuiITooltip.RedstoneMode", "AppEng.GuiITooltip.AlwaysActive" );
			registerApp( 0, Settings.REDSTONE_CONTROLLED, RedstoneMode.LOW_SIGNAL, "AppEng.GuiITooltip.RedstoneMode", "AppEng.GuiITooltip.ActiveWithoutSignal" );
			registerApp( 1, Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL, "AppEng.GuiITooltip.RedstoneMode", "AppEng.GuiITooltip.ActiveWithSignal" );
			registerApp( 2, Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE, "AppEng.GuiITooltip.RedstoneMode", "AppEng.GuiITooltip.ActiveOnPulse" );

			registerApp( 3, Settings.REDSTONE_EMITTER, RedstoneMode.IGNORE, "AppEng.GuiITooltip.RedstoneMode", "AppEng.GuiITooltip.AlwaysActive" );
			registerApp( 0, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL, "AppEng.GuiITooltip.RedstoneMode", "AppEng.GuiITooltip.ActiveWithoutSignal" );
			registerApp( 1, Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL, "AppEng.GuiITooltip.RedstoneMode", "AppEng.GuiITooltip.ActiveWithSignal" );

			registerApp( 0, Settings.REDSTONE_CONTROLLED, RedstoneMode.LOW_SIGNAL, "AppEng.GuiITooltip.RedstoneMode", "AppEng.GuiITooltip.EmitLevelsBelow" );
			registerApp( 1, Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL, "AppEng.GuiITooltip.RedstoneMode", "AppEng.GuiITooltip.EmitLevelAbove" );

			registerApp( 16 * 8 + 2, Settings.INCLUSION, IncludeExclude.WHITELIST, "AppEng.Gui.Whitelisted", "AppEng.Gui.WhitelistedDesc" );
			registerApp( 16 * 8 + 3, Settings.INCLUSION, IncludeExclude.BLACKLIST, "AppEng.Gui.Blacklisted", "AppEng.Gui.BlacklistedDesc" );

			registerApp( 34, Settings.COMPARISON, MatchingMode.FUZZY, "AppEng.GuiITooltip.MatchingMode", "AppEng.GuiITooltip.MatchingFuzzy" );
			registerApp( 32, Settings.COMPARISON, MatchingMode.PRECISE, "AppEng.GuiITooltip.MatchingMode", "AppEng.GuiITooltip.MatchingExact" );

			registerApp( 50, Settings.OPERATION_MODE, OperationMode.EMPTY, "AppEng.GuiITooltip.TransferDirection", "AppEng.GuiITooltip.TransferToNetwork" );
			registerApp( 51, Settings.OPERATION_MODE, OperationMode.FILL, "AppEng.GuiITooltip.TransferDirection", "AppEng.GuiITooltip.TransferToStorageCell" );

			registerApp( 51, Settings.IO_DIRECTION, RelativeDirection.LEFT, "AppEng.GuiITooltip.TransferDirection", "AppEng.GuiITooltip.TransferToStorageCell" );
			registerApp( 50, Settings.IO_DIRECTION, RelativeDirection.RIGHT, "AppEng.GuiITooltip.TransferDirection", "AppEng.GuiITooltip.TransferToNetwork" );

			registerApp( 48, Settings.SORT_DIRECTION, SortDir.ASCENDING, "AppEng.GuiITooltip.SortOrder", "AppEng.GuiITooltip.ToggleSortDirection" );
			registerApp( 49, Settings.SORT_DIRECTION, SortDir.DESCENDING, "AppEng.GuiITooltip.SortOrder", "AppEng.GuiITooltip.ToggleSortDirection" );

			registerApp( 16 * 2 + 3, Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH, "AppEng.GuiITooltip.SearchMode", "AppEng.GuiITooltip.SearchMode_Auto" );
			registerApp( 16 * 2 + 4, Settings.SEARCH_MODE, SearchBoxMode.MANUAL_SEARCH, "AppEng.GuiITooltip.SearchMode",
					"AppEng.GuiITooltip.SearchMode_Standard" );
			registerApp( 16 * 2 + 5, Settings.SEARCH_MODE, SearchBoxMode.NEI_AUTOSEARCH, "AppEng.GuiITooltip.SearchMode",
					"AppEng.GuiITooltip.SearchMode_NEIAuto" );
			registerApp( 16 * 2 + 6, Settings.SEARCH_MODE, SearchBoxMode.NEI_MANUAL_SEARCH, "AppEng.GuiITooltip.SearchMode",
					"AppEng.GuiITooltip.SearchMode_NEIStandard" );

			registerApp( 64, Settings.SORT_BY, SortOrder.NAME, "AppEng.GuiITooltip.SortBy", "AppEng.GuiITooltip.ItemName" );
			registerApp( 65, Settings.SORT_BY, SortOrder.AMOUNT, "AppEng.GuiITooltip.SortBy", "AppEng.GuiITooltip.NumberOfItems" );
			// registerApp( 66, Settings.SORT_BY, SortOrder.PRIORITY, "AppEng.GuiITooltip.SortBy",
			// "AppEng.GuiITooltip.PriorityCellOrder" );
			registerApp( 68, Settings.SORT_BY, SortOrder.MOD, "AppEng.GuiITooltip.SortBy", "AppEng.GuiITooltip.ItemID" );

			registerApp( 66, Settings.ACTIONS, ActionItems.WRENCH, "AppEng.GuiITooltip.PartitionStorage", "AppEng.GuiITooltip.PartitionStorageHint" );
			registerApp( 6, Settings.ACTIONS, ActionItems.CLOSE, "AppEng.Gui.Clear", "AppEng.GuiITooltip.ClearCraftingGrid" );

			registerApp( 16, Settings.VIEW_MODE, ViewItems.STORED, "AppEng.GuiITooltip.View", "AppEng.GuiITooltip.StoredItems" );
			registerApp( 18, Settings.VIEW_MODE, ViewItems.ALL, "AppEng.GuiITooltip.View", "AppEng.GuiITooltip.StoredCraftable" );
			registerApp( 19, Settings.VIEW_MODE, ViewItems.CRAFTABLE, "AppEng.GuiITooltip.View", "AppEng.GuiITooltip.Craftable" );

			registerApp( 16 * 6 + 0, Settings.FUZZY_MODE, FuzzyMode.PERCENT_25, "AppEng.GuiITooltip.FuzzyMode", "AppEng.GuiITooltip.FuzzyMode.Percent_25" );
			registerApp( 16 * 6 + 1, Settings.FUZZY_MODE, FuzzyMode.PERCENT_50, "AppEng.GuiITooltip.FuzzyMode", "AppEng.GuiITooltip.FuzzyMode.Percent_50" );
			registerApp( 16 * 6 + 2, Settings.FUZZY_MODE, FuzzyMode.PERCENT_75, "AppEng.GuiITooltip.FuzzyMode", "AppEng.GuiITooltip.FuzzyMode.Percent_75" );
			registerApp( 16 * 6 + 3, Settings.FUZZY_MODE, FuzzyMode.PERCENT_99, "AppEng.GuiITooltip.FuzzyMode", "AppEng.GuiITooltip.FuzzyMode.Percent_99" );
			registerApp( 16 * 6 + 4, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, "AppEng.GuiITooltip.FuzzyMode", "AppEng.GuiITooltip.FuzzyMode.IgnoreAll" );

			registerApp( 80, Settings.FULLNESS_MODE, FullnessMode.EMPTY, "AppEng.GuiITooltip.OperationMode", "AppEng.GuiITooltip.MoveWhenEmpty" );
			registerApp( 81, Settings.FULLNESS_MODE, FullnessMode.HALF, "AppEng.GuiITooltip.OperationMode", "AppEng.GuiITooltip.MoveWhenWorkIsDone" );
			registerApp( 82, Settings.FULLNESS_MODE, FullnessMode.FULL, "AppEng.GuiITooltip.OperationMode", "AppEng.GuiITooltip.MoveWhenFull" );

			registerApp( 16 * 8 + 0, Settings.TRASH_CATCH, YesNo.YES, "AppEng.GuiITooltip.TrashController", "AppEng.GuiITooltip.Disabled" );
			registerApp( 16 * 8 + 1, Settings.TRASH_CATCH, YesNo.NO, "AppEng.GuiITooltip.TrashController", "AppEng.GuiITooltip.Enable" );

			registerApp( 16 * 1 + 5, Settings.BLOCK, YesNo.YES, "AppEng.GuiITooltip.InterfaceBlockingMode", "AppEng.GuiITooltip.Blocking" );
			registerApp( 16 * 1 + 4, Settings.BLOCK, YesNo.NO, "AppEng.GuiITooltip.InterfaceBlockingMode", "AppEng.GuiITooltip.NonBlocking" );

			registerApp( 19, Settings.CRAFT, YesNo.YES, "AppEng.GuiITooltip.InterfaceCraftingMode", "AppEng.GuiITooltip.Craft" );
			registerApp( 17, Settings.CRAFT, YesNo.NO, "AppEng.GuiITooltip.InterfaceCraftingMode", "AppEng.GuiITooltip.DontCraft" );
		}
	}

	@Override
	public boolean isVisible()
	{
		return drawButton;
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
	{
		if ( this.drawButton )
		{
			int iconIndex = getIconIndex();

			if ( halfSize )
			{
				width = 8;
				height = 8;

				GL11.glPushMatrix();
				GL11.glTranslatef( this.xPosition, this.yPosition, 0.0F );
				GL11.glScalef( 0.5f, 0.5f, 0.5f );

				GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
				par1Minecraft.renderEngine.bindTexture( ExtraTextures.GuiTexture( "guis/states.png" ) );
				this.field_82253_i = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width
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
				GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
				par1Minecraft.renderEngine.bindTexture( ExtraTextures.GuiTexture( "guis/states.png" ) );
				this.field_82253_i = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width
						&& par3 < this.yPosition + this.height;

				int uv_y = (int) Math.floor( iconIndex / 16 );
				int uv_x = iconIndex - uv_y * 16;

				this.drawTexturedModalRect( this.xPosition, this.yPosition, 256 - 16, 256 - 16, 16, 16 );
				this.drawTexturedModalRect( this.xPosition, this.yPosition, uv_x * 16, uv_y * 16, 16, 16 );
				this.mouseDragged( par1Minecraft, par2, par3 );
			}
		}
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
