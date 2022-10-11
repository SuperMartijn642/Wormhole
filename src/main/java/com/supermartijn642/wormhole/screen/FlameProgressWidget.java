package com.supermartijn642.wormhole.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BaseWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Created 12/25/2020 by SuperMartijn642
 */
public class FlameProgressWidget extends BaseWidget {

    private static final ResourceLocation FLAME = new ResourceLocation("wormhole", "textures/gui/progress_flame.png");

    private final Supplier<Float> progress;

    public FlameProgressWidget(Supplier<Float> progress, int x, int y, int width, int height){
        super(x, y, width, height);
        this.progress = progress;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY){
        ScreenUtils.bindTexture(FLAME);
        float progress = Math.max(Math.min(this.progress.get(), 1), 0);
        if(progress != 1)
            ScreenUtils.drawTexture(poseStack, this.x, this.y, this.width, this.height * (1 - progress), 0, 0, 0.5f, (1 - progress));
        if(progress != 0)
            ScreenUtils.drawTexture(poseStack, this.x, this.y + this.height * (1 - progress), this.width, this.height * progress, 0.5f, 1 - progress, 0.5f, progress);
    }

    @Override
    public Component getNarrationMessage(){
        return null;
    }
}
