package dev.nikocreations.framecast;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

final class FrameCastMedia {
    private static final int MAX_IMAGE_TEXTURE_DIMENSION = 4096;
    private static final int MAX_VIDEO_TEXTURE_DIMENSION = 960;

    private static DynamicTexture texture;
    private static Identifier textureId;

    private static int textureWidth;
    private static int textureHeight;

    private static volatile Process decoderProcess;
    private static volatile Thread decoderThread;

    private static final AtomicReference<byte[]> PENDING_FRAME =
            new AtomicReference<>();

    private FrameCastMedia() {
    }

    static Identifier textureId() {
        return textureId;
    }
    private static Identifier registerTexture(String name, DynamicTexture value) {
        Identifier id = Identifier.fromNamespaceAndPath("framecast", name);
        Minecraft.getInstance().getTextureManager().register(id, value);
        return id;
    }


    static void reload() {
        stop();

        Path path = FrameCastConfig.resolveMediaPath();

        if (!Files.isRegularFile(path)) {
            FrameCastClient.LOGGER.warn(
                    "FrameCast media file not found: {}", path
            );
            return;
        }

        try {
            if (FrameCastConfig.resolvedType().equals("video")) {
                startVideo(path);
            } else {
                loadImage(path);
            }
        } catch (Exception exception) {
            stopDecoder();
            destroyTexture();

            FrameCastClient.LOGGER.error(
                    "Could not load FrameCast media from {}", path, exception
            );
        }
    }

    static void stop() {
        FrameCastAudio.stop();
        stopDecoder();
        destroyTexture();
    }

    private static void destroyTexture() {
        Minecraft client = Minecraft.getInstance();

        if (textureId != null) {
            if (texture != null) texture.close();
        }

        textureId = null;
        texture = null;
        textureWidth = 0;
        textureHeight = 0;
    }

    private static void loadImage(Path path) throws IOException {
        NativeImage image = readScaledImage(path);

        textureWidth = image.getWidth();
        textureHeight = image.getHeight();

        texture = new DynamicTexture(() -> "FrameCast", image);

        textureId = registerTexture("framecast_image", texture);

        FrameCastClient.LOGGER.info(
                "FrameCast image loaded: {} (texture {}x{})",
                path, textureWidth, textureHeight
        );
    }

