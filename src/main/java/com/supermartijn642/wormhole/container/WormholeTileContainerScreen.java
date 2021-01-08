package com.supermartijn642.wormhole.container;

import com.supermartijn642.wormhole.screen.IHoverTextWidget;
import com.supermartijn642.wormhole.screen.ITickableWidget;
import com.supermartijn642.wormhole.screen.WormholeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created 12/20/2020 by SuperMartijn642
 */
public abstract class WormholeTileContainerScreen<T extends TileEntity, S extends WormholeTileContainer<T>> extends GuiContainer {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("wormhole", "textures/gui/background.png");
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("wormhole", "textures/gui/slot.png");

    protected final List<WormholeWidget> widgets = new LinkedList<>();
    protected final List<ITickableWidget> tickableWidgets = new LinkedList<>();

    protected final S container;
    protected final ITextComponent title;

    public WormholeTileContainerScreen(S screenContainer, String titleKey){
        super(screenContainer);
        this.container = screenContainer;
        this.title = new TextComponentTranslation(titleKey);
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
    public void initGui(){
        this.xSize = this.sizeX();
        this.ySize = this.sizeY();
        super.initGui();

        this.widgets.clear();
        this.tickableWidgets.clear();
        T tile = this.getTileOrClose();
        if(tile != null)
            this.addWidgets(tile);
    }

    protected abstract void addWidgets(T tile);

    protected <T extends WormholeWidget> T addWidget(T widget){
        this.widgets.add(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.add((ITickableWidget)widget);
        return widget;
    }

    protected <T extends WormholeWidget> T removeWidget(T widget){
        this.widgets.remove(widget);
        if(widget instanceof ITickableWidget)
            this.tickableWidgets.remove(widget);
        return widget;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();

        T tile = this.getTileOrClose();
        if(tile == null)
            return;

        GlStateManager.translate(this.left(), this.top(), 0);
        this.renderBackground(tile, mouseX - this.left(), mouseY - this.top());

        for(Slot slot : this.container.inventorySlots){
            Minecraft.getMinecraft().getTextureManager().bindTexture(SLOT_TEXTURE);
            this.drawTexture(slot.xPos - 1, slot.yPos - 1, 18, 18);
        }
        GlStateManager.translate(-this.left(), -this.top(), 0);

        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableLighting();
        GlStateManager.translate(this.left(), this.top(), 0);
        for(WormholeWidget widget : this.widgets){
            widget.blitOffset = this.zLevel;
            widget.hovered = mouseX - this.left() > widget.x && mouseX - this.left() < widget.x + widget.width &&
                mouseY - this.top() > widget.y && mouseY - this.top() < widget.y + widget.height;
            widget.render(mouseX - this.left(), mouseY - this.top(), partialTicks);
        }

        this.renderForeground(tile, mouseX - this.left(), mouseY - this.top());

        for(WormholeWidget widget : this.widgets){
            if(widget instanceof IHoverTextWidget && widget.isHovered()){
                ITextComponent text = ((IHoverTextWidget)widget).getHoverText();
                if(text != null)
                    this.drawHoveringText(text.getFormattedText(), mouseX - this.left(), mouseY - this.top());
            }
        }
        GlStateManager.translate(-this.left(), -this.top(), 0);
        super.renderHoveredToolTip(mouseX, mouseY);
        this.renderTooltips(tile, mouseX - this.left(), mouseY - this.top());
    }

    protected void renderBackground(T tile, int mouseX, int mouseY){
        this.drawBackground(0, 0, this.sizeX(), this.sizeY());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y){
    }

    protected void renderForeground(T tile, int mouseX, int mouseY){
        this.fontRenderer.drawString(this.title.getFormattedText(), 8, 7, 4210752);
    }

    protected abstract void renderTooltips(T tile, int mouseX, int mouseY);

    protected void drawBackground(float x, float y, float width, float height){
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
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
        float z = this.zLevel;
        GlStateManager.color(1, 1, 1, 1);

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
    public boolean doesGuiPauseGame(){
        return false;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException{
        super.mouseClicked(mouseX, mouseY, button);

        mouseX -= this.left();
        mouseY -= this.top();

        for(WormholeWidget iguieventlistener : this.widgets)
            iguieventlistener.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick){
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        mouseX -= this.left();
        mouseY -= this.top();

        for(WormholeWidget iguieventlistener : this.widgets)
            iguieventlistener.mouseDragged(mouseX, mouseY, clickedMouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button){
        super.mouseReleased(mouseX, mouseY, button);

        mouseX -= this.left();
        mouseY -= this.top();

        for(WormholeWidget iguieventlistener : this.widgets)
            iguieventlistener.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void updateScreen(){
        for(WormholeWidget widget : this.widgets)
            if(widget instanceof ITickableWidget)
                ((ITickableWidget)widget).tick();
    }

    @Override
    public void handleMouseInput() throws IOException{
        super.handleMouseInput();

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth - (int)this.left();
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1 - (int)this.top();

        this.mouseScrolled(mouseX, mouseY, Mouse.getEventDWheel() / 120);
    }

    public void mouseScrolled(int mouseX, int mouseY, int scroll){
        for(WormholeWidget widget : this.widgets)
            widget.mouseScrolled(mouseX, mouseY, scroll);
    }

    public T getTileOrClose(){
        return this.container.getTileOrClose();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException{
        super.keyTyped(typedChar, keyCode);

        for(WormholeWidget widget : this.widgets)
            widget.keyTyped(typedChar, keyCode);
    }
}
