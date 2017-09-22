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

package appeng.api.networking.energy;


import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import appeng.api.config.Actionable;


/**
 * internal use only.
 */
public interface IEnergyGridProvider
{
	/**
	 * internal use only
	 * 
	 * Can return a list of providers behind the current.
	 * 
	 * An example would be something acting as proxy between different {@link IEnergyGrid}s.
	 * 
	 * This can contain duplicate entries, AE will ensure that each provider is only visited once.
	 * 
	 * internal use only
	 */
	@Nonnull
	Collection<IEnergyGridProvider> providers();

	/**
	 * internal use only
	 * 
	 * Extracts the requested amount from the provider.
	 * 
	 * This should never forward a call to another {@link IEnergyGridProvider}, instead return them via
	 * {@link IEnergyGridProvider#providers()}
	 * 
	 * @return the used amount
	 */
	@Nonnegative
	double extractProviderPower( @Nonnegative double amt, @Nonnull Actionable mode );

	/**
	 * Injects the offered amount into the provider.
	 * 
	 * This should never forward a call to another {@link IEnergyGridProvider}, instead return them via
	 * {@link IEnergyGridProvider#providers()}
	 * 
	 * internal use only
	 * 
	 * @return the leftover amount
	 */
	@Nonnegative
	double injectProviderPower( @Nonnegative double amt, @Nonnull Actionable mode );

	/**
	 * internal use only
	 * 
	 * Returns the current demand of an provider.
	 * 
	 * This should never forward a call to another {@link IEnergyGridProvider}, instead return them via
	 * {@link IEnergyGridProvider#providers()}
	 * 
	 * 
	 * @param d the max amount offered, the demand should never exceed it.
	 * @return the total amount demanded
	 */
	@Nonnegative
	double getProviderEnergyDemand( @Nonnegative double d );

	/**
	 * internal use only
	 * 
	 * AE currently uses this to enqueue the next visited provider.
	 * 
	 * There is no guarantee that this works on in a perfect way.
	 * It can be limited to the returns of the past {@link IEnergyGridProvider#providers()}, but not any future one
	 * discovered by visiting further providers.
	 * 
	 * E.g. inject into the the lowest one first or extract from the highest one.
	 * 
	 * @return the current stored amount.
	 * 
	 * 
	 */
	@Nonnegative
	double getProviderStoredEnergy();

	/**
	 * internal use only
	 * 
	 * AE currently uses this to enqueue the next visited provider.
	 * 
	 * There is no guarantee that this works on in a perfect way.
	 * It can be limited to the returns of the past {@link IEnergyGridProvider#providers()}, but not any future one
	 * discovered by visiting further providers.
	 * 
	 * E.g. inject into the the lowest one first or extract from the highest one.
	 * 
	 * @return the maximum amount stored.
	 */
	@Nonnegative
	double getProviderMaxEnergy();
}
