package com.supermartijn642.wormhole.targetdevice;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.screen.ArrowButton;
import com.supermartijn642.wormhole.screen.WormholeButton;
import com.supermartijn642.wormhole.screen.WormholeLabel;
import com.supermartijn642.wormhole.screen.WormholeScreen;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceAddPacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceMovePacket;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceRemovePacket;
import com.supermartijn642.wormhole.targetdevice.screen.CurrentTargetLabel;
import com.supermartijn642.wormhole.targetdevice.screen.TargetLabel;
import com.supermartijn642.wormhole.targetdevice.screen.TargetNameField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Created 10/8/2020 by SuperMartijn642
 */
public class TargetDeviceScreen extends WormholeScreen {

    private static final int BASE_HEIGHT = 20, HEIGHT_PER_TARGET = 15;
    private static final int WIDTH = 300;

    private final PlayerEntity player;
    public final Hand hand;
    private final BlockPos targetPos;
    private final float targetYaw;

    private final List<TargetNameField> textFields = new LinkedList<>();
    private final List<ArrowButton> upArrows = new LinkedList<>();
    private final List<ArrowButton> downArrows = new LinkedList<>();
    private final List<TargetLabel> coordLabels = new LinkedList<>();
    private final List<TargetLabel> facingLabels = new LinkedList<>();
    private final List<WormholeButton> removeButtons = new LinkedList<>();
    private TextFieldWidget currentTextField;
    private WormholeLabel currentCoordLabel, currentFacingLabel;
    private WormholeButton saveButton;

    public TargetDeviceScreen(PlayerEntity player, Hand hand, BlockPos pos, float yaw){
        super("wormhole.target_device.gui.title");
        this.player = player;
        this.hand = hand;
        this.targetPos = pos;
        this.targetYaw = yaw;
    }

    private void ensureTargetWidgetCount(int count){
        int now = this.textFields.size();
        while(this.textFields.size() < count)
            this.addTargetWidgets();
        while(this.textFields.size() > count)
            this.removeTargetWidgets();
        if(now != this.textFields.size())
            this.updateCurrentWidgets();
    }

    private void addTargetWidgets(){
        int y = BASE_HEIGHT + HEIGHT_PER_TARGET * this.textFields.size();
        final int index = this.textFields.size();
        // name field
        this.textFields.add(this.addWidget(new TargetNameField(this::getOrDefault, this.hand, index, 8, y)));
        // up arrow
        ArrowButton upArrowButton = new ArrowButton(69, y, true, () ->
            Wormhole.CHANNEL.sendToServer(new TargetDeviceMovePacket(this.hand, index, true))
        );
        upArrowButton.active = index > 0;
        this.upArrows.add(this.addWidget(upArrowButton));
        // down arrow
        ArrowButton downArrowButton = new ArrowButton(69, y + 5, false, () ->
            Wormhole.CHANNEL.sendToServer(new TargetDeviceMovePacket(this.hand, index, false))
        );
        downArrowButton.active = index < this.textFields.size() - 1;
        if(index > 0)
            downArrows.get(index - 1).active = true;
        this.downArrows.add(this.addWidget(downArrowButton));
        // labels
        this.coordLabels.add(this.addWidget(new TargetLabel(this::getOrDefault, index, 84, y, 100, 10, "wormhole.target_device.gui.coords", target -> "(" + target.x + "," + target.y + "," + target.z + ")", false)));
        this.facingLabels.add(this.addWidget(new TargetLabel(this::getOrDefault, index, 187, y, 50, 10, "wormhole.target_device.gui.facing", target -> "wormhole.direction." + Direction.fromAngle(target.yaw).toString(), true)));
        // remove button
        this.removeButtons.add(this.addWidget(new WormholeButton(250, y, 40, 10, "Remove", () ->
            Wormhole.CHANNEL.sendToServer(new TargetDeviceRemovePacket(this.hand, index)))
        ));
    }

    private void removeTargetWidgets(){
        this.removeWidget(this.textFields.get(this.textFields.size() - 1));
        this.textFields.remove(this.textFields.size() - 1);
        this.removeWidget(this.upArrows.get(this.upArrows.size() - 1));
        this.upArrows.remove(this.upArrows.size() - 1);
        this.removeWidget(this.downArrows.get(this.downArrows.size() - 1));
        this.downArrows.remove(this.downArrows.size() - 1);
        this.removeWidget(this.coordLabels.get(this.coordLabels.size() - 1));
        this.coordLabels.remove(this.coordLabels.size() - 1);
        this.removeWidget(this.facingLabels.get(this.facingLabels.size() - 1));
        this.facingLabels.remove(this.facingLabels.size() - 1);
        this.removeWidget(this.removeButtons.get(this.removeButtons.size() - 1));
        this.removeButtons.remove(this.removeButtons.size() - 1);
    }

