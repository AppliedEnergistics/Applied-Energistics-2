package appeng.items.tools.powered.powersink;

public class AEBasePoweredItem extends ThermalExpansion
{

	public AEBasePoweredItem(Class c, String subname) {
		super( c, subname );
		setMaxStackSize( 1 );
	}
}
