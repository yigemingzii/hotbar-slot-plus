package com.yigemingzi.hotbarslotplus;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public final class ExtraHotbarInventory implements Inventory {
    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(ExtraHotbarLayout.DEDICATED_SLOT_COUNT, ItemStack.EMPTY);

    public DefaultedList<ItemStack> stacks() {
        return stacks;
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack stack = Inventories.splitStack(stacks, slot, amount);
        if (!stack.isEmpty()) {
            markDirty();
        }

        return stack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = Inventories.removeStack(stacks, slot);
        if (!stack.isEmpty()) {
            markDirty();
        }

        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        stack.capCount(getMaxCount(stack));
        markDirty();
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        stacks.clear();
    }
}

