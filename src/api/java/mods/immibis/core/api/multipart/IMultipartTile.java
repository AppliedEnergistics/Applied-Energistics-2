package mods.immibis.core.api.multipart;


public interface IMultipartTile extends IPartContainer {
	/**
	 * Returns an ICoverSystem object, or null if this tile does not support a cover system
	 * @see mods.immibis.core.api.multipart.ICoverSystem 
	 */
	public ICoverSystem getCoverSystem();
}
