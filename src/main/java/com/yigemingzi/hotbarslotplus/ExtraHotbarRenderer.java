package com.yigemingzi.hotbarslotplus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public final class ExtraHotbarRenderer {
    private static final int SLOT_BACKGROUND = 0xA0101010;
    private static final int SLOT_BORDER = 0xFF8A8A8A;
    private static final int SLOT_BORDER_SOFT = 0x663A3A3A;
    private static final int PANEL_BACKGROUND = 0x5A000000;

    private ExtraHotbarRenderer() {
    }

    public static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        int rows = HotbarSlotPlusConfig.get().hudRows();
        if (rows <= 1) {
            return;
        }

        int centerX = context.getScaledWindowWidth() / 2;
        int left = centerX - ExtraHotbarLayout.ROW_WIDTH / 2;
        int vanillaHotbarTop = context.getScaledWindowHeight() - 22;

        for (int row = 1; row < rows; row++) {
            int logicalRow = HotbarScrollController.logicalRowForVisibleOffset(row);
            int top = vanillaHotbarTop - row * (ExtraHotbarLayout.ROW_HEIGHT + ExtraHotbarLayout.ROW_GAP);
            renderRow(context, client.player.getInventory(), dedicatedInventory(client), logicalRow, left, top, false);
        }
    }

    public static void renderInventoryPanel(DrawContext context, PlayerInventory inventory, Inventory dedicatedInventory, int left, int top, int rows) {
        int panelWidth = ExtraHotbarLayout.ROW_WIDTH + 8;
        int panelHeight = rows * (ExtraHotbarLayout.ROW_HEIGHT + ExtraHotbarLayout.ROW_GAP) - ExtraHotbarLayout.ROW_GAP + 8;
        context.fill(left - 4, top - 4, left - 4 + panelWidth, top - 4 + panelHeight, PANEL_BACKGROUND);

        for (int row = 0; row < rows; row++) {
            int logicalRow = HotbarScrollController.logicalRowForVisibleOffset(row, rows);
            int rowTop = top + row * (ExtraHotbarLayout.ROW_HEIGHT + ExtraHotbarLayout.ROW_GAP);
            renderRow(context, inventory, dedicatedInventory, logicalRow, left, rowTop, true);
        }
    }

    private static void renderRow(DrawContext context, PlayerInventory inventory, Inventory dedicatedInventory, int row, int left, int top, boolean includeHotbarSelection) {
        MinecraftClient client = MinecraftClient.getInstance();
        for (int column = 0; column < 9; column++) {
            int slotLeft = left + column * ExtraHotbarLayout.SLOT_SIZE;
            ItemStack stack = stackFor(row, column, inventory, dedicatedInventory);
            boolean selected = includeHotbarSelection && row == 0 && inventory.selectedSlot == column;

            drawSlotFrame(context, slotLeft, top, selected);
            if (!stack.isEmpty()) {
                context.drawItem(stack, slotLeft + 1, top + 1);
                context.drawItemInSlot(client.textRenderer, stack, slotLeft + 1, top + 1);
            }
        }
    }

    private static void drawSlotFrame(DrawContext context, int left, int top, boolean selected) {
        int border = selected ? 0xFFFFFFFF : SLOT_BORDER;
        context.fill(left, top, left + ExtraHotbarLayout.SLOT_SIZE, top + ExtraHotbarLayout.SLOT_SIZE, SLOT_BACKGROUND);
        context.fill(left, top, left + ExtraHotbarLayout.SLOT_SIZE, top + 1, border);
        context.fill(left, top + ExtraHotbarLayout.SLOT_SIZE - 1, left + ExtraHotbarLayout.SLOT_SIZE, top + ExtraHotbarLayout.SLOT_SIZE, SLOT_BORDER_SOFT);
        context.fill(left, top, left + 1, top + ExtraHotbarLayout.SLOT_SIZE, border);
        context.fill(left + ExtraHotbarLayout.SLOT_SIZE - 1, top, left + ExtraHotbarLayout.SLOT_SIZE, top + ExtraHotbarLayout.SLOT_SIZE, SLOT_BORDER_SOFT);
    }

    private static ItemStack stackFor(int row, int column, PlayerInventory inventory, Inventory dedicatedInventory) {
        if (row == 0 || HotbarSlotPlusConfig.get().storageMode() == HotbarSlotPlusConfig.StorageMode.INVENTORY_ROWS) {
            return inventory.getStack(ExtraHotbarLayout.playerInventoryIndex(row, column));
        }

        if (dedicatedInventory == null) {
            return ItemStack.EMPTY;
        }

        int index = ExtraHotbarLayout.dedicatedInventoryIndex(row, column);
        if (index < 0 || index >= dedicatedInventory.size()) {
            return ItemStack.EMPTY;
        }

        return dedicatedInventory.getStack(index);
    }

    private static Inventory dedicatedInventory(MinecraftClient client) {
        if (client.player instanceof ExtraHotbarInventoryHolder holder) {
            return holder.hotbarSlotPlus$getExtraHotbarInventory();
        }

        return null;
    }
}
