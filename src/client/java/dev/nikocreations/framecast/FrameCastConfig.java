package dev.nikocreations.framecast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

final class FrameCastConfig {
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("framecast.json");

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    static Data data = new Data();

    private FrameCastConfig() {
    }

    static void load() {
        try {
            Files.createDirectories(FILE.getParent());

            if (Files.notExists(FILE)) {
                data = new Data();
                save();
                return;
            }

            try (Reader reader =
                         Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                Data loaded = GSON.fromJson(reader, Data.class);
                data = loaded == null ? new Data() : loaded;
            }

            if (data.path == null) data.path = "";
            if (data.imagePath == null) {
                data.imagePath = "";
            }
            if (data.type == null || data.type.isBlank()) {
                data.type = "auto";
            }
            if (data.ffmpegPath == null) data.ffmpegPath = "";
            if (data.ffprobePath == null) data.ffprobePath = "";

        } catch (Exception exception) {
            data = new Data();
            FrameCastClient.LOGGER.error(
                    "Could not load FrameCast configuration", exception
            );
        }
    }

    static void save() {
        try {
            Files.createDirectories(FILE.getParent());

            try (Writer writer =
                         Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException exception) {
            FrameCastClient.LOGGER.error(
                    "Could not save FrameCast configuration", exception
            );
        }
    }

    static void clearScreen() {
        data.screen = null;
        save();
    }

    static Path resolveMediaPath() {
        String configured = data.path;

        // Backwards compatibility with the original image-only configuration.
        if (configured == null || configured.isBlank()) {
            configured = data.imagePath;
        }

        if (configured == null || configured.isBlank()) {
            configured = "~/Pictures/framecast.png";
        }

        configured = expandHome(configured.trim());

        Path path = Path.of(configured);

        if (!path.isAbsolute()) {
            path = FabricLoader.getInstance().getGameDir().resolve(path);
        }

        return path.normalize();
    }

    static String resolvedType() {
        String configured = data.type == null
                ? "auto"
                : data.type.trim().toLowerCase(Locale.ROOT);

        if (configured.equals("image") || configured.equals("video")) {
            return configured;
        }

        String file = resolveMediaPath()
                .getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        if (file.endsWith(".mp4")
                || file.endsWith(".mov")
                || file.endsWith(".m4v")
                || file.endsWith(".mkv")
                || file.endsWith(".webm")
                || file.endsWith(".avi")
                || file.endsWith(".flv")
                || file.endsWith(".wmv")
                || file.endsWith(".mpeg")
                || file.endsWith(".mpg")
                || file.endsWith(".3gp")
                || file.endsWith(".ts")) {
            return "video";
        }

        return "image";
    }

    static String expandHome(String value) {
        if (value.equals("~")) {
            return System.getProperty("user.home");
        }

        if (value.startsWith("~/")) {
            return System.getProperty("user.home")
                    + value.substring(1);
        }

        return value;
    }

    static final class Data {
        String type = "auto";
        String path = "";

        // Kept for older FrameCast configurations.
        String imagePath = "~/Pictures/framecast.png";

        boolean loop = true;

        // Leave empty to let FrameCast search common install paths and PATH.
        String ffmpegPath = "";
        String ffprobePath = "";

        Screen screen;
    }

    static final class Screen {
        Point first;
        Point second;
        String facing;
        String dimension;

        Screen() {
        }

        Screen(
                BlockPos first,
                BlockPos second,
                Direction facing,
                String dimension
        ) {
            this.first = new Point(first);
            this.second = new Point(second);
            this.facing = facing.getSerializedName();
            this.dimension = dimension;
        }
    }

    static final class Point {
        int x;
        int y;
        int z;

        Point() {
        }

        Point(BlockPos pos) {
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
        }
    }
}
