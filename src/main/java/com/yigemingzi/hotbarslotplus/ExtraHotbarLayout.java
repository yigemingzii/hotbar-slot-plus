package com.yigemingzi.hotbarslotplus;

public final class ExtraHotbarLayout {
    public static final int SLOT_SIZE = ExtraHotbarHudLayout.SLOT_PITCH;
    public static final int ROW_WIDTH = ExtraHotbarHudLayout.HOTBAR_WIDTH;
    public static final int ROW_HEIGHT = ExtraHotbarHudLayout.HOTBAR_HEIGHT;
    public static final int ROW_GAP = 0;
    public static final int DEDICATED_SLOT_START = 46;
    public static final int DEDICATED_ROW_COUNT = 7;
    public static final int DEDICATED_SLOT_COUNT = DEDICATED_ROW_COUNT * 9;

    private ExtraHotbarLayout() {
    }

    public static int playerInventoryIndex(int row, int column) {
        if (row == 0) {
            return column;
        }

        return 9 + (row - 1) * 9 + column;
    }

    public static int inventoryScreenSlotId(HotbarSlotPlusConfig.StorageMode mode, int row, int column) {
        if (row == 0) {
            return 36 + column;
        }

        if (mode == HotbarSlotPlusConfig.StorageMode.DEDICATED_SLOTS) {
            return DEDICATED_SLOT_START + (row - 1) * 9 + column;
        }

        return 9 + (row - 1) * 9 + column;
    }

    public static int dedicatedInventoryIndex(int row, int column) {
        return (row - 1) * 9 + column;
    }

    public static int columnAt(double mouseX, int left) {
        int relativeX = (int) Math.floor(mouseX) - left - ExtraHotbarHudLayout.ITEM_OFFSET;
        if (relativeX < 0 || relativeX >= SLOT_SIZE * 9) {
            return -1;
        }

        int column = relativeX / SLOT_SIZE;
        int columnX = relativeX - column * SLOT_SIZE;
        return column >= 0 && column < 9 && columnX < 16 ? column : -1;
    }

    public static int rowAt(double mouseY, int top, int rows) {
        int pitch = ROW_HEIGHT + ROW_GAP;
        int relativeY = (int) Math.floor(mouseY) - top;
        if (relativeY < 0 || relativeY >= rows * pitch - ROW_GAP) {
            return -1;
        }

        int row = relativeY / pitch;
        int rowY = relativeY - row * pitch;
        return row < rows && rowY < ROW_HEIGHT ? row : -1;
    }
}
