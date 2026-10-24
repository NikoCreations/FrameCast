package dev.nikocreations.framecast;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

final class FrameCastRenderer {
    private static volatile int brightness = 100;

    private FrameCastRenderer() {}

    static void setBrightness(int percentage) {
        brightness = Math.max(0, Math.min(100, percentage));
    }

    static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(FrameCastRenderer::render);
    }

    private static void render(LevelRenderContext context) {
        FrameCastConfig.Screen screen = FrameCastConfig.data.screen;
        Identifier texture = FrameCastMedia.textureId();

        if (screen == null || screen.first == null ||
                screen.second == null || texture == null) {
            return;
        }

        Direction facing = parseFacing(screen.facing);
        if (facing == null || facing.getAxis() == Direction.Axis.Y) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.gameRenderer == null ||
                minecraft.getCameraEntity() == null) {
            return;
        }

        if (screen.dimension != null &&
                !screen.dimension.isBlank() &&
                !screen.dimension.equals(
                        minecraft.level.dimension().toString())) {
            return;
        }

        Vec3 camera = context.levelState().cameraRenderState.pos;
        FrameCastConfig.Point a = screen.first;
        FrameCastConfig.Point b = screen.second;

        double minX = Math.min(a.x, b.x);
        double maxX = Math.max(a.x, b.x) + 1.0;
        double minY = Math.min(a.y, b.y);
        double maxY = Math.max(a.y, b.y) + 1.0;
        double minZ = Math.min(a.z, b.z);
        double maxZ = Math.max(a.z, b.z) + 1.0;

        PoseStack poses = context.poseStack();

        if (facing.getAxis() == Direction.Axis.Z) {
            float x0 = (float) (minX - camera.x);
            float x1 = (float) (maxX - camera.x);
            float y0 = (float) (minY - camera.y);
            float y1 = (float) (maxY - camera.y);
            float z = (float) (a.z +
                    (facing == Direction.SOUTH ? 1.01 : -0.01) -
                    camera.z);

            context.submitNodeCollector().submitCustomGeometry(
                    poses,
                    RenderTypes.entityCutout(texture),
                    (pose, buffer) -> {
                        if (facing == Direction.SOUTH) {
                            vertex(buffer, pose, x0, y0, z, 0, 1, 0, 0, 1);
                            vertex(buffer, pose, x1, y0, z, 1, 1, 0, 0, 1);
                            vertex(buffer, pose, x1, y1, z, 1, 0, 0, 0, 1);
                            vertex(buffer, pose, x0, y1, z, 0, 0, 0, 0, 1);
                        } else {
                            vertex(buffer, pose, x1, y0, z, 1, 1, 0, 0, -1);
                            vertex(buffer, pose, x0, y0, z, 0, 1, 0, 0, -1);
                            vertex(buffer, pose, x0, y1, z, 0, 0, 0, 0, -1);
                            vertex(buffer, pose, x1, y1, z, 1, 0, 0, 0, -1);
                        }
                    }
            );
        } else {
            float x = (float) (a.x +
                    (facing == Direction.EAST ? 1.01 : -0.01) -
                    camera.x);
            float y0 = (float) (minY - camera.y);
            float y1 = (float) (maxY - camera.y);
            float z0 = (float) (minZ - camera.z);
            float z1 = (float) (maxZ - camera.z);

            context.submitNodeCollector().submitCustomGeometry(
                    poses,
                    RenderTypes.entityCutout(texture),
                    (pose, buffer) -> {
                        if (facing == Direction.EAST) {
                            vertex(buffer, pose, x, y0, z1, 0, 1, 1, 0, 0);
                            vertex(buffer, pose, x, y0, z0, 1, 1, 1, 0, 0);
                            vertex(buffer, pose, x, y1, z0, 1, 0, 1, 0, 0);
                            vertex(buffer, pose, x, y1, z1, 0, 0, 1, 0, 0);
                        } else {
                            vertex(buffer, pose, x, y0, z0, 0, 1, -1, 0, 0);
                            vertex(buffer, pose, x, y0, z1, 1, 1, -1, 0, 0);
                            vertex(buffer, pose, x, y1, z1, 1, 0, -1, 0, 0);
                            vertex(buffer, pose, x, y1, z0, 0, 0, -1, 0, 0);
                        }
                    }
            );
        }
    }

    private static void vertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x, float y, float z,
            float u, float v,
            float nx, float ny, float nz
    ) {
        int intensity = (brightness * 255 + 50) / 100;

        buffer.addVertex(pose, x, y, z)
                .setColor(intensity, intensity, intensity, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0x00F000F0)
                .setNormal(pose, nx, ny, nz);
    }

    private static Direction parseFacing(String facing) {
        if (facing == null) {
            return null;
        }

        return switch (facing.toLowerCase(java.util.Locale.ROOT)) {
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "east" -> Direction.EAST;
            case "west" -> Direction.WEST;
            default -> null;
        };
    }
}
