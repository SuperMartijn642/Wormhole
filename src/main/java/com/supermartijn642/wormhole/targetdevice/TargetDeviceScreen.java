package com.supermartijn642.wormhole.targetdevice;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.gui.BaseScreen;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.TextFieldWidget;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.screen.ScreenBlockRenderer;
import com.supermartijn642.wormhole.screen.WormholeColoredButton;
import com.supermartijn642.wormhole.screen.WormholeLabel;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceAddPacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceRemovePacket;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public class TargetDeviceScreen extends BaseScreen {

    private static final ResourceLocation BACKGROUND = getTexture("select_target_screen/device_background");
    private static final ResourceLocation SELECT_HIGHLIGHT = getTexture("select_target_screen/device_select_highlight");
    private static final ResourceLocation HOVER_HIGHLIGHT = getTexture("select_target_screen/device_hover_highlight");
    private static final ResourceLocation LOCATION_ICON = getTexture("select_target_screen/location_icon");
    private static final ResourceLocation ENERGY_ICON = getTexture("select_target_screen/lightning_icon");
    private static final ResourceLocation DIMENSION_ICON = getTexture("select_target_screen/dimension_icon");
    private static final ResourceLocation DIRECTION_ICON = getTexture("select_target_screen/direction_icon");
    private static final ResourceLocation SEPARATOR = getTexture("select_target_screen/separator");

    private static ResourceLocation getTexture(String name){
        return new ResourceLocation("wormhole", "textures/gui/" + name + ".png");
    }

    private static final int WIDTH = 324, HEIGHT = 185;

    private final PlayerEntity player;
    public final Hand hand;
    private final BlockPos currentPos;
    private final float currentYaw;
    private int selectedTarget;
    private boolean selectedCurrentTarget = false;
    private TextFieldWidget currentTargetNameField;
    private final List<WormholeLabel> targetNameLabels = new LinkedList<>();
    private WormholeColoredButton removeButton;

    public TargetDeviceScreen(PlayerEntity player, Hand hand, BlockPos pos, float yaw){
        super(new TranslationTextComponent("wormhole.target_device.gui.title"));
        this.player = player;
        this.hand = hand;
        this.currentPos = pos;
        this.currentYaw = yaw;

        // set the selected target to a non-null target
        this.selectedTarget = this.getOrDefault(list -> {
            for(int i = 0; i < list.size(); i++)
                if(list.get(i) != null)
                    return i;
            return -1;
        }, -1);
        if(this.selectedTarget == -1)
            this.selectedCurrentTarget = true;
    }

    @Override
    protected float sizeX(){
        return WIDTH;
    }

    @Override
    protected float sizeY(){
        return HEIGHT;
    }

    @Override
    protected void addWidgets(){
        int targetCapacity = this.getFromStack(TargetDeviceItem::getMaxTargetCount, 0);

        for(int count = 0; count < Math.min(10, targetCapacity); count++){
            final int index = count;
            int y = 18 + index * 16;
            this.targetNameLabels.add(this.addWidget(new WormholeLabel(7, y, 102, 12, () -> this.getOrDefault(list -> list.size() > index ? list.get(index).name : "", ""), false)));
        }

        this.currentTargetNameField = this.addWidget(new TextFieldWidget(215, 18, 102, 12, "", PortalTarget.MAX_NAME_LENGTH));
        this.currentTargetNameField.setSuggestion(I18n.format("wormhole.target_device.gui.target_name"));
        if(this.selectedCurrentTarget)
            this.currentTargetNameField.setFocused(true);

        this.removeButton = this.addWidget(new WormholeColoredButton(131, 160, 62, 11, "", () -> {
            if(this.selectedTarget >= 0)
                Wormhole.CHANNEL.sendToServer(new TargetDeviceRemovePacket(this.hand, this.selectedTarget));
            else if(this.selectedCurrentTarget)
                Wormhole.CHANNEL.sendToServer(new TargetDeviceAddPacket(this.hand, this.currentTargetNameField.getText().trim(), this.currentPos, this.currentYaw));
        }));
    }

    @Override
    protected void render(int mouseX, int mouseY){
        ScreenUtils.bindTexture(BACKGROUND);
        ScreenUtils.drawTexture(0, 0, this.sizeX(), this.sizeY());

        // draw titles
        ScreenUtils.drawCenteredString(this.font, this.title, 58, 3, Integer.MAX_VALUE);
        ScreenUtils.drawCenteredString(this.font, I18n.format("wormhole.target_device.gui.current_location"), 266, 3, Integer.MAX_VALUE);

        GlStateManager.enableAlphaTest();
        // draw target select highlight
        if(this.selectedTarget >= 0){
            ScreenUtils.bindTexture(SELECT_HIGHLIGHT);
            ScreenUtils.drawTexture(5, 16 + 16 * this.selectedTarget, 106, 16);
        }else if(this.selectedCurrentTarget){
            ScreenUtils.bindTexture(SELECT_HIGHLIGHT);
            ScreenUtils.drawTexture(213, 16, 106, 16);
        }

        // draw hover highlight
        if(mouseX > 5 && mouseX < 111 && mouseY > 16 && mouseY < 176){
            int targetIndex = (mouseY - 16) / 16;
            if(this.getOrDefault(list -> list.size() > targetIndex && list.get(targetIndex) != null, false)){
                ScreenUtils.bindTexture(HOVER_HIGHLIGHT);
                ScreenUtils.drawTexture(5, 16 + targetIndex * 16, 106, 16);
            }
        }else if(mouseX > 213 && mouseX < 319 && mouseY > 16 && mouseY < 32 && !(mouseX > 215 && mouseX < 317 && mouseY > 18 && mouseY < 30)){
            ScreenUtils.bindTexture(HOVER_HIGHLIGHT);
            ScreenUtils.drawTexture(213, 16, 106, 16);
        }

        // draw target info
        if(this.selectedTarget >= 0){
            PortalTarget target = this.getOrDefault(list -> list.size() > this.selectedTarget ? list.get(this.selectedTarget) : null, null);
            if(target != null)
                this.renderTargetInfo(target.name, target.getPos(), target.dimension, target.dimensionDisplayName, target.yaw);
        }else if(this.selectedCurrentTarget){
            String dimension = this.player.world.getDimension().getType().getRegistryName().toString();
            String dimensionName = dimension.substring(Math.min(dimension.length() - 1, Math.max(0, dimension.indexOf(':') + 1))).toLowerCase();
            dimensionName = dimensionName.substring(0, 1).toUpperCase() + dimensionName.substring(1);
            for(int i = 0; i < dimensionName.length() - 1; i++)
                if(dimensionName.charAt(i) == '_' && Character.isAlphabetic(dimensionName.charAt(i + 1)))
                    dimensionName = dimensionName.substring(0, i) + ' ' + (i + 2 < dimensionName.length() ? dimensionName.substring(i + 1, i + 2).toUpperCase() + dimensionName.substring(i + 2) : dimensionName.substring(i + 1).toUpperCase());
            this.renderTargetInfo(this.currentTargetNameField.getText().trim(), this.currentPos, this.player.world.getDimension().getType().getId(), dimensionName, this.currentYaw);
        }

        this.updateAddRemoveButton();
    }

    private void renderTargetInfo(String name, BlockPos pos, int dimension, String dimensionName, float yaw){
        ScreenUtils.drawCenteredString(this.font, name, 162, 31, Integer.MAX_VALUE);

        ScreenUtils.bindTexture(SEPARATOR);
        ScreenUtils.drawTexture(124, 41, 77, 1);

        // location
        GlStateManager.enableAlphaTest();
        ScreenUtils.bindTexture(LOCATION_ICON);
        ScreenUtils.drawTexture(121, 47, 9, 9);
        ScreenUtils.drawString(this.font, "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")", 132, 48, Integer.MAX_VALUE);
        // dimension
        Block block = null;
        if(dimension == DimensionType.OVERWORLD.getId())
            block = Blocks.GRASS_PATH;
        else if(dimension == DimensionType.THE_NETHER.getId())
            block = Blocks.NETHERRACK;
        else if(dimension == DimensionType.THE_END.getId())
            block = Blocks.END_STONE;
        if(block == null){
            ScreenUtils.bindTexture(DIMENSION_ICON);
            ScreenUtils.drawTexture(121, 59, 9, 9);
        }else{
            ScreenBlockRenderer.drawBlock(block, this.left() + 125.5, this.top() + 63.5, 5.5, 45, 40);
        }
        ScreenUtils.drawString(this.font, dimensionName, 132, 60, Integer.MAX_VALUE);
        // direction
        GlStateManager.enableAlphaTest();
        ScreenUtils.bindTexture(DIRECTION_ICON);
        ScreenUtils.drawTexture(119, 69, 13, 13);
        ScreenUtils.drawString(this.font, I18n.format("wormhole.direction." + Direction.fromAngle(yaw).toString()), 132, 72, Integer.MAX_VALUE);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY){
        // location
        if(mouseX >= 120 && mouseX <= 131 && mouseY >= 46 && mouseY <= 57)
            this.renderTooltip(new TranslationTextComponent("wormhole.target.location").getFormattedText(), mouseX, mouseY);
            // dimension
        else if(mouseX >= 120 && mouseX <= 131 && mouseY >= 58 && mouseY <= 69)
            this.renderTooltip(new TranslationTextComponent("wormhole.target.dimension").getFormattedText(), mouseX, mouseY);
            // direction
        else if(mouseX >= 120 && mouseX <= 131 && mouseY >= 70 && mouseY <= 81)
            this.renderTooltip(new TranslationTextComponent("wormhole.target.direction").getFormattedText(), mouseX, mouseY);
    }

    private void updateAddRemoveButton(){
        if(this.selectedTarget >= 0){
            boolean notEmpty = this.getOrDefault(list -> list.size() > this.selectedTarget && list.get(this.selectedTarget) != null, false);
            this.removeButton.setVisible();
            this.removeButton.setColorRed();
            this.removeButton.setTextKey("wormhole.portal.targets.gui.remove");
            this.removeButton.active = notEmpty;
        }else if(this.selectedCurrentTarget){
            boolean notEmpty = !this.currentTargetNameField.getText().trim().isEmpty();
            boolean space = this.getOrDefault(list -> {
                if(list.size() < this.getFromStack(TargetDeviceItem::getMaxTargetCount, 0))
                    return true;
                for(PortalTarget target : list)
                    if(target == null)
                        return true;
                return false;
            }, false);
            this.removeButton.setVisible();
            this.removeButton.setColorWhite();
            this.removeButton.setTextKey("wormhole.portal.targets.gui.add");
            this.removeButton.active = notEmpty && space;
        }else{
            this.removeButton.setInvisible();
        }
    }

    public <T> T getOrDefault(Function<List<PortalTarget>,T> function, T other){
        ItemStack stack = this.player.getHeldItem(this.hand);
        if(!stack.isEmpty() && stack.getItem() instanceof TargetDeviceItem)
            return function.apply(TargetDeviceItem.getTargets(stack));
        this.closeScreen();
        return other;
    }

    public <T> T getFromStack(Function<ItemStack,T> function, T other){
        ItemStack stack = this.player.getHeldItem(this.hand);
        if(!stack.isEmpty() && stack.getItem() instanceof TargetDeviceItem)
            return function.apply(stack);
        this.closeScreen();
        return other;
    }

    @Override
    protected void onMousePress(int mouseX, int mouseY, int button){
        if(button != 0)
            return;

        if(mouseX > 5 && mouseX < 111 && mouseY > 16 && mouseY < 176){
            int targetIndex = (mouseY - 16) / 16;
            if(this.getOrDefault(list -> list.size() > targetIndex && list.get(targetIndex) != null, false)){
                AbstractButtonWidget.playClickSound();
                this.selectedTarget = targetIndex;
                this.selectedCurrentTarget = false;
            }
        }else if(mouseX > 213 && mouseX < 319 && mouseY > 16 && mouseY < 32 && !(mouseX > 215 && mouseX < 317 && mouseY > 18 && mouseY < 30)){
            AbstractButtonWidget.playClickSound();
            this.selectedTarget = -1;
            this.selectedCurrentTarget = true;
        }
    }
}
