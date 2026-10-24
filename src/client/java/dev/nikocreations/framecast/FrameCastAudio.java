package dev.nikocreations.framecast;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class FrameCastAudio {
    private static volatile Process process;
    private static volatile Thread thread;
    private static volatile boolean running;

    private FrameCastAudio() {}

    static synchronized void start(Path path, String ffmpeg) {
        stop();

        Thread worker = new Thread(() -> {
            Process child = null;
            SourceDataLine line = null;

            try {
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

                command.add("-i");
                command.add(path.toString());
                command.add("-vn");
                command.add("-sn");
                command.add("-dn");
                command.add("-ac");
                command.add("2");
                command.add("-ar");
                command.add("44100");
                command.add("-f");
                command.add("s16le");
                command.add("-acodec");
                command.add("pcm_s16le");
                command.add("pipe:1");

                child = new ProcessBuilder(command)
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .start();

                AudioFormat format = new AudioFormat(
                        44100.0f, 16, 2, true, false
                );
                DataLine.Info info = new DataLine.Info(
                        SourceDataLine.class, format
                );

                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                synchronized (FrameCastAudio.class) {
                    if (!running || thread != Thread.currentThread()) {
                        return;
                    }
                    process = child;
                }

                try (InputStream input = child.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int count;

                    while (running
                            && thread == Thread.currentThread()
                            && (count = input.read(buffer)) != -1) {
                        int offset = 0;
                        while (offset < count
                                && running
                                && thread == Thread.currentThread()) {
                            int written = line.write(
                                    buffer, offset, count - offset
                            );
                            if (written <= 0) break;
                            offset += written;
                        }
                    }
                }

                if (running && child.exitValue() != 0) {
                    FrameCastClient.LOGGER.warn(
                            "FrameCast audio decoder exited with code {}",
                            child.exitValue()
                    );
                }
            } catch (Exception exception) {
                if (running && thread == Thread.currentThread()) {
                    FrameCastClient.LOGGER.error(
                            "FrameCast audio playback failed", exception
                    );
                }
            } finally {
                if (line != null) {
                    try {
                        line.stop();
                        line.flush();
                        line.close();
                    } catch (Exception ignored) {}
                }
                if (child != null) child.destroyForcibly();

                synchronized (FrameCastAudio.class) {
                    if (process == child) process = null;
                    if (thread == Thread.currentThread()) {
                        thread = null;
                        running = false;
                    }
                }
            }
        }, "FrameCast-audio");

        thread = worker;
        running = true;
        worker.setDaemon(true);
        worker.start();
    }

    static synchronized void stop() {
        running = false;

        Process oldProcess = process;
        process = null;
        if (oldProcess != null) oldProcess.destroyForcibly();

        Thread oldThread = thread;
        thread = null;
        if (oldThread != null) oldThread.interrupt();
    }
}
