package appeng.items.storage;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import appeng.api.implementations.IStorageCell;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.items.materials.MaterialType;
import appeng.me.storage.CellInventory;
import appeng.me.storage.CellInventoryHandler;

public class ItemBasicStorageCell extends AEBaseItem implements IStorageCell
{

	final MaterialType component;
	final int totalBytes;
	final double idleDrain;

	public ItemBasicStorageCell(MaterialType whichCell, int Kilobytes) {
		super( ItemBasicStorageCell.class, Kilobytes + "k" );
		setfeature( EnumSet.of( AEFeature.StorageCells ) );
		maxStackSize = 1;
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
		CellInventory cd = ((CellInventoryHandler) CellInventory.getCell( i )).getCellInv();

		if ( cd != null )
		{
			l.add( cd.usedBytes() + " " + StatCollector.translateToLocal( "Appeng.GuiITooltip.Of" ) + " " + cd.totalBytes() + " "
					+ StatCollector.translateToLocal( "Appeng.GuiITooltip.BytesUsed" ) );
			l.add( cd.storedItemTypes() + " " + StatCollector.translateToLocal( "Appeng.GuiITooltip.Of" ) + " " + cd.getTotalItemTypes() + " "
					+ StatCollector.translateToLocal( "Appeng.GuiITooltip.Types" ) );
			/*
			 * if ( cd.isPreformatted() ) { String List = StatCollector.translateToLocal( cd.getListMode() ==
			 * ListMode.WHITELIST ? "AppEng.Gui.Whitelisted" : "AppEng.Gui.Blacklisted" ); if ( cd.isFuzzyPreformatted()
			 * ) l.add( StatCollector.translateToLocal( "Appeng.GuiITooltip.Partitioned" ) + " - " + List + " " +
			 * StatCollector.translateToLocal( "Appeng.GuiITooltip.Fuzzy" ) ); else l.add(
			 * StatCollector.translateToLocal( "Appeng.GuiITooltip.Partitioned" ) + " - " + List + " " +
			 * StatCollector.translateToLocal( "Appeng.GuiITooltip.Precise" ) ); }
			 */
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
}
