package com.plusls.MasaGadget.impl.gui;

import fi.dy.masa.malilib.gui.widgets.WidgetLabel;
import fi.dy.masa.malilib.render.GuiContext;
import lombok.Getter;
import lombok.Setter;
import top.hendrixshen.magiclib.api.render.context.GuiRenderContext;
import top.hendrixshen.magiclib.api.render.context.RenderContext;

@Getter
@Setter
public class ScalableWidgetLabel extends WidgetLabel {
    private float scale;

    public ScalableWidgetLabel(int x, int y, int width, int height, int textColor, float scale, String... text) {
        super(x, y, width, height, textColor, text);
        this.scale = scale;
    }

    @Override
    public void render(GuiContext ctx, int mouseX, int mouseY, boolean selected) {
        if (this.visible) {
            this.drawLabelBackground(ctx);
            GuiRenderContext renderContext = RenderContext.gui(ctx);

            int fontHeight = this.fontHeight;
            int yCenter = this.y + this.height / 2 + this.borderSize / 2;
            int yTextStart = yCenter - 1 - this.labels.size() * fontHeight / 2;

            for (int i = 0; i < this.labels.size(); i++) {
                String text = this.labels.get(i);
                double x = this.x + (this.centered ? this.width / 2.0 : 0);
                double y = yTextStart + i * fontHeight * scale;
                renderContext.pushMatrix();
                renderContext.scale(scale, scale);
                x /= scale;
                y /= scale;

                if (this.centered) {
                    this.drawCenteredStringWithShadow(ctx, (int) x, (int) y, this.textColor, text);
                } else {
                    this.drawStringWithShadow(ctx, (int) x, (int) y, this.textColor, text);
                }

                renderContext.popMatrix();
            }
        }
    }
}
