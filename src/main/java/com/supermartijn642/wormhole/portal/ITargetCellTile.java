package com.supermartijn642.wormhole.portal;

import java.util.List;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public interface ITargetCellTile {

    int getTargetCapacity();

    PortalTarget getTarget(int index);

    void setTarget(int index, PortalTarget target);

    List<PortalTarget> getTargets();

}
