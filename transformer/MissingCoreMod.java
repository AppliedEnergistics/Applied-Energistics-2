package appeng.transformer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;

public class MissingCoreMod extends CustomModLoadingErrorDisplayException
{

	private static final long serialVersionUID = -966774766922821652L;
	private boolean deobf = false;

	@Override
	public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer)
	{
		Class clz = errorScreen.getClass();
		try
		{
			clz.getField( "mc" );
			deobf = true;
		}
		catch (Throwable e)
		{

		}
	}

	@Override
	public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime)
	{
		int offset = 10;
		drawCenteredString( fontRenderer, "Sorry, couldn't load AE2 Properly.", errorScreen.width / 2, offset += 15, 0xffffff );
		drawCenteredString( fontRenderer, "Please make sure that AE2 is installed into your mods folder.", errorScreen.width / 2, offset += 15, 0xeeeeee );

		offset += 15;

		if ( deobf )
		{
			offset += 15;
			drawCenteredString( fontRenderer, "In a developer environment add the following too your args,", errorScreen.width / 2, offset += 15, 0xffffff );
			drawCenteredString( fontRenderer, "-Dfml.coreMods.load=appeng.transformer.AppEngCore", errorScreen.width / 2, offset += 15, 0xeeeeee );
		}
		else
		{
			drawCenteredString( fontRenderer, "You're launcher may refer to this by diffrent names,", errorScreen.width / 2, offset += 15, 0xffffff );

			offset += 5;

			drawCenteredString( fontRenderer, "MultiMC calls this tab \"Loader Mods\"", errorScreen.width / 2, offset += 15, 0xeeeeee );
			drawCenteredString( fontRenderer, "Magic Launcher calls this tab \"External Mods\"", errorScreen.width / 2, offset += 15, 0xeeeeee );
			drawCenteredString( fontRenderer, "Most other launchers refer to this tab as just \"Mods\"", errorScreen.width / 2, offset += 15, 0xeeeeee );

			offset += 15;

			drawCenteredString( fontRenderer, "Also make sure that the AE2 file is a .jar, and not a .zip", errorScreen.width / 2, offset += 15, 0xffffff );
		}
	}

	public void drawCenteredString(FontRenderer fontRenderer, String string, int x, int y, int colour)
	{
		fontRenderer.drawStringWithShadow( string, x - fontRenderer.getStringWidth( string.replaceAll( "\\P{InBasic_Latin}", "" ) ) / 2, y, colour );
	}
}