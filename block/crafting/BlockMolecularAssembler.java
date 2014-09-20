package appeng.block.crafting;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockAssembler;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMolecularAssembler extends AEBaseBlock
{

	public BlockMolecularAssembler() {
		super( BlockMolecularAssembler.class, Material.iron );
		setFeature( EnumSet.of( AEFeature.MolecularAssembler ) );
		setTileEntity( TileMolecularAssembler.class );
		isOpaque = false;
		lightOpacity = 1;
	}

	public static boolean booleanAlphaPass = false;

	@Override
	public boolean canRenderInPass(int pass)
	{
		booleanAlphaPass = pass == 1;
		return pass == 0 || pass == 1;
	}

	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockAssembler.class;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		TileMolecularAssembler tg = getTileEntity( w, x, y, z );
		if ( tg != null && !p.isSneaking() )
		{
			Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_MAC );
			return true;
		}
		return false;
	}
}
