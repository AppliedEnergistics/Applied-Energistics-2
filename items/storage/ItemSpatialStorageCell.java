package appeng.items.storage;

import java.util.EnumSet;

import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.items.materials.MaterialType;

public class ItemSpatialStorageCell extends AEBaseItem
{

	final MaterialType component;
	final int maxRegion;

	public ItemSpatialStorageCell(MaterialType whichCell, int spatialScale) {
		super( ItemSpatialStorageCell.class, spatialScale + "Cubed" );
		setfeature( EnumSet.of( AEFeature.SpatialIO ) );
		setMaxStackSize( 1 );
		maxRegion = spatialScale;
		component = whichCell;
	}

}
