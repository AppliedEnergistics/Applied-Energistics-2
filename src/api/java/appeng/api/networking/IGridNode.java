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

package appeng.api.networking;


import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.IAppEngApi;
import appeng.api.util.IReadOnlyCollection;


/**
 * Gives you a view into your Nodes connections and information.
 *
 * updateState, getGrid, destroy are required to implement a proper IGridHost.
 *
 * Don't Implement; Acquire from {@link IAppEngApi}.createGridNode
 */
public interface IGridNode
{

	/**
	 * lets you walk the grid stating at the current node using a IGridVisitor, generally not needed, please use only if
	 * required.
	 *
	 * @param visitor visitor
	 */
	void beginVisit( IGridVisitor visitor );

	/**
	 * inform the node that your IGridBlock has changed its internal state, and force the node to update.
	 *
	 * ALWAYS make sure that your tile entity is in the world, and has its node properly saved to be returned from the
	 * host before updating state,
	 *
	 * If your entity is not in the world, or if you IGridHost returns a different node for the same side you will
	 * likely crash the game.
	 */
	void updateState();

	/**
	 * get the machine represented by the node.
	 *
	 * @return grid host
	 */
	IGridHost getMachine();

	/**
	 * get the grid for the node, this can change at a moments notice.
	 *
	 * @return grid
	 */
	IGrid getGrid();

	/**
	 * By destroying your node, you destroy any connections, and its existence in the grid, use in invalidate, or
	 * onChunkUnload
	 */
	void destroy();

	/**
	 * @return the world the node is located in
	 */
	World getWorld();

	/**
	 * @return a set of the connected sides, UNKNOWN represents an invisible connection
	 */
	EnumSet<ForgeDirection> getConnectedSides();

	/**
	 * lets you iterate a nodes connections
	 *
	 * @return grid connections
	 */
	IReadOnlyCollection<IGridConnection> getConnections();

	/**
	 * @return the IGridBlock for this node
	 */
	IGridBlock getGridBlock();

	/**
	 * Reflects the networks status, returns true only if the network is powered, and the network is not booting, this
	 * also takes into account channels.
	 *
	 * @return true if is Network node active, and participating.
	 */
	boolean isActive();

	/**
	 * this should be called for each node you create, if you have a nodeData compound to load from, you can store all
	 * your nods on a single compound using name.
	 *
	 * Important: You must call this before updateState.
	 *
	 * @param name     nbt name
	 * @param nodeData to be loaded data
	 */
	void loadFromNBT( String name, NBTTagCompound nodeData );

	/**
	 * this should be called for each node you maintain, you can save all your nodes to the same tag with different
	 * names, if you fail to complete the load / save procedure, network state may be lost between game load/saves.
	 *
	 * @param name     nbt name
	 * @param nodeData to be saved data
	 */
	void saveToNBT( String name, NBTTagCompound nodeData );

	/**
	 * @return if the node's channel requirements are currently met, use this for display purposes, use isActive for
	 * status.
	 */
	boolean meetsChannelRequirements();

	/**
	 * see if this node has a certain flag
	 *
	 * @param flag flags
	 *
	 * @return true if has flag
	 */
	boolean hasFlag( GridFlags flag );

	/**
	 * @return the ownerID this represents the person who placed the node.
	 */
	int getPlayerID();

	/**
	 * tell the node who was responsible for placing it, failure to do this may result in in-compatibility with the
	 * security system. Called instead of loadFromNBT when initially placed, once set never required again, the value is saved with the Node NBT.
	 *
	 * @param playerID new player id
	 */
	void setPlayerID( int playerID );
}