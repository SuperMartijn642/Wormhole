package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.EnergyFormat;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import com.supermartijn642.core.gui.widget.premade.LabelWidget;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.WormholeClient;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalAddTargetPacket;
import com.supermartijn642.wormhole.portal.packets.PortalClearTargetPacket;
import com.supermartijn642.wormhole.portal.packets.PortalMoveTargetPacket;
import com.supermartijn642.wormhole.portal.packets.PortalSelectTargetPacket;
import com.supermartijn642.wormhole.screen.ArrowButton;
import com.supermartijn642.wormhole.screen.WormholeColoredButton;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 11/3/2020 by SuperMartijn642
 */
public class PortalTargetScreen extends PortalGroupScreen {

    private static final ResourceLocation BACKGROUND = getTexture("select_target_screen/background"), BACKGROUND_WITH_DEVICE = getTexture("select_target_screen/background_with_device");
    private static final ResourceLocation SELECT_HIGHLIGHT = getTexture("select_target_screen/select_highlight"), SELECT_HIGHLIGHT_DEVICE = getTexture("select_target_screen/device_select_highlight");
    private static final ResourceLocation HOVER_HIGHLIGHT = getTexture("select_target_screen/hover_highlight"), HOVER_HIGHLIGHT_DEVICE = getTexture("select_target_screen/device_hover_highlight");
    private static final ResourceLocation LOCATION_ICON = getTexture("select_target_screen/location_icon");
    private static final ResourceLocation ENERGY_ICON = getTexture("select_target_screen/lightning_icon");
    private static final ResourceLocation DIMENSION_ICON = getTexture("select_target_screen/dimension_icon");
    private static final ResourceLocation DIRECTION_ICON = getTexture("select_target_screen/direction_icon");
    private static final ResourceLocation STAR_ICON = getTexture("select_target_screen/star_icon");
    private static final ResourceLocation SEPARATOR = getTexture("select_target_screen/separator");

    private static ResourceLocation getTexture(String name){
        return new ResourceLocation("wormhole", "textures/gui/" + name + ".png");
    }

    private static final int WIDTH = 240, HEIGHT = 185;
    private static final int WIDTH_WITH_DEVICE = 353, HEIGHT_WITH_DEVICE = 185;

    private final boolean hasTargetDevice;
    public final InteractionHand hand;

    private int scrollOffset = 0;
    private int selectedPortalTarget;
    private int selectedDeviceTarget = -1;
    private final List<LabelWidget> portalTargetNameLabels = new LinkedList<>();
    private final List<ArrowButton> portalUpArrows = new LinkedList<>();
    private final List<ArrowButton> portalDownArrows = new LinkedList<>();
    private final List<LabelWidget> deviceTargetNameLabels = new LinkedList<>();
    private WormholeColoredButton selectButton, removeButton;
    private PortalTargetEditColorButton colorButton;

    public PortalTargetScreen(BlockPos pos){
        super(0, 0, pos);

        // set the selected target to the portal's active target if it's not null
        if(this.validateObjectOrClose())
            this.selectedPortalTarget = this.object.getActiveTarget() == null ? -1 : this.object.getActiveTargetIndex();

        // check for a target device
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack stack = ClientUtils.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        if(!(stack.getItem() instanceof TargetDeviceItem)){
            stack = ClientUtils.getPlayer().getItemInHand(InteractionHand.OFF_HAND);
            hand = InteractionHand.OFF_HAND;
        }
        this.hasTargetDevice = stack.getItem() instanceof TargetDeviceItem;
        this.hand = hand;
    }

    public PortalTargetScreen(BlockPos pos, int scrollOffset, int selectedPortalTarget, int selectedDeviceTarget){
        this(pos);
        if(this.validateObjectOrClose())
            this.scrollOffset = Math.min(scrollOffset, Math.max(0, this.object.getTotalTargetCapacity() - 10));
        this.selectedPortalTarget = selectedPortalTarget;
        this.selectedDeviceTarget = selectedDeviceTarget;
    }

    @Override
    protected Component getNarrationMessage(PortalGroup object){
        return TextComponents.translation("wormhole.portal.targets.gui.title").get();
    }

