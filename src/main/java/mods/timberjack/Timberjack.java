package mods.timberjack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = Timberjack.MOD_ID, version = Timberjack.VERSION)
public class Timberjack {
    public static final String MOD_ID = "timberjack";
    public static final String VERSION = "1.0";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new TimberjackEventHandler());
    }
}
