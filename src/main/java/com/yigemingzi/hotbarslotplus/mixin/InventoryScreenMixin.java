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

    private InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hotbarSlotPlus$renderExtraHotbars(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!hotbarSlotPlus$shouldShowExtraSlotsPanel()) {
            return;
        }

        int rows = HotbarSlotPlusConfig.get().inventoryRows();
        ExtraHotbarRenderer.renderInventoryPanel(
                context,
                this.client.player.getInventory(),
                this.client.player instanceof com.yigemingzi.hotbarslotplus.ExtraHotbarInventoryHolder holder ? holder.hotbarSlotPlus$getExtraHotbarInventory() : null,
                hotbarSlotPlus$panelLeft(),
                hotbarSlotPlus$panelTop(rows),
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
        int panelLeft = hotbarSlotPlus$panelLeft();
        int panelTop = hotbarSlotPlus$panelTop(rows);
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

    private int hotbarSlotPlus$panelLeft() {
        int right = this.x + this.backgroundWidth + HOTBAR_SLOT_PLUS_PANEL_GAP;
        int left = this.x - ExtraHotbarLayout.ROW_WIDTH - HOTBAR_SLOT_PLUS_PANEL_GAP;
        boolean preferRight = HotbarSlotPlusConfig.get().inventoryPanelOnRight();
        int preferred = preferRight ? right : left;
        if (hotbarSlotPlus$fitsHorizontally(preferred)) {
            return preferred;
        }

        int fallback = preferRight ? left : right;
        if (hotbarSlotPlus$fitsHorizontally(fallback)) {
            return fallback;
        }

        return Math.max(
                HOTBAR_SLOT_PLUS_SCREEN_MARGIN,
                Math.min(preferred, this.width - ExtraHotbarLayout.ROW_WIDTH - HOTBAR_SLOT_PLUS_SCREEN_MARGIN)
        );
    }

    private int hotbarSlotPlus$panelTop(int rows) {
        int panelHeight = rows * ExtraHotbarLayout.ROW_HEIGHT;
        int preferred = this.y + (this.backgroundHeight - panelHeight) / 2;
        return Math.max(
                HOTBAR_SLOT_PLUS_SCREEN_MARGIN,
                Math.min(preferred, this.height - panelHeight - HOTBAR_SLOT_PLUS_SCREEN_MARGIN)
        );
    }

    private boolean hotbarSlotPlus$shouldShowExtraSlotsPanel() {
        return HotbarSlotPlusConfig.get().storageMode() == HotbarSlotPlusConfig.StorageMode.DEDICATED_SLOTS;
    }

    private boolean hotbarSlotPlus$fitsHorizontally(int left) {
        return left >= HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                && left + ExtraHotbarLayout.ROW_WIDTH <= this.width - HOTBAR_SLOT_PLUS_SCREEN_MARGIN;
    }
}
