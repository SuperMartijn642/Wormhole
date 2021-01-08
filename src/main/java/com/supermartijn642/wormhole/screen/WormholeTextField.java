package com.supermartijn642.wormhole.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;

/**
 * Created 1/7/2021 by SuperMartijn642
 */
public abstract class WormholeTextField extends WormholeWidget implements ITickableWidget {

    private String text;
    protected int maxLength;
    private int cursorBlinkCounter;
    protected boolean focused;
    protected int lineScrollOffset;
    protected int cursorPosition;
    protected int selectionPos;

    public WormholeTextField(int x, int y, int width, int height, String defaultText, int maxLength){
        super(x, y, width, height);
        this.text = defaultText;
        this.maxLength = maxLength;
    }

    @Override
    public void tick(){
        this.cursorBlinkCounter++;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        this.drawBackground();

        int textColor = this.active ? 14737632 : 7368816;
        int relativeCursor = this.cursorPosition - this.lineScrollOffset;
        int relativeSelection = this.selectionPos - this.lineScrollOffset;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String s = fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.width - 8);
        boolean cursorInView = relativeCursor >= 0 && relativeCursor <= s.length();
        boolean shouldBlink = this.focused && this.cursorBlinkCounter / 6 % 2 == 0 && cursorInView;
        int left = this.x + 4;
        int top = this.y + (this.height - 8) / 2;
        int leftOffset = left;

        if(relativeSelection > s.length())
            relativeSelection = s.length();

        if(!s.isEmpty()){
            String s1 = cursorInView ? s.substring(0, relativeCursor) : s;
            leftOffset = fontRenderer.drawStringWithShadow(s1, (float)left, (float)top, textColor);
        }

        boolean cursorAtEnd = this.cursorPosition < this.text.length();
        int k1 = leftOffset;

        if(!cursorInView)
            k1 = relativeCursor > 0 ? left + this.width : left;
        else if(cursorAtEnd){
            k1 = leftOffset - 1;
            leftOffset--;
        }

        if(!s.isEmpty() && cursorInView && relativeCursor < s.length())
            fontRenderer.drawStringWithShadow(s.substring(relativeCursor), (float)leftOffset, (float)top, textColor);

        if(shouldBlink){
            if(cursorAtEnd)
                Gui.drawRect(k1, top - 1, k1 + 1, top + 1 + fontRenderer.FONT_HEIGHT, -3092272);
            else
                fontRenderer.drawStringWithShadow("_", (float)k1, (float)top, textColor);
        }

