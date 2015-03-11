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

package appeng.api.parts;


import java.io.IOException;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.networking.IGridNode;


public interface IPart extends IBoxProvider
{

	/**
	 * get an ItemStack that represents the bus, should contain the settings for whatever, can also be used in
	 * conjunction with removePart to take a part off and drop it or something.
	 *
	 * This is used to drop the bus, and to save the bus, when saving the bus, wrenched is false, and writeToNBT will be
	 * called to save important details about the part, if the part is wrenched include in your NBT Data any settings
	 * you might want to keep around, you can restore those settings when constructing your part.
	 *
	 * @param type , what kind of ItemStack to return?
	 *
	 * @return item of part
	 */
	ItemStack getItemStack( PartItemStack type );

	/**
	 * render item form for inventory, or entity.
	 *
	 * GL Available
	 *
	 * @param rh       helper
	 * @param renderer renderer
	 */
	@SideOnly( Side.CLIENT )
	void renderInventory( IPartRenderHelper rh, RenderBlocks renderer );

	/**
	 * render world renderer ( preferred )
	 *
	 * GL is NOT Available
	 *
	 * @param x        x coord
	 * @param y        y coord
	 * @param z        z coord
	 * @param rh       helper
	 * @param renderer renderer
	 */
	@SideOnly( Side.CLIENT )
	void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer );

	/**
	 * render TESR.
	 *
	 * GL Available
	 *
	 * @param x        x coord
	 * @param y        y coord
	 * @param z        z coord
	 * @param rh       helper
	 * @param renderer renderer
	 */
	@SideOnly( Side.CLIENT )
	void renderDynamic( double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer );

	/**
	 * @return the Block sheet icon used when rendering the breaking particles, return null to use the ItemStack
	 * texture.
	 */
	@SideOnly( Side.CLIENT )
	IIcon getBreakingTexture();

	/**
	 * return true only if your part require dynamic rendering, must be consistent.
	 *
	 * @return true to enable renderDynamic
	 */
	boolean requireDynamicRender();

	/**
	 * @return if the bus has a solid side, and you can place random stuff on it like torches or levers
	 */
	boolean isSolid();

	/**
	 * @return true if this part can connect to redstone ( also MFR Rednet )
	 */
	boolean canConnectRedstone();

	/**
	 * Write the part information for saving, the part will be saved with getItemStack(false) and this method will be
	 * called after to load settings, inventory or other values from the world.
	 *
	 * @param data to be written nbt data
	 */
	void writeToNBT( NBTTagCompound data );

	/**
	 * Read the previously written NBT Data. this is the mirror for writeToNBT
	 *
	 * @param data to be read nbt data
	 */
	void readFromNBT( NBTTagCompound data );

	/**
	 * @return get the amount of light produced by the bus
	 */
	int getLightLevel();

	/**
	 * does this part act like a ladder?
	 *
	 * @param entity climbing entity
	 *
	 * @return true if entity can climb
	 */
	boolean isLadder( EntityLivingBase entity );

	/**
	 * a block around the bus's host has been changed.
	 */
	void onNeighborChanged();

	/**
	 * @return output redstone on facing side
	 */
	int isProvidingStrongPower();

	/**
	 * @return output redstone on facing side
	 */
	int isProvidingWeakPower();

	/**
	 * write data to bus packet.
	 *
	 * @param data to be written data
	 *
	 * @throws IOException
	 */
	void writeToStream( ByteBuf data ) throws IOException;

	/**
	 * read data from bus packet.
	 *
	 * @param data to be read data
	 *
	 * @return true will re-draw the part.
	 *
	 * @throws IOException
	 */
	boolean readFromStream( ByteBuf data ) throws IOException;

	/**
	 * get the Grid Node for the Bus, be sure your IGridBlock is NOT isWorldAccessible, if it is your going to cause
	 * crashes.
	 *
	 * or null if you don't have a grid node.
	 *
	 * @return grid node
	 */
	IGridNode getGridNode();

	/**
	 * called when an entity collides with the bus.
	 *
	 * @param entity colliding entity
	 */
	void onEntityCollision( Entity entity );

	/**
	 * called when your part is being removed from the world.
	 */
	void removeFromWorld();

	/**
	 * called when your part is being added to the world.
	 */
	void addToWorld();

	/**
	 * used for tunnels.
	 *
	 * @return a grid node that represents the external facing side, these must be isWorldAccessible with the correct
	 * faces marked as external
	 */
	IGridNode getExternalFacingNode();

	/**
	 * called by the Part host to keep your part informed.
	 *
	 * @param host part side
	 * @param tile tile entity of part
	 */
	void setPartHostInfo( ForgeDirection side, IPartHost host, TileEntity tile );

	/**
	 * Called when you right click the part, very similar to Block.onActivateBlock
	 *
	 * @param player right clicking player
	 * @param pos    position of block
	 *
	 * @return if your activate method performed something.
	 */
	boolean onActivate( EntityPlayer player, Vec3 pos );

	/**
	 * Called when you right click the part, very similar to Block.onActivateBlock
	 *
	 * @param player shift right clicking player
	 * @param pos    position of block
	 *
	 * @return if your activate method performed something, you should use false unless you really need it.
	 */
	boolean onShiftActivate( EntityPlayer player, Vec3 pos );

	/**
	 * Add drops to the items being dropped into the world, if your item stores its contents when wrenched use the
	 * wrenched boolean to control what data is saved vs dropped when it is broken.
	 *
	 * @param drops    item drops if wrenched
	 * @param wrenched control flag for wrenched vs broken
	 */
	void getDrops( List<ItemStack> drops, boolean wrenched );

	/**
	 * @return 0 - 8, reasonable default 3-4, this controls the cable connection to the node.
	 */
	int cableConnectionRenderTo();

	/**
	 * same as Block.randomDisplayTick, for but parts.
	 *
	 * @param world world of block
	 * @param x     x coord of block
	 * @param y     y coord of block
	 * @param z     z coord of block
	 * @param r     random
	 */
	void randomDisplayTick( World world, int x, int y, int z, Random r );

	/**
	 * Called when placed in the world by a player, this happens before addWorld.
	 *
	 * @param player placing player
	 * @param held   held item
	 * @param side   placing side
	 */
	void onPlacement( EntityPlayer player, ItemStack held, ForgeDirection side );

	/**
	 * Used to determine which parts can be placed on what cables.
	 *
	 * @param what placed part
	 *
	 * @return true if the part can be placed on this support.
	 */
	boolean canBePlacedOn( BusSupport what );
}