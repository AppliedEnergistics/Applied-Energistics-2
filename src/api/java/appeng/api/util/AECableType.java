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


public enum AECableType
{
	/**
	 * No Cable present.
	 */
	NONE( AECableVariant.NONE, AECableSize.NONE ),

	/**
	 * Connections to this block should render as glass.
	 */
	GLASS( AECableVariant.GLASS, AECableSize.NORMAL ),

	/**
	 * Connections to this block should render as covered.
	 */
	COVERED( AECableVariant.COVERED, AECableSize.NORMAL ),

	/**
	 * Connections to this block should render as smart.
	 */
	SMART( AECableVariant.SMART, AECableSize.NORMAL ),

	/**
	 * Smart Dense Cable, represents a tier 2 block that can carry 32 channels.
	 */
	DENSE_COVERED( AECableVariant.COVERED, AECableSize.DENSE ),

	/**
	 * Smart Dense Cable, represents a tier 2 block that can carry 32 channels.
	 */
	DENSE_SMART( AECableVariant.SMART, AECableSize.DENSE );

	public static final AECableType[] VALIDCABLES = {
			GLASS,
			COVERED,
			SMART,
			DENSE_COVERED,
			DENSE_SMART
	};

	private final AECableVariant variant;
	private final AECableSize size;

	private AECableType( AECableVariant variant, AECableSize size )
	{
		this.variant = variant;
		this.size = size;
	}

	public AECableSize size()
	{
		return this.size;
	}

	public AECableVariant variant()
	{
		return this.variant;
	}

	public boolean isValid()
	{
		return this.variant != AECableVariant.NONE && this.size != AECableSize.NONE;
	}

	public boolean isDense()
	{
		return this.size == AECableSize.DENSE;
	}

	public boolean isSmart()
	{
		return this.variant == AECableVariant.SMART;
	}

	public static AECableType min( AECableType a, AECableType b )
	{
		final AECableVariant v = AECableVariant.min( a.variant(), b.variant() );
		final AECableSize s = AECableSize.min( a.size(), b.size() );

		return AECableType.from( v, s );
	}

	public static AECableType max( AECableType a, AECableType b )
	{
		final AECableVariant v = AECableVariant.max( a.variant(), b.variant() );
		final AECableSize s = AECableSize.max( a.size(), b.size() );

		return AECableType.from( v, s );
	}

	private static AECableType from( AECableVariant variant, AECableSize size )
	{
		switch( variant )
		{
			case GLASS:
				switch( size )
				{
					case NORMAL:
						return GLASS;
					default:
						break;
				}

				break;
			case COVERED:
				switch( size )
				{
					case NORMAL:
						return COVERED;
					case DENSE:
						return DENSE_COVERED;
					default:
						break;
				}

				break;
			case SMART:
				switch( size )
				{
					case NORMAL:
						return SMART;
					case DENSE:
						return DENSE_SMART;
					default:
						break;
				}
				break;
			default:
				break;
		}

		return NONE;
	}
}
