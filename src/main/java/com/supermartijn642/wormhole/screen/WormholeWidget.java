package com.supermartijn642.wormhole.screen;

/**
 * Created 1/6/2021 by SuperMartijn642
 */
public abstract class WormholeWidget {

    public int x, y;
    public int width, height;
    public boolean active = true;
    public boolean hovered = false;
    public float blitOffset = 0;

    public WormholeWidget(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(int mouseX, int mouseY, float partialTicks);

    public boolean isHovered(){
        return hovered;
    }

    public void mouseClicked(int mouseX, int mouseY, int button){
    }

    public void mouseDragged(int mouseX, int mouseY, int button){
    }

    public void mouseReleased(int mouseX, int mouseY, int button){
    }

    public void mouseScrolled(int mouseX, int mouseY, int scroll){
    }

    public void keyTyped(char c, int keyCode){
    }

}
