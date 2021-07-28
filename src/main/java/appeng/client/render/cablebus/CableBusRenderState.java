/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.cablebus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;

/**
 * This class captures the entire rendering state needed for a cable bus and transports it to the rendering thread for
 * processing.
 */
public class CableBusRenderState {

    public static final ModelProperty<CableBusRenderState> PROPERTY = new ModelProperty<>();

    // The cable type used for rendering the outgoing connections to other blocks
    // and attached parts
    private AECableType cableType = AECableType.NONE;

    // The type to use for rendering the core of the cable.
    private CableCoreType coreType;

    private AEColor cableColor = AEColor.TRANSPARENT;

    // Describes the outgoing connections of this cable bus to other blocks, and how
    // they should be rendered
    private EnumMap<Direction, AECableType> connectionTypes = new EnumMap<>(Direction.class);

    // Indicate on which sides signified by connectionTypes above, there is another
    // cable bus. If a side is connected,
    // but it is absent from this
    // set, then it means that there is a Grid host, but not a cable bus on that
    // side (i.e. an interface, a controller,
    // etc.)
    private EnumSet<Direction> cableBusAdjacent = EnumSet.noneOf(Direction.class);

    // Specifies the number of channels used for the connection to a given side.
    // Only contains entries if
    // connections contains a corresponding entry.
    private EnumMap<Direction, Integer> channelsOnSide = new EnumMap<>(Direction.class);

    private EnumMap<Direction, IPartModel> attachments = new EnumMap<>(Direction.class);

    // For each attachment, this contains the distance from the edge until which a
    // cable connection should be drawn
    private EnumMap<Direction, Integer> attachmentConnections = new EnumMap<>(Direction.class);

    // Contains the facade to use for each side that has a facade attached
    private EnumMap<Direction, FacadeRenderState> facades = new EnumMap<>(Direction.class);

    // Used for Facades.
    private WeakReference<BlockAndTintGetter> world;
    private BlockPos pos;

    // Contains the bounding boxes of all parts on the cable bus to allow facades to
    // cut out holes for the parts. This
    // list is only populated if there are
    // facades on this cable bus
    private List<AABB> boundingBoxes = new ArrayList<>();

    // Additional model data passed to the part models
    private EnumMap<Direction, IModelData> partModelData = new EnumMap<>(Direction.class);

    public CableCoreType getCoreType() {
        return this.coreType;
    }

    public void setCoreType(CableCoreType coreType) {
        this.coreType = coreType;
    }

    public AECableType getCableType() {
        return this.cableType;
    }

    public void setCableType(AECableType cableType) {
        this.cableType = cableType;
    }

    public AEColor getCableColor() {
        return this.cableColor;
    }

    public void setCableColor(AEColor cableColor) {
        this.cableColor = cableColor;
    }

    public EnumMap<Direction, Integer> getChannelsOnSide() {
        return this.channelsOnSide;
    }

    public EnumMap<Direction, AECableType> getConnectionTypes() {
        return this.connectionTypes;
    }

    public void setConnectionTypes(EnumMap<Direction, AECableType> connectionTypes) {
        this.connectionTypes = connectionTypes;
    }

    public void setChannelsOnSide(EnumMap<Direction, Integer> channelsOnSide) {
        this.channelsOnSide = channelsOnSide;
    }

    public EnumSet<Direction> getCableBusAdjacent() {
        return this.cableBusAdjacent;
    }

    public void setCableBusAdjacent(EnumSet<Direction> cableBusAdjacent) {
        this.cableBusAdjacent = cableBusAdjacent;
    }

    public EnumMap<Direction, IPartModel> getAttachments() {
        return this.attachments;
    }

    public EnumMap<Direction, Integer> getAttachmentConnections() {
        return this.attachmentConnections;
    }

    public EnumMap<Direction, FacadeRenderState> getFacades() {
        return this.facades;
    }

    public BlockAndTintGetter getWorld() {
        return this.world.get();
    }

    public void setWorld(BlockAndTintGetter world) {
        this.world = new WeakReference<>(world);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public List<AABB> getBoundingBoxes() {
        return this.boundingBoxes;
    }

    public EnumMap<Direction, IModelData> getPartModelData() {
        return this.partModelData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.attachmentConnections == null ? 0 : this.attachmentConnections.hashCode());
        result = prime * result + (this.cableBusAdjacent == null ? 0 : this.cableBusAdjacent.hashCode());
        result = prime * result + (this.cableColor == null ? 0 : this.cableColor.hashCode());
        result = prime * result + (this.cableType == null ? 0 : this.cableType.hashCode());
        result = prime * result + (this.channelsOnSide == null ? 0 : this.channelsOnSide.hashCode());
        result = prime * result + (this.connectionTypes == null ? 0 : this.connectionTypes.hashCode());
        result = prime * result + (this.coreType == null ? 0 : this.coreType.hashCode());
        result = prime * result + (this.partModelData == null ? 0 : this.partModelData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final CableBusRenderState other = (CableBusRenderState) obj;

        return this.cableColor == other.cableColor && this.cableType == other.cableType
                && this.coreType == other.coreType
                && Objects.equals(this.attachmentConnections, other.attachmentConnections)
                && Objects.equals(this.cableBusAdjacent, other.cableBusAdjacent)
                && Objects.equals(this.channelsOnSide, other.channelsOnSide)
                && Objects.equals(this.connectionTypes, other.connectionTypes)
                && Objects.equals(this.partModelData, other.partModelData);
    }
}
