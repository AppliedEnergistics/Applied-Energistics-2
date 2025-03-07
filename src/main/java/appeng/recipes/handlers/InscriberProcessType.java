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

package appeng.recipes.handlers;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public enum InscriberProcessType implements StringRepresentable {
    /**
     * Doesn't spend the optional inputs (top and bottom). Used for example to make printed circuits, or copy presses.
     */
    INSCRIBE("inscribe"),

    /**
     * Spends the optional inputs. Used for example to turn printed circuits into processors.
     */
    PRESS("press");

    private final String serializedName;

    public static Codec<InscriberProcessType> CODEC = StringRepresentable.fromEnum(InscriberProcessType::values);

    public static StreamCodec<FriendlyByteBuf, InscriberProcessType> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(InscriberProcessType.class);

    InscriberProcessType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
