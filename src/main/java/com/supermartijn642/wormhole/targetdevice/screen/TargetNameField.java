package com.supermartijn642.wormhole.targetdevice.screen;

import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.screen.WormholeTextField;
import com.supermartijn642.wormhole.targetdevice.ITargetProvider;
import com.supermartijn642.wormhole.targetdevice.packets.TargetDeviceNamePacket;
import net.minecraft.util.EnumHand;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 10/28/2020 by SuperMartijn642
 */
public class TargetNameField extends WormholeTextField {

    private final ITargetProvider targetProvider;
    private final EnumHand hand;
    private final int targetIndex;
    private String lastTargetText;
    private List<String> pastText = new LinkedList<>();

    public TargetNameField(ITargetProvider targetProvider, EnumHand hand, int targetIndex, int x, int y){
        super(x, y, 59, 10, "", PortalTarget.MAX_NAME_LENGTH);
        this.targetProvider = targetProvider;
        this.hand = hand;
        this.targetIndex = targetIndex;

        this.setTextSuppressed(targetProvider.getFromTargets(list -> list.size() > targetIndex ? list.get(targetIndex).name : "", ""));
        this.lastTargetText = this.getText();
    }

    public void tick(){
        super.tick();

        String s = this.targetProvider.getFromTargets(list -> list.size() > this.targetIndex ? list.get(this.targetIndex).name : "", "");
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
        this.pastText.add(oldText);
        Wormhole.channel.sendToServer(new TargetDeviceNamePacket(this.hand, this.targetIndex, newText));
    }
}
