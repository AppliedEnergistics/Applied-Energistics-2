package appeng.client.gui.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;


public class GuiNumberBox extends GuiTextField
{
	
	final Class type;
	
	public GuiNumberBox(FontRenderer p_i1032_1_, int p_i1032_2_, int p_i1032_3_, int p_i1032_4_, int p_i1032_5_,Class type) {
		super( p_i1032_1_, p_i1032_2_, p_i1032_3_, p_i1032_4_, p_i1032_5_ );
		this.type = type;
	}

	@Override
	public void writeText(String p_146191_1_)
	{
		String original = getText();
		super.writeText( p_146191_1_ );
		
		try
		{
			if ( type == int.class || type == Integer.class )
				Integer.parseInt( getText() );
			else if ( type == long.class || type == Long.class )
				Long.parseLong( getText() );
			else if ( type == double.class || type == Double.class )
				Double.parseDouble( getText() );
		}
		catch(NumberFormatException e )
		{
			setText( original );
		}
	}
	

}
