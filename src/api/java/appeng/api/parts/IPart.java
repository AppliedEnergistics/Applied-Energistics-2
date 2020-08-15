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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.AttributeList;

import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;

public interface IPart extends ICustomCableConnection {

    /**
     * get an ItemStack that represents the bus, should contain the settings for
     * whatever, can also be used in conjunction with removePart to take a part off
     * and drop it or something.
     * <p>
     * This is used to drop the bus, and to save the bus, when saving the bus,
     * wrenched is false, and writeToNBT will be called to save important details
     * about the part, if the part is wrenched include in your NBT Data any settings
     * you might want to keep around, you can restore those settings when
     * constructing your part.
     *
     * @param type , what kind of ItemStack to return?
     * @return item of part
     */
    ItemStack getItemStack(PartItemStack type);

    /**
     * Render dynamic portions of this part, as part of the cable bus TESR. This
     * part has to return true for {@link #requireDynamicRender()} in order for this
     * method to be called.
     */
    @Environment(EnvType.CLIENT)
    default void renderDynamic(float partialTicks, MatrixStack matrixStack, VertexConsumerProvider buffers,
            int combinedLightIn, int combinedOverlayIn) {
    }

    /**
     * return true only if your part require dynamic rendering, must be consistent.
     *
     * @return true to enable renderDynamic
     */
    boolean requireDynamicRender();

    /**
     * @return if the bus has a solid side, and you can place random stuff on it
     *         like torches or levers
     */
    boolean isSolid();

    /**
     * @return true if this part can connect to redstone ( also MFR Rednet )
     */
    boolean canConnectRedstone();

    /**
     * Write the part information for saving, the part will be saved with
     * getItemStack(false) and this method will be called after to load settings,
     * inventory or other values from the world.
     *
     * @param data to be written nbt data
     */
    void writeToNBT(CompoundTag data);

    /**
     * Read the previously written NBT Data. this is the mirror for writeToNBT
     *
     * @param data to be read nbt data
     */
    void readFromNBT(CompoundTag data);

    /**
     * @return get the amount of light produced by the bus
     */
    int getLightLevel();

    /**
     * does this part act like a ladder?
     *
     * @param entity climbing entity
     * @return true if entity can climb
     */
    boolean isLadder(LivingEntity entity);

    /**
     * a block around the bus's host has been changed.
     */
    void onNeighborUpdate(BlockView w, BlockPos pos, BlockPos neighbor);

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
     * @throws IOException
     */
    void writeToStream(PacketByteBuf data) throws IOException;

    /**
     * read data from bus packet.
     *
     * @param data to be read data
     * @return true will re-draw the part.
     * @throws IOException
     */
    boolean readFromStream(PacketByteBuf data) throws IOException;

    /**
     * get the Grid Node for the Bus, be sure your IGridBlock is NOT
     * isWorldAccessible, if it is your going to cause crashes.
     * <p>
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
    void onEntityCollision(Entity entity);

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
     * @return a grid node that represents the external facing side, these must be
     *         isWorldAccessible with the correct faces marked as external
     */
    IGridNode getExternalFacingNode();

    /**
     * called by the Part host to keep your part informed.
     *
     * @param host part side
     * @param tile block entity of part
     */
    void setPartHostInfo(AEPartLocation side, IPartHost host, BlockEntity tile);

    /**
     * Called when you right click the part, very similar to Block.onActivateBlock
     *
     * @param player right clicking player
     * @param hand   hand used
     * @param pos    position of block
     * @return if your activate method performed something.
     */
    boolean onActivate(PlayerEntity player, Hand hand, Vec3d pos);

    /**
     * Called when you right click the part, very similar to Block.onActivateBlock
     *
     * @param player shift right clicking player
     * @param hand   hand used
     * @param pos    position of block
     * @return if your activate method performed something, you should use false
     *         unless you really need it.
     */
    boolean onShiftActivate(PlayerEntity player, Hand hand, Vec3d pos);

