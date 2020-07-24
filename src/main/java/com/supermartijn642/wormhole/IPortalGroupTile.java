package com.supermartijn642.wormhole;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public interface IPortalGroupTile {

    void setGroup(PortalGroup group);

    boolean hasGroup();

    void onBreak();

}
