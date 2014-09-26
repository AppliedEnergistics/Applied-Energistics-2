package mods.immibis.core.api.multipart;


/**
 * An object that allows extra parts to be added to multipart tiles.
 * For example, you can add covers to InfiniTubes transport conduits
 * because they use a MicroblockCoverSystem.
 * 
 * This interface's existence is a bit unintuitive, but it was  
 */
public interface ICoverSystem extends IPartContainer {
	/**
	 * Converts the block containing this cover system into
	 * a block containing only parts from this cover system,
	 * or to air if the cover system has no parts.
	 * 
	 * Call it when, for example, all wires in a RedLogic wire block are destroyed,
	 * to replace it with a microblock container block if there were any microblocks
	 * in the wire block.
	 */
	public void convertToContainerBlock();
}
