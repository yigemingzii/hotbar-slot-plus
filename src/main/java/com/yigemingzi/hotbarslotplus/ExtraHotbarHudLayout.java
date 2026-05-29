package com.yigemingzi.hotbarslotplus;

import net.minecraft.client.gui.DrawContext;

public final class ExtraHotbarHudLayout {
    public static final int HOTBAR_WIDTH = 182;
    public static final int HOTBAR_HALF_WIDTH = 91;
    public static final int HOTBAR_HEIGHT = 22;
    public static final int SLOT_PITCH = 20;
    public static final int SLOT_SIZE = 20;
    public static final int ITEM_OFFSET = 3;
    public static final int ROW_PITCH = HOTBAR_HEIGHT;
    public static final int ROW_NUMBER_GAP = 8;

    private ExtraHotbarHudLayout() {
    }

    public static int visibleRows() {
        HotbarSlotPlusConfig config = HotbarSlotPlusConfig.get();
        return Math.max(1, Math.min(config.hudRows(), config.effectiveTotalRows()));
    }

    public static int bottomHudOffset() {
        return Math.max(0, visibleRows() - 1) * ROW_PITCH;
    }

    public static int left(DrawContext context) {
        return context.getScaledWindowWidth() / 2 - HOTBAR_HALF_WIDTH;
    }

    public static int rowTop(DrawContext context, int visibleOffset) {
        return context.getScaledWindowHeight() - HOTBAR_HEIGHT - visibleOffset * ROW_PITCH;
    }

    public static int slotLeft(int rowLeft, int column) {
        return rowLeft + ITEM_OFFSET + column * SLOT_PITCH;
    }
}
