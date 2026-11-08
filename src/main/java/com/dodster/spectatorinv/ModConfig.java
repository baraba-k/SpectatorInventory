package com.dodster.spectatorinv;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModConfig {

    private ModConfig() {
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue OFFLINE_PLAYER_SUPPORT =
            BUILDER.comment("Whether inventories of players who are offline may be spectated.")
                    .define("enableOfflinePlayerSupport", true);

    public static final ModConfigSpec.BooleanValue UNKNOWN_PLAYER_SUPPORT =
            BUILDER.comment("Whether to create spectator inventories for players who never played on this server before.")
                    .define("enableUnknownPlayerSupport", true);

    public static final ModConfigSpec.ConfigValue<String> INVENTORY_TITLE =
            BUILDER.comment("Title of the inventory spectator view. May contain '<player>' which is replaced by the target's name.")
                    .define("titles.inventory", "<player>'s inventory");

    public static final ModConfigSpec.ConfigValue<String> ENDERCHEST_TITLE =
            BUILDER.comment("Title of the ender chest spectator view. May contain '<player>' which is replaced by the target's name.")
                    .define("titles.enderchest", "<player>'s enderchest");

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void init(ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, SPEC);
    }
}