    @Override
    protected int width(PortalGroup object){
        return this.hasTargetDevice ? WIDTH_WITH_DEVICE : WIDTH;
    }

    @Override
    protected int height(PortalGroup object){
        return this.hasTargetDevice ? HEIGHT_WITH_DEVICE : HEIGHT;
    }

    @Override
    protected void addWidgets(PortalGroup group){
        this.addPortalTargetWidgets(group);
        if(this.hasTargetDevice)
            this.addDeviceTargetWidgets();

        this.selectButton = this.addWidget(new WormholeColoredButton(160, 146, 62, 11, TextComponents.translation("wormhole.portal.targets.gui.select").get(), () ->
            Wormhole.CHANNEL.sendToServer(new PortalSelectTargetPacket(this.object, this.selectedPortalTarget))
        ));
        this.removeButton = this.addWidget(new WormholeColoredButton(160, 160, 62, 11, TextComponents.translation("wormhole.portal.targets.gui.remove").get(), () -> {
            if(this.selectedPortalTarget >= 0)
                Wormhole.CHANNEL.sendToServer(new PortalClearTargetPacket(this.object, this.selectedPortalTarget));
            else if(this.selectedDeviceTarget >= 0)
                Wormhole.CHANNEL.sendToServer(new PortalAddTargetPacket(this.object, this.hand, this.selectedDeviceTarget));
        }));
        Supplier<DyeColor> color = () -> {
            PortalTarget target = this.object.getTarget(this.selectedPortalTarget);
            return target == null ? null : target.color;
        };
        this.colorButton = this.addWidget(new PortalTargetEditColorButton(this, 150, 91, () -> this.selectedPortalTarget, color, () -> WormholeClient.openPortalTargetScreen(this.pos, this.scrollOffset, this.selectedPortalTarget, this.selectedDeviceTarget)));

        // back button
        this.addWidget(new ButtonWidget(-35, 5, 30, 15, TextComponents.translation("wormhole.portal.targets.gui.return").get(), () -> WormholeClient.openPortalOverviewScreen(this.pos)));
    }

    @Override
    protected void renderBackground(PoseStack poseStack, int mouseX, int mouseY, PortalGroup object){
        ScreenUtils.bindTexture(this.hasTargetDevice ? BACKGROUND_WITH_DEVICE : BACKGROUND);
        ScreenUtils.drawTexture(poseStack, 0, 0, this.width(), this.height());

        // draw target select highlight
        if(this.selectedPortalTarget >= this.scrollOffset && this.selectedPortalTarget < this.scrollOffset + 10){
            ScreenUtils.bindTexture(SELECT_HIGHLIGHT);
            ScreenUtils.drawTexture(poseStack, 5, 16 + 16 * (this.selectedPortalTarget - this.scrollOffset), 130, 16);
        }else if(this.hasTargetDevice && this.selectedDeviceTarget >= 0 && this.selectedDeviceTarget < 10){
            ScreenUtils.bindTexture(SELECT_HIGHLIGHT_DEVICE);
            ScreenUtils.drawTexture(poseStack, 242, 16 + 16 * this.selectedDeviceTarget, 106, 16);
        }

        super.renderBackground(poseStack, mouseX, mouseY, object);
    }

