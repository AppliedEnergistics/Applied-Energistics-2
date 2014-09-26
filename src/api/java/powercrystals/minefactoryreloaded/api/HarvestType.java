package powercrystals.minefactoryreloaded.api;

/**
 * @author PowerCrystals
 *
 * Determines what algorithm the Harvester uses when it encounters this IFactoryHarvestable in the world.
 */
public enum HarvestType
{
	/**
	 * Just break the single block - no special action needed. e.g. Carrots, flowers, wheat.
	 */
	Normal,
	/**
	 * Search for harvestable blocks adjacent to this block but leave this block. e.g. Pumpkin, melon
	 */
	Gourd,
	/**
	 * Search for identical blocks above.
	 */
	Column,
	/**
	 * Search for identical blocks above but leave the bottom one for the future. e.g. Cactus, sugarcane.
	 */
	LeaveBottom,
	/**
	 * This block is the base of a tree and the harvester should enter tree-cutting mode.
	 */
	Tree,
	/**
	 * This block is the base of the tree and the harvester should enter tree-cutting mode.
	 * The tree is searched for in the negative y axis instead.
	 */
	TreeFlipped,
	/**
	 * This block is part of a tree as above.
	 */
	TreeLeaf,
	/**
	 * This block is part of a tree as above, but fruits are cut before logs. e.g. cocoa
	 * The tree is not searched for.
	 */
	TreeFruit
}
