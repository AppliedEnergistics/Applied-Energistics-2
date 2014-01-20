package appeng.items.storage;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.IItemGroup;
import appeng.api.implementations.IStorageCell;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.materials.MaterialType;
import appeng.me.storage.CellInventory;
import appeng.me.storage.CellInventoryHandler;
import appeng.util.Platform;

public class ItemBasicStorageCell extends AEBaseItem implements IStorageCell, IItemGroup
{

	final MaterialType component;
	final int totalBytes;
	final double idleDrain;

	public ItemBasicStorageCell(MaterialType whichCell, int Kilobytes) {
		super( ItemBasicStorageCell.class, Kilobytes + "k" );
		setfeature( EnumSet.of( AEFeature.StorageCells ) );
		setMaxStackSize( 1 );
		totalBytes = Kilobytes * 1024;
		component = whichCell;

		switch (component)
		{
		case Cell1kPart:
			idleDrain = 0.5;
			break;
		case Cell4kPart:
			idleDrain = 1.0;
			break;
		case Cell16kPart:
			idleDrain = 1.5;
			break;
		case Cell64kPart:
			idleDrain = 2.0;
			break;
		default:
			idleDrain = 0.0;
		}
	}

	@Override
	public void addInformation(ItemStack i, EntityPlayer p, List l, boolean b)
	{
		IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( i, StorageChannel.ITEMS );

		if ( cdi instanceof CellInventoryHandler )
		{
			CellInventory cd = ((CellInventoryHandler) cdi).getCellInv();
			if ( cd != null )
			{
				l.add( cd.usedBytes() + " " + GuiText.Of.getLocal() + " " + cd.totalBytes() + " " + GuiText.BytesUsed.getLocal() );
				l.add( cd.storedItemTypes() + " " + GuiText.Of.getLocal() + " " + cd.getTotalItemTypes() + " " + GuiText.Types.getLocal() );
				/*
				 * if ( cd.isPreformatted() ) { String List = StatCollector.translateToLocal( cd.getListMode() ==
				 * ListMode.WHITELIST ? "AppEng.Gui.Whitelisted" : "AppEng.Gui.Blacklisted" ); if (
				 * cd.isFuzzyPreformatted() ) l.add( StatCollector.translateToLocal( "Appeng.GuiITooltip.Partitioned" )
				 * + " - " + List + " " + StatCollector.translateToLocal( "Appeng.GuiITooltip.Fuzzy" ) ); else l.add(
				 * StatCollector.translateToLocal( "Appeng.GuiITooltip.Partitioned" ) + " - " + List + " " +
				 * StatCollector.translateToLocal( "Appeng.GuiITooltip.Precise" ) ); }
				 */
			}
		}
	}

	@Override
	public int getBytes(ItemStack cellItem)
	{
		return totalBytes;
	}

	@Override
	public int BytePerType(ItemStack iscellItem)
	{
		return 8;
	}

	@Override
	public int getTotalTypes(ItemStack cellItem)
	{
		return 63;
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
		return idleDrain;
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
	public String getUnlocalizedGroupName(ItemStack is)
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable(ItemStack is)
	{
		return true;
	}

}
