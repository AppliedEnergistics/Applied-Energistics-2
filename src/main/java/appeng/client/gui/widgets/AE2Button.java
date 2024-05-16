package appeng.client.gui.widgets;

import appeng.core.AppEng;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AE2Button extends Button {
	protected static final WidgetSprites SPRITES = new WidgetSprites(
			AppEng.makeId("button"), AppEng.makeId("button_disabled"), AppEng.makeId("button_highlighted")
	);

	public AE2Button(int pX, int pY, int pWidth, int pHeight, Component component, OnPress onPress) {
		super(pX, pY, pWidth, pHeight, component, onPress, supplier -> Component.empty());
	}

	public AE2Button(Component component, OnPress onPress) {
		super(0, 0, 0, 0, component, onPress, supplier -> Component.empty());
	}

	@Override
	protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		pGuiGraphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight()); // TO BE MODIFIED
		pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = getFGColor();
		this.renderString(pGuiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
	}
}
