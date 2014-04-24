package appeng.tile.crafting;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class TileMolecularAssembler extends AENetworkInvTile implements IAEAppEngInventory, ISidedInventory, IUpgradeableHost, IConfigManagerHost
{

	static final int[] sides = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	static final ItemStack is = AEApi.instance().blocks().blockMolecularAssembler.stack( 1 );

	private AppEngInternalInventory inv = new AppEngInternalInventory( this, 9 + 2 );
	private IConfigManager settings = new ConfigManager( this );
	private UpgradeInventory upgrades = new UpgradeInventory( is, this, getUpgradeSlots() );

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return upgrades.getInstalledUpgrades( u );
	}

	protected int getUpgradeSlots()
	{
		return 5;
	}

	private class TileMolecularAssemblerHandler extends AETileEventHandler
	{

		public TileMolecularAssemblerHandler() {
			super( TileEventType.WORLD_NBT );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			upgrades.writeToNBT( data, "upgrades" );
			inv.writeToNBT( data, "inv" );
			settings.writeToNBT( data );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			upgrades.readFromNBT( data, "upgrades" );
			inv.readFromNBT( data, "inv" );
			settings.readFromNBT( data );
		}

	};

	public TileMolecularAssembler() {
		settings.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		addNewHandler( new TileMolecularAssemblerHandler() );
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if ( i >= 9 )
			return false;

		if ( hasPattern() )
		{

		}

		return false;
	}

	private boolean hasPattern()
	{
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 9;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{

	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection whichSide)
	{
		return sides;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return settings;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "upgrades" ) )
			return upgrades;

		if ( name.equals( "mac" ) )
			return inv;

		return null;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{

	}

}
