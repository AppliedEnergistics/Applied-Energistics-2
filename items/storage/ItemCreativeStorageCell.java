package appeng.items.storage;

import java.util.EnumSet;

import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;

public class ItemCreativeStorageCell extends AEBaseItem
{

	public ItemCreativeStorageCell() {
		super( ItemCreativeStorageCell.class );
		setfeature( EnumSet.of( AEFeature.StorageCells, AEFeature.Creative ) );
		maxStackSize = 1;
	}

}
