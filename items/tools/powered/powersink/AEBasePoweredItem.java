package appeng.items.tools.powered.powersink;

public class AEBasePoweredItem extends UniversalElectricity
{

	public AEBasePoweredItem(Class c, String subname) {
		super( c, subname );
		setMaxStackSize( 1 );
	}
}
