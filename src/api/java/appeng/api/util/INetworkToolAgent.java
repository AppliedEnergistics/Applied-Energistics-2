package appeng.api.util;

import net.minecraft.util.MovingObjectPosition;

/**
 * Implement on Tile or part to customize if the info gui opens, or an action is preformed.
 */
public interface INetworkToolAgent
{

	boolean showNetworkInfo(MovingObjectPosition where);

}
