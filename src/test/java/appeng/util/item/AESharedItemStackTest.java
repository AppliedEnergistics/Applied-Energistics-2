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

package appeng.util.item;

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.testing.EqualsTester;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class AESharedItemStackTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.bootStrap();
    }

    // Test stack -> Name for debugging the tests
    final Map<AESharedItemStack, String> stacks = new IdentityHashMap<>();

    AESharedItemStackTest() {
        var TEST_ITEM = new Item(new Item.Properties());

        ItemStack nameTag1 = new ItemStack(TEST_ITEM);
        stacks.put(new AESharedItemStack(nameTag1), "no-nbt");

        // NBT
        ItemStack nameTag2 = new ItemStack(TEST_ITEM);
        nameTag2.setHoverName(new TextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag2), "nbt1");

        // Different NBT
        ItemStack nameTag3 = new ItemStack(TEST_ITEM);
        nameTag3.setHoverName(new TextComponent("ABCDEFGH"));
        stacks.put(new AESharedItemStack(nameTag3), "nbt2");
    }

    /**
     * Tests equality between shared item stacks.
     */
    @Test
    void testEquals() {
        EqualsTester tester = new EqualsTester();
        for (AESharedItemStack stack : stacks.keySet()) {
            // Add the stack, and a pristine copy of the stack
            tester.addEqualityGroup(stack, new AESharedItemStack(stack.getDefinition().copy()));
        }

        // Test that using the same item stack instance makes two separate shared stacks equal
        ItemStack itemStack = new ItemStack(Items.CRAFTING_TABLE);
        tester.addEqualityGroup(
                new AESharedItemStack(itemStack),
                new AESharedItemStack(itemStack));

        tester.testEquals();
    }

}
