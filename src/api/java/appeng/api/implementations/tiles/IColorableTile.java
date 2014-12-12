package appeng.api.implementations.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.AEColor;

public interface IColorableTile
{

	AEColor getColor();

	boolean recolourBlock(ForgeDirection side, AEColor colour, EntityPlayer who);

}
