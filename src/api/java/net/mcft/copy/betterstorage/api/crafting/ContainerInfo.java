package net.mcft.copy.betterstorage.api.crafting;

import net.minecraft.item.ItemStack;

public class ContainerInfo {
	
	private ItemStack containerItem = null;
	private boolean doesLeaveCrafting = true;
	
	public ItemStack getContainerItem() { return containerItem; }
	public boolean doesLeaveCrafting() { return doesLeaveCrafting; }
	
	public void set(ItemStack containerItem, boolean doesLeaveCrafting) {
		this.containerItem = containerItem;
		this.doesLeaveCrafting = doesLeaveCrafting;
	}
	
}
