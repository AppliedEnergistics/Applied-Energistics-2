/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.cablebus;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import appeng.api.parts.IPartModel;
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

	// Indicate on which sides signified by connectionTypes above, there is another cable bus. If a side is connected,
	// but it is absent from this
	// set, then it means that there is a Grid host, but not a cable bus on that side (i.e. an interface, a controller,
	// etc.)
	private EnumSet<EnumFacing> cableBusAdjacent = EnumSet.noneOf( EnumFacing.class );

	// Specifies the number of channels used for the connection to a given side. Only contains entries if
	// connections contains a corresponding entry.
	private EnumMap<EnumFacing, Integer> channelsOnSide = new EnumMap<>( EnumFacing.class );

	private EnumMap<EnumFacing, IPartModel> attachments = new EnumMap<>( EnumFacing.class );

	// For each attachment, this contains the distance from the edge until which a cable connection should be drawn
	private EnumMap<EnumFacing, Integer> attachmentConnections = new EnumMap<>( EnumFacing.class );

	// Contains the facade to use for each side that has a facade attached
	private EnumMap<EnumFacing, FacadeRenderState> facades = new EnumMap<>( EnumFacing.class );

	// Contains the bounding boxes of all parts on the cable bus to allow facades to cut out holes for the parts. This
	// list is only populated if there are
	// facades on this cable bus
	private List<AxisAlignedBB> boundingBoxes = new ArrayList<>();

	public CableCoreType getCoreType()
	{
		return this.coreType;
	}

	public void setCoreType( CableCoreType coreType )
	{
		this.coreType = coreType;
	}

	public AECableType getCableType()
	{
		return this.cableType;
	}

	public void setCableType( AECableType cableType )
	{
		this.cableType = cableType;
	}

	public AEColor getCableColor()
	{
		return this.cableColor;
	}

	public void setCableColor( AEColor cableColor )
	{
		this.cableColor = cableColor;
	}

	public EnumMap<EnumFacing, Integer> getChannelsOnSide()
	{
		return this.channelsOnSide;
	}

	public EnumMap<EnumFacing, AECableType> getConnectionTypes()
	{
		return this.connectionTypes;
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
		return this.cableBusAdjacent;
	}

	public void setCableBusAdjacent( EnumSet<EnumFacing> cableBusAdjacent )
	{
		this.cableBusAdjacent = cableBusAdjacent;
	}

	public EnumMap<EnumFacing, IPartModel> getAttachments()
	{
		return this.attachments;
	}

	public EnumMap<EnumFacing, Integer> getAttachmentConnections()
	{
		return this.attachmentConnections;
	}

	public EnumMap<EnumFacing, FacadeRenderState> getFacades()
	{
		return this.facades;
	}

	public List<AxisAlignedBB> getBoundingBoxes()
	{
		return this.boundingBoxes;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( attachmentConnections == null ) ? 0 : attachmentConnections.hashCode() );
		result = prime * result + ( ( attachments == null ) ? 0 : attachments.hashCode() );
		result = prime * result + ( ( boundingBoxes == null ) ? 0 : boundingBoxes.hashCode() );
		result = prime * result + ( ( cableBusAdjacent == null ) ? 0 : cableBusAdjacent.hashCode() );
		result = prime * result + ( ( cableColor == null ) ? 0 : cableColor.hashCode() );
		result = prime * result + ( ( cableType == null ) ? 0 : cableType.hashCode() );
		result = prime * result + ( ( channelsOnSide == null ) ? 0 : channelsOnSide.hashCode() );
		result = prime * result + ( ( connectionTypes == null ) ? 0 : connectionTypes.hashCode() );
		result = prime * result + ( ( coreType == null ) ? 0 : coreType.hashCode() );
		result = prime * result + ( ( facades == null ) ? 0 : facades.hashCode() );
		return result;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj )
		{
			return true;
		}
		if( obj == null )
		{
			return false;
		}
		if( getClass() != obj.getClass() )
		{
			return false;
		}
		final CableBusRenderState other = (CableBusRenderState) obj;
		if( attachmentConnections == null )
		{
			if( other.attachmentConnections != null )
			{
				return false;
			}
		}
		else if( !attachmentConnections.equals( other.attachmentConnections ) )
		{
			return false;
		}
		if( attachments == null )
		{
			if( other.attachments != null )
			{
				return false;
			}
		}
		else if( !attachments.equals( other.attachments ) )
			return false;
		if( boundingBoxes == null )
		{
			if( other.boundingBoxes != null )
			{
				return false;
			}
		}
		else if( !boundingBoxes.equals( other.boundingBoxes ) )
		{
			return false;
		}
		if( cableBusAdjacent == null )
		{
			if( other.cableBusAdjacent != null )
			{
				return false;
			}
		}
		else if( !cableBusAdjacent.equals( other.cableBusAdjacent ) )
		{
			return false;
		}
		if( cableColor != other.cableColor )
		{
			return false;
		}
		if( cableType != other.cableType )
		{
			return false;
		}
		if( channelsOnSide == null )
		{
			if( other.channelsOnSide != null )
			{
				return false;
			}
		}
		else if( !channelsOnSide.equals( other.channelsOnSide ) )
		{
			return false;
		}
		if( connectionTypes == null )
		{
			if( other.connectionTypes != null )
			{
				return false;
			}
		}
		else if( !connectionTypes.equals( other.connectionTypes ) )
		{
			return false;
		}
		if( coreType != other.coreType )
		{
			return false;
		}
		if( facades == null )
		{
			if( other.facades != null )
			{
				return false;
			}
		}
		else if( !facades.equals( other.facades ) )
		{
			return false;
		}
		return true;
	}

}
