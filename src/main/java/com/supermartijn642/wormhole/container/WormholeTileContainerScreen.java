package com.supermartijn642.wormhole.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.wormhole.screen.IHoverTextWidget;
import com.supermartijn642.wormhole.screen.ITickableWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 12/20/2020 by SuperMartijn642
 */
public abstract class WormholeTileContainerScreen<T extends TileEntity, S extends WormholeTileContainer<T>> extends ContainerScreen<S> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("wormhole", "textures/gui/background.png");
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("wormhole", "textures/gui/slot.png");

    protected final List<Widget> widgets = new LinkedList<>();
    protected final List<ITickableWidget> tickableWidgets = new LinkedList<>();

    public WormholeTileContainerScreen(S screenContainer, PlayerInventory inv, String titleKey){
        super(screenContainer, inv, new TranslationTextComponent(titleKey));
    }

    protected abstract int sizeX();

    protected abstract int sizeY();

    protected int left(){
        return (this.width - this.sizeX()) / 2;
    }

    protected int top(){
        return (this.height - this.sizeY()) / 2;
    }

    @Override
    public int getXSize(){
        return this.sizeX();
    }

    @Override
    public int getYSize(){
        return this.sizeY();
    }

    @Override
    public int getGuiLeft(){
        return this.left();
    }

    @Override
    public int getGuiTop(){
        return this.top();
    }

    @Override
    protected void init(){
        this.xSize = this.sizeX();
        this.ySize = this.sizeY();
        super.init();

        this.widgets.clear();
        this.tickableWidgets.clear();
        T tile = this.getTileOrClose();
        if(tile != null)
            this.addWidgets(tile);
    }

    protected abstract void addWidgets(T tile);

    protected <T extends Widget> T addWidget(T widget){
        this.widgets.add(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.add((ITickableWidget)widget);
        return widget;
    }

    protected <T extends Widget> T removeWidget(T widget){
        this.widgets.remove(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.remove(widget);
        return widget;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(matrixStack);

        T tile = this.getTileOrClose();
        if(tile == null)
            return;

        matrixStack.translate(this.left(), this.top(), 0);
        this.renderBackground(matrixStack, tile, mouseX, mouseY);

        for(Slot slot : this.container.inventorySlots){
            Minecraft.getInstance().textureManager.bindTexture(SLOT_TEXTURE);
            this.drawTexture(matrixStack, slot.xPos - 1, slot.yPos - 1, 18, 18);
        }
        matrixStack.translate(-this.left(), -this.top(), 0);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        matrixStack.translate(this.left(), this.top(), 0);
        for(Widget widget : this.widgets)
            widget.render(matrixStack, mouseX - (int)this.left(), mouseY - (int)this.top(), partialTicks);

        this.renderForeground(matrixStack, tile, mouseX, mouseY);
        matrixStack.translate(-this.left(), -this.top(), 0);

        for(Widget widget : this.widgets){
            if(widget instanceof IHoverTextWidget && widget.isHovered()){
                ITextComponent text = ((IHoverTextWidget)widget).getHoverText();
                if(text != null)
                    this.renderTooltip(matrixStack, text, mouseX, mouseY);
            }
        }
        super.func_230459_a_(matrixStack, mouseX, mouseY);
        this.renderTooltips(matrixStack, tile, mouseX - (int)this.left(), mouseY - (int)this.top());
    }

    protected void renderBackground(MatrixStack matrixStack, T tile, int mouseX, int mouseY){
        this.drawBackground(matrixStack, 0, 0, this.sizeX(), this.sizeY());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y){}

    protected void renderForeground(MatrixStack matrixStack, T tile, int mouseX, int mouseY){
    }

    protected abstract void renderTooltips(MatrixStack matrixStack, T tile, int mouseX, int mouseY);

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
        boolean flag = super.mouseClicked(mouseX, mouseY, button);

        mouseX -= this.left();
        mouseY -= this.top();

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
        boolean flag = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        mouseX -= this.left();
        mouseY -= this.top();

        for(IGuiEventListener iguieventlistener : this.getEventListeners())
            if(iguieventlistener.mouseDragged(mouseX, mouseY, button, dragX, dragY))
                flag = true;

        return flag;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        boolean flag = super.mouseReleased(mouseX, mouseY, button);

        mouseX -= this.left();
        mouseY -= this.top();

        for(IGuiEventListener iguieventlistener : this.getEventListeners())
            if(iguieventlistener.mouseReleased(mouseX, mouseY, button))
                flag = true;

        return flag;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        boolean flag = super.mouseScrolled(mouseX, mouseY, delta);

        mouseX -= this.left();
        mouseY -= this.top();

        for(IGuiEventListener iguieventlistener : this.getEventListeners())
            if(iguieventlistener.mouseScrolled(mouseX, mouseY, delta))
                flag = true;

        return flag;
    }

    @Override
    public void tick(){
        for(Widget widget : this.widgets)
            if(widget instanceof ITickableWidget)
                ((ITickableWidget)widget).tick();
    }

    public T getTileOrClose(){
        return this.container.getTileOrClose();
    }
}