    @Override
    protected void render(PoseStack poseStack, int mouseX, int mouseY, PortalGroup group){
        super.render(poseStack, mouseX, mouseY, group);

        // draw titles
        ScreenUtils.drawCenteredString(poseStack, TextComponents.translation("wormhole.portal.targets.gui.title").get(), 70, 3, Integer.MAX_VALUE);
        if(this.hasTargetDevice)
            ScreenUtils.drawCenteredString(poseStack, TextComponents.translation("wormhole.target_device.gui.title").get(), 296, 3, Integer.MAX_VALUE);

        // draw hover highlight
        if(mouseX > 5 && mouseX < 135 && mouseY > 16 && mouseY < 176){
            int targetIndex = (mouseY - 16) / 16;
            if(group.getTarget(targetIndex + this.scrollOffset) != null){
                ScreenUtils.bindTexture(HOVER_HIGHLIGHT);
                ScreenUtils.drawTexture(poseStack, 5, 16 + targetIndex * 16, 130, 16);
            }
        }else if(this.hasTargetDevice && mouseX > 242 && mouseX < 348 && mouseY > 16 && mouseY < 176){
            int targetIndex = (mouseY - 16) / 16;
            if(this.getFromDeviceTargets(list -> list.size() > targetIndex && list.get(targetIndex) != null, false)){
                ScreenUtils.bindTexture(HOVER_HIGHLIGHT_DEVICE);
                ScreenUtils.drawTexture(poseStack, 242, 16 + targetIndex * 16, 106, 16);
            }
        }

        int activeTarget = group.getActiveTargetIndex();
        // draw target numbers
        for(int count = 0; count < Math.min(10, this.portalTargetNameLabels.size()); count++){
            if(count + this.scrollOffset != activeTarget)
                ScreenUtils.drawCenteredString(poseStack, this.scrollOffset + count + 1 + ".", 14, 21 + count * 16);
            else{
                ScreenUtils.bindTexture(STAR_ICON);
                ScreenUtils.drawTexture(poseStack, 8, 19 + 16 * count, 10, 10);
            }
        }

        // draw target info
        if(this.selectedPortalTarget >= 0 && this.selectedPortalTarget < group.getTotalTargetCapacity()){
            PortalTarget target = group.getTarget(this.selectedPortalTarget);
            if(target != null)
                this.renderTargetInfo(poseStack, group, target, true);
        }else if(this.hasTargetDevice && this.selectedDeviceTarget >= 0 && this.selectedDeviceTarget < 10){
            PortalTarget target = this.getFromDeviceTargets(list -> this.selectedDeviceTarget < list.size() ? list.get(this.selectedDeviceTarget) : null, null);
            if(target != null)
                this.renderTargetInfo(poseStack, group, target, false);
        }

        this.updateSelectRemoveColorButtons(group);
    }

    private void renderTargetInfo(PoseStack poseStack, PortalGroup group, PortalTarget target, boolean showColor){
        ScreenUtils.drawCenteredString(poseStack, target.name, 191, 31, Integer.MAX_VALUE);

        ScreenUtils.bindTexture(SEPARATOR);
        ScreenUtils.drawTexture(poseStack, 153, 41, 77, 1);

        // location
        ScreenUtils.bindTexture(LOCATION_ICON);
        ScreenUtils.drawTexture(poseStack, 150, 47, 9, 9);
        ScreenUtils.drawString(poseStack, "(" + target.x + ", " + target.y + ", " + target.z + ")", 161, 48, Integer.MAX_VALUE);
        // dimension
        Block block = null;
        if(target.dimension.equals(Level.OVERWORLD))
            block = Blocks.DIRT_PATH;
        else if(target.dimension.equals(Level.NETHER))
            block = Blocks.NETHERRACK;
        else if(target.dimension.equals(Level.END))
            block = Blocks.END_STONE;
        if(block == null){
            ScreenUtils.bindTexture(DIMENSION_ICON);
            ScreenUtils.drawTexture(poseStack, 150, 59, 9, 9);
        }else{
            ScreenBlockRenderer.drawBlock(poseStack, block, 154.5, 63.5, 5.5, 45, 40);
        }
        ScreenUtils.drawString(poseStack, target.getDimensionDisplayName(), 161, 60, Integer.MAX_VALUE);
        // direction
        ScreenUtils.bindTexture(DIRECTION_ICON);
        ScreenUtils.drawTexture(poseStack, 148, 69, 13, 13);
        ScreenUtils.drawString(poseStack, TextComponents.translation("wormhole.direction." + Direction.fromYRot(target.yaw)).get(), 161, 72, Integer.MAX_VALUE);

        ScreenUtils.bindTexture(SEPARATOR);
        ScreenUtils.drawTexture(poseStack, 153, 85, 77, 1);

        if(showColor){
            // color
            ScreenUtils.drawString(poseStack, TextComponents.translation("wormhole.color." + (target.color == null ? "random" : target.color.getName())).get(), 161, 92, Integer.MAX_VALUE);

            ScreenUtils.bindTexture(SEPARATOR);
            ScreenUtils.drawTexture(poseStack, 153, 105, 77, 1);
        }

        // energy cost
        ScreenUtils.bindTexture(ENERGY_ICON);
        ScreenUtils.drawTexture(poseStack, 150, showColor ? 111 : 91, 9, 9);
        int cost = PortalGroup.getTeleportCostToTarget(ClientUtils.getWorld(), group.getCenterPos(), target);
        ScreenUtils.drawString(poseStack, EnergyFormat.formatEnergyWithUnit(cost), 161, showColor ? 112 : 92, Integer.MAX_VALUE);
    }

