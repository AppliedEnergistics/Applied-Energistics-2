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

/* Example:

 NBTTagCompound msg = new NBTTagCompound();
 NBTTagCompound in = new NBTTagCompound();
 NBTTagCompound out = new NBTTagCompound();

 new ItemStack( Blocks.iron_ore ).writeToNBT( in );
 new ItemStack( Items.iron_ingot ).writeToNBT( out );
 msg.setTag( "in", in );
 msg.setTag( "out", out );
 msg.setInteger( "turns", 8 );

 FMLInterModComms.sendMessage( "appliedenergistics2", "add-grindable", msg );

 -- or --

 NBTTagCompound msg = new NBTTagCompound();
 NBTTagCompound in = new NBTTagCompound();
 NBTTagCompound out = new NBTTagCompound();
 NBTTagCompound optional = new NBTTagCompound();

 new ItemStack( Blocks.iron_ore ).writeToNBT( in );
 new ItemStack( Items.iron_ingot ).writeToNBT( out );
 new ItemStack( Blocks.gravel ).writeToNBT( optional );
 msg.setTag( "in", in );
 msg.setTag( "out", out );
 msg.setTag( "optional", optional );
 msg.setFloat( "chance", 0.5 );
 msg.setInteger( "turns", 8 );

 FMLInterModComms.sendMessage( "appliedenergistics2", "add-grindable", msg );

 */

package appeng.core.api.imc;


import appeng.api.AEApi;
import appeng.api.features.IGrinderRecipe;
import appeng.api.features.IGrinderRecipeBuilder;
import appeng.api.features.IGrinderRegistry;
import appeng.core.api.IIMCProcessor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;


public class IMCGrinder implements IIMCProcessor {
    @Override
    public void process(final IMCMessage m) {
        final NBTTagCompound msg = m.getNBTValue();
        final NBTTagCompound inTag = (NBTTagCompound) msg.getTag("in");
        final NBTTagCompound outTag = (NBTTagCompound) msg.getTag("out");

        final ItemStack in = new ItemStack(inTag);
        final ItemStack out = new ItemStack(outTag);

        final int turns = msg.getInteger("turns");

        if (in.isEmpty()) {
            throw new IllegalStateException("invalid input");
        }

        if (out.isEmpty()) {
            throw new IllegalStateException("invalid output");
        }

        if (msg.hasKey("optional")) {
            final NBTTagCompound optionalTag = (NBTTagCompound) msg.getTag("optional");
            final ItemStack optional = new ItemStack(optionalTag);

            if (optional.isEmpty()) {
                throw new IllegalStateException("invalid optional");
            }

            final float chance = msg.getFloat("chance");
            final IGrinderRegistry grinderRegistry = AEApi.instance().registries().grinder();
            final IGrinderRecipeBuilder builder = grinderRegistry.builder();
            final IGrinderRecipe grinderRecipe = builder.withInput(in)
                    .withOutput(out)
                    .withFirstOptional(optional, chance)
                    .withTurns(turns)
                    .build();

            grinderRegistry.addRecipe(grinderRecipe);
        } else {
            final IGrinderRegistry grinderRegistry = AEApi.instance().registries().grinder();
            final IGrinderRecipeBuilder builder = grinderRegistry.builder();
            final IGrinderRecipe grinderRecipe = builder.withInput(in)
                    .withOutput(out)
                    .withTurns(turns)
                    .build();

            grinderRegistry.addRecipe(grinderRecipe);
        }
    }
}
