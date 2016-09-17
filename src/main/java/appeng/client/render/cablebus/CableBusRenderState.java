package appeng.client.render.cablebus;


import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import appeng.api.util.AECableType;
import appeng.api.util.AEColor;


/**
 * This class captures the entire rendering state needed for a cable bus and transports it to the rendering thread
 * for processing.
 */
public class CableBusRenderState
{

	// The cable type used for rendering the outgoing connections to other blocks and attached parts
	private AECableType cableType = AECableType.NONE;

	// The type to use for rendering the core of the cable.
	private CableCoreType coreType;

	private AEColor cableColor = AEColor.TRANSPARENT;

	// Describes the outgoing connections of this cable bus to other blocks, and how they should be rendered
	private EnumMap<EnumFacing, AECableType> connectionTypes = new EnumMap<>( EnumFacing.class );

	// Indicate on which sides signified by connectionTypes above, there is another cable bus. If a side is connected, but it is absent from this
	// set, then it means that there is a Grid host, but not a cable bus on that side (i.e. an interface, a controller, etc.)
	private EnumSet<EnumFacing> cableBusAdjacent = EnumSet.noneOf( EnumFacing.class );

	// Specifies the number of channels used for the connection to a given side. Only contains entries if
	// connections contains a corresponding entry.
	private EnumMap<EnumFacing, Integer> channelsOnSide = new EnumMap<>( EnumFacing.class );

	private EnumMap<EnumFacing, List<ResourceLocation>> attachments = new EnumMap<>( EnumFacing.class );

	// For each attachment, this contains the distance from the edge until which a cable connection should be drawn
	private EnumMap<EnumFacing, Integer> attachmentConnections = new EnumMap<>( EnumFacing.class );

	public CableCoreType getCoreType()
	{
		return coreType;
	}

	public void setCoreType( CableCoreType coreType )
	{
		this.coreType = coreType;
	}

	public AECableType getCableType()
	{
		return cableType;
	}

	public void setCableType( AECableType cableType )
	{
		this.cableType = cableType;
	}

	public AEColor getCableColor()
	{
		return cableColor;
	}

	public void setCableColor( AEColor cableColor )
	{
		this.cableColor = cableColor;
	}

	public EnumMap<EnumFacing, Integer> getChannelsOnSide()
	{
		return channelsOnSide;
	}

	public EnumMap<EnumFacing, AECableType> getConnectionTypes()
	{
		return connectionTypes;
	}

	public void setConnectionTypes( EnumMap<EnumFacing, AECableType> connectionTypes )
	{
		this.connectionTypes = connectionTypes;
	}

	public void setChannelsOnSide( EnumMap<EnumFacing, Integer> channelsOnSide )
	{
		this.channelsOnSide = channelsOnSide;
	}

	public EnumSet<EnumFacing> getCableBusAdjacent()
	{
		return cableBusAdjacent;
	}

	public void setCableBusAdjacent( EnumSet<EnumFacing> cableBusAdjacent )
	{
		this.cableBusAdjacent = cableBusAdjacent;
	}

	public EnumMap<EnumFacing, List<ResourceLocation>> getAttachments()
	{
		return attachments;
	}

	public EnumMap<EnumFacing, Integer> getAttachmentConnections()
	{
		return attachmentConnections;
	}

}