    /**
     * Called when you left click the part, very similar to Block.onBlockBreakStart
     *
     * @param player left clicking player
     * @param hand   hand used
     * @param pos    position of block
     * @return if your activate method performed something, you should use false
     *         unless you really need it.
     */
    default boolean onClicked(PlayerEntity player, Hand hand, Vec3d pos) {
        return false;
    }

    /**
     * Called when you shift-left click the part, very similar to
     * Block.onBlockBreakStart
     *
     * @param player shift-left clicking player
     * @param hand   hand used
     * @param pos    position of block
     * @return if your activate method performed something, you should use false
     *         unless you really need it.
     */
    default boolean onShiftClicked(PlayerEntity player, Hand hand, Vec3d pos) {
        return false;
    }

    /**
     * Add drops to the items being dropped into the world, if your item stores its
     * contents when wrenched use the wrenched boolean to control what data is saved
     * vs dropped when it is broken.
     *
     * @param drops    item drops if wrenched
     * @param wrenched control flag for wrenched vs broken
     */
    void getDrops(List<ItemStack> drops, boolean wrenched);

    /**
     * @return 0 - 8, reasonable default 3-4, this controls the cable connection to
     *         the node. -1 to render connection yourself.
     */
    @Override
    float getCableConnectionLength(AECableType cable);

    /**
     * same as Block.randomDisplayTick, for but parts.
     *
     * @param world world of block
     * @param pos   location of block
     * @param r     random
     */
    void randomDisplayTick(World world, BlockPos pos, Random r);

    /**
     * Called when placed in the world by a player, this happens before addWorld.
     *
     * @param player placing player
     * @param held   held item
     * @param side   placing side
     */
    void onPlacement(PlayerEntity player, Hand hand, ItemStack held, AEPartLocation side);

    /**
     * Used to determine which parts can be placed on what cables.
     * <p>
     * Dense cables are not allowed for functional (getGridNode returns a node)
     * parts. Doing so will result in crashes.
     *
     * @param what placed part
     * @return true if the part can be placed on this support.
     */
    boolean canBePlacedOn(BusSupport what);

    /**
     * This method is used when a chunk is rebuilt to determine how this part should
     * be rendered. The returned models should represent the part oriented north.
     * They will be automatically rotated to match the part's actual orientation.
     * Tint indices 1-4 can be used in the models to access the parts color.
     *
     * <dl>
     * <dt>Tint Index 1</dt>
     * <dd>The {@link AEColor#blackVariant dark variant color} of the cable that
     * this part is attached to.</dd>
     * <dt>Tint Index 2</dt>
     * <dd>The {@link AEColor#mediumVariant color} of the cable that this part is
     * attached to.</dd>
     * <dt>Tint Index 3</dt>
     * <dd>The {@link AEColor#whiteVariant bright variant color} of the cable that
     * this part is attached to.</dd>
     * <dt>Tint Index 4</dt>
     * <dd>A color variant that is between the cable's {@link AEColor#mediumVariant
     * color} and its {@link AEColor#whiteVariant bright variant}.</dd>
     * </dl>
     *
     * <b>Important:</b> All models must have been registered via the
     * {@link IPartModels} API before use.
     */
    @Nonnull
    default IPartModel getStaticModels() {
        return new IPartModel() {
        };
    }

    /**
     * Implement this method if your part exposes capabilitys. Any requests for
     * capabilities on the cable bus will be forwarded to parts on the appropriate
     * side.
     *
     * @see alexiil.mc.lib.attributes.AttributeProvider
     */
    default void addAllAttributes(AttributeList<?> to) {
    }

    /**
     * Additional rendering data to be passed to the models for rendering this part.
     *
     * @return The rendering data to pass to the model. Only useful if custom models
     *         are used. Can be null to not pass anything.
     */
    @Nullable
    default Object getModelData() {
        return null;
    }

    /**
     * add your collision information to the the list.
     *
     * @param bch collision boxes
     */
    void getBoxes(final IPartCollisionHelper bch);

    /**
     * This will be used by the core to add information about this part to a crash
     * report if it is attached to a host that caused a crash during tick
     * processing.
     *
     * @param section The crash report section the information will be added to.
     */
    default void addEntityCrashInfo(CrashReportSection section) {
    }

}
