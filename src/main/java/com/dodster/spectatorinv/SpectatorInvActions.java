package com.dodster.spectatorinv;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class SpectatorInvActions {

    private SpectatorInvActions() {
    }

    static void openMainInventory(ServerPlayer spectator, SpectatorInvPlayers.Target target) {
        MinecraftServer server = spectator.getServer();
        SpectatorInvPlayers.InventoryHandle handle = SpectatorInvPlayers.handle(server, target);
        MainSpectateContainer top = new MainSpectateContainer(handle);
        String title = ModConfig.INVENTORY_TITLE.get().replace("<player>", target.name());
        boolean own = target.online() == spectator || (target.online() == null && target.uuid().equals(spectator.getUUID()));
        spectator.openMenu(provider(top, SpectatorInvMenu.TOP_ROWS_MAIN, own, handle::save, title));
    }

    static void openEnderChest(ServerPlayer spectator, SpectatorInvPlayers.Target target) {
        MinecraftServer server = spectator.getServer();
        SpectatorInvPlayers.InventoryHandle handle = SpectatorInvPlayers.handle(server, target);
        EnderSpectateContainer top = new EnderSpectateContainer(handle);
        String title = ModConfig.ENDERCHEST_TITLE.get().replace("<player>", target.name());
        boolean own = target.online() == spectator || (target.online() == null && target.uuid().equals(spectator.getUUID()));
        spectator.openMenu(provider(top, SpectatorInvMenu.TOP_ROWS_ENDER, own, handle::save, title));
    }

    private static MenuProvider provider(Container top, int topRows, boolean spectatingOwn,
                                         Runnable onClose, String title) {
        Component component = Component.literal(title);
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return component;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new SpectatorInvMenu(containerId, playerInventory, top, topRows, spectatingOwn, onClose);
            }
        };
    }

    static Item parseItem(String raw) {
        ResourceLocation location = ResourceLocation.tryParse(raw);
        if (location == null) {
            location = ResourceLocation.tryParse("minecraft:" + raw.toLowerCase());
        }
        if (location == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? null : item;
    }

    static int clearMain(SpectatorInvPlayers.InventoryHandle handle, Item type, int amount) {
        int removed = 0;
        for (int i = 0; i < 41; i++) {
            ItemStack current = handle.getMain(i);
            if (current.isEmpty() || (type != null && !current.is(type))) {
                continue;
            }
            if (amount < 0) {
                removed += current.getCount();
                handle.setMain(i, ItemStack.EMPTY);
            } else {
                int take = Math.min(current.getCount(), amount - removed);
                removed += take;
                current.shrink(take);
                if (current.isEmpty()) {
                    handle.setMain(i, ItemStack.EMPTY);
                }
                if (removed >= amount) {
                    break;
                }
            }
        }
        if (removed > 0) {
            handle.onChanged();
        }
        return removed;
    }

    static int clearEnder(SpectatorInvPlayers.InventoryHandle handle, Item type, int amount) {
        int removed = 0;
        for (int i = 0; i < 27; i++) {
            ItemStack current = handle.getEnder(i);
            if (current.isEmpty() || (type != null && !current.is(type))) {
                continue;
            }
            if (amount < 0) {
                removed += current.getCount();
                handle.setEnder(i, ItemStack.EMPTY);
            } else {
                int take = Math.min(current.getCount(), amount - removed);
                removed += take;
                current.shrink(take);
                if (current.isEmpty()) {
                    handle.setEnder(i, ItemStack.EMPTY);
                }
                if (removed >= amount) {
                    break;
                }
            }
        }
        if (removed > 0) {
            handle.onChanged();
        }
        return removed;
    }

    static void cloneMain(SpectatorInvPlayers.InventoryHandle from, SpectatorInvPlayers.InventoryHandle to) {
        for (int i = 0; i < 41; i++) {
            to.setMain(i, from.getMain(i).copy());
        }
        to.onChanged();
    }

    static void cloneEnder(SpectatorInvPlayers.InventoryHandle from, SpectatorInvPlayers.InventoryHandle to) {
        for (int i = 0; i < 27; i++) {
            to.setEnder(i, from.getEnder(i).copy());
        }
        to.onChanged();
    }
}