package com.yigemingzi.hotbarslotplus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public final class HotbarScrollController {
    private static int activeRow;
    private static int pageStartRow;

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
        if (totalRows <= 1 || visibleRows <= 0) {
            activeRow = 0;
            pageStartRow = 0;
            return false;
        }

        if (isAltDown(client)) {
            changePage(client, inventory, step, totalRows, visibleRows);
            return true;
        }

        normalizeState(totalRows, visibleRows);
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
        int visibleRows = Math.min(visibleRowsHint, totalRows);
        normalizeState(totalRows, visibleRows);
        return Math.min(totalRows - 1, pageStartRow + visibleOffset);
    }

    private static void changePage(MinecraftClient client, PlayerInventory inventory, int step, int totalRows, int visibleRows) {
        if (totalRows <= visibleRows) {
            return;
        }

        int pageCount = Math.max(1, (int) Math.ceil(totalRows / (double) visibleRows));
        int page = pageStartRow / visibleRows;
        int nextPage = Math.floorMod(page + step, pageCount);
        pageStartRow = Math.min(nextPage * visibleRows, Math.max(0, totalRows - visibleRows));
        setActiveRow(client, inventory, pageStartRow);
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
        inventory.markDirty();
    }

    private static void swapHotbarWithRow(MinecraftClient client, int row) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }

        HotbarSlotPlusConfig.StorageMode mode = HotbarSlotPlusConfig.get().storageMode();
        for (int column = 0; column < 9; column++) {
            int slotId = ExtraHotbarLayout.inventoryScreenSlotId(mode, row, column);
            client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, slotId, column, SlotActionType.SWAP, client.player);
        }
    }

    private static void normalizeState(int totalRows, int visibleRows) {
        activeRow = Math.max(0, Math.min(activeRow, totalRows - 1));
        pageStartRow = Math.max(0, Math.min(pageStartRow, Math.max(0, totalRows - visibleRows)));
        if (activeRow < pageStartRow || activeRow >= pageStartRow + visibleRows) {
            activeRow = pageStartRow;
        }
    }

    private static int scrollStep(double amount) {
        return -((int) Math.signum(amount));
    }

    private static boolean isAltDown(MinecraftClient client) {
        long handle = client.getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
