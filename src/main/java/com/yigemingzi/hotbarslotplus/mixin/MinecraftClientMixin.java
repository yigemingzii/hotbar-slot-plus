package com.yigemingzi.hotbarslotplus.mixin;

import com.yigemingzi.hotbarslotplus.HotbarScrollController;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Unique
    private int hotbarSlotPlus$pickRestoreRow = -1;

    @Inject(method = "doItemPick", at = @At("HEAD"))
    private void hotbarSlotPlus$prepareVanillaPick(CallbackInfo ci) {
        hotbarSlotPlus$pickRestoreRow = HotbarScrollController.prepareForVanillaPick((MinecraftClient) (Object) this);
    }

    @Inject(method = "doItemPick", at = @At("RETURN"))
    private void hotbarSlotPlus$restoreAfterVanillaPick(CallbackInfo ci) {
        HotbarScrollController.restoreAfterVanillaPick((MinecraftClient) (Object) this, hotbarSlotPlus$pickRestoreRow);
        hotbarSlotPlus$pickRestoreRow = -1;
    }
}
