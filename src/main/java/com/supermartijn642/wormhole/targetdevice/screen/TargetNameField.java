package com.supermartijn642.wormhole.targetdevice.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.screen.ITickableWidget;
import com.supermartijn642.wormhole.targetdevice.ITargetProvider;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceNamePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Hand;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 10/28/2020 by SuperMartijn642
 */
public class TargetNameField extends Widget implements IRenderable, IGuiEventListener, ITickableWidget {

    private final ITargetProvider targetProvider;
    private final Hand hand;
    private final int targetIndex;
    private String text;
    private String lastTargetText;
    private List<String> pastText = new LinkedList<>();
    private int cursorBlinkCounter;
    private boolean isEnabled = true;
    private boolean field_212956_h;
    /**
     * The current character index that should be used as start of the rendered text.
     */
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;

    public TargetNameField(ITargetProvider targetProvider, Hand hand, int targetIndex, int x, int y){
        super(x, y, 59, 10, new TranslationTextComponent("wormhole.target.name"));
        this.targetProvider = targetProvider;
        this.hand = hand;
        this.targetIndex = targetIndex;

        this.text = targetProvider.getFromTargets(list -> list.size() > targetIndex ? list.get(targetIndex).name : "", "");
        this.lastTargetText = this.text;
    }

    public void tick(){
        this.cursorBlinkCounter++;

        String s = this.targetProvider.getFromTargets(list -> list.size() > this.targetIndex ? list.get(this.targetIndex).name : "", "");
        if(!s.equals(this.lastTargetText)){
            if(s.equals(this.text))
                this.pastText.clear();
            else{
                int index = this.pastText.indexOf(s);
                if(index < 0){
                    this.text = s;
                    this.setCursorPositionEnd();
                    this.setSelectionPos(this.cursorPosition);
                }else
                    this.pastText.subList(0, index + 1).clear();
            }
            this.lastTargetText = s;
        }
    }

    protected IFormattableTextComponent getNarrationMessage(){
        ITextComponent itextcomponent = this.getMessage();
        return new TranslationTextComponent("gui.narrate.editBox", itextcomponent, this.text);
    }

    /**
     * returns the text between the cursor and selectionEnd
     */
    public String getSelectedText(){
        int i = Math.min(this.cursorPosition, this.selectionEnd);
        int j = Math.max(this.cursorPosition, this.selectionEnd);
        return this.text.substring(i, j);
    }

    /**
     * Adds the given text after the cursor, or replaces the currently selected text if there is a selection.
     */
    public void writeText(String textToWrite){
        int i = Math.min(this.cursorPosition, this.selectionEnd);
        int j = Math.max(this.cursorPosition, this.selectionEnd);
        int k = PortalTarget.MAX_NAME_LENGTH - this.text.length() - (i - j);
        String s = SharedConstants.filterAllowedCharacters(textToWrite);
        int l = s.length();
        if(k < l){
            s = s.substring(0, k);
            l = k;
        }

        this.pastText.add(this.text);
        this.text = (new StringBuilder(this.text)).replace(i, j, s).toString();
        this.clampCursorPosition(i + l);
        this.setSelectionPos(this.cursorPosition);
        this.onTextChanged(this.text);
    }

    private void onTextChanged(String text){
        Wormhole.CHANNEL.sendToServer(new TargetDeviceNamePacket(this.hand, this.targetIndex, text));
        this.nextNarration = Util.milliTime() + 500L;
    }

    private void delete(int p_212950_1_){
        if(Screen.hasControlDown()){
            this.deleteWords(p_212950_1_);
        }else{
            this.deleteFromCursor(p_212950_1_);
        }

    }

    /**
     * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in
     * which case the selection is deleted instead.
     */
    public void deleteWords(int num){
        if(!this.text.isEmpty()){
            if(this.selectionEnd != this.cursorPosition){
                this.writeText("");
            }else{
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
            }
        }
    }

    /**
     * Deletes the given number of characters from the current cursor's position, unless there is currently a selection,
     * in which case the selection is deleted instead.
     */
    public void deleteFromCursor(int num){
        if(!this.text.isEmpty()){
            if(this.selectionEnd != this.cursorPosition){
                this.writeText("");
            }else{
                int i = this.func_238516_r_(num);
                int j = Math.min(i, this.cursorPosition);
                int k = Math.max(i, this.cursorPosition);
                if(j != k){
                    this.text = new StringBuilder(this.text).delete(j, k).toString();
                    this.setCursorPosition(j);
                }
            }
        }
    }

