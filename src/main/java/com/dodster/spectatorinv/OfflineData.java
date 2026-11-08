package com.dodster.spectatorinv;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

final class OfflineData {

    private OfflineData() {
    }

    static Path playerDataDir(MinecraftServer server) {
        return server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
    }

    static Path dataFile(Path playerDataDir, UUID uuid) {
        return playerDataDir.resolve(uuid + ".dat");
    }

    static Optional<CompoundTag> read(MinecraftServer server, UUID uuid) {
        Path file = dataFile(playerDataDir(server), uuid);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()));
        } catch (IOException e) {
            SpectatorInvMod.LOGGER.error("Could not read player data for " + uuid, e);
            return Optional.empty();
        }
    }

    static void write(MinecraftServer server, UUID uuid, CompoundTag tag) {
        Path file = dataFile(playerDataDir(server), uuid);
        try {
            NbtIo.writeCompressed(tag, file);
        } catch (IOException e) {
            SpectatorInvMod.LOGGER.error("Could not write player data for " + uuid, e);
        }
    }

    static ItemStack[] readMain(HolderLookup.Provider registries, CompoundTag root) {
        ItemStack[] slots = new ItemStack[41];
        ListTag list = root.getList("Inventory", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int index = entry.getInt("Slot");
            if (0 <= index && index < slots.length) {
                slots[index] = ItemStack.parseOptional(registries, entry);
            }
        }
        return slots;
    }

    static ItemStack[] readEnder(HolderLookup.Provider registries, CompoundTag root) {
        ItemStack[] slots = new ItemStack[27];
        ListTag list = root.getList("EnderItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int index = entry.getInt("Slot");
            if (0 <= index && index < slots.length) {
                slots[index] = ItemStack.parseOptional(registries, entry);
            }
        }
        return slots;
    }

    static CompoundTag writeMain(HolderLookup.Provider registries, CompoundTag root, ItemStack[] slots) {
        ListTag list = new ListTag();
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = slots[i];
            if (stack != null && !stack.isEmpty()) {
                CompoundTag entry = (CompoundTag) stack.save(registries);
                entry.putInt("Slot", i);
                list.add(entry);
            }
        }
        root.put("Inventory", list);
        return root;
    }

    static CompoundTag writeEnder(HolderLookup.Provider registries, CompoundTag root, ItemStack[] slots) {
        ListTag list = new ListTag();
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = slots[i];
            if (stack != null && !stack.isEmpty()) {
                CompoundTag entry = (CompoundTag) stack.save(registries);
                entry.putInt("Slot", i);
                list.add(entry);
            }
        }
        root.put("EnderItems", list);
        return root;
    }
}