        if(relativeSelection != relativeCursor){
            int l1 = left + fontRenderer.getStringWidth(s.substring(0, relativeSelection));
            this.drawSelectionBox(k1, top - 1, l1 - 1, top + 1 + fontRenderer.FONT_HEIGHT);
        }
    }

    protected void drawBackground(){
        Gui.drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, -6250336);
        Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
    }

    protected void drawSelectionBox(int startX, int startY, int endX, int endY){
        if(startX < endX){
            int i = startX;
            startX = endX;
            endX = i;
        }

        if(startY < endY){
            int j = startY;
            startY = endY;
            endY = j;
        }

        if(endX > this.x + this.width)
            endX = this.x + this.width;

        if(startX > this.x + this.width)
            startX = this.x + this.width;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(startX, startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void clear(){
        this.setText("");
    }

    public void setText(String text){
        String oldText = this.text;

        this.setTextSuppressed(text);

        if(!oldText.equals(this.text))
            this.onTextChanged(oldText, text);
    }

    public String getText(){
        return this.text;
    }

    /**
     * Sets {@code text} without calling {@link WormholeTextField#onTextChanged(String, String)}
     */
    public void setTextSuppressed(String text){
        if(text == null)
            text = "";
        else if(text.length() > this.maxLength)
            text = ChatAllowedCharacters.filterAllowedCharacters(text.substring(0, this.maxLength));

        this.lineScrollOffset = 0;
        this.cursorPosition = 0;
        this.selectionPos = 0;
        this.text = text;
    }

    protected void addTextAtCursor(String text){
        String oldText = text;

        text = ChatAllowedCharacters.filterAllowedCharacters(text);
        if(text.length() + this.text.length() > this.maxLength)
            text = text.substring(0, Math.max(this.maxLength - this.text.length(), 0));

        this.text = this.text.substring(0, this.cursorPosition) + text + this.text.substring(this.cursorPosition);
        this.cursorPosition += text.length();
        this.selectionPos = this.cursorPosition;
        this.moveLineOffsetToCursor();

        if(!text.isEmpty())
            this.onTextChanged(oldText, this.text);
    }

    protected void removeAtCursor(){
        if(this.text.isEmpty())
            return;

        String oldText = text;
        if(this.cursorPosition != this.selectionPos){
            this.text = this.text.substring(0, Math.min(this.cursorPosition, this.selectionPos)) + this.text.substring(Math.max(this.cursorPosition, this.selectionPos));
            this.cursorPosition = this.selectionPos = Math.min(this.cursorPosition, this.selectionPos);
        }else if(this.cursorPosition > 0){
            this.text = this.text.substring(0, this.cursorPosition - 1) + this.text.substring(this.cursorPosition);
            this.cursorPosition -= 1;
            this.selectionPos -= 1;
        }
        this.moveLineOffsetToCursor();

        this.onTextChanged(oldText, this.text);
    }

    protected void moveLineOffsetToCursor(){
        if(this.lineScrollOffset > this.cursorPosition)
            this.lineScrollOffset = this.cursorPosition;
        else if(this.lineScrollOffset < this.cursorPosition){
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            while(fontRenderer.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.width - 8).length() + this.lineScrollOffset < this.cursorPosition - 1)
                this.lineScrollOffset++;
        }
    }

    public String getSelectedText(){
        if(this.cursorPosition == this.selectionPos)
            return "";

        return this.text.substring(Math.min(this.cursorPosition, this.selectionPos), Math.max(this.cursorPosition, this.selectionPos));
    }

    protected abstract void onTextChanged(String oldText, String newText);

    @Override
    public void keyTyped(char typedChar, int keyCode){
        if(!this.canWrite())
            return;

        if(GuiScreen.isKeyComboCtrlA(keyCode)){
            this.lineScrollOffset = 0;
            this.cursorPosition = this.text.length();
            this.selectionPos = 0;
        }else if(GuiScreen.isKeyComboCtrlC(keyCode)){
            GuiScreen.setClipboardString(this.getSelectedText());
        }else if(GuiScreen.isKeyComboCtrlV(keyCode)){
            this.addTextAtCursor(GuiScreen.getClipboardString());
        }else if(GuiScreen.isKeyComboCtrlX(keyCode)){
            GuiScreen.setClipboardString(this.getSelectedText());
            this.addTextAtCursor("");
        }else{
            switch(keyCode){
                case 14:
                    this.removeAtCursor();
                    break;
                case 199:
                    if(GuiScreen.isShiftKeyDown()){
                        this.cursorPosition = this.text.length();
                        this.selectionPos = this.text.length();
                    }else{
                        this.cursorPosition = 0;
                        this.selectionPos = 0;
                    }
                    this.moveLineOffsetToCursor();
                    break;
                case 203:
                    this.cursorPosition = Math.max(0, this.cursorPosition - 1);
                    if(GuiScreen.isShiftKeyDown())
                        this.selectionPos = this.cursorPosition;
                    this.moveLineOffsetToCursor();
                    break;
                case 205:
                    this.cursorPosition = Math.min(this.text.length(), this.cursorPosition + 1);
                    if(GuiScreen.isShiftKeyDown())
                        this.selectionPos = this.cursorPosition;
                    this.moveLineOffsetToCursor();
                    break;
                case 207:
                    if(GuiScreen.isShiftKeyDown()){
                        this.cursorPosition = 0;
                        this.selectionPos = 0;
                    }else{
                        this.cursorPosition = this.text.length();
                        this.selectionPos = this.text.length();
                    }
                    this.moveLineOffsetToCursor();
                    break;
                case 211:
                    if(this.cursorPosition != this.selectionPos)
                        this.addTextAtCursor("");
                    break;
                default:
                    if(ChatAllowedCharacters.isAllowedCharacter(typedChar))
                        this.addTextAtCursor(Character.toString(typedChar));
                    break;
            }
        }
    }

    public boolean canWrite(){
        return this.active && this.focused;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button){
        this.focused = this.active && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;

        if(this.focused){
            if(button == 1)
                this.clear();
            else{
                int offset = MathHelper.floor(mouseX) - this.x - 4;

                FontRenderer font = Minecraft.getMinecraft().fontRenderer;
                String s = font.trimStringToWidth(this.text.substring(this.lineScrollOffset), Math.min(offset, this.width - 8));
                this.cursorPosition = s.length() + this.lineScrollOffset;
            }
        }
    }
}
