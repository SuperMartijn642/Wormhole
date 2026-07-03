package com.supermartijn642.wormhole.targetdevice;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.ItemBaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.premade.LabelWidget;
import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.screen.ScreenBlockRenderer;
import com.supermartijn642.wormhole.screen.WormholeColoredButton;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceAddPacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceRemovePacket;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public class TargetDeviceScreen extends ItemBaseWidget {

    public static final Identifier BACKGROUND = getTexture("select_target_screen/device_background");
    public static final Identifier SELECT_HIGHLIGHT = getTexture("select_target_screen/device_select_highlight");
    public static final Identifier HOVER_HIGHLIGHT = getTexture("select_target_screen/device_hover_highlight");
    public static final Identifier LOCATION_ICON = getTexture("select_target_screen/location_icon");
    public static final Identifier ENERGY_ICON = getTexture("select_target_screen/lightning_icon");
    public static final Identifier DIMENSION_ICON = getTexture("select_target_screen/dimension_icon");
    public static final Identifier DIRECTION_ICON = getTexture("select_target_screen/direction_icon");
    public static final Identifier SEPARATOR = getTexture("select_target_screen/separator");

    private static Identifier getTexture(String name){
        return Identifier.fromNamespaceAndPath("wormhole", "gui/" + name);
    }

    private static final int WIDTH = 324, HEIGHT = 185;

    private final InteractionHand hand;
    private final BlockPos currentPos;
    private final float currentYaw;
    private int selectedTarget;
    private boolean selectedCurrentTarget = false;
    private TextFieldWidget currentTargetNameField;
    private final List<LabelWidget> targetNameLabels = new LinkedList<>();
    private WormholeColoredButton removeButton;

    public TargetDeviceScreen(InteractionHand hand, BlockPos pos, float yaw){
        super(0, 0, WIDTH, HEIGHT, hand, stack -> stack.getItem() instanceof TargetDeviceItem);
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
    protected Component getNarrationMessage(ItemStack object){
        return TextComponents.translation("wormhole.target_device.gui.title").get();
    }

    @Override
    protected void addWidgets(ItemStack stack){
        int targetCapacity = TargetDeviceItem.getMaxTargetCount(stack);

        for(int count = 0; count < Math.min(10, targetCapacity); count++){
            final int index = count;
            int y = 18 + index * 16;
            this.targetNameLabels.add(this.addWidget(new LabelWidget(7, y, 102, 12, () -> TextComponents.string(this.getOrDefault(list -> list.size() > index ? list.get(index).name : "", "")).get())));
        }

        this.currentTargetNameField = this.addWidget(new TextFieldWidget(215, 18, 102, 12, "", PortalTarget.MAX_NAME_LENGTH));
        this.currentTargetNameField.setSuggestion(TextComponents.translation("wormhole.target_device.gui.target_name").format());
        if(this.selectedCurrentTarget)
            this.currentTargetNameField.setFocused(true);

        this.removeButton = this.addWidget(new WormholeColoredButton(131, 160, 62, 11, TextComponents.empty().get(), () -> {
            if(this.selectedTarget >= 0)
                Wormhole.CHANNEL.sendToServer(new TargetDeviceRemovePacket(this.hand, this.selectedTarget));
            else if(this.selectedCurrentTarget)
                Wormhole.CHANNEL.sendToServer(new TargetDeviceAddPacket(this.hand, this.currentTargetNameField.getText().trim(), this.currentPos, this.currentYaw));
        }));
    }

    @Override
    protected void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, ItemStack object){
        graphics.submitSprite(BACKGROUND, 0, 0, this.width(), this.height());

        // draw target select highlight
        if(this.selectedTarget >= 0)
            graphics.submitSprite(SELECT_HIGHLIGHT, 5, 16 + 16 * this.selectedTarget, 106, 16);
        else if(this.selectedCurrentTarget)
            graphics.submitSprite(SELECT_HIGHLIGHT, 213, 16, 106, 16);

        super.renderBackground(context, graphics, mouseX, mouseY, object);
    }

    @Override
    protected void renderForeground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, ItemStack object){
        super.renderForeground(context, graphics, mouseX, mouseY, object);

        // draw titles
        graphics.submitText(TextComponents.translation("wormhole.target_device.gui.title").get(), 58, 3, p -> p.color(Integer.MAX_VALUE).centerHorizontally());
        graphics.submitText(TextComponents.translation("wormhole.target_device.gui.current_location").get(), 266, 3, p -> p.color(Integer.MAX_VALUE).centerHorizontally());

        // draw hover highlight
        if(mouseX > 5 && mouseX < 111 && mouseY > 16 && mouseY < 176){
            int targetIndex = (mouseY - 16) / 16;
            if(this.getOrDefault(list -> list.size() > targetIndex && list.get(targetIndex) != null, false))
                graphics.submitSprite(HOVER_HIGHLIGHT, 5, 16 + targetIndex * 16, 106, 16);
        }else if(mouseX > 213 && mouseX < 319 && mouseY > 16 && mouseY < 32)
            graphics.submitSprite(HOVER_HIGHLIGHT, 213, 16, 106, 16);

        // draw target info
        if(this.selectedTarget >= 0){
            PortalTarget target = this.getOrDefault(list -> list.size() > this.selectedTarget ? list.get(this.selectedTarget) : null, null);
            if(target != null)
                this.renderTargetInfo(graphics, target.name, target.getPos(), target.dimension, target.dimensionDisplayName, target.yaw);
        }else if(this.selectedCurrentTarget){
            ResourceKey<Level> dimension = ClientUtils.getWorld().dimension();
            this.renderTargetInfo(graphics, this.currentTargetNameField.getText().trim(), this.currentPos, dimension, TextComponents.dimension(dimension).get(), this.currentYaw);
        }

        this.updateAddRemoveButton();
    }

    private void renderTargetInfo(GuiGraphicsHelper graphics, String name, BlockPos pos, ResourceKey<Level> dimension, Component dimensionName, float yaw){
        graphics.submitText(name, 162, 31, p -> p.color(Integer.MAX_VALUE).centerHorizontally());

        graphics.submitSprite(SEPARATOR, 124, 41, 77, 1);

        // location
        graphics.submitSprite(LOCATION_ICON, 121, 47, 9, 9);
        graphics.submitText("(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")", 132, 48, p -> p.color(Integer.MAX_VALUE));
        // dimension
        Block block;
        if(dimension.equals(Level.OVERWORLD))
            block = Blocks.DIRT_PATH;
        else if(dimension.equals(Level.NETHER))
            block = Blocks.NETHERRACK;
        else if(dimension.equals(Level.END))
            block = Blocks.END_STONE;
        else
            block = null;
        if(block == null)
            graphics.submitSprite(DIMENSION_ICON, 121, 59, 9, 9);
        else
            graphics.submitFeatures(
                119, 57, 13, 13,
                (poseStack, output) -> ScreenBlockRenderer.drawBlock(poseStack, output, block, 6.5, 6.5, 5.5, 45, 40)
            );
        graphics.submitText(dimensionName, 132, 60, p -> p.color(Integer.MAX_VALUE));
        // direction
        graphics.submitSprite(DIRECTION_ICON, 119, 69, 13, 13);
        graphics.submitText(TextComponents.translation("wormhole.direction." + Direction.fromYRot(yaw)).get(), 132, 72, p -> p.color(Integer.MAX_VALUE));
    }

    @Override
    public void renderTooltips(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY, ItemStack object){
        // location
        if(mouseX >= 120 && mouseX <= 131 && mouseY >= 46 && mouseY <= 57)
            graphics.submitTooltipForTopStratum(c -> c.text(TextComponents.translation("wormhole.target.location").get()), mouseX, mouseY);
            // dimension
        else if(mouseX >= 120 && mouseX <= 131 && mouseY >= 58 && mouseY <= 69)
            graphics.submitTooltipForTopStratum(c -> c.text(TextComponents.translation("wormhole.target.dimension").get()), mouseX, mouseY);
            // direction
        else if(mouseX >= 120 && mouseX <= 131 && mouseY >= 70 && mouseY <= 81)
            graphics.submitTooltipForTopStratum(c -> c.text(TextComponents.translation("wormhole.target.direction").get()), mouseX, mouseY);
    }

    private void updateAddRemoveButton(){
        if(this.selectedTarget >= 0){
            boolean notEmpty = this.getOrDefault(list -> list.size() > this.selectedTarget && list.get(this.selectedTarget) != null, false);
            this.removeButton.setVisible();
            this.removeButton.setColorRed();
            this.removeButton.setText(TextComponents.translation("wormhole.portal.targets.gui.remove").get());
            this.removeButton.setActive(notEmpty);
        }else if(this.selectedCurrentTarget){
            boolean notEmpty = !this.currentTargetNameField.getText().trim().isEmpty();
            boolean space = this.getOrDefault(list -> {
                if(list.size() < TargetDeviceItem.getMaxTargetCount(this.object))
                    return true;
                for(PortalTarget target : list)
                    if(target == null)
                        return true;
                return false;
            }, false);
            this.removeButton.setVisible();
            this.removeButton.setColorWhite();
            this.removeButton.setText(TextComponents.translation("wormhole.portal.targets.gui.add").get());
            this.removeButton.setActive(notEmpty && space);
        }else{
            this.removeButton.setInvisible();
        }
    }

    public <T> T getOrDefault(Function<List<PortalTarget>,T> function, T other){
        if(this.validateObjectOrClose())
            return function.apply(TargetDeviceItem.getTargets(this.object));
        return other;
    }

    @Override
    protected boolean mousePressed(int mouseX, int mouseY, MouseButtonInfo info, boolean isDoubleClick, boolean hasBeenHandled, ItemStack object){
        hasBeenHandled |= super.mousePressed(mouseX, mouseY, info, isDoubleClick, hasBeenHandled, object);

        if(info.button() == 0){
            if(mouseX > 5 && mouseX < 111 && mouseY > 16 && mouseY < 176){
                int targetIndex = (mouseY - 16) / 16;
                if(this.getOrDefault(list -> list.size() > targetIndex && list.get(targetIndex) != null, false)){
                    AbstractButtonWidget.playClickSound();
                    this.selectedTarget = targetIndex;
                    this.selectedCurrentTarget = false;
                }
                return true;
            }else if(mouseX > 213 && mouseX < 319 && mouseY > 16 && mouseY < 32){
                AbstractButtonWidget.playClickSound();
                this.selectedTarget = -1;
                this.selectedCurrentTarget = true;
                return true;
            }
        }

        return hasBeenHandled;
    }
}
