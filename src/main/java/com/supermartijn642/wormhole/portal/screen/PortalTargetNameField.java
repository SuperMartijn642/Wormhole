package com.supermartijn642.wormhole.portal.screen;

import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;
import com.supermartijn642.wormhole.Wormhole;
import com.supermartijn642.wormhole.portal.PortalTarget;
import com.supermartijn642.wormhole.portal.packets.PortalNameTargetPacket;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created 11/5/2020 by SuperMartijn642
 */
public class PortalTargetNameField extends TextFieldWidget {

    private final PortalGroupScreen screen;
    private final Supplier<Integer> targetIndex;
    private String lastTargetText;
    private final List<String> pastText = new LinkedList<>();

    public PortalTargetNameField(PortalGroupScreen screen, Supplier<Integer> targetIndex, int x, int y){
        super(x - 1, y - 1, 61, 12, "", PortalTarget.MAX_NAME_LENGTH);
        this.screen = screen;
        this.targetIndex = targetIndex;

        PortalTarget target = screen.getPortalGroup().getTarget(targetIndex.get());
        this.setTextSuppressed(target == null ? "" : target.name);
        this.lastTargetText = this.getText();
    }

    @Override
    public void update(){
        super.update();

        PortalTarget target = screen.getPortalGroup().getTarget(targetIndex.get());
        String s = target == null ? "" : target.name;
        if(!s.equals(this.lastTargetText)){
            if(s.equals(this.getText()))
                this.pastText.clear();
            else{
                int index = this.pastText.indexOf(s);
                if(index < 0){
                    this.setTextSuppressed(s);
                    this.cursorPosition = this.selectionPos = this.getText().length();
                    this.moveLineOffsetToCursor();
                }else
                    this.pastText.subList(0, index + 1).clear();
            }
            this.lastTargetText = s;
        }
    }

    @Override
    protected void onTextChanged(String oldText, String newText){
        this.pastText.add(oldText);
        Wormhole.CHANNEL.sendToServer(new PortalNameTargetPacket(this.screen.getPortalGroup(), this.targetIndex.get(), newText));
    }
}
