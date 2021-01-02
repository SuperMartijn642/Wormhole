package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalAddTargetPacket;
import com.supermartijn642.wormhole.portal.packets.PortalClearTargetPacket;
import com.supermartijn642.wormhole.portal.packets.PortalMoveTargetPacket;
import com.supermartijn642.wormhole.portal.packets.PortalSelectTargetPacket;
import com.supermartijn642.wormhole.screen.ArrowButton;
import com.supermartijn642.wormhole.screen.WormholeButton;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceMovePacket;
import com.supermartijn642.wormhole.targetdevice.screen.TargetLabel;
import com.supermartijn642.wormhole.targetdevice.screen.TargetNameField;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Created 11/3/2020 by SuperMartijn642
 */
public class PortalTargetScreen extends PortalGroupScreen {

    private static final int MAX_HEIGHT = 210;
    private static final int BASE_HEIGHT = 20, HEIGHT_PER_TARGET = 15;
    private static final int PORTAL_WIDTH = 359, TARGET_DEVICE_WIDTH = 300;

    private final PlayerEntity player;
    private final boolean hasTargetDevice;
    public final Hand hand;
    private int maxPortalTargets;

    private int scrollOffset = 0;
    private ArrowButton scrollUpArrow, scrollDownArrow;
    private final List<PortalTargetNameField> portalTextFields = new LinkedList<>();
    private final List<ArrowButton> portalUpArrows = new LinkedList<>();
    private final List<ArrowButton> portalDownArrows = new LinkedList<>();
    private final List<PortalTargetLabel> portalCoordLabels = new LinkedList<>();
    private final List<PortalTargetLabel> portalFacingLabels = new LinkedList<>();
    private final List<PortalTargetEditColorButton> portalColorButtons = new LinkedList<>();
    private final List<WormholeButton> portalSelectButtons = new LinkedList<>();
    private final List<WormholeButton> portalClearButtons = new LinkedList<>();
    private final List<TargetNameField> deviceTextFields = new LinkedList<>();
    private final List<ArrowButton> deviceUpArrows = new LinkedList<>();
    private final List<ArrowButton> deviceDownArrows = new LinkedList<>();
    private final List<TargetLabel> deviceCoordLabels = new LinkedList<>();
    private final List<TargetLabel> deviceFacingLabels = new LinkedList<>();
    private final List<WormholeButton> deviceAddButtons = new LinkedList<>();

    public PortalTargetScreen(BlockPos pos, PlayerEntity player){
        super("wormhole.portal.targets.gui.title", pos);
        this.player = player;

        // check for a target device
        Hand hand = Hand.MAIN_HAND;
        ItemStack stack = player.getHeldItem(Hand.MAIN_HAND);
        if(!(stack.getItem() instanceof TargetDeviceItem)){
            stack = player.getHeldItem(Hand.OFF_HAND);
            hand = Hand.OFF_HAND;
        }
        this.hasTargetDevice = stack.getItem() instanceof TargetDeviceItem;
        this.hand = hand;

        this.maxPortalTargets = (MAX_HEIGHT - BASE_HEIGHT - (this.hasTargetDevice ? Math.max(this.getFromDeviceTargets(List::size, 0), 1) * HEIGHT_PER_TARGET : 0)) / HEIGHT_PER_TARGET;
    }

