/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.api.stacks;

import java.util.Objects;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.ValueInput;

import appeng.core.AppEng;
import appeng.core.localization.GuiText;

final class AEFluidKeys extends AEKeyType {
    private static final ResourceLocation ID = AppEng.makeId("f");

    static final AEFluidKeys INSTANCE = new AEFluidKeys();

    private AEFluidKeys() {
        super(ID, AEFluidKey.class, GuiText.Fluids.text());
    }

    @Override
    public MapCodec<? extends AEKey> codec() {
        return AEFluidKey.MAP_CODEC;
    }

    @Override
    public int getAmountPerOperation() {
        // On Forge this was 125mb (so 125/1000th of a bucket)
        return AEFluidKey.AMOUNT_BUCKET * 125 / 1000;
    }

    @Override
    public int getAmountPerByte() {
        return 8 * AEFluidKey.AMOUNT_BUCKET;
    }

    @Override
    public AEFluidKey readFromPacket(RegistryFriendlyByteBuf input) {
        Objects.requireNonNull(input);

        return AEFluidKey.fromPacket(input);
    }

    @Override
    public AEFluidKey loadKeyFromTag(ValueInput input) {
        return AEFluidKey.fromTag(input);
    }

    @Override
    public int getAmountPerUnit() {
        return AEFluidKey.AMOUNT_BUCKET;
    }

    @Override
    public Stream<TagKey<?>> getTagNames() {
        return BuiltInRegistries.FLUID.listTagIds().map(t -> t);
    }

    @Override
    public String getUnitSymbol() {
        return "B";
    }
}
