package com.yigemingzi.hotbarslotplus.mixin;

import com.yigemingzi.hotbarslotplus.ExtraHotbarLayout;
import com.yigemingzi.hotbarslotplus.ExtraHotbarRenderer;
import com.yigemingzi.hotbarslotplus.HotbarSlotPlusConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
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
    private static final int HOTBAR_SLOT_PLUS_RECIPE_BOOK_WIDTH = 147;
    private static final int HOTBAR_SLOT_PLUS_RECIPE_BOOK_HEIGHT = 166;
    private static final int HOTBAR_SLOT_PLUS_RECIPE_BOOK_TAB_WIDTH = 32;
    private static final int HOTBAR_SLOT_PLUS_RECIPE_BOOK_LEFT_OFFSET = 86;
    private static final int HOTBAR_SLOT_PLUS_WARNING_WIDTH = 74;
    private static final int HOTBAR_SLOT_PLUS_WARNING_HEIGHT = 24;
    private static final int HOTBAR_SLOT_PLUS_WARNING_BACKGROUND = 0xAA220000;
    private static final int HOTBAR_SLOT_PLUS_WARNING_COLOR = 0xFFFF3030;
    private static final int HOTBAR_SLOT_PLUS_WARNING_TEXT_COLOR = 0xFFFF7070;

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
        int[] occupiedBounds = hotbarSlotPlus$occupiedBounds();
        int right = occupiedBounds[2] + HOTBAR_SLOT_PLUS_PANEL_GAP;
        int left = occupiedBounds[0] - ExtraHotbarLayout.ROW_WIDTH - HOTBAR_SLOT_PLUS_PANEL_GAP;
        int centeredTop = hotbarSlotPlus$clampPanelTop(occupiedBounds[1] + (occupiedBounds[3] - occupiedBounds[1] - panelHeight) / 2, panelHeight);
        boolean preferRight = HotbarSlotPlusConfig.get().inventoryPanelOnRight();
        int[] preferred = hotbarSlotPlus$validPlacement(preferRight ? right : left, centeredTop, panelHeight);
        if (preferred != null) {
            return preferred;
        }

        int[] fallback = hotbarSlotPlus$validPlacement(preferRight ? left : right, centeredTop, panelHeight);
        if (fallback != null) {
            return fallback;
        }

        int centeredLeft = hotbarSlotPlus$clampPanelLeft(occupiedBounds[0] + (occupiedBounds[2] - occupiedBounds[0] - ExtraHotbarLayout.ROW_WIDTH) / 2);
        int[] below = hotbarSlotPlus$validPlacement(centeredLeft, occupiedBounds[3] + HOTBAR_SLOT_PLUS_PANEL_GAP, panelHeight);
        if (below != null) {
            return below;
        }

        return hotbarSlotPlus$validPlacement(centeredLeft, occupiedBounds[1] - panelHeight - HOTBAR_SLOT_PLUS_PANEL_GAP, panelHeight);
    }

    private boolean hotbarSlotPlus$shouldShowExtraSlotsPanel() {
        return HotbarSlotPlusConfig.get().storageMode() == HotbarSlotPlusConfig.StorageMode.DEDICATED_SLOTS;
    }

    private int[] hotbarSlotPlus$validPlacement(int left, int top, int panelHeight) {
        if (left < HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || top < HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || left + ExtraHotbarLayout.ROW_WIDTH > this.width - HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || top + panelHeight > this.height - HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || hotbarSlotPlus$intersectsBlockedArea(left, top, ExtraHotbarLayout.ROW_WIDTH, panelHeight)) {
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

    private boolean hotbarSlotPlus$intersectsBlockedArea(int left, int top, int width, int height) {
        if (hotbarSlotPlus$intersects(left, top, width, height, this.x, this.y, this.backgroundWidth, this.backgroundHeight)) {
            return true;
        }

        int[] recipeBookBounds = hotbarSlotPlus$recipeBookBounds();
        return recipeBookBounds != null
                && hotbarSlotPlus$intersects(
                left,
                top,
                width,
                height,
                recipeBookBounds[0],
                recipeBookBounds[1],
                recipeBookBounds[2] - recipeBookBounds[0],
                recipeBookBounds[3] - recipeBookBounds[1]
        );
    }

    private boolean hotbarSlotPlus$intersects(int left, int top, int width, int height, int blockedLeft, int blockedTop, int blockedWidth, int blockedHeight) {
        return left < blockedLeft + blockedWidth
                && left + width > blockedLeft
                && top < blockedTop + blockedHeight
                && top + height > blockedTop;
    }

    private int[] hotbarSlotPlus$occupiedBounds() {
        int left = this.x;
        int top = this.y;
        int right = this.x + this.backgroundWidth;
        int bottom = this.y + this.backgroundHeight;

        int[] recipeBookBounds = hotbarSlotPlus$recipeBookBounds();
        if (recipeBookBounds != null) {
            left = Math.min(left, recipeBookBounds[0]);
            top = Math.min(top, recipeBookBounds[1]);
            right = Math.max(right, recipeBookBounds[2]);
            bottom = Math.max(bottom, recipeBookBounds[3]);
        }

        return new int[]{left, top, right, bottom};
    }

    private int[] hotbarSlotPlus$recipeBookBounds() {
        RecipeBookWidget recipeBook = ((InventoryScreen) (Object) this).getRecipeBookWidget();
        if (recipeBook == null || !recipeBook.isOpen()) {
            return null;
        }

        int centeredInventoryLeft = (this.width - this.backgroundWidth) / 2;
        boolean recipeBookShiftedInventory = this.x > centeredInventoryLeft + HOTBAR_SLOT_PLUS_RECIPE_BOOK_LEFT_OFFSET / 2;
        int leftOffset = recipeBookShiftedInventory ? HOTBAR_SLOT_PLUS_RECIPE_BOOK_LEFT_OFFSET : 0;
        int bookLeft = (this.width - HOTBAR_SLOT_PLUS_RECIPE_BOOK_WIDTH) / 2 - leftOffset - HOTBAR_SLOT_PLUS_RECIPE_BOOK_TAB_WIDTH;
        int bookTop = (this.height - HOTBAR_SLOT_PLUS_RECIPE_BOOK_HEIGHT) / 2;
        return new int[]{
                bookLeft,
                bookTop,
                bookLeft + HOTBAR_SLOT_PLUS_RECIPE_BOOK_TAB_WIDTH + HOTBAR_SLOT_PLUS_RECIPE_BOOK_WIDTH,
                bookTop + HOTBAR_SLOT_PLUS_RECIPE_BOOK_HEIGHT
        };
    }

    private void hotbarSlotPlus$renderNoSpaceWarning(DrawContext context) {
        int[] occupiedBounds = hotbarSlotPlus$occupiedBounds();
        int centeredTop = hotbarSlotPlus$clampWarningTop(occupiedBounds[1] + (occupiedBounds[3] - occupiedBounds[1] - HOTBAR_SLOT_PLUS_WARNING_HEIGHT) / 2);
        boolean preferRight = HotbarSlotPlusConfig.get().inventoryPanelOnRight();
        int[] placement = hotbarSlotPlus$warningPlacement(preferRight ? occupiedBounds[2] + 2 : occupiedBounds[0] - HOTBAR_SLOT_PLUS_WARNING_WIDTH - 2, centeredTop);
        if (placement == null) {
            placement = hotbarSlotPlus$warningPlacement(preferRight ? occupiedBounds[0] - HOTBAR_SLOT_PLUS_WARNING_WIDTH - 2 : occupiedBounds[2] + 2, centeredTop);
        }
        if (placement == null) {
            placement = hotbarSlotPlus$warningPlacement(hotbarSlotPlus$clampWarningLeft(occupiedBounds[0] + (occupiedBounds[2] - occupiedBounds[0] - HOTBAR_SLOT_PLUS_WARNING_WIDTH) / 2), occupiedBounds[3] + 2);
        }
        if (placement == null) {
            placement = hotbarSlotPlus$warningPlacement(hotbarSlotPlus$clampWarningLeft(occupiedBounds[0] + (occupiedBounds[2] - occupiedBounds[0] - HOTBAR_SLOT_PLUS_WARNING_WIDTH) / 2), occupiedBounds[1] - HOTBAR_SLOT_PLUS_WARNING_HEIGHT - 2);
        }

        int left = placement == null ? hotbarSlotPlus$clampWarningLeft(this.width / 2 - HOTBAR_SLOT_PLUS_WARNING_WIDTH / 2) : placement[0];
        int top = placement == null ? hotbarSlotPlus$clampWarningTop(HOTBAR_SLOT_PLUS_SCREEN_MARGIN) : placement[1];
        context.fill(left, top, left + HOTBAR_SLOT_PLUS_WARNING_WIDTH, top + HOTBAR_SLOT_PLUS_WARNING_HEIGHT, HOTBAR_SLOT_PLUS_WARNING_BACKGROUND);
        context.fill(left, top, left + HOTBAR_SLOT_PLUS_WARNING_WIDTH, top + 2, HOTBAR_SLOT_PLUS_WARNING_COLOR);
        context.fill(left, top + HOTBAR_SLOT_PLUS_WARNING_HEIGHT - 2, left + HOTBAR_SLOT_PLUS_WARNING_WIDTH, top + HOTBAR_SLOT_PLUS_WARNING_HEIGHT, HOTBAR_SLOT_PLUS_WARNING_COLOR);
        context.fill(left, top, left + 2, top + HOTBAR_SLOT_PLUS_WARNING_HEIGHT, HOTBAR_SLOT_PLUS_WARNING_COLOR);
        context.fill(left + HOTBAR_SLOT_PLUS_WARNING_WIDTH - 2, top, left + HOTBAR_SLOT_PLUS_WARNING_WIDTH, top + HOTBAR_SLOT_PLUS_WARNING_HEIGHT, HOTBAR_SLOT_PLUS_WARNING_COLOR);

        Text text = Text.translatable("text.hotbar-slot-plus.inventory.no_space");
        int textLeft = left + (HOTBAR_SLOT_PLUS_WARNING_WIDTH - this.textRenderer.getWidth(text)) / 2;
        int textTop = top + (HOTBAR_SLOT_PLUS_WARNING_HEIGHT - this.textRenderer.fontHeight) / 2;
        context.drawTextWithShadow(this.textRenderer, text, textLeft, textTop, HOTBAR_SLOT_PLUS_WARNING_TEXT_COLOR);
    }

    private int[] hotbarSlotPlus$warningPlacement(int left, int top) {
        if (left < HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || top < HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || left + HOTBAR_SLOT_PLUS_WARNING_WIDTH > this.width - HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || top + HOTBAR_SLOT_PLUS_WARNING_HEIGHT > this.height - HOTBAR_SLOT_PLUS_SCREEN_MARGIN
                || hotbarSlotPlus$intersectsBlockedArea(left, top, HOTBAR_SLOT_PLUS_WARNING_WIDTH, HOTBAR_SLOT_PLUS_WARNING_HEIGHT)) {
            return null;
        }

        return new int[]{left, top};
    }

    private int hotbarSlotPlus$clampWarningLeft(int left) {
        return Math.max(
                HOTBAR_SLOT_PLUS_SCREEN_MARGIN,
                Math.min(left, this.width - HOTBAR_SLOT_PLUS_WARNING_WIDTH - HOTBAR_SLOT_PLUS_SCREEN_MARGIN)
        );
    }

    private int hotbarSlotPlus$clampWarningTop(int top) {
        return Math.max(
                HOTBAR_SLOT_PLUS_SCREEN_MARGIN,
                Math.min(top, this.height - HOTBAR_SLOT_PLUS_WARNING_HEIGHT - HOTBAR_SLOT_PLUS_SCREEN_MARGIN)
        );
    }
}
