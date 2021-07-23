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

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;

class AESharedItemStackTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    // Test stack -> Name for debugging the tests
    final Map<AESharedItemStack, String> stacks = new IdentityHashMap<>();

    AESharedItemStackTest() {
        TestItemWithCaps TEST_ITEM = new TestItemWithCaps();

        net.minecraft.world.item.ItemStack nameTag1 = new net.minecraft.world.item.ItemStack(TEST_ITEM);
        stacks.put(new AESharedItemStack(nameTag1), "no-nbt");

        // NBT
        net.minecraft.world.item.ItemStack nameTag2 = new net.minecraft.world.item.ItemStack(TEST_ITEM);
        nameTag2.setDisplayName(new TextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag2), "nbt1");

        // Different NBT
        net.minecraft.world.item.ItemStack nameTag3 = new net.minecraft.world.item.ItemStack(TEST_ITEM);
        nameTag3.setDisplayName(new TextComponent("ABCDEFGH"));
        stacks.put(new AESharedItemStack(nameTag3), "nbt2");

        // NBT + Cap
        CompoundTag capNbt = new CompoundTag();
        capNbt.putInt("Parent", 1);
        net.minecraft.world.item.ItemStack nameTag4 = new net.minecraft.world.item.ItemStack(TEST_ITEM, 1, capNbt);
        nameTag4.setDisplayName(new TextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag4), "nbt1+cap1");

        // NBT + Different Cap
        CompoundTag capNbt2 = new CompoundTag();
        capNbt2.putInt("Parent", 123);
        ItemStack nameTag5 = new net.minecraft.world.item.ItemStack(TEST_ITEM, 1, capNbt2);
        nameTag5.setDisplayName(new TextComponent("Hello World"));
        stacks.put(new AESharedItemStack(nameTag5), "nbt1+cap2");
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
        net.minecraft.world.item.ItemStack itemStack = new ItemStack(Items.CRAFTING_TABLE);
        tester.addEqualityGroup(
                new AESharedItemStack(itemStack),
                new AESharedItemStack(itemStack));

        tester.testEquals();
    }

}