    private void updateCurrentWidgets(){
        int y = 2 * BASE_HEIGHT + Math.max(this.textFields.size(), 1) * HEIGHT_PER_TARGET + 1;
        this.currentTextField.y = y;
        this.currentCoordLabel.y = y;
        this.currentFacingLabel.y = y;
        this.saveButton.y = y;
    }

    @Override
    protected float sizeX(){
        return WIDTH;
    }

    @Override
    protected float sizeY(){
        return this.getOrDefault(list -> 2 * BASE_HEIGHT + (Math.max(list.size(), 1) + 1) * HEIGHT_PER_TARGET + 1, 0);
    }

    @Override
    protected void addWidgets(){
        // remove current widgets
        while(this.textFields.size() > 0)
            this.removeTargetWidgets();

        // current location
        this.currentTextField = this.addWidget(new TextFieldWidget(this.font, 8, 0, 59, 10, I18n.format("wormhole.target_device.gui.target_name")));
        this.currentCoordLabel = this.addWidget(new CurrentTargetLabel(84, 0, 100, 10, "wormhole.gui.label", "wormhole.target_device.gui.coords", "(" + this.targetPos.getX() + "," + this.targetPos.getY() + "," + this.targetPos.getZ() + ")", false));
        this.currentFacingLabel = this.addWidget(new CurrentTargetLabel(187, 0, 50, 10, "wormhole.gui.label", "wormhole.target_device.gui.facing", "wormhole.direction." + Direction.fromAngle(this.targetYaw), true));
        this.saveButton = this.addWidget(new WormholeButton(250, 0, 40, 10, "Save", () -> {
            if(!this.currentTextField.getText().trim().isEmpty())
                Wormhole.CHANNEL.sendToServer(new TargetDeviceAddPacket(this.hand, this.currentTextField.getText().trim(), this.targetPos, this.targetYaw));
        }));

        int targets = this.getOrDefault(List::size, -1);
        if(targets != -1){
            this.ensureTargetWidgetCount(targets);
            this.updateCurrentWidgets();
        }
    }

    @Override
    protected void render(int mouseX, int mouseY){
        List<PortalTarget> targets = this.getOrDefault(list -> list, null);
        if(targets == null)
            return;

        this.ensureTargetWidgetCount(targets.size());

        this.saveButton.active = !this.currentTextField.getText().trim().isEmpty() && targets.size() < this.getFromStack(TargetDeviceItem::getMaxTargetCount, 0);

        int height = BASE_HEIGHT + Math.max(this.getOrDefault(List::size, 0), 1) * HEIGHT_PER_TARGET;
        this.drawTop(targets, this.sizeX(), height);
        GlStateManager.translated(0, height + 1, 0);
        this.drawBottom(this.sizeX(), BASE_HEIGHT + HEIGHT_PER_TARGET);
    }

    private void drawTop(List<PortalTarget> targets, float width, float height){
        this.drawBackground(0, 0, width, height);
        this.font.drawString(this.title.getFormattedText(), 8, 7, 4210752);
    }

    private void drawBottom(float width, float height){
        this.drawBackground(0, 0, width, height);
        this.font.drawString(new TranslationTextComponent("wormhole.target_device.gui.current_location").getFormattedText(), 8, 7, 4210752);
    }

    @Override
    protected void renderTooltips(int mouseX, int mouseY){

    }

    public <T> T getOrDefault(Function<List<PortalTarget>,T> function, T other){
        ItemStack stack = this.player.getHeldItem(this.hand);
        if(!stack.isEmpty() && stack.getItem() instanceof TargetDeviceItem)
            return function.apply(TargetDeviceItem.getTargets(stack));
        Minecraft.getInstance().player.closeScreen();
        return other;
    }

    public <T> T getFromStack(Function<ItemStack,T> function, T other){
        ItemStack stack = this.player.getHeldItem(this.hand);
        if(!stack.isEmpty() && stack.getItem() instanceof TargetDeviceItem)
            return function.apply(stack);
        Minecraft.getInstance().player.closeScreen();
        return other;
    }

    @Override
    public void tick(){
        super.tick();
        if(this.currentTextField != null)
            this.currentTextField.tick();
    }
}