    private void addPortalTargetWidgets(PortalGroup group){
        int portalCapacity = group.getTotalTargetCapacity();

        for(int count = 0; count < Math.min(10, portalCapacity); count++){
            final int index = count;
            int y = 18 + index * 16;
            this.portalTargetNameLabels.add(this.addWidget(new LabelWidget(20, y, 102, 12, () -> {
                PortalTarget target = this.object.getTarget(index + this.scrollOffset);
                return (target == null ? TextComponents.empty() : TextComponents.string(target.name)).get();
            })));
            // up arrow
            ArrowButton upArrowButton = new ArrowButton(123, y + 1, true, () -> {
                if(this.selectedPortalTarget == this.scrollOffset + index)
                    this.selectedPortalTarget -= 1;
                else if(this.selectedPortalTarget == this.scrollOffset + index - 1)
                    this.selectedPortalTarget += 1;
                Wormhole.CHANNEL.sendToServer(new PortalMoveTargetPacket(this.object, this.scrollOffset + index, true));
            });
            this.portalUpArrows.add(this.addWidget(upArrowButton));
            // down arrow
            ArrowButton downArrowButton = new ArrowButton(123, y + 6, false, () -> {
                if(this.selectedPortalTarget == this.scrollOffset + index)
                    this.selectedPortalTarget += 1;
                else if(this.selectedPortalTarget == this.scrollOffset + index + 1)
                    this.selectedPortalTarget -= 1;
                Wormhole.CHANNEL.sendToServer(new PortalMoveTargetPacket(this.object, this.scrollOffset + index, false));
            });
            this.portalDownArrows.add(this.addWidget(downArrowButton));
        }

        this.updateArrowButtons(group);
    }

