package appeng.client.integrations.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public record AttunementDisplay(Ingredient inputs, Item tunnel, Component... description) {
}
