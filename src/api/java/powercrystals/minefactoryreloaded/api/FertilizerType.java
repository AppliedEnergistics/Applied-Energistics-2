package powercrystals.minefactoryreloaded.api;

/**
 * @author PowerCrystals
 *
 * Determines what kind of action a given fertilizer can perform. Your IFactoryFertilizable instances should check this before
 * performing any action to maintain future compatibility.
 */
public enum FertilizerType
{
	/**
	 * The fertilizer will fertilize nothing.
	 */
	None,
	/**
	 * The fertilizer will fertilize grass.
	 */
	Grass,
	/**
	 * The fertilizer will grow a plant.
	 */
	GrowPlant,
	/**
	 * The fertilizer will grow magical crops.
	 */
	GrowMagicalCrop,
}