    /**
     * Gets the starting index of the word at the specified number of words away from the cursor position.
     */
    public int getNthWordFromCursor(int numWords){
        return this.getNthWordFromPos(numWords, this.getCursorPosition());
    }

    /**
     * Gets the starting index of the word at a distance of the specified number of words away from the given position.
     */
    private int getNthWordFromPos(int n, int pos){
        return this.getNthWordFromPosWS(n, pos, true);
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
     */
    private int getNthWordFromPosWS(int n, int pos, boolean skipWs){
        int i = pos;
        boolean flag = n < 0;
        int j = Math.abs(n);

        for(int k = 0; k < j; ++k){
            if(!flag){
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if(i == -1){
                    i = l;
                }else{
                    while(skipWs && i < l && this.text.charAt(i) == ' '){
                        ++i;
                    }
                }
            }else{
                while(skipWs && i > 0 && this.text.charAt(i - 1) == ' '){
                    --i;
                }

                while(i > 0 && this.text.charAt(i - 1) != ' '){
                    --i;
                }
            }
        }

        return i;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursorBy(int num){
        this.setCursorPosition(this.func_238516_r_(num));
    }

    private int func_238516_r_(int p_238516_1_){
        return Util.func_240980_a_(this.text, this.cursorPosition, p_238516_1_);
    }

    /**
     * Sets the current position of the cursor.
     */
    public void setCursorPosition(int pos){
        this.clampCursorPosition(pos);
        if(!this.field_212956_h){
            this.setSelectionPos(this.cursorPosition);
        }

        this.onTextChanged(this.text);
    }

    public void clampCursorPosition(int pos){
        this.cursorPosition = MathHelper.clamp(pos, 0, this.text.length());
    }

    /**
     * Moves the cursor to the very start of this text box.
     */
    public void setCursorPositionZero(){
        this.setCursorPosition(0);
    }

    /**
     * Moves the cursor to the very end of this text box.
     */
    public void setCursorPositionEnd(){
        this.setCursorPosition(this.text.length());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(!this.canWrite()){
            return false;
        }else{
            this.field_212956_h = Screen.hasShiftDown();
            if(Screen.isSelectAll(keyCode)){
                this.setCursorPositionEnd();
                this.setSelectionPos(0);
                return true;
            }else if(Screen.isCopy(keyCode)){
                Minecraft.getInstance().keyboardListener.setClipboardString(this.getSelectedText());
                return true;
            }else if(Screen.isPaste(keyCode)){
                if(this.isEnabled){
                    this.writeText(Minecraft.getInstance().keyboardListener.getClipboardString());
                }

                return true;
            }else if(Screen.isCut(keyCode)){
                Minecraft.getInstance().keyboardListener.setClipboardString(this.getSelectedText());
                if(this.isEnabled){
                    this.writeText("");
                }

                return true;
            }else{
                switch(keyCode){
                    case 259:
                        if(this.isEnabled){
                            this.field_212956_h = false;
                            this.delete(-1);
                            this.field_212956_h = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if(this.isEnabled){
                            this.field_212956_h = false;
                            this.delete(1);
                            this.field_212956_h = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if(Screen.hasControlDown()){
                            this.setCursorPosition(this.getNthWordFromCursor(1));
                        }else{
                            this.moveCursorBy(1);
                        }

                        return true;
                    case 263:
                        if(Screen.hasControlDown()){
                            this.setCursorPosition(this.getNthWordFromCursor(-1));
                        }else{
                            this.moveCursorBy(-1);
                        }

                        return true;
                    case 268:
                        this.setCursorPositionZero();
                        return true;
                    case 269:
                        this.setCursorPositionEnd();
                        return true;
                }
            }
        }
    }

    public boolean canWrite(){
        return this.visible && this.isFocused() && this.isEnabled;
    }

    public boolean charTyped(char codePoint, int modifiers){
        if(!this.canWrite()){
            return false;
        }else if(SharedConstants.isAllowedCharacter(codePoint)){
            if(this.isEnabled){
                this.writeText(Character.toString(codePoint));
            }
            return true;
        }else{
            return false;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(!this.visible){
            return false;
        }else{
            boolean flag = mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
            this.setFocused(flag);

            if(this.isFocused() && flag && button == 0){
                int i = MathHelper.floor(mouseX) - this.x;
                i -= 4;

                FontRenderer font = Minecraft.getInstance().fontRenderer;
                String s = font.func_238412_a_(this.text.substring(this.lineScrollOffset), this.getAdjustedWidth());
                this.setCursorPosition(font.func_238412_a_(s, i).length() + this.lineScrollOffset);
                return true;
            }else{
                return false;
            }
        }
    }

    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
        if(this.visible){
            int i = this.isFocused() ? -1 : -6250336;
            fill(matrixStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, i);
            fill(matrixStack, this.x, this.y, this.x + this.width, this.y + this.height, -16777216);

            int enabledTextColor = 14737632;
            int disabledTextColor = 7368816;
            int i2 = this.isEnabled ? enabledTextColor : disabledTextColor;
            int j = this.cursorPosition - this.lineScrollOffset;
            int k = this.selectionEnd - this.lineScrollOffset;
            FontRenderer font = Minecraft.getInstance().fontRenderer;
            String s = font.func_238412_a_(this.text.substring(this.lineScrollOffset), this.getAdjustedWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused() && this.cursorBlinkCounter / 6 % 2 == 0 && flag;
            int l = this.x + 4;
            int i1 = this.y + (this.height - 8) / 2;
            int j1 = l;
            if(k > s.length()){
                k = s.length();
            }

            if(!s.isEmpty()){
                String s1 = flag ? s.substring(0, j) : s;
                j1 = font.func_243246_a(matrixStack, new StringTextComponent(s1), (float)l, (float)i1, i2);
            }

            boolean flag2 = this.cursorPosition < this.text.length() || this.text.length() >= PortalTarget.MAX_NAME_LENGTH;
            int k1 = j1;
            if(!flag){
                k1 = j > 0 ? l + this.width : l;
            }else if(flag2){
                k1 = j1 - 1;
                --j1;
            }

            if(!s.isEmpty() && flag && j < s.length()){
                font.func_243246_a(matrixStack, new StringTextComponent(s.substring(j)), (float)j1, (float)i1, i2);
            }

            if(flag1){
                if(flag2){
                    AbstractGui.fill(matrixStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
                }else{
                    font.drawStringWithShadow(matrixStack, "_", (float)k1, (float)i1, i2);
                }
            }

            if(k != j){
                int l1 = l + font.getStringWidth(s.substring(0, k));
                this.drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
            }

        }
    }

    /**
     * Draws the blue selection box.
     */
    private void drawSelectionBox(int startX, int startY, int endX, int endY){
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

        if(endX > this.x + this.width){
            endX = this.x + this.width;
        }

        if(startX > this.x + this.width){
            startX = this.x + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(startX, startY, 0.0D).endVertex();
        tessellator.draw();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    /**
     * returns the current position of the cursor
     */
    public int getCursorPosition(){
        return this.cursorPosition;
    }

    public boolean changeFocus(boolean focus){
        return this.visible && this.isEnabled && super.changeFocus(focus);
    }

    public boolean isMouseOver(double mouseX, double mouseY){
        return this.visible && mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
    }

    protected void onFocusedChanged(boolean focused){
        if(focused){
            this.cursorBlinkCounter = 0;
        }
    }

    /**
     * returns the width of the textbox depending on if background drawing is enabled
     */
    public int getAdjustedWidth(){
        return this.width - 8;
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the
     * selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
     */
    public void setSelectionPos(int position){
        int i = this.text.length();
        this.selectionEnd = MathHelper.clamp(position, 0, i);
        if(this.lineScrollOffset > i){
            this.lineScrollOffset = i;
        }

        int j = this.getAdjustedWidth();
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        String s = font.func_238412_a_(this.text.substring(this.lineScrollOffset), j);
        int k = s.length() + this.lineScrollOffset;
        if(this.selectionEnd == this.lineScrollOffset){
            this.lineScrollOffset -= font.func_238413_a_(this.text, j, true).length();
        }

        if(this.selectionEnd > k){
            this.lineScrollOffset += this.selectionEnd - k;
        }else if(this.selectionEnd <= this.lineScrollOffset){
            this.lineScrollOffset -= this.lineScrollOffset - this.selectionEnd;
        }

        this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, i);
    }

    public void setEnabled(boolean enabled){
        this.isEnabled = enabled;
    }
}
