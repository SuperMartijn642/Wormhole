package com.supermartijn642.wormhole.targetdevice;

import com.supermartijn642.wormhole.portal.PortalTarget;

import java.util.List;
import java.util.function.Function;

/**
 * Created 11/4/2020 by SuperMartijn642
 */
public interface ITargetProvider {

    <T> T getFromTargets(Function<List<PortalTarget>, T> function, T defaultValue);
}
