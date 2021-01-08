package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalNameTargetPacket;
import com.supermartijn642.wormhole.screen.WormholeTextField;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalTargetNameField extends WormholeTextField {

    private final PortalGroupScreen screen;
    private final Supplier<Integer> targetIndex;
    private String lastTargetText;
    private final List<String> pastText = new LinkedList<>();

    public PortalTargetNameField(PortalGroupScreen screen, Supplier<Integer> targetIndex, int x, int y){
        super(x, y, 59, 10, "", PortalTarget.MAX_NAME_LENGTH);
        this.screen = screen;
        this.targetIndex = targetIndex;

        PortalTarget target = screen.getPortalGroup().getTarget(targetIndex.get());
        this.setTextSuppressed(target == null ? "" : target.name);
        this.lastTargetText = this.getText();
    }

    public void tick(){
        super.tick();

        PortalTarget target = screen.getPortalGroup().getTarget(targetIndex.get());
        String s = target == null ? "" : target.name;
        if(!s.equals(this.lastTargetText)){
            if(s.equals(this.getText()))
                this.pastText.clear();
            else{
                int index = this.pastText.indexOf(s);
                if(index < 0){
                    this.setTextSuppressed(s);
                    this.cursorPosition = this.getText().length();
                    this.selectionPos = this.cursorPosition;
                }else
                    this.pastText.subList(0, index + 1).clear();
            }
            this.lastTargetText = s;
        }
    }

    @Override
    protected void onTextChanged(String oldText, String newText){
        Wormhole.channel.sendToServer(new PortalNameTargetPacket(this.screen.getPortalGroup(), this.targetIndex.get(), newText));
    }
}
