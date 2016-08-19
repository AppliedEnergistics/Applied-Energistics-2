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


public enum AECableType
{
	/**
	 * No Cable present.
	 */
	NONE( null, 0 ),

	/**
	 * Connections to this block should render as glass.
	 */
	GLASS( "glass", 0 ),

	/**
	 * Connections to this block should render as covered.
	 */
	COVERED( "covered", 0 ),

	/**
	 * Connections to this block should render as smart.
	 */
	SMART( "smart", 8 ),

	/**
	 * Dense Cable, represents a tier 2 block that can carry 32 channels.
	 */
	DENSE( "dense", 32 );

	public static final AECableType[] VALIDCABLES = { GLASS, COVERED, SMART, DENSE };

	private final String type;
	private final int displayedChannels;
	private final ResourceLocation model;
	private final ResourceLocation connectionModel;
	private final ResourceLocation straightModel;

	private AECableType( String type, int displayedChannels )
	{
		this.type = type;
		this.displayedChannels = displayedChannels;
		this.model = new ResourceLocation( "appliedenergistics2", "part/cable/" + type + "/center" );
		this.connectionModel = new ResourceLocation( "appliedenergistics2", "part/cable/" + type + "/connection" );
		this.straightModel = new ResourceLocation( "appliedenergistics2", "part/cable/" + type + "/straight" );
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

}
