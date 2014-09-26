package powercrystals.minefactoryreloaded.api.rednet.connectivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines how RedNet cable connects to a block
 * <p>
 * None:				RedNet will never connect to this block (if this is all you want: use IRedNetNoConnection)
 * <p>
 * CableSingle:			Connections will use the cable renderer with a single band, best used for whole blocks
 * <br>
 * PlateSingle:			Connections will use the plate renderer with a single band, used for conveyers and rails
 * <p>
 * CableAll:			Connections permit access to all 16 bands
 * <br>
 * PlateAll:			Connections permit access to all 16 bands
 * <p><p>
 * Forced connection modes are best used for decoration blocks: RedNet will not connect normally,
 * but will if the user forces it. Typically, IRedNetDecorative is desired for this instead
 * <p>
 * ForcedCableSingle:	Connections permit access to a single band, only when the cable is in forced connection mode
 * <br>
 * ForcedPlateSingle:	Connections permit access to a single band, only when the cable is in forced connection mode
 * <p>
 * ForcedCableAll:		Connections permit access to all 16 bands, only when the cable is in forced connection mode
 * <br>
 * ForcedPlateAll:		Connections permit access to all 16 bands, only when the cable is in forced connection mode
 * <p><p>
 * The decorative nodes are for when you want rednet to decide how to connect to your block,
 * but also need to receive full updates from the network.
 * <p>
 * DecorativeSingle:	Connections permit access to a single band, using standard connection logic
 * <br>
 * DecorativeAll:		Connections permit access to all 16 bands, using standard connection logic
 */
public enum RedNetConnectionType
{
	None,				//  0; 0000000
	CableSingle,		// 11; 0001011
	PlateSingle,		// 13; 0001101
	CableAll,			// 19; 0010011
	PlateAll,			// 21; 0010101
	ForcedCableSingle,	// 43; 0101011
	ForcedPlateSingle,	// 45; 0101101
	ForcedCableAll,		// 51; 0110011
	ForcedPlateAll,		// 53; 0110101
	DecorativeSingle,   // NA; 0001001
	DecorativeAll;      // NA; 0010001
	
	public final boolean isConnected = this.ordinal() != 0; // 0 bit (mask: 1)
	public final boolean isSingleSubnet = this.name().endsWith("Single"); // 3 bit (mask: 8)
	public final boolean isAllSubnets = this.name().endsWith("All"); // 4 bit (mask: 16) 
	public final boolean isPlate = this.name().contains("Plate"); // 2 bit (mask: 4)
	public final boolean isCable = this.name().contains("Cable"); // 1 bit (mask: 2)
	public final boolean isConnectionForced = this.name().startsWith("Forced"); // 5 bit (mask: 32)
	public final boolean isDecorative = this.name().startsWith("Decorative");
	public final short flags = toFlags(isConnected, isCable, isPlate,
			isSingleSubnet, isAllSubnets, isConnectionForced);
	
	public static final RedNetConnectionType fromFlags(short flags)
	{
		return connections.get(flags);
	}
	
	private static final short toFlags(boolean ...flags)
	{
		short ret = 0;
		for (int i = flags.length; i --> 0;)
			ret |= (flags[i] ? 1 : 0) << i;
		return ret;
	}
	
	private static final Map<Short, RedNetConnectionType> connections = new HashMap<Short, RedNetConnectionType>();
	
	static {
		for (RedNetConnectionType type : RedNetConnectionType.values())
			connections.put(type.flags, type);
	}
}
