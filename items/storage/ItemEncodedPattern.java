package appeng.items.storage;

import java.util.EnumSet;

import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;

public class ItemEncodedPattern extends AEBaseItem
{

	public ItemEncodedPattern() {
		super( ItemEncodedPattern.class );
		setfeature( EnumSet.of( AEFeature.Crafting ) );
		setMaxStackSize( 1 );
	}

}
