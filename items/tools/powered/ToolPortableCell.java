package appeng.items.tools.powered;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.IStorageCell;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.helpers.CellItemViewer;
import appeng.helpers.IGuiItem;
import appeng.items.storage.CellConfig;
import appeng.items.storage.CellUpgrades;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.storage.CellInventory;
import appeng.me.storage.CellInventoryHandler;
import appeng.util.Platform;

public class ToolPortableCell extends AEBasePoweredItem implements IStorageCell, IGuiItem
{

	public ToolPortableCell() {
		super( ToolPortableCell.class, null );
		setfeature( EnumSet.of( AEFeature.PortableCell, AEFeature.StorageCells, AEFeature.PoweredTools ) );
		maxStoredPower = 20000;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer player)
	{
		Platform.openGUI( player, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_PORTABLE_CELL );
		return item;
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer player, List lines, boolean advancedItemTooltips)
	{
		super.addInformation( is, player, lines, advancedItemTooltips );

		IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( is, StorageChannel.ITEMS );

		if ( cdi instanceof CellInventoryHandler )
		{
			CellInventory cd = ((CellInventoryHandler) cdi).getCellInv();
			if ( cd != null )
			{
				lines.add( cd.usedBytes() + " " + GuiText.Of.getLocal() + " " + cd.totalBytes() + " " + GuiText.BytesUsed.getLocal() );
				lines.add( cd.storedItemTypes() + " " + GuiText.Of.getLocal() + " " + cd.getTotalItemTypes() + " " + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public int getBytes(ItemStack cellItem)
	{
		return 512;
	}

	@Override
	public int BytePerType(ItemStack iscellItem)
	{
		return 8;
	}

	@Override
	public int getTotalTypes(ItemStack cellItem)
	{
		return 27;
	}

	@Override
	public boolean isBlackListed(ItemStack cellItem, IAEItemStack requsetedAddition)
	{
		Item i = requsetedAddition.getItem();

		if ( i instanceof IStorageCell )
			return !((IStorageCell) i).storableInStorageCell();

		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell(ItemStack i)
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}

	@Override
	public IInventory getUpgradesInventory(ItemStack is)
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory(ItemStack is)
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is)
	{
		String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch (Throwable t)
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode)
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public boolean isEditable(ItemStack is)
	{
		return true;
	}

	@Override
	public Object getGuiObject(ItemStack is)
	{
		return new CellItemViewer( is );
	}
}
