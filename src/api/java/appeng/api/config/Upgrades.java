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

package appeng.api.config;


import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;


public enum Upgrades
{
	/**
	 * Gold Tier Upgrades.
	 */
	CAPACITY( 0 ),
	REDSTONE( 0 ),
	CRAFTING( 0 ),

	/**
	 * Diamond Tier Upgrades.
	 */
	FUZZY( 1 ),
	SPEED( 1 ),
	INVERTER( 1 );

	private final int tier;
	private final Map<ItemStack, Integer> supportedMax = new HashMap<>();

	Upgrades( final int tier )
	{
		this.tier = tier;
	}

	/**
	 * @return list of Items/Blocks that support this upgrade, and how many it supports.
	 */
	public Map<ItemStack, Integer> getSupported()
	{
		return this.supportedMax;
	}

	/**
	 * Registers a specific amount of this upgrade into a specific machine
	 *
	 * @param item machine in which this upgrade can be installed
	 * @param maxSupported amount how many upgrades can be installed
	 */
	public void registerItem( final IItemDefinition item, final int maxSupported )
	{
		item.maybeStack( 1 ).ifPresent( is -> this.registerItem( is, maxSupported ) );
	}

	/**
	 * Registers a specific amount of this upgrade into a specific machine
	 *
	 * @param stack machine in which this upgrade can be installed
	 * @param maxSupported amount how many upgrades can be installed
	 */
	public void registerItem( final ItemStack stack, final int maxSupported )
	{
		if( stack != null )
		{
			this.supportedMax.put( stack, maxSupported );
		}
	}

	public int getTier()
	{
		return this.tier;
	}
}
