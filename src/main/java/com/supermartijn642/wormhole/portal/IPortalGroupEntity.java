package com.supermartijn642.wormhole.portal;

/**
 * Created 7/23/2020 by SuperMartijn642
 */
public interface IPortalGroupEntity {

    boolean hasGroup();

    PortalGroup getGroup();

    void onBreak();

}
