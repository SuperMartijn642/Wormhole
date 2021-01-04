package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public abstract class WormholeScreen extends Screen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("wormhole", "textures/gui/background.png");

    protected final List<Widget> widgets = new LinkedList<>();
    protected final List<ITickableWidget> tickableWidgets = new LinkedList<>();

    protected WormholeScreen(String titleKey){
        super(new TranslationTextComponent(titleKey));
    }

    protected abstract float sizeX();

    protected abstract float sizeY();

    protected float left(){
        return (this.width - this.sizeX()) / 2;
    }

    protected float top(){
        return (this.height - this.sizeY()) / 2;
    }

    @Override
    protected void init(){
        this.widgets.clear();
        this.tickableWidgets.clear();
        this.addWidgets();
    }

    protected abstract void addWidgets();

    protected <T extends Widget> T addWidget(T widget){
        this.widgets.add(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.add((ITickableWidget)widget);
        if(widget instanceof AbstractButton)
            this.addButton(widget);
        else
            this.children.add(widget);
        return widget;
    }

    protected <T extends Widget> T removeWidget(T widget){
        this.widgets.remove(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.remove(widget);
        if(widget instanceof AbstractButton) this.buttons.remove(widget);
        this.children.remove(widget);
        return widget;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        this.renderBackground();

        GlStateManager.translated(this.left(), this.top(), 0);
        mouseX -= this.left();
        mouseY -= this.top();

        GlStateManager.pushMatrix();
        this.render(mouseX, mouseY);
        GlStateManager.popMatrix();
        for(Widget widget : this.widgets)
            widget.render(mouseX, mouseY, partialTicks);
        for(Widget widget : this.widgets){
            if(widget instanceof IHoverTextWidget && widget.isHovered()){
                ITextComponent text = ((IHoverTextWidget)widget).getHoverText();
                if(text != null)
                    this.renderTooltip(text.getFormattedText(), mouseX, mouseY);
            }
        }
        this.renderTooltips(mouseX, mouseY);
    }

    protected abstract void render(int mouseX, int mouseY);

    protected abstract void renderTooltips(int mouseX, int mouseY);

    protected void drawBackground(float x, float y, float width, float height){
        Minecraft.getInstance().textureManager.bindTexture(BACKGROUND_TEXTURE);
        // corners
        this.drawTexture(x, y, 4, 4, 0, 0, 4 / 9f, 4 / 9f);
        this.drawTexture(x + width - 4, y, 4, 4, 5 / 9f, 0, 4 / 9f, 4 / 9f);
        this.drawTexture(x + width - 4, y + height - 4, 4, 4, 5 / 9f, 5 / 9f, 4 / 9f, 4 / 9f);
        this.drawTexture(x, y + height - 4, 4, 4, 0, 5 / 9f, 4 / 9f, 4 / 9f);
        // edges
        this.drawTexture(x + 4, y, width - 8, 4, 4 / 9f, 0, 1 / 9f, 4 / 9f);
        this.drawTexture(x + 4, y + height - 4, width - 8, 4, 4 / 9f, 5 / 9f, 1 / 9f, 4 / 9f);
        this.drawTexture(x, y + 4, 4, height - 8, 0, 4 / 9f, 4 / 9f, 1 / 9f);
        this.drawTexture(x + width - 4, y + 4, 4, height - 8, 5 / 9f, 4 / 9f, 4 / 9f, 1 / 9f);
        // center
        this.drawTexture(x + 4, y + 4, width - 8, height - 8, 4 / 9f, 4 / 9f, 1 / 9f, 1 / 9f);
    }

    protected void drawTexture(float x, float y, float width, float height){
        drawTexture(x, y, width, height, 0, 0, 1, 1);
    }

    protected void drawTexture(float x, float y, float width, float height, float tx, float ty, float twidth, float theight){
        int z = this.getBlitOffset();
        GlStateManager.enableAlphaTest();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, z).tex(tx, ty + theight).endVertex();
        buffer.pos(x + width, y + height, z).tex(tx + twidth, ty + theight).endVertex();
        buffer.pos(x + width, y, z).tex(tx + twidth, ty).endVertex();
        buffer.pos(x, y, z).tex(tx, ty).endVertex();
        tessellator.draw();
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        mouseX -= this.left();
        mouseY -= this.top();

        boolean flag = false;
        for(IGuiEventListener iguieventlistener : this.children){
            if(iguieventlistener.mouseClicked(mouseX, mouseY, button)){
                this.setFocused(iguieventlistener);
                if(button == 0)
                    this.setDragging(true);
                flag = true;
            }
        }

        return flag;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY){
        mouseX -= this.left();
        mouseY -= this.top();
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        mouseX -= this.left();
        mouseY -= this.top();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        mouseX -= this.left();
        mouseY -= this.top();
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void tick(){
        for(Widget widget : this.widgets)
            if(widget instanceof ITickableWidget)
                ((ITickableWidget)widget).tick();
    }
}