    public PortalTargetScreen(BlockPos pos, int scrollOffset, PlayerEntity player){
        this(pos, player);
        this.scrollOffset = Math.min(scrollOffset, Math.max(0, this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0) - this.maxPortalTargets));
    }

    @Override
    protected float sizeX(){
        return PORTAL_WIDTH;
    }

    private float getTopSizeY(){
        return BASE_HEIGHT + Math.max(Math.min(this.maxPortalTargets, this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0)), 1) * HEIGHT_PER_TARGET;
    }

    private float getBottomSizeY(){
        return this.hasTargetDevice ? BASE_HEIGHT + Math.max(this.getFromDeviceTargets(List::size, 0), 1) * HEIGHT_PER_TARGET : 0;
    }

    @Override
    protected float sizeY(){
        return this.getTopSizeY() + this.getBottomSizeY() + 1;
    }

    @Override
    protected void addWidgets(){
        // remove current widgets
        while(this.portalTextFields.size() > 0)
            this.removePortalTargetWidgets();
        while(this.deviceTextFields.size() > 0)
            this.removeDeviceTargetWidgets();

        this.scrollOffset = Math.min(this.scrollOffset, Math.max(0, this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0) - this.maxPortalTargets));

        // back button
        this.addWidget(new WormholeButton(-35, 5, 30, 15, "wormhole.portal.targets.gui.return", () -> ClientProxy.openPortalOverviewScreen(this.pos)));

        this.ensurePortalTargetWidgetCount(Math.min(this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0), this.maxPortalTargets));
        this.ensureDeviceTargetWidgetCount(this.hasTargetDevice ? this.getFromDeviceTargets(List::size, 0) : 0);
    }

    @Override
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY){
        this.scrollOffset = Math.min(this.scrollOffset, Math.max(0, this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0) - this.maxPortalTargets));

        this.ensurePortalTargetWidgetCount(Math.min(this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0), this.maxPortalTargets));
        this.ensureDeviceTargetWidgetCount(this.hasTargetDevice ? this.getFromDeviceTargets(List::size, 0) : 0);

        for(int i = 0; i < this.portalTextFields.size(); i++){
            this.portalTextFields.get(i).setEnabled(this.getPortalGroup().getTarget(i + this.scrollOffset) != null);
            this.portalClearButtons.get(i).active = this.getPortalGroup().getTarget(i + this.scrollOffset) != null;
            this.portalSelectButtons.get(i).active = this.getPortalGroup().getTarget(i + this.scrollOffset) != null && this.getPortalGroup().getActiveTargetIndex() != i + this.scrollOffset;
        }
        if(this.hasTargetDevice)
            this.deviceAddButtons.forEach(button -> button.active = this.getPortalGroup().hasTargetSpaceLeft());

        this.drawTop(matrixStack);
        if(this.hasTargetDevice){
            matrixStack.translate((PORTAL_WIDTH - TARGET_DEVICE_WIDTH) / 2, this.getTopSizeY() + 1, 0);
            this.drawBottom(matrixStack);
        }
    }

    private void drawTop(MatrixStack matrixStack){
        this.drawBackground(matrixStack, 0, 0, PORTAL_WIDTH, this.getTopSizeY());
        this.font.func_238407_a_(matrixStack, this.title, 8, 7, 4210752);

        for(int i = 0; i < this.portalTextFields.size(); i++)
            this.font.drawString(matrixStack, (this.scrollOffset + i + 1) + ".", 8, this.portalTextFields.get(i).y + 2, 4210752);

        // back button
        this.drawBackground(matrixStack, -40, 0, 40, 25);
    }

    private void drawBottom(MatrixStack matrixStack){
        this.drawBackground(matrixStack, 0, 0, TARGET_DEVICE_WIDTH, this.getBottomSizeY());
        this.font.func_238407_a_(matrixStack, new TranslationTextComponent("wormhole.target_device.gui.current_location"), 8, 7, 4210752);
    }

    @Override
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY){

    }

    private void ensurePortalTargetWidgetCount(int count){
        int now = this.portalTextFields.size();
        while(this.portalTextFields.size() < count)
            this.addPortalTargetWidgets();
        while(this.portalTextFields.size() > count)
            this.removePortalTargetWidgets();
        if(now != this.portalTextFields.size())
            this.updateDeviceWidgetsPositions();
    }

    private void addPortalTargetWidgets(){
        int y = BASE_HEIGHT + HEIGHT_PER_TARGET * this.portalTextFields.size();
        final int index = this.portalTextFields.size();
        // name field
        this.portalTextFields.add(this.addWidget(new PortalTargetNameField(this, () -> this.scrollOffset + index, 20, y)));
        // up arrow
        ArrowButton upArrowButton = new ArrowButton(81, y, true, () ->
            Wormhole.CHANNEL.sendToServer(new PortalMoveTargetPacket(this.getPortalGroup(), this.scrollOffset + index, true))
        );
        upArrowButton.active = index > 0;
        this.portalUpArrows.add(this.addWidget(upArrowButton));
        // down arrow
        ArrowButton downArrowButton = new ArrowButton(81, y + 5, false, () ->
            Wormhole.CHANNEL.sendToServer(new PortalMoveTargetPacket(this.getPortalGroup(), this.scrollOffset + index, false))
        );
        downArrowButton.active = index < this.portalTextFields.size() - 1;
        if(index > 0)
            this.portalDownArrows.get(index - 1).active = true;
        this.portalDownArrows.add(this.addWidget(downArrowButton));
        // labels
        this.portalCoordLabels.add(this.addWidget(new PortalTargetLabel(this, () -> this.scrollOffset + index, 96, y, 100, 10, "wormhole.target_device.gui.coords", target -> "(" + target.x + "," + target.y + "," + target.z + ")", false)));
        this.portalFacingLabels.add(this.addWidget(new PortalTargetLabel(this, () -> this.scrollOffset + index, 199, y, 50, 10, "wormhole.target_device.gui.facing", target -> "wormhole.direction." + Direction.fromAngle(target.yaw).toString(), true)));
        // color button
        this.portalColorButtons.add(this.addWidget(new PortalTargetEditColorButton(this, this.pos, 253, y, () -> this.scrollOffset + index,
            () -> this.getFromPortalGroup(group -> {
                PortalTarget target = group.getTarget(this.scrollOffset + index);
                return target == null ? null : target.color;
            }, null),
            () -> ClientProxy.openPortalTargetScreen(this.pos, this.scrollOffset))));
        // select button
        this.portalSelectButtons.add(this.addWidget(new WormholeButton(267, y, 40, 10, "Select", () ->
            Wormhole.CHANNEL.sendToServer(new PortalSelectTargetPacket(this.getPortalGroup(), this.scrollOffset + index)))
        ));// remove button
        this.portalClearButtons.add(this.addWidget(new WormholeButton(309, y, 40, 10, "Clear", () ->
            Wormhole.CHANNEL.sendToServer(new PortalClearTargetPacket(this.getPortalGroup(), this.scrollOffset + index)))
        ));
    }

    private void removePortalTargetWidgets(){
        this.removeWidget(this.portalTextFields.get(this.portalTextFields.size() - 1));
        this.portalTextFields.remove(this.portalTextFields.size() - 1);
        this.removeWidget(this.portalUpArrows.get(this.portalUpArrows.size() - 1));
        this.portalUpArrows.remove(this.portalUpArrows.size() - 1);
        this.removeWidget(this.portalDownArrows.get(this.portalDownArrows.size() - 1));
        this.portalDownArrows.remove(this.portalDownArrows.size() - 1);
        this.removeWidget(this.portalCoordLabels.get(this.portalCoordLabels.size() - 1));
        this.portalCoordLabels.remove(this.portalCoordLabels.size() - 1);
        this.removeWidget(this.portalFacingLabels.get(this.portalFacingLabels.size() - 1));
        this.portalFacingLabels.remove(this.portalFacingLabels.size() - 1);
        this.removeWidget(this.portalSelectButtons.get(this.portalSelectButtons.size() - 1));
        this.portalSelectButtons.remove(this.portalSelectButtons.size() - 1);
        this.removeWidget(this.portalClearButtons.get(this.portalClearButtons.size() - 1));
        this.portalClearButtons.remove(this.portalClearButtons.size() - 1);
    }

    private void ensureDeviceTargetWidgetCount(int count){
        int now = this.deviceTextFields.size();
        while(this.deviceTextFields.size() < count)
            this.addDeviceTargetWidgets();
        while(this.deviceTextFields.size() > count)
            this.removeDeviceTargetWidgets();
        if(now != this.deviceTextFields.size())
            this.maxPortalTargets = (MAX_HEIGHT - BASE_HEIGHT - (this.hasTargetDevice ? Math.max(this.getFromDeviceTargets(List::size, 0), 1) * HEIGHT_PER_TARGET : 0)) / HEIGHT_PER_TARGET;
    }

    private void addDeviceTargetWidgets(){
        int x = (PORTAL_WIDTH - TARGET_DEVICE_WIDTH) / 2;
        int y = BASE_HEIGHT * 2 + HEIGHT_PER_TARGET * (this.portalTextFields.size() + this.deviceTextFields.size());
        final int index = this.deviceTextFields.size();
        // name field
        this.deviceTextFields.add(this.addWidget(new TargetNameField(this::getFromDeviceTargets, this.hand, index, x + 8, y)));
        // up arrow
        ArrowButton upArrowButton = new ArrowButton(x + 69, y, true, () ->
            Wormhole.CHANNEL.sendToServer(new TargetDeviceMovePacket(this.hand, index, true))
        );
        upArrowButton.active = index > 0;
        this.deviceUpArrows.add(this.addWidget(upArrowButton));
        // down arrow
        ArrowButton downArrowButton = new ArrowButton(x + 69, y + 5, false, () ->
            Wormhole.CHANNEL.sendToServer(new TargetDeviceMovePacket(this.hand, index, false))
        );
        downArrowButton.active = index < this.deviceTextFields.size() - 1;
        if(index > 0)
            deviceDownArrows.get(index - 1).active = true;
        this.deviceDownArrows.add(this.addWidget(downArrowButton));
        // labels
        this.deviceCoordLabels.add(this.addWidget(new TargetLabel(this::getFromDeviceTargets, index, x + 84, y, 100, 10, "wormhole.target_device.gui.coords", target -> "(" + target.x + "," + target.y + "," + target.z + ")", false)));
        this.deviceFacingLabels.add(this.addWidget(new TargetLabel(this::getFromDeviceTargets, index, x + 187, y, 50, 10, "wormhole.target_device.gui.facing", target -> "wormhole.direction." + Direction.fromAngle(target.yaw).toString(), true)));
        // remove button
        this.deviceAddButtons.add(this.addWidget(new WormholeButton(x + 250, y, 40, 10, "Add", () ->
            Wormhole.CHANNEL.sendToServer(new PortalAddTargetPacket(this.getPortalGroup(), this.hand, index))
        )));
    }

    private void removeDeviceTargetWidgets(){
        this.removeWidget(this.deviceTextFields.get(this.deviceTextFields.size() - 1));
        this.deviceTextFields.remove(this.deviceTextFields.size() - 1);
        this.removeWidget(this.deviceUpArrows.get(this.deviceUpArrows.size() - 1));
        this.deviceUpArrows.remove(this.deviceUpArrows.size() - 1);
        this.removeWidget(this.deviceDownArrows.get(this.deviceDownArrows.size() - 1));
        this.deviceDownArrows.remove(this.deviceDownArrows.size() - 1);
        this.removeWidget(this.deviceCoordLabels.get(this.deviceCoordLabels.size() - 1));
        this.deviceCoordLabels.remove(this.deviceCoordLabels.size() - 1);
        this.removeWidget(this.deviceFacingLabels.get(this.deviceFacingLabels.size() - 1));
        this.deviceFacingLabels.remove(this.deviceFacingLabels.size() - 1);
        this.removeWidget(this.deviceAddButtons.get(this.deviceAddButtons.size() - 1));
        this.deviceAddButtons.remove(this.deviceAddButtons.size() - 1);
    }

    private void updateDeviceWidgetsPositions(){
        for(int index = 0; index < this.deviceTextFields.size(); index++){
            int y = BASE_HEIGHT * 2 + HEIGHT_PER_TARGET * (this.portalTextFields.size() + index);
            this.deviceTextFields.get(index).y = y;
            this.deviceUpArrows.get(index).y = y;
            this.deviceDownArrows.get(index).y = y + 5;
            this.deviceCoordLabels.get(index).y = y;
            this.deviceFacingLabels.get(index).y = y;
            this.deviceAddButtons.get(index).y = y;
        }
    }

    public <T> T getFromDeviceTargets(Function<List<PortalTarget>,T> function, T other){
        ItemStack stack = this.player.getHeldItem(this.hand);
        if(!stack.isEmpty() && stack.getItem() instanceof TargetDeviceItem)
            return function.apply(TargetDeviceItem.getTargets(stack));
        this.closeScreen();
        return other;
    }

    private void scroll(int amount){
        this.scrollOffset = Math.min(Math.max(0, this.scrollOffset + amount), Math.max(0, this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0) - this.maxPortalTargets));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll){
        if(super.mouseScrolled(mouseX, mouseY, scroll))
            return true;

        if(mouseX >= this.left() && mouseX <= this.left() + this.sizeX() && mouseY >= this.top() && mouseY <= this.sizeY()){
            this.scroll(-(int)scroll);
            return true;
        }

        return false;
    }
}
