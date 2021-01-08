package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.util.math.vector.Matrix4f;
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
        return widget instanceof AbstractButton ? this.addButton(widget) : this.addListener(widget);
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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);

        matrixStack.translate(this.left(), this.top(), 0);
        mouseX -= this.left();
        mouseY -= this.top();

        matrixStack.push();
        this.render(matrixStack, mouseX, mouseY);
        matrixStack.pop();
        for(Widget widget : this.widgets)
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
        for(Widget widget : this.widgets){
            if(widget instanceof IHoverTextWidget && widget.isHovered()){
                ITextComponent text = ((IHoverTextWidget)widget).getHoverText();
                if(text != null)
                    this.renderTooltip(matrixStack, text, mouseX, mouseY);
            }
        }
        this.renderTooltips(matrixStack, mouseX, mouseY);
    }

    protected abstract void render(MatrixStack matrixStack, int mouseX, int mouseY);

    protected abstract void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY);

    protected void drawBackground(MatrixStack matrixStack, float x, float y, float width, float height){
        Minecraft.getInstance().textureManager.bindTexture(BACKGROUND_TEXTURE);
        // corners
        this.drawTexture(matrixStack, x, y, 4, 4, 0, 0, 4 / 9f, 4 / 9f);
        this.drawTexture(matrixStack, x + width - 4, y, 4, 4, 5 / 9f, 0, 4 / 9f, 4 / 9f);
        this.drawTexture(matrixStack, x + width - 4, y + height - 4, 4, 4, 5 / 9f, 5 / 9f, 4 / 9f, 4 / 9f);
        this.drawTexture(matrixStack, x, y + height - 4, 4, 4, 0, 5 / 9f, 4 / 9f, 4 / 9f);
        // edges
        this.drawTexture(matrixStack, x + 4, y, width - 8, 4, 4 / 9f, 0, 1 / 9f, 4 / 9f);
        this.drawTexture(matrixStack, x + 4, y + height - 4, width - 8, 4, 4 / 9f, 5 / 9f, 1 / 9f, 4 / 9f);
        this.drawTexture(matrixStack, x, y + 4, 4, height - 8, 0, 4 / 9f, 4 / 9f, 1 / 9f);
        this.drawTexture(matrixStack, x + width - 4, y + 4, 4, height - 8, 5 / 9f, 4 / 9f, 4 / 9f, 1 / 9f);
        // center
        this.drawTexture(matrixStack, x + 4, y + 4, width - 8, height - 8, 4 / 9f, 4 / 9f, 1 / 9f, 1 / 9f);
    }

    protected void drawTexture(MatrixStack matrix, float x, float y, float width, float height){
        drawTexture(matrix, x, y, width, height, 0, 0, 1, 1);
    }

    protected void drawTexture(MatrixStack matrixStack, float x, float y, float width, float height, float tx, float ty, float twidth, float theight){
        int z = this.getBlitOffset();
        Matrix4f matrix = matrixStack.getLast().getMatrix();

        GlStateManager.enableAlphaTest();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(matrix, x, y + height, z).tex(tx, ty + theight).endVertex();
        buffer.pos(matrix, x + width, y + height, z).tex(tx + twidth, ty + theight).endVertex();
        buffer.pos(matrix, x + width, y, z).tex(tx + twidth, ty).endVertex();
        buffer.pos(matrix, x, y, z).tex(tx, ty).endVertex();
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
        for(IGuiEventListener iguieventlistener : this.getEventListeners()){
            if(iguieventlistener.mouseClicked(mouseX, mouseY, button)){
                this.setListener(iguieventlistener);
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
