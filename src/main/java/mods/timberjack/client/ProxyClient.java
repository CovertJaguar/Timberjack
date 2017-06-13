/*
 * Copyright (c) CovertJaguar, 2011-2017
 *
 * This work (Timberjack) is licensed under the "MIT" License,
 * see LICENSE in root folder for details.
 */

package mods.timberjack.client;

import mods.timberjack.client.rendering.RenderEntityTimber;
import mods.timberjack.common.Proxy;
import mods.timberjack.common.entity.EntityTimber;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

/**
 * Created by CovertJaguar on 5/25/2017 for Railcraft.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
@SuppressWarnings("unused")
public class ProxyClient extends Proxy {

    @Override
    public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityTimber.class, RenderEntityTimber::new);
    }
}
