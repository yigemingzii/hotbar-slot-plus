package com.yigemingzi.hotbarslotplus.mixin;

import com.yigemingzi.hotbarslotplus.ExtraHotbarHudLayout;
import com.yigemingzi.hotbarslotplus.ExtraHotbarRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.JumpingMount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void hotbarSlotPlus$renderExtraHotbarRows(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ExtraHotbarRenderer.renderHud(context, tickCounter);
        ci.cancel();
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"))
    private void hotbarSlotPlus$pushExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        hotbarSlotPlus$pushBottomHud(context);
    }

    @Inject(method = "renderExperienceBar", at = @At("RETURN"))
    private void hotbarSlotPlus$popExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        hotbarSlotPlus$popBottomHud(context);
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
    private void hotbarSlotPlus$pushExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        hotbarSlotPlus$pushBottomHud(context);
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
    private void hotbarSlotPlus$popExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        hotbarSlotPlus$popBottomHud(context);
    }

    @Inject(method = "renderMountJumpBar", at = @At("HEAD"))
    private void hotbarSlotPlus$pushMountJumpBar(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        hotbarSlotPlus$pushBottomHud(context);
    }

    @Inject(method = "renderMountJumpBar", at = @At("RETURN"))
    private void hotbarSlotPlus$popMountJumpBar(JumpingMount mount, DrawContext context, int x, CallbackInfo ci) {
        hotbarSlotPlus$popBottomHud(context);
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void hotbarSlotPlus$pushStatusBars(DrawContext context, CallbackInfo ci) {
        hotbarSlotPlus$pushBottomHud(context);
    }

    @Inject(method = "renderStatusBars", at = @At("RETURN"))
    private void hotbarSlotPlus$popStatusBars(DrawContext context, CallbackInfo ci) {
        hotbarSlotPlus$popBottomHud(context);
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"))
    private void hotbarSlotPlus$pushMountHealth(DrawContext context, CallbackInfo ci) {
        hotbarSlotPlus$pushBottomHud(context);
    }

    @Inject(method = "renderMountHealth", at = @At("RETURN"))
    private void hotbarSlotPlus$popMountHealth(DrawContext context, CallbackInfo ci) {
        hotbarSlotPlus$popBottomHud(context);
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"))
    private void hotbarSlotPlus$pushHeldItemTooltip(DrawContext context, CallbackInfo ci) {
        hotbarSlotPlus$pushBottomHud(context);
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("RETURN"))
    private void hotbarSlotPlus$popHeldItemTooltip(DrawContext context, CallbackInfo ci) {
        hotbarSlotPlus$popBottomHud(context);
    }

    @Unique
    private void hotbarSlotPlus$pushBottomHud(DrawContext context) {
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, -ExtraHotbarHudLayout.bottomHudOffset(), 0.0F);
    }

    @Unique
    private void hotbarSlotPlus$popBottomHud(DrawContext context) {
        context.getMatrices().pop();
    }
}
