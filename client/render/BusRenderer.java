package appeng.client.render;

import java.util.HashMap;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.client.ClientHelper;
import appeng.facade.IFacadeItem;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BusRenderer implements IItemRenderer
{

	public static final BusRenderer instance = new BusRenderer();

	public RenderBlocksWorkaround renderer = new RenderBlocksWorkaround();
	public static final HashMap<Integer, IPart> renderPart = new HashMap();

	public IPart getRenderer(ItemStack is, IPartItem c)
	{
		int id = (Item.getIdFromItem( is.getItem() ) << Platform.DEF_OFFSET) | is.getItemDamage();

		IPart part = renderPart.get( id );
		if ( part == null )
		{
			part = c.createPartFromItemStack( is );
			if ( part != null )
				renderPart.put( id, part );
		}

		return part;
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		if ( item == null )
			return;

		GL11.glPushMatrix();
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		GL11.glEnable( GL11.GL_ALPHA_TEST );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_BLEND );

		if ( type == ItemRenderType.ENTITY )
			GL11.glTranslatef( -0.5f, -0.5f, -0.5f );
		if ( type == ItemRenderType.INVENTORY )
			GL11.glTranslatef( 0.0f, -0.1f, 0.0f );

		GL11.glTranslated( 0.2, 0.3, 0.1 );
		GL11.glScaled( 1.2, 1.2, 1. );

		GL11.glColor4f( 1, 1, 1, 1 );
		Tessellator.instance.setColorOpaque_F( 1, 1, 1 );

		BusRenderHelper.instance.setBounds( 0, 0, 0, 1, 1, 1 );
		BusRenderHelper.instance.setTexture( null );
		BusRenderHelper.instance.setInvColor( 0xffffff );
		renderer.blockAccess = ClientHelper.proxy.getWorld();

		BusRenderHelper.instance.ax = ForgeDirection.EAST;
		BusRenderHelper.instance.ay = ForgeDirection.UP;
		BusRenderHelper.instance.az = ForgeDirection.SOUTH;

		if ( item.getItem() instanceof IFacadeItem )
		{
			IFacadeItem fi = (IFacadeItem) item.getItem();
			IFacadePart fp = fi.createPartFromItemStack( item, ForgeDirection.SOUTH );

			if ( fp != null )
				fp.renderInventory( BusRenderHelper.instance, renderer );
		}
		else
		{
			IPart ip = getRenderer( item, (IPartItem) item.getItem() );
			if ( ip != null )
				ip.renderInventory( BusRenderHelper.instance, renderer );
		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
