package com.dodster.spectatorinv;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(SpectatorInvMod.MOD_ID)
public class SpectatorInvMod {

    public static final String MOD_ID = "spectatorinv";
    public static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger("Spectator Inventory");

    public SpectatorInvMod(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            throw new RuntimeException("Spectator Inventory is a server-only mod and must be installed on the server, not the client.");
        }
        ModConfig.init(modContainer);
        NeoForge.EVENT_BUS.addListener(SpectatorInvCommands::registerCommands);
    }
}