    private void addDeviceTargetWidgets(){
        ItemStack stack = ClientUtils.getPlayer().getItemInHand(this.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        int deviceCapacity = TargetDeviceItem.getMaxTargetCount(stack);

        for(int count = 0; count < Math.min(10, deviceCapacity); count++){
            final int index = count;
            int y = 18 + index * 16;
            this.deviceTargetNameLabels.add(this.addWidget(new LabelWidget(244, y, 102, 12, () -> {
                PortalTarget target = getFromDeviceTargets(list -> list.size() > index ? list.get(index) : null, null);
                return (target == null ? TextComponents.empty() : TextComponents.string(target.name)).get();
            })));
        }
    }

    @Override
    public void update(PortalGroup group){
        super.update(group);

        this.updateArrowButtons(group);
    }

    private void updateArrowButtons(PortalGroup group){
        int capacity = group.getTotalTargetCapacity();
        for(int i = 0; i < this.portalUpArrows.size() && i < this.portalDownArrows.size(); i++){
            final int index = i + this.scrollOffset;
            boolean hasTarget = group.getTarget(index) != null;
            this.portalUpArrows.get(i).active = index > 0 && hasTarget;
            this.portalDownArrows.get(i).active = index < capacity - 1 && hasTarget;
        }
    }

    private void updateSelectRemoveColorButtons(PortalGroup group){
        if(this.selectedPortalTarget >= 0){
            boolean notEmpty = group.getTarget(this.selectedPortalTarget) != null;
            this.selectButton.setVisible();
            this.selectButton.setColorGreen();
            this.selectButton.setText(TextComponents.translation("wormhole.portal.targets.gui.select").get());
            this.selectButton.setActive(notEmpty && group.getActiveTargetIndex() != this.selectedPortalTarget);
            this.removeButton.setVisible();
            this.removeButton.setColorRed();
            this.removeButton.setText(TextComponents.translation("wormhole.portal.targets.gui.remove").get());
            this.removeButton.setActive(notEmpty);
            this.colorButton.visible = notEmpty;
        }else if(this.selectedDeviceTarget >= 0){
            boolean notEmpty = this.getFromDeviceTargets(targets -> targets.size() > this.selectedDeviceTarget && targets.get(this.selectedDeviceTarget) != null, false);
            this.selectButton.setInvisible();
            this.removeButton.setVisible();
            this.removeButton.setColorWhite();
            this.removeButton.setText(TextComponents.translation("wormhole.portal.targets.gui.add").get());
            this.removeButton.setActive(notEmpty);
            this.colorButton.visible = false;
        }else{
            this.selectButton.setInvisible();
            this.removeButton.setInvisible();
            this.colorButton.visible = false;
        }
    }

    @Override
    protected void renderTooltips(PoseStack poseStack, int mouseX, int mouseY, PortalGroup group){
        // location
        if(mouseX >= 149 && mouseX <= 160 && mouseY >= 46 && mouseY <= 57)
            ScreenUtils.drawTooltip(poseStack, TextComponents.translation("wormhole.target.location").get(), mouseX, mouseY);
            // dimension
        else if(mouseX >= 149 && mouseX <= 160 && mouseY >= 58 && mouseY <= 69)
            ScreenUtils.drawTooltip(poseStack, TextComponents.translation("wormhole.target.dimension").get(), mouseX, mouseY);
            // direction
        else if(mouseX >= 149 && mouseX <= 160 && mouseY >= 70 && mouseY <= 81)
            ScreenUtils.drawTooltip(poseStack, TextComponents.translation("wormhole.target.direction").get(), mouseX, mouseY);
            // energy
        else if(mouseX >= 149 && mouseX <= 160 && (this.selectedPortalTarget >= 0 ? mouseY >= 110 && mouseY <= 121 : mouseY >= 90 && mouseY <= 101))
            ScreenUtils.drawTooltip(poseStack, TextComponents.translation("wormhole.target.teleport_cost").get(), mouseX, mouseY);

        super.renderTooltips(poseStack, mouseX, mouseY, group);
    }

    public <T> T getFromDeviceTargets(Function<List<PortalTarget>,T> function, T other){
        ItemStack stack = ClientUtils.getPlayer().getItemInHand(this.hand);
        if(!stack.isEmpty() && stack.getItem() instanceof TargetDeviceItem)
            return function.apply(TargetDeviceItem.getTargets(stack));
        return other;
    }

    private void scroll(int amount){
        this.scrollOffset = Math.min(Math.max(0, this.scrollOffset + amount), Math.max(0, this.object.getTotalTargetCapacity() - 10));
    }

    @Override
    protected boolean mouseScrolled(int mouseX, int mouseY, double scrollAmount, boolean hasBeenHandled, PortalGroup object){
        if(!hasBeenHandled && mouseX >= 5 && mouseX <= 135 && mouseY >= 16 && mouseY <= 183){
            this.scroll(-(int)scrollAmount);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollAmount, hasBeenHandled, object);
    }

    @Override
    protected boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled, PortalGroup group){
        hasBeenHandled |= super.mousePressed(mouseX, mouseY, button, hasBeenHandled, group);

        if(button == 0){
            if(mouseX > 5 && mouseX < 135 && mouseY > 16 && mouseY < 176){
                int targetIndex = (mouseY - 16) / 16 + this.scrollOffset;
                if(group.getTarget(targetIndex) != null){
                    AbstractButtonWidget.playClickSound();
                    this.selectedPortalTarget = targetIndex;
                    this.selectedDeviceTarget = -1;
                }
                return true;
            }else if(this.hasTargetDevice && mouseX > 242 && mouseX < 348 && mouseY > 16 && mouseY < 176){
                int targetIndex = (mouseY - 16) / 16;
                if(this.getFromDeviceTargets(list -> list.size() > targetIndex && list.get(targetIndex) != null, false)){
                    AbstractButtonWidget.playClickSound();
                    this.selectedPortalTarget = -1;
                    this.selectedDeviceTarget = targetIndex;
                }
                return true;
            }
        }

        return hasBeenHandled;
    }
}
