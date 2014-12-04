package appeng.api.parts;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Reports a selected part from th IPartHost
 */
public class SelectedPart
{

	/**
	 * selected part.
	 */
	public final IPart part;

	/**
	 * facade part.
	 */
	public final IFacadePart facade;

	/**
	 * side the part is mounted too, or {@link ForgeDirection}.UNKNOWN for cables.
	 */
	public final ForgeDirection side;

	public SelectedPart() {
		part = null;
		facade = null;
		side = ForgeDirection.UNKNOWN;
	}

	public SelectedPart(IPart part, ForgeDirection side) {
		this.part = part;
		facade = null;
		this.side = side;
	}

	public SelectedPart(IFacadePart facade, ForgeDirection side) {
		part = null;
		this.facade = facade;
		this.side = side;
	}

}
