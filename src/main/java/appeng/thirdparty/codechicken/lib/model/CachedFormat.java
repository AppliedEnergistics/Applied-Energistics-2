/*
 * Copyright (c) 2018 covers1624
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package appeng.thirdparty.codechicken.lib.model;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;


/**
 * A simple VertexFormat cache.
 * This caches the existence of attributes and their indexes.
 *
 * @author covers1624
 */
public class CachedFormat
{

	public static final Map<VertexFormat, CachedFormat> formatCache = new ConcurrentHashMap<>();

	/**
	 * Lookup or create the CachedFormat for a given VertexFormat.
	 *
	 * @param format The format to lookup.
	 *
	 * @return The CachedFormat.
	 */
	public static CachedFormat lookup( VertexFormat format )
	{
		return formatCache.computeIfAbsent( format, CachedFormat::new );
	}

	public VertexFormat format;

	public boolean hasPosition;
	public boolean hasNormal;
	public boolean hasColor;
	public boolean hasUV;
	public boolean hasLightMap;

	public int positionIndex = -1;
	public int normalIndex = -1;
	public int colorIndex = -1;
	public int uvIndex = -1;
	public int lightMapIndex = -1;

	public int elementCount;

	/**
	 * Caches the vertex format element indexes for efficiency.
	 *
	 * @param format The format.
	 */
	public CachedFormat( VertexFormat format )
	{
		this.format = format;
		elementCount = format.getElementCount();
		for( int i = 0; i < elementCount; i++ )
		{
			VertexFormatElement element = format.getElement( i );
			switch( element.getUsage() )
			{
				case POSITION:
					if( hasPosition )
					{
						throw new IllegalStateException( "Found 2 position elements.." );
					}
					hasPosition = true;
					positionIndex = i;
					break;
				case NORMAL:
					if( hasNormal )
					{
						throw new IllegalStateException( "Found 2 normal elements.." );
					}
					hasNormal = true;
					normalIndex = i;
					break;
				case COLOR:
					if( hasColor )
					{
						throw new IllegalStateException( "Found 2 color elements.." );
					}
					hasColor = true;
					colorIndex = i;
					break;
				case UV:
					if( element.getIndex() == 0 )
					{
						if( hasUV )
						{
							throw new IllegalStateException( "Found 2 UV elements.." );
						}
						hasUV = true;
						uvIndex = i;
						break;
					}
					else if( element.getIndex() == 1 )
					{
						if( hasLightMap )
						{
							throw new IllegalStateException( "Found 2 LightMap elements.." );
						}
						hasLightMap = true;
						lightMapIndex = i;
						break;
					}
					break;
			}
		}
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj )
		{
			return true;
		}
		if( !( obj instanceof CachedFormat ) )
		{
			return false;
		}
		CachedFormat other = (CachedFormat) obj;
		return other.elementCount == elementCount &&//
				other.positionIndex == positionIndex &&//
				other.normalIndex == normalIndex &&//
				other.colorIndex == colorIndex &&//
				other.uvIndex == uvIndex &&//
				other.lightMapIndex == lightMapIndex;
	}

	@Override
	public int hashCode()
	{
		int result = 1;
		result = 31 * result + elementCount;
		result = 31 * result + positionIndex;
		result = 31 * result + normalIndex;
		result = 31 * result + colorIndex;
		result = 31 * result + uvIndex;
		result = 31 * result + lightMapIndex;
		return result;
	}
}
