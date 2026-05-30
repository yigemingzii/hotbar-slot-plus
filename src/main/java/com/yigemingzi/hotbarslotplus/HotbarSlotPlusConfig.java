package com.yigemingzi.hotbarslotplus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HotbarSlotPlusConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MIN_ROWS = 1;
    private static final int MAX_VISIBLE_ROWS = 4;
    private static final int MAX_TOTAL_ROWS = 8;
    private static HotbarSlotPlusConfig INSTANCE = new HotbarSlotPlusConfig();

    private int hudRows = 1;
    private int inventoryRows = 1;
    private int totalRows = 4;
    private PanelSide inventoryPanelSide = PanelSide.RIGHT;
    private StorageMode storageMode = StorageMode.INVENTORY_ROWS;

    public static HotbarSlotPlusConfig get() {
        return INSTANCE;
    }

    public static void load() {
        Path path = configPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                HotbarSlotPlusConfig loaded = GSON.fromJson(reader, HotbarSlotPlusConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    INSTANCE.clamp();
                }
            } catch (IOException ignored) {
                INSTANCE = new HotbarSlotPlusConfig();
            }
        }

        save();
    }

    public static void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException ignored) {
            // The mod can still render with in-memory defaults if the config file cannot be written.
        }
    }

    public int hudRows() {
        return hudRows;
    }

    public int inventoryRows() {
        return inventoryRows;
    }

    public int totalRows() {
        if (storageMode == StorageMode.INVENTORY_ROWS) {
            return 4;
        }

        return totalRows;
    }

    public int effectiveTotalRows() {
        if (storageMode == StorageMode.INVENTORY_ROWS) {
            return 4;
        }

        return totalRows;
    }

    public boolean inventoryPanelOnRight() {
        return inventoryPanelSide == PanelSide.RIGHT;
    }

    public PanelSide inventoryPanelSide() {
        return inventoryPanelSide;
    }

    public StorageMode storageMode() {
        return storageMode;
    }

    public void setHudRows(int rows) {
        hudRows = clampVisibleRows(rows);
        save();
    }

    public void setInventoryRows(int rows) {
        inventoryRows = clampVisibleRows(rows);
        save();
    }

    public void setTotalRows(int rows) {
        totalRows = storageMode == StorageMode.INVENTORY_ROWS ? 4 : clampTotalRows(rows);
        save();
    }

    public void setInventoryPanelSide(PanelSide side) {
        inventoryPanelSide = side == null ? PanelSide.RIGHT : side;
        save();
    }

    public void setStorageMode(StorageMode mode) {
        storageMode = mode == null ? StorageMode.INVENTORY_ROWS : mode;
        if (storageMode == StorageMode.INVENTORY_ROWS) {
            totalRows = 4;
        }
        save();
    }

    private void clamp() {
        hudRows = clampVisibleRows(hudRows);
        inventoryRows = clampVisibleRows(inventoryRows);
        totalRows = clampTotalRows(totalRows);
        if (inventoryPanelSide == null) {
            inventoryPanelSide = PanelSide.RIGHT;
        }
        if (storageMode == null) {
            storageMode = StorageMode.INVENTORY_ROWS;
        }
        if (storageMode == StorageMode.INVENTORY_ROWS) {
            totalRows = 4;
        }
    }

    private static int clampVisibleRows(int rows) {
        return Math.max(MIN_ROWS, Math.min(MAX_VISIBLE_ROWS, rows));
    }

    private static int clampTotalRows(int rows) {
        return Math.max(MIN_ROWS, Math.min(MAX_TOTAL_ROWS, rows));
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(HotbarSlotPlusClient.MOD_ID + ".json");
    }

    public enum PanelSide {
        LEFT,
        RIGHT
    }

    public enum StorageMode {
        INVENTORY_ROWS,
        DEDICATED_SLOTS
    }
}
