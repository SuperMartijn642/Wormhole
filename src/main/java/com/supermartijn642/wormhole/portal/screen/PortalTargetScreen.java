package com.supermartijn642.wormhole.portal.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.wormhole.ClientProxy;
import com.supermartijn642.wormhole.EnergyFormat;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalGroup;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalAddTargetPacket;
import com.supermartijn642.wormhole.portal.packets.PortalClearTargetPacket;
import com.supermartijn642.wormhole.portal.packets.PortalMoveTargetPacket;
import com.supermartijn642.wormhole.portal.packets.PortalSelectTargetPacket;
import com.supermartijn642.wormhole.screen.ArrowButton;
import com.supermartijn642.wormhole.screen.WormholeButton;
import com.supermartijn642.wormhole.screen.WormholeColoredButton;
import com.supermartijn642.wormhole.screen.WormholeLabel;
import com.supermartijn642.wormhole.targetdevice.TargetDeviceItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

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

    private final PlayerEntity player;
    private final boolean hasTargetDevice;
    public final Hand hand;

    private int scrollOffset = 0;
    private int selectedPortalTarget;
    private int selectedDeviceTarget = -1;
    private final List<WormholeLabel> portalTargetNameLabels = new LinkedList<>();
    private final List<ArrowButton> portalUpArrows = new LinkedList<>();
    private final List<ArrowButton> portalDownArrows = new LinkedList<>();
    private final List<WormholeLabel> deviceTargetNameLabels = new LinkedList<>();
    private WormholeColoredButton selectButton, removeButton;
    private PortalTargetEditColorButton colorButton;

    public PortalTargetScreen(BlockPos pos, PlayerEntity player){
        super("wormhole.portal.targets.gui.title", pos);
        this.player = player;

        // set the selected target to the portal's active target if it's not null
        this.selectedPortalTarget = this.getFromPortalGroup(PortalGroup::getActiveTarget, null) == null ? -1 : this.getFromPortalGroup(PortalGroup::getActiveTargetIndex, -1);

        // check for a target device
        Hand hand = Hand.MAIN_HAND;
        ItemStack stack = player.getHeldItem(Hand.MAIN_HAND);
        if(!(stack.getItem() instanceof TargetDeviceItem)){
            stack = player.getHeldItem(Hand.OFF_HAND);
            hand = Hand.OFF_HAND;
        }
        this.hasTargetDevice = stack.getItem() instanceof TargetDeviceItem;
        this.hand = hand;
    }

    public PortalTargetScreen(BlockPos pos, PlayerEntity player, int scrollOffset, int selectedPortalTarget, int selectedDeviceTarget){
        this(pos, player);
        this.scrollOffset = Math.min(scrollOffset, Math.max(0, this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0) - 10));
        this.selectedPortalTarget = selectedPortalTarget;
        this.selectedDeviceTarget = selectedDeviceTarget;
    }

    @Override
    protected float sizeX(){
        return this.hasTargetDevice ? WIDTH_WITH_DEVICE : WIDTH;
    }

    @Override
    protected float sizeY(){
        return this.hasTargetDevice ? HEIGHT_WITH_DEVICE : HEIGHT;
    }

    @Override
    protected void addWidgets(){
        this.addPortalTargetWidgets();
        if(this.hasTargetDevice)
            this.addDeviceTargetWidgets();

        this.selectButton = this.addWidget(new WormholeColoredButton(160, 146, 62, 11, "wormhole.portal.targets.gui.select", () ->
            Wormhole.CHANNEL.sendToServer(new PortalSelectTargetPacket(this.getPortalGroup(), this.selectedPortalTarget))
        ));
        this.removeButton = this.addWidget(new WormholeColoredButton(160, 160, 62, 11, "wormhole.portal.targets.gui.remove", () -> {
            if(this.selectedPortalTarget >= 0)
                Wormhole.CHANNEL.sendToServer(new PortalClearTargetPacket(this.getPortalGroup(), this.selectedPortalTarget));
            else if(this.selectedDeviceTarget >= 0)
                Wormhole.CHANNEL.sendToServer(new PortalAddTargetPacket(this.getPortalGroup(), this.hand, this.selectedDeviceTarget));
        }));
        Supplier<DyeColor> color = () -> {
            PortalTarget target = this.getFromPortalGroup(group -> group.getTarget(this.selectedPortalTarget), null);
            return target == null ? null : target.color;
        };
        this.colorButton = this.addWidget(new PortalTargetEditColorButton(this, 150, 91, () -> this.selectedPortalTarget, color, () -> ClientProxy.openPortalTargetScreen(this.pos, this.scrollOffset, this.selectedPortalTarget, this.selectedDeviceTarget)));

        // back button
        this.addWidget(new WormholeButton(-35, 5, 30, 15, "wormhole.portal.targets.gui.return", () -> ClientProxy.openPortalOverviewScreen(this.pos)));
    }

    @Override
    protected void render(MatrixStack matrixStack, int mouseX, int mouseY){
        ScreenUtils.bindTexture(this.hasTargetDevice ? BACKGROUND_WITH_DEVICE : BACKGROUND);
        ScreenUtils.drawTexture(matrixStack, 0, 0, this.sizeX(), this.sizeY());

        // draw titles
        ScreenUtils.drawCenteredString(matrixStack, this.font, this.title, 70, 3, Integer.MAX_VALUE);
        if(this.hasTargetDevice)
            ScreenUtils.drawCenteredString(matrixStack, this.font, I18n.format("wormhole.target_device.gui.title"), 296, 3, Integer.MAX_VALUE);

        GlStateManager.enableAlphaTest();
        // draw target select highlight
        if(this.selectedPortalTarget >= this.scrollOffset && this.selectedPortalTarget < this.scrollOffset + 10){
            ScreenUtils.bindTexture(SELECT_HIGHLIGHT);
            ScreenUtils.drawTexture(matrixStack, 5, 16 + 16 * (this.selectedPortalTarget - this.scrollOffset), 130, 16);
        }else if(this.hasTargetDevice && this.selectedDeviceTarget >= 0 && this.selectedDeviceTarget < 10){
            ScreenUtils.bindTexture(SELECT_HIGHLIGHT_DEVICE);
            ScreenUtils.drawTexture(matrixStack, 242, 16 + 16 * this.selectedDeviceTarget, 106, 16);
        }

        // draw hover highlight
        if(mouseX > 5 && mouseX < 135 && mouseY > 16 && mouseY < 176){
            int targetIndex = (mouseY - 16) / 16;
            if(this.getFromPortalGroup(group -> group.getTarget(targetIndex + this.scrollOffset) != null, false)){
                ScreenUtils.bindTexture(HOVER_HIGHLIGHT);
                ScreenUtils.drawTexture(matrixStack, 5, 16 + targetIndex * 16, 130, 16);
            }
        }else if(this.hasTargetDevice && mouseX > 242 && mouseX < 348 && mouseY > 16 && mouseY < 176){
            int targetIndex = (mouseY - 16) / 16;
            if(this.getFromDeviceTargets(list -> list.size() > targetIndex && list.get(targetIndex) != null, false)){
                ScreenUtils.bindTexture(HOVER_HIGHLIGHT_DEVICE);
                ScreenUtils.drawTexture(matrixStack, 242, 16 + targetIndex * 16, 106, 16);
            }
        }

        int activeTarget = this.getFromPortalGroup(PortalGroup::getActiveTargetIndex, -1);
        // draw target numbers
        for(int count = 0; count < Math.min(10, this.portalTargetNameLabels.size()); count++){
            if(count + this.scrollOffset != activeTarget)
                ScreenUtils.drawCenteredString(matrixStack, this.font, this.scrollOffset + count + 1 + ".", 14, 21 + count * 16, Integer.MAX_VALUE);
            else{
                GlStateManager.enableAlphaTest();
                ScreenUtils.bindTexture(STAR_ICON);
                ScreenUtils.drawTexture(matrixStack, 8, 19 + 16 * count, 10, 10);
            }
        }

        // draw target info
        if(this.selectedPortalTarget >= 0 && this.selectedPortalTarget < this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0)){
            PortalTarget target = this.getFromPortalGroup(group -> group.getTarget(this.selectedPortalTarget), null);
            if(target != null)
                this.renderTargetInfo(matrixStack, target, true);
        }else if(this.hasTargetDevice && this.selectedDeviceTarget >= 0 && this.selectedDeviceTarget < 10){
            PortalTarget target = this.getFromDeviceTargets(list -> this.selectedDeviceTarget < list.size() ? list.get(this.selectedDeviceTarget) : null, null);
            if(target != null)
                this.renderTargetInfo(matrixStack, target, false);
        }

        this.updateSelectRemoveColorButtons();
    }

    private void renderTargetInfo(MatrixStack matrixStack, PortalTarget target, boolean showColor){
        ScreenUtils.drawCenteredString(matrixStack, this.font, target.name, 191, 31, Integer.MAX_VALUE);

        ScreenUtils.bindTexture(SEPARATOR);
        ScreenUtils.drawTexture(matrixStack, 153, 41, 77, 1);

        // location
        GlStateManager.enableAlphaTest();
        ScreenUtils.bindTexture(LOCATION_ICON);
        ScreenUtils.drawTexture(matrixStack, 150, 47, 9, 9);
        ScreenUtils.drawString(matrixStack, this.font, "(" + target.x + ", " + target.y + ", " + target.z + ")", 161, 48, Integer.MAX_VALUE);
        // dimension
        Block block = null;
        if(target.dimension.equals(World.OVERWORLD.getLocation().toString()))
            block = Blocks.GRASS_PATH;
        else if(target.dimension.equals(World.THE_NETHER.getLocation().toString()))
            block = Blocks.NETHERRACK;
        else if(target.dimension.equals(World.THE_END.getLocation().toString()))
            block = Blocks.END_STONE;
        if(block == null){
            ScreenUtils.bindTexture(DIMENSION_ICON);
            ScreenUtils.drawTexture(matrixStack, 150, 59, 9, 9);
        }else{
            ScreenBlockRenderer.drawBlock(block, this.left() + 154.5, this.top() + 63.5, 5.5, 45, 40);
        }
        ScreenUtils.drawString(matrixStack, this.font, target.getDimensionDisplayName(), 161, 60, Integer.MAX_VALUE);
        // direction
        GlStateManager.enableAlphaTest();
        ScreenUtils.bindTexture(DIRECTION_ICON);
        ScreenUtils.drawTexture(matrixStack, 148, 69, 13, 13);
        ScreenUtils.drawString(matrixStack, this.font, I18n.format("wormhole.direction." + Direction.fromAngle(target.yaw).toString()), 161, 72, Integer.MAX_VALUE);

        ScreenUtils.bindTexture(SEPARATOR);
        ScreenUtils.drawTexture(matrixStack, 153, 85, 77, 1);

        if(showColor){
            // color
            ScreenUtils.drawString(matrixStack, this.font, I18n.format("wormhole.color." + (target.color == null ? "random" : target.color.getTranslationKey())), 161, 92, Integer.MAX_VALUE);

            ScreenUtils.bindTexture(SEPARATOR);
            ScreenUtils.drawTexture(matrixStack, 153, 105, 77, 1);
        }

        // energy cost
        GlStateManager.enableAlphaTest();
        ScreenUtils.bindTexture(ENERGY_ICON);
        ScreenUtils.drawTexture(matrixStack, 150, showColor ? 111 : 91, 9, 9);
        int cost = PortalGroup.getTeleportCostToTarget(this.player.world, this.getFromPortalGroup(PortalGroup::getCenterPos, BlockPos.ZERO), target);
        ScreenUtils.drawString(matrixStack, this.font, EnergyFormat.formatEnergy(cost), 161, showColor ? 112 : 92, Integer.MAX_VALUE);
    }

    private void addPortalTargetWidgets(){
        int portalCapacity = this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0);

        for(int count = 0; count < Math.min(10, portalCapacity); count++){
            final int index = count;
            int y = 18 + index * 16;
            this.portalTargetNameLabels.add(this.addWidget(new WormholeLabel(20, y, 102, 12, () -> {
                PortalTarget target = getFromPortalGroup(group -> group.getTarget(index + this.scrollOffset), null);
                return target != null ? target.name : "";
            }, false)));
            // up arrow
            ArrowButton upArrowButton = new ArrowButton(123, y + 1, true, () -> {
                if(this.selectedPortalTarget == this.scrollOffset + index)
                    this.selectedPortalTarget -= 1;
                else if(this.selectedPortalTarget == this.scrollOffset + index - 1)
                    this.selectedPortalTarget += 1;
                Wormhole.CHANNEL.sendToServer(new PortalMoveTargetPacket(this.getPortalGroup(), this.scrollOffset + index, true));
            });
            this.portalUpArrows.add(this.addWidget(upArrowButton));
            // down arrow
            ArrowButton downArrowButton = new ArrowButton(123, y + 6, false, () -> {
                if(this.selectedPortalTarget == this.scrollOffset + index)
                    this.selectedPortalTarget += 1;
                else if(this.selectedPortalTarget == this.scrollOffset + index + 1)
                    this.selectedPortalTarget -= 1;
                Wormhole.CHANNEL.sendToServer(new PortalMoveTargetPacket(this.getPortalGroup(), this.scrollOffset + index, false));
            });
            this.portalDownArrows.add(this.addWidget(downArrowButton));
        }

        this.updateArrowButtons();
    }

    private void addDeviceTargetWidgets(){
        ItemStack stack = this.player.getHeldItem(this.hand);
        if(stack.isEmpty() || !(stack.getItem() instanceof TargetDeviceItem))
            return;
        int deviceCapacity = TargetDeviceItem.getMaxTargetCount(stack);

        for(int count = 0; count < Math.min(10, deviceCapacity); count++){
            final int index = count;
            int y = 18 + index * 16;
            this.deviceTargetNameLabels.add(this.addWidget(new WormholeLabel(244, y, 102, 12, () -> {
                PortalTarget target = getFromDeviceTargets(list -> list.size() > index ? list.get(index) : null, null);
                return target != null ? target.name : "";
            }, false)));
        }
    }

    @Override
    public void tick(){
        super.tick();

        this.updateArrowButtons();
    }

    private void updateArrowButtons(){
        int capacity = this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0);
        for(int i = 0; i < this.portalUpArrows.size() && i < this.portalDownArrows.size(); i++){
            final int index = i + this.scrollOffset;
            boolean hasTarget = this.getFromPortalGroup(group -> group.getTarget(index) != null, false);
            this.portalUpArrows.get(i).active = index > 0 && hasTarget;
            this.portalDownArrows.get(i).active = index < capacity - 1 && hasTarget;
        }
    }

    private void updateSelectRemoveColorButtons(){
        if(this.selectedPortalTarget >= 0){
            boolean notEmpty = this.getFromPortalGroup(group -> group.getTarget(this.selectedPortalTarget), null) != null;
            this.selectButton.setVisible();
            this.selectButton.setColorGreen();
            this.selectButton.setTextKey("wormhole.portal.targets.gui.select");
            this.selectButton.active = notEmpty && this.getFromPortalGroup(PortalGroup::getActiveTargetIndex, -1) != this.selectedPortalTarget;
            this.removeButton.setVisible();
            this.removeButton.setColorRed();
            this.removeButton.setTextKey("wormhole.portal.targets.gui.remove");
            this.removeButton.active = notEmpty;
            this.colorButton.visible = true;
        }else if(this.selectedDeviceTarget >= 0){
            boolean notEmpty = this.getFromDeviceTargets(group -> group.size() > this.selectedDeviceTarget && group.get(this.selectedDeviceTarget) != null, false);
            this.selectButton.setInvisible();
            this.removeButton.setVisible();
            this.removeButton.setColorWhite();
            this.removeButton.setTextKey("wormhole.portal.targets.gui.add");
            this.removeButton.active = notEmpty;
            this.colorButton.visible = false;
        }else{
            this.selectButton.setInvisible();
            this.removeButton.setInvisible();
            this.colorButton.visible = false;
        }
    }

    @Override
    protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY){
        // location
        if(mouseX >= 149 && mouseX <= 160 && mouseY >= 46 && mouseY <= 57)
            this.renderTooltip(matrixStack, new TranslationTextComponent("wormhole.target.location"), mouseX, mouseY);
            // dimension
        else if(mouseX >= 149 && mouseX <= 160 && mouseY >= 58 && mouseY <= 69)
            this.renderTooltip(matrixStack, new TranslationTextComponent("wormhole.target.dimension"), mouseX, mouseY);
            // direction
        else if(mouseX >= 149 && mouseX <= 160 && mouseY >= 70 && mouseY <= 81)
            this.renderTooltip(matrixStack, new TranslationTextComponent("wormhole.target.direction"), mouseX, mouseY);
            // energy
        else if(mouseX >= 149 && mouseX <= 160 && (this.selectedPortalTarget >= 0 ? mouseY >= 110 && mouseY <= 121 : mouseY >= 90 && mouseY <= 101))
            this.renderTooltip(matrixStack, new TranslationTextComponent("wormhole.target.teleport_cost"), mouseX, mouseY);
    }

    public <T> T getFromDeviceTargets(Function<List<PortalTarget>,T> function, T other){
        ItemStack stack = this.player.getHeldItem(this.hand);
        if(!stack.isEmpty() && stack.getItem() instanceof TargetDeviceItem)
            return function.apply(TargetDeviceItem.getTargets(stack));
        this.closeScreen();
        return other;
    }

    private void scroll(int amount){
        this.scrollOffset = Math.min(Math.max(0, this.scrollOffset + amount), Math.max(0, this.getFromPortalGroup(PortalGroup::getTotalTargetCapacity, 0) - 10));
    }

    @Override
    protected void onMouseScroll(int mouseX, int mouseY, double scroll){
        if(mouseX >= 5 && mouseX <= 135 && mouseY >= 16 && mouseY <= 183)
            this.scroll(-(int)scroll);
    }

    @Override
    protected void onMousePress(int mouseX, int mouseY, int button){
        if(button != 0)
            return;

        if(mouseX > 5 && mouseX < 135 && mouseY > 16 && mouseY < 176){
            int targetIndex = (mouseY - 16) / 16 + this.scrollOffset;
            if(this.getFromPortalGroup(group -> group.getTarget(targetIndex) != null, false)){
                AbstractButtonWidget.playClickSound();
                this.selectedPortalTarget = targetIndex;
                this.selectedDeviceTarget = -1;
            }
        }else if(this.hasTargetDevice && mouseX > 242 && mouseX < 348 && mouseY > 16 && mouseY < 176){
            int targetIndex = (mouseY - 16) / 16;
            if(this.getFromDeviceTargets(list -> list.size() > targetIndex && list.get(targetIndex) != null, false)){
                AbstractButtonWidget.playClickSound();
                this.selectedPortalTarget = -1;
                this.selectedDeviceTarget = targetIndex;
            }
        }
    }
}
