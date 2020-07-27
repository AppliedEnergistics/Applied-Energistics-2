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

package appeng.core.sync.network;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import appeng.core.sync.BasePacket;

public interface NetworkHandler {

    static NetworkHandler instance() {
        return NetworkHandlerHolder.INSTANCE;
    }

    void sendToAll(final BasePacket message);

    void sendTo(final BasePacket message, final ServerPlayerEntity player);

    void sendToAllAround(final BasePacket message, final TargetPoint point);

    void sendToDimension(final BasePacket message, final World world);

    void sendToServer(final BasePacket message);

}
