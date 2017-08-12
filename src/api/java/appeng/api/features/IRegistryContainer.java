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

package appeng.api.features;


import appeng.api.AEInjectable;
import appeng.api.movable.IMovableRegistry;
import appeng.api.networking.IGridCacheRegistry;
import appeng.api.parts.IPartModels;
import appeng.api.storage.ICellRegistry;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @author yueh
 * @version rv5
 * @since rv0
 */
@AEInjectable
public interface IRegistryContainer
{

	/**
	 * Use the movable registry to white list your tiles.
	 */
	IMovableRegistry movable();

	/**
	 * Add new Grid Caches for use during run time, only use during loading phase.
	 */
	IGridCacheRegistry gridCache();

	/**
	 * Add additional special comparison functionality, AE Uses this internally for Bees.
	 */
	ISpecialComparisonRegistry specialComparison();

	/**
	 * Lets you register your items as wireless terminals
	 */
	IWirelessTermRegistry wireless();

	/**
	 * Allows you to register new cell types, these will function in drives
	 */
	ICellRegistry cell();

	/**
	 * Manage grinder recipes via API
	 */
	IGrinderRegistry grinder();

	/**
	 * Manage inscriber recipes via API
	 */
	IInscriberRegistry inscriber();

	/**
	 * Manage charger via API
	 */
	IChargerRegistry charger();

	/**
	 * get access to the locatable registry
	 */
	ILocatableRegistry locatable();

	/**
	 * get access to the p2p tunnel registry.
	 */
	IP2PTunnelRegistry p2pTunnel();

	/**
	 * get access to the ammo registry.
	 */
	IMatterCannonAmmoRegistry matterCannon();

	/**
	 * get access to the player registry
	 */
	IPlayerRegistry players();

	/**
	 * get access to the ae2 recipe api
	 */
	IRecipeHandlerRegistry recipes();

	/**
	 * get access to the world-gen api.
	 */
	IWorldGen worldgen();

	/**
	 * Register your IPart models before using them.
	 */
	IPartModels partModels();
}
