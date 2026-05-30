package com.yigemingzi.hotbarslotplus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public final class HotbarScrollController {
    private static int activeRow;
    private static int pageStartRow;
    private static HotbarSlotPlusConfig.StorageMode activeStorageMode = HotbarSlotPlusConfig.StorageMode.INVENTORY_ROWS;

    private HotbarScrollController() {
    }

    public static boolean handleHotbarScroll(PlayerInventory inventory, double amount) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null || client.currentScreen != null) {
            return false;
        }

        int step = scrollStep(amount);
        if (step == 0) {
            return false;
        }

        HotbarSlotPlusConfig config = HotbarSlotPlusConfig.get();
        int totalRows = config.effectiveTotalRows();
        int visibleRows = Math.min(config.hudRows(), totalRows);
        normalizeStorageMode(client, inventory);
        if (totalRows <= 1 || visibleRows <= 0) {
            resetToBaseHotbar(client, inventory);
            return false;
        }

        if (isAltDown(client)) {
            changePage(client, inventory, step, totalRows, visibleRows);
            return true;
        }

        normalizeState(client, inventory, totalRows, visibleRows);
        int localRow = activeRow - pageStartRow;
        int visibleSlots = visibleRows * 9;
        int selectedIndex = localRow * 9 + inventory.selectedSlot;
        int nextIndex = Math.floorMod(selectedIndex + step, visibleSlots);
        int nextRow = pageStartRow + nextIndex / 9;
        int nextSlot = nextIndex % 9;

        setActiveRow(client, inventory, nextRow);
        inventory.selectedSlot = nextSlot;
        return true;
    }

    public static int logicalRowForVisibleOffset(int visibleOffset) {
        return logicalRowForVisibleOffset(visibleOffset, HotbarSlotPlusConfig.get().hudRows());
    }

    public static int logicalRowForVisibleOffset(int visibleOffset, int visibleRowsHint) {
        HotbarSlotPlusConfig config = HotbarSlotPlusConfig.get();
        int totalRows = config.effectiveTotalRows();
        int visibleRows = clampedVisibleRows(visibleRowsHint, totalRows);
        normalizeStateWithCurrentClient(totalRows, visibleRows);
        return Math.min(totalRows - 1, pageStartRow + visibleOffset);
    }

    public static int[] visibleRowsInDisplayOrder(int visibleRowsHint) {
        HotbarSlotPlusConfig config = HotbarSlotPlusConfig.get();
        int totalRows = config.effectiveTotalRows();
        int visibleRows = clampedVisibleRows(visibleRowsHint, totalRows);
        normalizeStateWithCurrentClient(totalRows, visibleRows);

        int[] rows = new int[visibleRows];
        for (int index = 0; index < visibleRows; index++) {
            rows[index] = Math.min(totalRows - 1, pageStartRow + index);
        }
        return rows;
    }

    public static int activeRow() {
        int totalRows = HotbarSlotPlusConfig.get().effectiveTotalRows();
        return Math.max(0, Math.min(activeRow, totalRows - 1));
    }

    public static int prepareForVanillaPick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            return -1;
        }

        if (HotbarSlotPlusConfig.get().storageMode() != HotbarSlotPlusConfig.StorageMode.INVENTORY_ROWS || activeRow == 0) {
            return -1;
        }

        int rowToRestore = activeRow;
        resetToBaseHotbar(client, client.player.getInventory());
        return rowToRestore;
    }

    public static void restoreAfterVanillaPick(MinecraftClient client, int rowToRestore) {
        if (rowToRestore <= 0 || client.player == null || client.interactionManager == null) {
            return;
        }

        HotbarSlotPlusConfig config = HotbarSlotPlusConfig.get();
        if (config.storageMode() != HotbarSlotPlusConfig.StorageMode.INVENTORY_ROWS || rowToRestore >= config.effectiveTotalRows()) {
            return;
        }

        setActiveRow(client, client.player.getInventory(), rowToRestore);
    }

    public static int actualStorageRowForLogicalRow(int logicalRow) {
        int active = activeRow();
        if (logicalRow == active) {
            return 0;
        }

        if (logicalRow == 0 && active != 0) {
            return active;
        }

        return logicalRow;
    }

    private static void changePage(MinecraftClient client, PlayerInventory inventory, int step, int totalRows, int visibleRows) {
        if (totalRows <= visibleRows) {
            return;
        }

        normalizeState(client, inventory, totalRows, visibleRows);
        int localRow = activeRow - pageStartRow;
        int pageCount = Math.max(1, (int) Math.ceil(totalRows / (double) visibleRows));
        int page = pageStartRow / visibleRows;
        int nextPage = Math.floorMod(page + step, pageCount);
        pageStartRow = Math.min(nextPage * visibleRows, Math.max(0, totalRows - visibleRows));
        setActiveRow(client, inventory, Math.min(pageStartRow + localRow, totalRows - 1));
    }

    private static void setActiveRow(MinecraftClient client, PlayerInventory inventory, int nextRow) {
        if (nextRow == activeRow) {
            return;
        }

        if (activeRow != 0) {
            swapHotbarWithRow(client, activeRow);
        }

        if (nextRow != 0) {
            swapHotbarWithRow(client, nextRow);
        }

        activeRow = nextRow;
        activeStorageMode = HotbarSlotPlusConfig.get().storageMode();
        inventory.markDirty();
    }

    private static void swapHotbarWithRow(MinecraftClient client, int row) {
        swapHotbarWithRow(client, row, HotbarSlotPlusConfig.get().storageMode());
    }

    private static void swapHotbarWithRow(MinecraftClient client, int row, HotbarSlotPlusConfig.StorageMode mode) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }

        if (mode == HotbarSlotPlusConfig.StorageMode.INVENTORY_ROWS && client.interactionManager.hasCreativeInventory()) {
            swapInventoryRowsInCreative(client, client.player.getInventory(), row);
            return;
        }

        for (int column = 0; column < 9; column++) {
            int slotId = ExtraHotbarLayout.inventoryScreenSlotId(mode, row, column);
            client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, slotId, column, SlotActionType.SWAP, client.player);
        }
    }

    private static void swapInventoryRowsInCreative(MinecraftClient client, PlayerInventory inventory, int row) {
        for (int column = 0; column < 9; column++) {
            int baseIndex = ExtraHotbarLayout.playerInventoryIndex(0, column);
            int rowIndex = ExtraHotbarLayout.playerInventoryIndex(row, column);
            ItemStack baseStack = inventory.getStack(baseIndex);
            ItemStack rowStack = inventory.getStack(rowIndex);
            inventory.setStack(baseIndex, rowStack);
            inventory.setStack(rowIndex, baseStack);
            client.interactionManager.clickCreativeStack(rowStack, 36 + column);
            client.interactionManager.clickCreativeStack(baseStack, rowIndex);
        }
    }

    private static void normalizeState(MinecraftClient client, PlayerInventory inventory, int totalRows, int visibleRows) {
        if (activeRow >= totalRows) {
            resetToBaseHotbar(client, inventory);
        }

        pageStartRow = Math.max(0, Math.min(pageStartRow, Math.max(0, totalRows - visibleRows)));
        if (activeRow < pageStartRow || activeRow >= pageStartRow + visibleRows) {
            activeRow = pageStartRow;
        }
    }

    private static void normalizeStorageMode(MinecraftClient client, PlayerInventory inventory) {
        if (activeStorageMode != HotbarSlotPlusConfig.get().storageMode()) {
            resetToBaseHotbar(client, inventory);
        }
    }

    private static void normalizeStateWithCurrentClient(int totalRows, int visibleRows) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerInventory inventory = client.player == null ? null : client.player.getInventory();
        if (inventory != null) {
            normalizeStorageMode(client, inventory);
        }
        normalizeState(client, inventory, totalRows, visibleRows);
    }

    private static void resetToBaseHotbar(MinecraftClient client, PlayerInventory inventory) {
        if (activeRow != 0 && client != null && inventory != null) {
            swapHotbarWithRow(client, activeRow, activeStorageMode);
        }

        activeRow = 0;
        pageStartRow = 0;
        activeStorageMode = HotbarSlotPlusConfig.get().storageMode();
        if (inventory != null) {
            inventory.markDirty();
        }
    }

    private static int scrollStep(double amount) {
        return -((int) Math.signum(amount));
    }

    private static int clampedVisibleRows(int visibleRowsHint, int totalRows) {
        return Math.max(1, Math.min(visibleRowsHint, totalRows));
    }

    private static boolean isAltDown(MinecraftClient client) {
        long handle = client.getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
