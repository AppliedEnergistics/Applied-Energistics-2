package appeng.api.implementations.parts;

import appeng.api.networking.IGridHost;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;

/**
 * Implemented on the {@link IPart}s cable objects that can be placed at {@link ForgeDirection}.UNKNOWN in
 * {@link IPartHost}s
 */
public interface IPartCable extends IPart, IGridHost
{

	/**
	 * does this cable support buses?
	 */
	BusSupport supportsBuses();

	/**
	 * @return the current color of the cable.
	 */
	AEColor getCableColor();

	/**
	 * @return the Cable type.
	 */
	AECableType getCableConnectionType();

	/**
	 * Change the color of the cable, this should cost a small amount of dye, or something.
	 * 
	 * @param newColor new color
	 * @return if the color change was successful.
	 */
	boolean changeColor(AEColor newColor, EntityPlayer who);

	/**
	 * Change sides on the cables node.
	 * 
	 * Called by AE, do not invoke.
	 * 
	 * @param sides sides of cable
	 */
	void setValidSides(EnumSet<ForgeDirection> sides);

	/**
	 * used to tests if a cable connects to neighbors visually.
	 * 
	 * @param side neighbor side
	 * @return true if this side is currently connects to an external block.
	 */
	boolean isConnected(ForgeDirection side);

}
