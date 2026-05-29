package com.yigemingzi.hotbarslotplus.mixin;

import com.yigemingzi.hotbarslotplus.ExtraHotbarLayout;
import com.yigemingzi.hotbarslotplus.ExtraHotbarRenderer;
import com.yigemingzi.hotbarslotplus.HotbarSlotPlusConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    private static final int HOTBAR_SLOT_PLUS_PANEL_GAP = 10;
    private static final int HOTBAR_SLOT_PLUS_SCREEN_MARGIN = 4;
    private static final int HOTBAR_SLOT_PLUS_WARNING_SIZE = 12;
    private static final int HOTBAR_SLOT_PLUS_WARNING_COLOR = 0xFFFF3030;

    private InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hotbarSlotPlus$renderExtraHotbars(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!hotbarSlotPlus$shouldShowExtraSlotsPanel()) {
            return;
        }

        int rows = HotbarSlotPlusConfig.get().inventoryRows();
        int[] placement = hotbarSlotPlus$panelPlacement(rows);
        if (placement == null) {
            hotbarSlotPlus$renderNoSpaceWarning(context);
            return;
        }

        ExtraHotbarRenderer.renderInventoryPanel(
                context,
                this.client.player.getInventory(),
                this.client.player instanceof com.yigemingzi.hotbarslotplus.ExtraHotbarInventoryHolder holder ? holder.hotbarSlotPlus$getExtraHotbarInventory() : null,
                placement[0],
                placement[1],
                rows
        );
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void hotbarSlotPlus$clickExtraHotbar(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.client == null || this.client.interactionManager == null || this.client.player == null) {
            return;
        }

        if (!hotbarSlotPlus$shouldShowExtraSlotsPanel()) {
            return;
        }

        int rows = HotbarSlotPlusConfig.get().inventoryRows();
        int[] placement = hotbarSlotPlus$panelPlacement(rows);
        if (placement == null) {
            return;
        }

        int panelLeft = placement[0];
        int panelTop = placement[1];
        if (!ExtraHotbarLayout.containsPanel(mouseX, mouseY, panelLeft, panelTop, rows)) {
            return;
        }

        int row = ExtraHotbarLayout.rowAt(mouseY, panelTop, rows);
        int column = ExtraHotbarLayout.columnAt(mouseX, panelLeft);
        if (row < 0 || column < 0 || (button != 0 && button != 1)) {
            cir.setReturnValue(true);
            return;
        }

        int logicalRow = com.yigemingzi.hotbarslotplus.HotbarScrollController.logicalRowForVisibleOffset(rows - 1 - row, rows);
        int storageRow = com.yigemingzi.hotbarslotplus.HotbarScrollController.actualStorageRowForLogicalRow(logicalRow);
        int slotId = ExtraHotbarLayout.inventoryScreenSlotId(HotbarSlotPlusConfig.get().storageMode(), storageRow, column);
        this.client.interactionManager.clickSlot(this.handler.syncId, slotId, button, SlotActionType.PICKUP, this.client.player);
        cir.setReturnValue(true);
    }

    private int[] hotbarSlotPlus$panelPlacement(int rows) {
        int panelHeight = rows * ExtraHotbarLayout.ROW_HEIGHT;
        int right = this.x + this.backgroundWidth + HOTBAR_SLOT_PLUS_PANEL_GAP;
        int left = this.x - ExtraHotbarLayout.ROW_WIDTH - HOTBAR_SLOT_PLUS_PANEL_GAP;
        int centeredTop = hotbarSlotPlus$clampPanelTop(this.y + (this.backgroundHeight - panelHeight) / 2, panelHeight);
        boolean preferRight = HotbarSlotPlusConfig.get().inventoryPanelOnRight();
        int[] preferred = hotbarSlotPlus$validPlacement(preferRight ? right : left, centeredTop, panelHeight);
        if (preferred != null) {
            return preferred;
        }

        int[] fallback = hotbarSlotPlus$validPlacement(preferRight ? left : right, centeredTop, panelHeight);
        if (fallback != null) {
            return fallback;
        }

        int centeredLeft = hotbarSlotPlus$clampPanelLeft(this.x + (this.backgroundWidth - ExtraHotbarLayout.ROW_WIDTH) / 2);
        int[] below = hotbarSlotPlus$validPlacement(centeredLeft, this.y + this.backgroundHeight + HOTBAR_SLOT_PLUS_PANEL_GAP, panelHeight);
        if (below != null) {
            return below;
        }

        return hotbarSlotPlus$validPlacement(centeredLeft, this.y - panelHeight - HOTBAR_SLOT_PLUS_PANEL_GAP, panelHeight);
    }

    private boolean hotbarSlotPlus$shouldShowExtraSlotsPanel() {
        return HotbarSlotPlusConfig.get().storageMode() == HotbarSlotPlusConfig.StorageMode.DEDICATED_SLOTS;
    }

    private int[] hotbarSlotPlus$validPlacement(int left, int top, int panelHeight) {
        if (left < HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || top < HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || left + ExtraHotbarLayout.ROW_WIDTH > this.width - HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || top + panelHeight > this.height - HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || hotbarSlotPlus$intersectsInventory(left, top, ExtraHotbarLayout.ROW_WIDTH, panelHeight)) {
            return null;
        }

        return new int[]{left, top};
    }

    private int hotbarSlotPlus$clampPanelLeft(int left) {
        return Math.max(
                HOTBAR_SLOT_PLUS_SCREEN_MARGIN,
                Math.min(left, this.width - ExtraHotbarLayout.ROW_WIDTH - HOTBAR_SLOT_PLUS_SCREEN_MARGIN)
        );
    }

    private int hotbarSlotPlus$clampPanelTop(int top, int panelHeight) {
        return Math.max(
                HOTBAR_SLOT_PLUS_SCREEN_MARGIN,
                Math.min(top, this.height - panelHeight - HOTBAR_SLOT_PLUS_SCREEN_MARGIN)
        );
    }

    private boolean hotbarSlotPlus$intersectsInventory(int left, int top, int width, int height) {
        return left < this.x + this.backgroundWidth
                && left + width > this.x
                && top < this.y + this.backgroundHeight
                && top + height > this.y;
    }

    private void hotbarSlotPlus$renderNoSpaceWarning(DrawContext context) {
        int centeredTop = hotbarSlotPlus$clampWarningTop(this.y + (this.backgroundHeight - HOTBAR_SLOT_PLUS_WARNING_SIZE) / 2);
        boolean preferRight = HotbarSlotPlusConfig.get().inventoryPanelOnRight();
        int[] placement = hotbarSlotPlus$warningPlacement(preferRight ? this.x + this.backgroundWidth + 2 : this.x - HOTBAR_SLOT_PLUS_WARNING_SIZE - 2, centeredTop);
        if (placement == null) {
            placement = hotbarSlotPlus$warningPlacement(preferRight ? this.x - HOTBAR_SLOT_PLUS_WARNING_SIZE - 2 : this.x + this.backgroundWidth + 2, centeredTop);
        }
        if (placement == null) {
            placement = hotbarSlotPlus$warningPlacement(hotbarSlotPlus$clampWarningLeft(this.x + (this.backgroundWidth - HOTBAR_SLOT_PLUS_WARNING_SIZE) / 2), this.y + this.backgroundHeight + 2);
        }
        if (placement == null) {
            placement = hotbarSlotPlus$warningPlacement(hotbarSlotPlus$clampWarningLeft(this.x + (this.backgroundWidth - HOTBAR_SLOT_PLUS_WARNING_SIZE) / 2), this.y - HOTBAR_SLOT_PLUS_WARNING_SIZE - 2);
        }

        int left = placement == null ? hotbarSlotPlus$clampWarningLeft(this.x + this.backgroundWidth - HOTBAR_SLOT_PLUS_WARNING_SIZE - HOTBAR_SLOT_PLUS_SCREEN_MARGIN) : placement[0];
        int top = placement == null ? hotbarSlotPlus$clampWarningTop(this.y + HOTBAR_SLOT_PLUS_SCREEN_MARGIN) : placement[1];
        context.fill(left, top, left + HOTBAR_SLOT_PLUS_WARNING_SIZE, top + 1, HOTBAR_SLOT_PLUS_WARNING_COLOR);
        context.fill(left, top + HOTBAR_SLOT_PLUS_WARNING_SIZE - 1, left + HOTBAR_SLOT_PLUS_WARNING_SIZE, top + HOTBAR_SLOT_PLUS_WARNING_SIZE, HOTBAR_SLOT_PLUS_WARNING_COLOR);
        context.fill(left, top, left + 1, top + HOTBAR_SLOT_PLUS_WARNING_SIZE, HOTBAR_SLOT_PLUS_WARNING_COLOR);
        context.fill(left + HOTBAR_SLOT_PLUS_WARNING_SIZE - 1, top, left + HOTBAR_SLOT_PLUS_WARNING_SIZE, top + HOTBAR_SLOT_PLUS_WARNING_SIZE, HOTBAR_SLOT_PLUS_WARNING_COLOR);
    }

    private int[] hotbarSlotPlus$warningPlacement(int left, int top) {
        if (left < HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || top < HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || left + HOTBAR_SLOT_PLUS_WARNING_SIZE > this.width - HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || top + HOTBAR_SLOT_PLUS_WARNING_SIZE > this.height - HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || hotbarSlotPlus$intersectsInventory(left, top, HOTBAR_SLOT_PLUS_WARNING_SIZE, HOTBAR_SLOT_PLUS_WARNING_SIZE)) {
            return null;
        }

        return new int[]{left, top};
    }

    private int hotbarSlotPlus$clampWarningLeft(int left) {
        return Math.max(
                HOTBAR_SLOT_PLUS_SCREEN_MARGIN,
                Math.min(left, this.width - HOTBAR_SLOT_PLUS_WARNING_SIZE - HOTBAR_SLOT_PLUS_SCREEN_MARGIN)
        );
    }

    private int hotbarSlotPlus$clampWarningTop(int top) {
        return Math.max(
                HOTBAR_SLOT_PLUS_SCREEN_MARGIN,
                Math.min(top, this.height - HOTBAR_SLOT_PLUS_WARNING_SIZE - HOTBAR_SLOT_PLUS_SCREEN_MARGIN)
        );
    }
}
