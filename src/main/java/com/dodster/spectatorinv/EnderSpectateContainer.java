package com.dodster.spectatorinv;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

class EnderSpectateContainer implements Container {

    private final SpectatorInvPlayers.InventoryHandle handle;

    EnderSpectateContainer(SpectatorInvPlayers.InventoryHandle handle) {
        this.handle = handle;
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < 27; i++) {
            if (!getItem(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        if (index < 0 || index >= 27) return ItemStack.EMPTY;
        return handle.getEnder(index);
    }

    @Override
    public ItemStack removeItem(int index, int amount) {
        if (index < 0 || index >= 27) return ItemStack.EMPTY;
        ItemStack stack = handle.getEnder(index);
        if (!stack.isEmpty() && amount > 0) {
            ItemStack removed = stack.split(amount);
            if (!removed.isEmpty()) setChanged();
            return removed;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index < 0 || index >= 27) return ItemStack.EMPTY;
        ItemStack stack = handle.getEnder(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        handle.setEnder(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index >= 0 && index < 27) {
            handle.setEnder(index, stack);
        }
    }

    @Override
    public void setChanged() {
        handle.onChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < 27; i++) {
            handle.setEnder(i, ItemStack.EMPTY);
        }
        setChanged();
    }
}