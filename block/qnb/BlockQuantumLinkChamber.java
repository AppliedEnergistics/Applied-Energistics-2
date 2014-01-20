package appeng.block.qnb;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQNB;
import appeng.client.render.effects.EnergyFx;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.Platform;

public class BlockQuantumLinkChamber extends AEBaseBlock
{

	public BlockQuantumLinkChamber() {
		super( BlockQuantumLinkChamber.class, Material.glass );
		setfeature( EnumSet.of( AEFeature.QuantumNetworkBridge ) );
		setTileEntiy( TileQuantumBridge.class );
		float shave = 2.0f / 16.0f;
		setBlockBounds( shave, shave, shave, 1.0f - shave, 1.0f - shave, 1.0f - shave );
		setLightOpacity( 0 );
		isFullSize = isOpaque = false;
	}

	@Override
	public void randomDisplayTick(World w, int bx, int by, int bz, Random r)
	{
		TileQuantumBridge bridge = getTileEntity( w, bx, by, bz );
		if ( bridge != null )
		{
			if ( bridge.hasQES() )
			{
				float x = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;
				float y = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;
				float z = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;

				EnergyFx fx = new EnergyFx( w, bx + x + 0.5, by + y + 0.5, bz + z + 0.5, Item.diamond );

				fx.motionX = -x * 0.1;
				fx.motionY = -y * 0.1;
				fx.motionZ = -z * 0.1;

				Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, int pointlessnumber)
	{
		TileQuantumBridge bridge = getTileEntity( w, x, y, z );
		if ( bridge != null )
			bridge.neighborUpdate();
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQNB.class;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if ( p.isSneaking() )
			return false;

		TileQuantumBridge tg = getTileEntity( w, x, y, z );
		if ( tg != null )
		{
			if ( Platform.isServer() )
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_QNB );
			return true;
		}
		return false;
	}

}
