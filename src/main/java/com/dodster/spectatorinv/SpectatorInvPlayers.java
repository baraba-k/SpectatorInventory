package com.dodster.spectatorinv;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

final class SpectatorInvPlayers {

    private SpectatorInvPlayers() {
    }

    record Target(UUID uuid, String name, ServerPlayer online) {
    }

    static Optional<Target> resolve(MinecraftServer server, String argument, boolean allowUnknown) {
        UUID uuid;
        try {
            uuid = UUID.fromString(argument.trim());
        } catch (IllegalArgumentException e) {
            uuid = null;
        }

        if (uuid != null) {
            ServerPlayer online = server.getPlayerList().getPlayer(uuid);
            String name = online != null ? online.getGameProfile().getName() : cachedName(server, uuid);
            if (name == null) name = "unknown";
            return Optional.of(new Target(uuid, name, online));
        }

        ServerPlayer online = server.getPlayerList().getPlayerByName(argument);
        if (online != null) {
            return Optional.of(new Target(online.getUUID(), online.getGameProfile().getName(), online));
        }

        GameProfileCache cache = server.getProfileCache();
        if (cache != null) {
            Optional<GameProfile> cached = cache.get(argument);
            if (cached.isPresent()) {
                return Optional.of(new Target(cached.get().getId(), argument, null));
            }
        }

        if (allowUnknown) {
            return Optional.of(new Target(UUID.randomUUID(), argument, null));
        }
        return Optional.empty();
    }

    private static String cachedName(MinecraftServer server, UUID uuid) {
        GameProfileCache cache = server.getProfileCache();
        if (cache == null) return null;
        return cache.get(uuid).map(GameProfile::getName).orElse(null);
    }

    static InventoryHandle handle(MinecraftServer server, Target target) {
        if (target.online() != null) {
            return new OnlineHandle(target.online());
        }
        return new OfflineHandle(server, target.uuid());
    }

    interface InventoryHandle {
        ItemStack getMain(int index);

        void setMain(int index, ItemStack stack);

        ItemStack getEnder(int index);

        void setEnder(int index, ItemStack stack);

        void onChanged();

        void save();
    }

    private static final class OnlineHandle implements InventoryHandle {

        private final ServerPlayer player;

        OnlineHandle(ServerPlayer player) {
            this.player = player;
        }

        @Override
        public ItemStack getMain(int index) {
            return player.getInventory().getItem(index);
        }

        @Override
        public void setMain(int index, ItemStack stack) {
            player.getInventory().setItem(index, stack);
        }

        @Override
        public ItemStack getEnder(int index) {
            return player.getEnderChestInventory().getItem(index);
        }

        @Override
        public void setEnder(int index, ItemStack stack) {
            player.getEnderChestInventory().setItem(index, stack);
        }

        @Override
        public void onChanged() {
            player.getInventory().setChanged();
            player.getEnderChestInventory().setChanged();
        }

        @Override
        public void save() {
        }
    }

    private static final class OfflineHandle implements InventoryHandle {

        private final MinecraftServer server;
        private final UUID uuid;
        private final CompoundTag root;
        private final ItemStack[] main = new ItemStack[41];
        private final ItemStack[] ender = new ItemStack[27];

        OfflineHandle(MinecraftServer server, UUID uuid) {
            this.server = server;
            this.uuid = uuid;
            this.root = OfflineData.read(server, uuid).orElse(new CompoundTag());
            System.arraycopy(OfflineData.readMain(server.registryAccess(), root), 0, main, 0, main.length);
            System.arraycopy(OfflineData.readEnder(server.registryAccess(), root), 0, ender, 0, ender.length);
        }

        @Override
        public ItemStack getMain(int index) {
            ItemStack stack = main[index];
            return stack == null ? ItemStack.EMPTY : stack;
        }

        @Override
        public void setMain(int index, ItemStack stack) {
            main[index] = stack;
        }

        @Override
        public ItemStack getEnder(int index) {
            ItemStack stack = ender[index];
            return stack == null ? ItemStack.EMPTY : stack;
        }

        @Override
        public void setEnder(int index, ItemStack stack) {
            ender[index] = stack;
        }

        @Override
        public void onChanged() {
        }

        @Override
        public void save() {
            OfflineData.writeMain(server.registryAccess(), root, main);
            OfflineData.writeEnder(server.registryAccess(), root, ender);
            OfflineData.write(server, uuid, root);
        }
    }
}