package com.supermartijn642.wormhole.container;

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
    public void render(int mouseX, int mouseY, float partialTicks){
        this.renderBackground();

        T tile = this.getTileOrClose();
        if(tile == null)
            return;

        GlStateManager.translated(this.left(), this.top(), 0);
        this.renderBackground(tile, mouseX, mouseY);

        for(Slot slot : this.container.inventorySlots){
            Minecraft.getInstance().textureManager.bindTexture(SLOT_TEXTURE);
            this.drawTexture(slot.xPos - 1, slot.yPos - 1, 18, 18);
        }
        GlStateManager.translated(-this.left(), -this.top(), 0);

        super.render(mouseX, mouseY, partialTicks);

        GlStateManager.translated(this.left(), this.top(), 0);
        for(Widget widget : this.widgets)
            widget.render(mouseX - (int)this.left(), mouseY - (int)this.top(), partialTicks);

        this.renderForeground(tile, mouseX, mouseY);
        GlStateManager.translated(-this.left(), -this.top(), 0);

        for(Widget widget : this.widgets){
            if(widget instanceof IHoverTextWidget && widget.isHovered()){
                ITextComponent text = ((IHoverTextWidget)widget).getHoverText();
                if(text != null)
                    this.renderTooltip(text.getFormattedText(), mouseX, mouseY);
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
        this.renderTooltips(tile, mouseX - (int)this.left(), mouseY - (int)this.top());
    }

    protected void renderBackground(T tile, int mouseX, int mouseY){
        this.drawBackground(0, 0, this.sizeX(), this.sizeY());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y){
    }

    protected void renderForeground(T tile, int mouseX, int mouseY){
    }

    protected abstract void renderTooltips(T tile, int mouseX, int mouseY);

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
        boolean flag = super.mouseClicked(mouseX, mouseY, button);

        mouseX -= this.left();
        mouseY -= this.top();

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
        boolean flag = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        mouseX -= this.left();
        mouseY -= this.top();

        for(IGuiEventListener iguieventlistener : this.children)
            if(iguieventlistener.mouseDragged(mouseX, mouseY, button, dragX, dragY))
                flag = true;

        return flag;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        boolean flag = super.mouseReleased(mouseX, mouseY, button);

        mouseX -= this.left();
        mouseY -= this.top();

        for(IGuiEventListener iguieventlistener : this.children)
            if(iguieventlistener.mouseReleased(mouseX, mouseY, button))
                flag = true;

        return flag;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        boolean flag = super.mouseScrolled(mouseX, mouseY, delta);

        mouseX -= this.left();
        mouseY -= this.top();

        for(IGuiEventListener iguieventlistener : this.children)
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
