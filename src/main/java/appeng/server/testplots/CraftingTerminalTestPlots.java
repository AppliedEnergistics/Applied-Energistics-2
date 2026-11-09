package appeng.server.testplots;

import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayer;

import appeng.api.stacks.AEItemKey;
import appeng.core.definitions.AEParts;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantics;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.server.testworld.PlotBuilder;

@TestPlotClass
public final class CraftingTerminalTestPlots {
    private CraftingTerminalTestPlots() {
    }

    /**
     * Recipe viewers that can take the result (EMI's "craft to inventory") fill the grid and then click the result
     * slot. This verifies that the click actually crafts in a crafting terminal.
     */
    @TestPlot("crafting_terminal_quick_craft")
    public static void quickCraftFromRecipe(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.drive(origin).addItemCell64k().add(Items.JUNGLE_PLANKS, 6);
        plot.cable(origin.above()).part(Direction.NORTH, AEParts.CRAFTING_TERMINAL);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> helper.getGrid(origin)) // Wait for grid init
                    .thenWaitUntil(() -> helper.check(
                            helper.getPart(origin.above(), Direction.NORTH, CraftingTerminalPart.class).isActive(),
                            "crafting terminal is not active"))
                    .thenExecute(() -> {
                        var part = helper.getPart(origin.above(), Direction.NORTH, CraftingTerminalPart.class);

                        var player = new FakePlayer(helper.getLevel(),
                                new GameProfile(UUID.randomUUID(), "test-quick-craft"));
                        try {
                            var menu = new CraftingTermMenu(1, player.getInventory(), part);
                            menu.setLocator(MenuLocators.forPart(part));
                            player.containerMenu = menu;
                            menu.broadcastChanges();

                            // Jungle stairs are six jungle planks, i.e. one craft per plank in storage
                            var templates = NonNullList.withSize(9, ItemStack.EMPTY);
                            for (var slot : Set.of(0, 3, 4, 6, 7, 8)) {
                                templates.set(slot, new ItemStack(Items.JUNGLE_PLANKS));
                            }
                            new FillCraftingGridFromRecipePacket(ResourceLocation.parse("minecraft:jungle_stairs"),
                                    templates, false).handleOnServer(player);

                            var outputSlot = menu.getSlots(SlotSemantics.CRAFTING_RESULT).get(0);
                            helper.check(!outputSlot.getItem().isEmpty(),
                                    "result slot is empty after filling the grid");

                            // Same packet the client sends when the result slot is clicked with shift
                            new InventoryActionPacket(InventoryAction.CRAFT_SHIFT, outputSlot.index, 0)
                                    .handleOnServer(player);

                            var crafted = 0;
                            var inventory = player.getInventory();
                            for (var i = 0; i < inventory.getContainerSize(); i++) {
                                var stack = inventory.getItem(i);
                                if (stack.is(Items.JUNGLE_STAIRS)) {
                                    crafted += stack.getCount();
                                }
                            }
                            helper.check(crafted > 0,
                                    "expected the crafted item in the player inventory, but found " + crafted);
                            helper.check(outputSlot.getItem().isEmpty(), "result slot still holds an item");
                        } finally {
                            player.closeContainer();
                        }
                    })
                    .thenSucceed();
        });
    }

    /**
     * "Craft one to inventory" crafts one recipe's result, not a whole stack.
     */
    @TestPlot("crafting_terminal_craft_one_to_inventory")
    public static void craftOneToInventory(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.drive(origin).addItemCell64k().add(Items.JUNGLE_PLANKS, 6);
        plot.cable(origin.above()).part(Direction.NORTH, AEParts.CRAFTING_TERMINAL);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> helper.getGrid(origin)) // Wait for grid init
                    .thenWaitUntil(() -> helper.check(
                            helper.getPart(origin.above(), Direction.NORTH, CraftingTerminalPart.class).isActive(),
                            "crafting terminal is not active"))
                    .thenExecute(() -> {
                        var part = helper.getPart(origin.above(), Direction.NORTH, CraftingTerminalPart.class);

                        var player = new FakePlayer(helper.getLevel(),
                                new GameProfile(UUID.randomUUID(), "test-craft-one"));
                        try {
                            var menu = new CraftingTermMenu(1, player.getInventory(), part);
                            menu.setLocator(MenuLocators.forPart(part));
                            player.containerMenu = menu;
                            menu.broadcastChanges();

                            var templates = NonNullList.withSize(9, ItemStack.EMPTY);
                            for (var slot : Set.of(0, 3, 4, 6, 7, 8)) {
                                templates.set(slot, new ItemStack(Items.JUNGLE_PLANKS));
                            }
                            new FillCraftingGridFromRecipePacket(ResourceLocation.parse("minecraft:jungle_stairs"),
                                    templates, false).handleOnServer(player);

                            var outputSlot = menu.getSlots(SlotSemantics.CRAFTING_RESULT).get(0);
                            var perCraft = outputSlot.getItem().getCount();
                            helper.check(perCraft > 0, "result slot is empty after filling the grid");

                            new InventoryActionPacket(InventoryAction.CRAFT_ONE, outputSlot.index, 0)
                                    .handleOnServer(player);

                            var crafted = 0;
                            var inventory = player.getInventory();
                            for (var i = 0; i < inventory.getContainerSize(); i++) {
                                var stack = inventory.getItem(i);
                                if (stack.is(Items.JUNGLE_STAIRS)) {
                                    crafted += stack.getCount();
                                }
                            }
                            helper.check(crafted == perCraft,
                                    "expected one craft (" + perCraft + ") in the player inventory, but found "
                                            + crafted);
                            helper.check(menu.getCarried().isEmpty(), "cursor is not empty");
                        } finally {
                            player.closeContainer();
                        }
                    })
                    .thenSucceed();
        });
    }

    /**
     * When filling the crafting grid from a recipe, items that are already in the grid but in the wrong slot should be
     * moved to a slot where they fit, instead of being pushed to network storage and then not being found again (since
     * the cached network inventory is only updated once per tick).
     */
    @TestPlot("crafting_terminal_fill_grid_moves_misplaced_items")
    public static void fillGridMovesMisplacedItems(PlotBuilder plot) {
        var origin = BlockPos.ZERO;
        plot.creativeEnergyCell(origin.below());
        plot.storageDrive(origin);
        plot.cable(origin.above()).part(Direction.NORTH, AEParts.CRAFTING_TERMINAL);

        plot.test(helper -> {
            helper.startSequence()
                    .thenExecute(() -> helper.getGrid(origin)) // Wait for grid init
                    .thenWaitUntil(() -> helper.check(
                            helper.getPart(origin.above(), Direction.NORTH, CraftingTerminalPart.class).isActive(),
                            "crafting terminal is not active"))
                    .thenExecute(() -> {
                        var part = helper.getPart(origin.above(), Direction.NORTH, CraftingTerminalPart.class);
                        var craftingGrid = part.getSubInventory(CraftingTerminalPart.INV_CRAFTING);

                        // Jungle stairs are shaped:
                        // # _ _
                        // # # _
                        // # # #
                        var recipeSlots = Set.of(0, 3, 4, 6, 7, 8);

                        // Fill the grid accordingly, but put the plank for slot 8 into slot 2 instead
                        for (var slot : Set.of(0, 3, 4, 6, 7, 2)) {
                            craftingGrid.setItemDirect(slot, new ItemStack(Items.JUNGLE_PLANKS));
                        }

                        // Use a separate fake player, so we don't interfere with the shared one
                        var player = new FakePlayer(helper.getLevel(),
                                new GameProfile(UUID.randomUUID(), "test-crafting-terminal"));
                        try {
                            // NeoForge's FakePlayer ignores openMenu, so we have to set the menu ourselves
                            var menu = new CraftingTermMenu(1, player.getInventory(), part);
                            menu.setLocator(MenuLocators.forPart(part));
                            player.containerMenu = menu;
                            // Opening the menu normally would do this, and it's required to update the link status
                            menu.broadcastChanges();
                            helper.check(menu.getLinkStatus().connected(), "crafting terminal is not connected");

                            // Simulates JEI/REI requesting the crafting grid to be filled for the recipe
                            var templates = NonNullList.withSize(9, ItemStack.EMPTY);
                            for (var slot : recipeSlots) {
                                templates.set(slot, new ItemStack(Items.JUNGLE_PLANKS));
                            }
                            new FillCraftingGridFromRecipePacket(ResourceLocation.parse("minecraft:jungle_stairs"),
                                    templates, false).handleOnServer(player);

                            for (var slot = 0; slot < craftingGrid.size(); slot++) {
                                var stack = craftingGrid.getStackInSlot(slot);
                                if (recipeSlots.contains(slot)) {
                                    helper.check(stack.is(Items.JUNGLE_PLANKS) && stack.getCount() == 1,
                                            "expected one jungle plank in slot " + slot + ", but got " + stack);
                                } else {
                                    helper.check(stack.isEmpty(),
                                            "expected slot " + slot + " to be empty, but got " + stack);
                                }
                            }

                            var storage = helper.getGrid(origin).getStorageService().getInventory();
                            helper.assertContainsNot(storage, AEItemKey.of(Items.JUNGLE_PLANKS));
                            helper.check(!player.getInventory().contains(new ItemStack(Items.JUNGLE_PLANKS)),
                                    "jungle plank should not have been moved to the player inventory");
                        } finally {
                            player.closeContainer();
                        }
                    })
                    .thenSucceed();
        });
    }
}
