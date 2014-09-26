package uristqwerty.CraftGuide;

import net.minecraft.entity.player.EntityPlayer;

public interface CraftGuideSide
{
	public void initKeybind();
	public void checkKeybind();
	public void preInit();
	public void reloadRecipes();
	public void openGUI(EntityPlayer player);
	public void initNetworkChannels();

	/* Ensure that Tessellator isn't drawing. More to prevent an otherwise nonfatal
	 *  rendering errors from crashing Minecraft entirely. Better for CraftGuide to
	 *  be unusable (invisible items, offset stuff, other graphical glitches), than
	 *  to dump the player back to windows. Since keyboard input not affected by
	 *  render issues, you can always exit the GUI and continue playing without it.
	 */
	public void stopTessellating();
}
