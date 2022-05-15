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

package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.parts.IPartHost;
import appeng.core.sync.BasePacket;

/**
 * Packet sent when a player left-clicks on a part attached to a cable bus. This packet contains the hit position to
 * restore the part that was hit on the client.
 */
public class PartLeftClickPacket extends BasePacket {
    private BlockHitResult hitResult;
    private boolean alternateUseMode;

    public PartLeftClickPacket(FriendlyByteBuf stream) {
        this.hitResult = stream.readBlockHitResult();
        this.alternateUseMode = stream.readBoolean();
    }

    public PartLeftClickPacket(BlockHitResult hitResult, boolean alternateUseMode) {
        var data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeBlockHitResult(hitResult);
        data.writeBoolean(alternateUseMode);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(ServerPlayer player) {
        var localPos = hitResult.getLocation().subtract(
                hitResult.getBlockPos().getX(),
                hitResult.getBlockPos().getY(),
                hitResult.getBlockPos().getZ());

        if (player.level.getBlockEntity(hitResult.getBlockPos()) instanceof IPartHost partHost) {
            var selectedPart = partHost.selectPartLocal(localPos);
            if (selectedPart.part != null) {
                if (!alternateUseMode) {
                    selectedPart.part.onClicked(player, localPos);
                } else {
                    selectedPart.part.onShiftClicked(player, localPos);
                }
            }
        }
    }
}
