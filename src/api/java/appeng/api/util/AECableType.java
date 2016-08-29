/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.util;


import net.minecraft.util.ResourceLocation;

import appeng.client.render.cablebus.CableCoreType;
import appeng.core.AppEng;


public enum AECableType
{
	/**
	 * No Cable present.
	 */
	NONE( null, 0, null, null ),

	/**
	 * Connections to this block should render as glass.
	 */
	GLASS( "glass", 0, CableCoreType.GLASS, "parts/cable/glass/" ),

	/**
	 * Connections to this block should render as covered.
	 */
	COVERED( "covered", 0, CableCoreType.COVERED, "parts/cable/covered/" ),

	/**
	 * Connections to this block should render as smart.
	 */
	SMART( "smart", 8, CableCoreType.COVERED, "parts/cable/smart/" ),

	/**
	 * Dense Cable, represents a tier 2 block that can carry 32 channels.
	 */
	DENSE( "dense", 32, CableCoreType.DENSE, "parts/cable/dense/" );

	public static final AECableType[] VALIDCABLES = {
			GLASS,
			COVERED,
			SMART,
			DENSE
	};

	private final CableCoreType coreType;
	private final String type;
	private final int displayedChannels;
	private final ResourceLocation model;
	private final ResourceLocation connectionModel;
	private final ResourceLocation straightModel;
	private final String textureFolder;

	private AECableType( String type, int displayedChannels, CableCoreType coreType, String textureFolder )
	{
		this.type = type;
		this.displayedChannels = displayedChannels;
		this.model = new ResourceLocation( "appliedenergistics2", "part/cable/" + type + "/center" );
		this.connectionModel = new ResourceLocation( "appliedenergistics2", "part/cable/" + type + "/connection" );
		this.straightModel = new ResourceLocation( "appliedenergistics2", "part/cable/" + type + "/straight" );
		this.coreType = coreType;
		this.textureFolder = textureFolder;
	}

	public int displayedChannels()
	{
		return displayedChannels;
	}

	public ResourceLocation getModel()
	{
		return model;
	}

	public ResourceLocation getConnectionModel()
	{
		return connectionModel;
	}

	public ResourceLocation getStraightModel()
	{
		return straightModel;
	}

	/**
	 * @return The type of core that should be rendered when this cable isn't straight and needs to have a core to attach connections to. Is null for the NULL
	 * cable.
	 */
	public CableCoreType getCoreType()
	{
		return coreType;
	}

	public ResourceLocation getConnectionTexture( AEColor color )
	{
		if ( textureFolder == null ) {
			throw new IllegalStateException( "Cable type " + name() + " does not support connections." );
		}
		return new ResourceLocation( AppEng.MOD_ID, textureFolder + color.name().toLowerCase() );
	}

}
