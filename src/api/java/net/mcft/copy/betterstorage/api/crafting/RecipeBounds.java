package net.mcft.copy.betterstorage.api.crafting;

import net.minecraft.item.ItemStack;

public class RecipeBounds {
	
	public final int minX, maxX, minY, maxY;
	
	public int getWidth() { return (maxX - minX + 1); }
	public int getHeight() { return (maxY - minY + 1); }
	
	public RecipeBounds(int minX, int maxX, int minY, int maxY) {
		this.minX = minX; this.maxX = maxX;
		this.minY = minY; this.maxY = maxY;
	}
	public RecipeBounds(ItemStack[] input) {
		int minX = 2, maxX = -1;
		int minY = 2, maxY = -1;
		for (int x = 0; x <= 2; x++)
			for (int y = 0; y <= 2; y++)
				if (input[x + y * 3] != null) {
					minX = Math.min(minX, x);
					maxX = Math.max(maxX, x);
					minY = Math.min(minY, y);
					maxY = Math.max(maxY, y);
				}
		if ((minX <= maxX) && (minY <= maxY)) {
			this.minX = minX;
			this.minY = minY;
		} else this.minX = this.minY = 0;
		this.maxX = maxX;
		this.maxY = maxY;
	}
	
}
