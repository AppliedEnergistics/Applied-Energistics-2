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

package appeng.recipes.mattercannon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class MatterCannonAmmoSerializer implements RecipeSerializer<MatterCannonAmmo> {

    public static final MatterCannonAmmoSerializer INSTANCE = new MatterCannonAmmoSerializer();

    private static final Codec<MatterCannonAmmo> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ammo").forGetter(MatterCannonAmmo::getAmmo),
                Codec.FLOAT.fieldOf("weight").forGetter(MatterCannonAmmo::getWeight))
                .apply(builder, MatterCannonAmmo::new);
    });

    private MatterCannonAmmoSerializer() {
    }

    @Override
    public Codec<MatterCannonAmmo> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public MatterCannonAmmo fromNetwork(FriendlyByteBuf buffer) {
        var ammo = Ingredient.fromNetwork(buffer);
        var weight = buffer.readFloat();
        return new MatterCannonAmmo(ammo, weight);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, MatterCannonAmmo recipe) {
        recipe.getAmmo().toNetwork(buffer);
        buffer.writeFloat(recipe.getWeight());
    }

}
