package appeng.items.tools.powered.powersink;

public class AEBasePoweredItem extends RedstoneFlux
{

	public AEBasePoweredItem(Class c, String subName) {
		super( c, subName );
		setMaxStackSize( 1 );
	}
}
