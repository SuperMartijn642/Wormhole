package com.supermartijn642.wormhole.portal;

import java.util.List;

/**
 * Created 10/29/2020 by SuperMartijn642
 */
public interface ITargetCellEntity {

    int getTargetCapacity();

    PortalTarget getTarget(int index);

    void setTarget(int index, PortalTarget target);

    List<PortalTarget> getTargets();

    /**
     * Gets the number of targets which have actually been set, i.e. are not null.
     */
    int getNonNullTargetCount();

}