    /*
     * Reads large images with source subsampling instead of first allocating
     * a full-resolution raster. The file itself is never modified.
     */
    private static NativeImage readScaledImage(Path path) throws IOException {
        try (ImageInputStream input =
                     ImageIO.createImageInputStream(path.toFile())) {

            if (input == null) {
                throw new IOException("Could not open image: " + path);
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new IOException(
                        "Unsupported image format. Try PNG or JPEG: " + path
                );
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(input, false, true);

                int sourceWidth = reader.getWidth(0);
                int sourceHeight = reader.getHeight(0);

                if (sourceWidth <= 0 || sourceHeight <= 0) {
                    throw new IOException("Invalid image dimensions");
                }

                int largestDimension = Math.max(sourceWidth, sourceHeight);

                int sample = Math.max(
                        1,
                        (int) Math.ceil(
                                largestDimension
                                        / (double) MAX_IMAGE_TEXTURE_DIMENSION
                        )
                );

                ImageReadParam params = reader.getDefaultReadParam();

                if (sample > 1) {
                    params.setSourceSubsampling(sample, sample, 0, 0);
                }

                BufferedImage decoded = reader.read(0, params);

                if (decoded == null) {
                    throw new IOException("Image decoder returned no pixels");
                }

                int width = decoded.getWidth();
                int height = decoded.getHeight();

                NativeImage output = new NativeImage(width, height, false);

                try {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            output.setPixel(
                                    x,
                                    y,
                                    toNativeColor(decoded.getRGB(x, y))
                            );
                        }
                    }
                } catch (RuntimeException exception) {
                    output.close();
                    throw exception;
                } finally {
                    decoded.flush();
                }

                if (sample > 1) {
                    FrameCastClient.LOGGER.info(
                            "Large image {} ({}x{}) sampled by factor {} to {}x{}",
                            path, sourceWidth, sourceHeight, sample, width, height
                    );
                }

                return output;
            } finally {
                reader.dispose();
            }
        }
    }

    /*
     * Minecraft 26.3 NativeImage#setPixel expects ARGB.
     * BufferedImage.getRGB() already returns ARGB.
     */
    private static int toNativeColor(int argb) {
        return argb;
    }

    private static void startVideo(Path path) throws Exception {
        String ffprobe = findExecutable(
                FrameCastConfig.data.ffprobePath, "ffprobe"
        );

        String ffmpeg = findExecutable(
                FrameCastConfig.data.ffmpegPath, "ffmpeg"
        );

        int[] sourceDimensions = probeDimensions(ffprobe, path);

        int sourceWidth = sourceDimensions[0];
        int sourceHeight = sourceDimensions[1];

        double scale = Math.min(
                1.0,
                MAX_VIDEO_TEXTURE_DIMENSION
                        / (double) Math.max(sourceWidth, sourceHeight)
        );

        int width = Math.max(
                2,
                ((int) Math.round(sourceWidth * scale)) & ~1
        );

        int height = Math.max(
                2,
                ((int) Math.round(sourceHeight * scale)) & ~1
        );

        List<String> command = new ArrayList<>();

        command.add(ffmpeg);
        command.add("-hide_banner");
        command.add("-loglevel");
        command.add("error");
        command.add("-nostdin");

        if (FrameCastConfig.data.loop) {
            command.add("-stream_loop");
            command.add("-1");
        }

        command.add("-re");
        command.add("-i");
        command.add(path.toString());

        command.add("-an");
        command.add("-sn");
        command.add("-dn");
        command.add("-vf");
        command.add("scale=" + width + ":" + height + ":flags=fast_bilinear");
        command.add("-pix_fmt");
        command.add("rgba");
        command.add("-f");
        command.add("rawvideo");
        command.add("pipe:1");

        Process process = new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();

        decoderProcess = process;

        FrameCastAudio.start(
                path,
                findExecutable(FrameCastConfig.data.ffmpegPath, "ffmpeg")
        );

        NativeImage blank = new NativeImage(width, height, false);

        textureWidth = width;
        textureHeight = height;

        texture = new DynamicTexture(() -> "FrameCast", blank);

        textureId = registerTexture("framecast_video", texture);

        Thread thread = new Thread(
                () -> decodeVideo(process, width, height),
                "FrameCast-video-decoder"
        );

        thread.setDaemon(true);
        decoderThread = thread;
        thread.start();

        FrameCastClient.LOGGER.info(
                "FrameCast video started: {} ({}x{} output)",
                path, width, height
        );
    }

    private static int[] probeDimensions(
            String ffprobe,
            Path path
    ) throws Exception {
        Process process = new ProcessBuilder(
                ffprobe,
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height",
                "-of", "csv=s=x:p=0",
                path.toString()
        ).redirectError(ProcessBuilder.Redirect.DISCARD).start();

        byte[] output;

        try (InputStream stream = process.getInputStream()) {
            output = stream.readAllBytes();
        }

        if (!process.waitFor(15, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("ffprobe timed out");
        }

        if (process.exitValue() != 0) {
            throw new IOException(
                    "ffprobe could not read the video. Check its format and path."
            );
        }

        String text = new String(output, StandardCharsets.UTF_8).trim();

        for (String line : text.split("\\R")) {
            String[] dimensions = line.trim().split("x");

            if (dimensions.length == 2) {
                try {
                    int width = Integer.parseInt(dimensions[0].trim());
                    int height = Integer.parseInt(dimensions[1].trim());

                    if (width > 0 && height > 0) {
                        return new int[]{width, height};
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore non-dimension output lines.
                }
            }
        }

        throw new IOException(
                "Could not detect video dimensions. ffprobe output: " + text
        );
    }

    private static String findExecutable(String configured, String name) {
        if (configured != null && !configured.isBlank()) {
            return FrameCastConfig.expandHome(configured.trim());
        }

        String[] candidates = {
                "/opt/homebrew/bin/" + name,
                "/usr/local/bin/" + name,
                "/usr/bin/" + name
        };

        for (String candidate : candidates) {
            if (Files.isExecutable(Path.of(candidate))) {
                return candidate;
            }
        }

        // Fall back to PATH, which is useful on Linux and Windows setups.
        return name;
    }

    private static void decodeVideo(
            Process process,
            int width,
            int height
    ) {
        final int bytesPerFrame = width * height * 4;

        try (InputStream input = process.getInputStream()) {
            while (decoderProcess == process
                    && !Thread.currentThread().isInterrupted()) {

                byte[] frame = new byte[bytesPerFrame];
                int offset = 0;

                while (offset < bytesPerFrame && decoderProcess == process) {
                    int count = input.read(
                            frame, offset, bytesPerFrame - offset
                    );

                    if (count < 0) {
                        break;
                    }

                    if (count == 0) {
                        continue;
                    }

                    offset += count;
                }

                if (offset != bytesPerFrame) {
                    break;
                }

                // Keep the most recent frame if Minecraft cannot upload
                // every frame in time.
                PENDING_FRAME.set(frame);
            }
        } catch (IOException exception) {
            if (decoderProcess == process) {
                FrameCastClient.LOGGER.warn(
                        "FrameCast video decoder stopped", exception
                );
            }
        }
    }

    /*
     * Runs from the Minecraft client tick, where updating the GPU texture
     * is safe. Decoding itself happens on the background thread.
     */
    static void tick() {
        byte[] frame = PENDING_FRAME.getAndSet(null);

        DynamicTexture currentTexture = texture;
        if (frame == null || currentTexture == null) {
            return;
        }

        int width = textureWidth;
        int height = textureHeight;

        if (width <= 0 || height <= 0
                || (long) width * height * 4 != frame.length) {
            return;
        }

        NativeImage image = currentTexture.getPixels();
        if (image == null
                || image.getWidth() != width
                || image.getHeight() != height) {
            return;
        }

        int offset = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int red = frame[offset++] & 0xFF;
                int green = frame[offset++] & 0xFF;
                int blue = frame[offset++] & 0xFF;
                int alpha = frame[offset++] & 0xFF;

                int color = (alpha << 24)
                        | (red << 16)
                        | (green << 8)
                        | blue;

                image.setPixel(x, y, color);
            }
        }

        currentTexture.upload();
    }

    private static void stopDecoder() {
        Process process = decoderProcess;
        decoderProcess = null;

        PENDING_FRAME.set(null);

        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }

        Thread thread = decoderThread;
        decoderThread = null;

        if (thread != null) {
            thread.interrupt();
        }
    }
}
