package com.yigemingzi.hotbarslotplus.compat;

import com.yigemingzi.hotbarslotplus.HotbarSlotPlusConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class HotbarSlotPlusConfigScreen {
    private HotbarSlotPlusConfigScreen() {
    }

    public static Screen create(Screen parent) {
        HotbarSlotPlusConfig config = HotbarSlotPlusConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("text.hotbar-slot-plus.config.title"));
        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("text.hotbar-slot-plus.config.general"));

        general.addEntry(entries.startIntSlider(Text.translatable("text.hotbar-slot-plus.config.hud_rows"), config.hudRows(), 1, 4)
                .setDefaultValue(1)
                .setSaveConsumer(config::setHudRows)
                .build());
        general.addEntry(entries.startIntSlider(Text.translatable("text.hotbar-slot-plus.config.inventory_rows"), config.inventoryRows(), 1, 4)
                .setDefaultValue(1)
                .setSaveConsumer(config::setInventoryRows)
                .build());
        general.addEntry(entries.startIntSlider(Text.translatable("text.hotbar-slot-plus.config.total_rows"), config.totalRows(), 1, 8)
                .setDefaultValue(4)
                .setSaveConsumer(config::setTotalRows)
                .build());
        general.addEntry(entries.startEnumSelector(Text.translatable("text.hotbar-slot-plus.config.panel_side"), HotbarSlotPlusConfig.PanelSide.class, config.inventoryPanelSide())
                .setEnumNameProvider(side -> Text.translatable("text.hotbar-slot-plus.panel_side." + side.name().toLowerCase()))
                .setDefaultValue(HotbarSlotPlusConfig.PanelSide.RIGHT)
                .setSaveConsumer(config::setInventoryPanelSide)
                .build());
        general.addEntry(entries.startEnumSelector(Text.translatable("text.hotbar-slot-plus.config.storage_mode"), HotbarSlotPlusConfig.StorageMode.class, config.storageMode())
                .setEnumNameProvider(mode -> Text.translatable("text.hotbar-slot-plus.storage_mode." + mode.name().toLowerCase()))
                .setDefaultValue(HotbarSlotPlusConfig.StorageMode.INVENTORY_ROWS)
                .setSaveConsumer(config::setStorageMode)
                .build());

        builder.setSavingRunnable(HotbarSlotPlusConfig::save);
        return builder.build();
    }
}
