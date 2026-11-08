package com.dodster.spectatorinv;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

public class SpectatorInvMenu extends AbstractContainerMenu {

    static final int TOP_ROWS_MAIN = 6;
    static final int TOP_ROWS_ENDER = 3;
    static final int DEFAULT_PLAYER_ROWS = 4;

    private final Container top;
    private final int topRows;
    private final int topSlots;
    private final boolean spectatingOwn;
    private final Runnable onClose;

    public SpectatorInvMenu(int containerId, Inventory playerInventory, Container top, int topRows,
                      boolean spectatingOwn, @Nullable Runnable onClose) {
        super(topRows == TOP_ROWS_MAIN ? MenuType.GENERIC_9x6 : MenuType.GENERIC_9x3, containerId);
        this.top = top;
        this.topRows = topRows;
        this.topSlots = topRows * 9;
        this.spectatingOwn = spectatingOwn;
        this.onClose = onClose;

        addTopSlots();
        addPlayerSlots(playerInventory);
    }

    private void addTopSlots() {
        for (int row = 0; row < topRows; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                addSlot(makeTopSlot(slotIndex, x, y));
            }
        }
    }

    private Slot makeTopSlot(int slotIndex, int x, int y) {
        if (topRows == TOP_ROWS_MAIN) {
            switch (slotIndex) {
                case 36: return new EquipmentSlot(top, 36, x, y, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
                case 37: return new EquipmentSlot(top, 37, x, y, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
                case 38: return new EquipmentSlot(top, 38, x, y, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
                case 39: return new EquipmentSlot(top, 39, x, y, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
                case 40: return new OffhandSlot(top, 40, x, y);
                default:
                    if (slotIndex <= 35) return new Slot(top, slotIndex, x, y);
                    return new InaccessibleSlot(slotIndex, x, y);
            }
        }
        return new Slot(top, slotIndex, x, y);
    }

    private void addPlayerSlots(Inventory playerInventory) {
        int magicAddY = (topRows - DEFAULT_PLAYER_ROWS) * 18;

        for (int row = 1; row <= 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                int x = 8 + col * 18;
                int y = 103 + row * 18 + magicAddY;
                addSlot(new Slot(playerInventory, index, x, y));
            }
        }

        for (int col = 0; col < 9; col++) {
            int x = 8 + col * 18;
            int y = 161 + magicAddY;
            addSlot(new Slot(playerInventory, col, x, y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int rawIndex) {
        if (spectatingOwn) return ItemStack.EMPTY;

        Slot slot = getSlot(rawIndex);
        if (slot == null || !slot.hasItem() || slot instanceof InaccessibleSlot) {
            return ItemStack.EMPTY;
        }

        ItemStack itemStack = slot.getItem().copy();
        if (rawIndex < topSlots) {
            if (!moveItemStackTo(slot.getItem(), topSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(slot.getItem(), 0, topSlots, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (slot.getItem().isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getTopRows() {
        return topRows;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (onClose != null) {
            onClose.run();
        }
    }

    static ItemStack placeholder() {
        ItemStack stack = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        stack.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        return stack;
    }

    private static class InaccessibleSlot extends Slot {

        private final ItemStack placeholder;

        InaccessibleSlot(int index, int x, int y) {
            super(new net.minecraft.world.SimpleContainer(1), 0, x, y);
            this.placeholder = placeholder();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean hasItem() {
            return false;
        }

        @Override
        public void set(ItemStack stack) {
            setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 0;
        }

        @Override
        public ItemStack remove(int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean allowModification(Player player) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public ItemStack getItem() {
            return placeholder;
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }

    private static class EquipmentSlot extends Slot {

        private final ResourceLocation noItemIcon;

        EquipmentSlot(Container inventory, int index, int x, int y, ResourceLocation noItemIcon) {
            super(inventory, index, x, y);
            this.noItemIcon = noItemIcon;
        }

        @Nullable
        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, noItemIcon);
        }
    }

    private static class OffhandSlot extends EquipmentSlot {
        OffhandSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
        }
    }
}