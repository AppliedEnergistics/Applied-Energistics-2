package appeng.items.tools.powered.powersink;

public class AEBasePoweredItem extends RedstoneFlux
{

	public AEBasePoweredItem(Class c, String subname) {
		super( c, subname );
		setMaxStackSize( 1 );
	}
}
