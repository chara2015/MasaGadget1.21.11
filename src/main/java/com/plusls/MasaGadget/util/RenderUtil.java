package com.plusls.MasaGadget.util;

import com.mojang.blaze3d.vertex.*;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class RenderUtil {
    public static void drawConnectLine(Vec3 pos1, Vec3 pos2, double expend, Color4f pos1Color, Color4f pos2Color, @NotNull Color4f lineColor) {
        RenderUtil.drawOutlineBox(pos1, expend, pos1Color);
        RenderUtil.drawLine(pos1, pos2, lineColor, 1.0f);
        RenderUtil.drawOutlineBox(pos2, expend, pos2Color);
    }

    public static void drawLine(Vec3 pos1, Vec3 pos2, Color4f color) {
        RenderUtil.drawLine(pos1, pos2, color, 1.0f);
    }

    public static void drawLine(Vec3 pos1, Vec3 pos2, Color4f color, float lineWidth) {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        pos1 = pos1.subtract(camPos);
        pos2 = pos2.subtract(camPos);
        RenderContext ctx = new RenderContext(
                () -> "masa_gadget:line",
                MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL
        );
        BufferBuilder builder = ctx.getBuilder();
        builder.addVertex((float) pos1.x(), (float) pos1.y(), (float) pos1.z()).setColor(color.r, color.g, color.b, color.a).setLineWidth(lineWidth);
        builder.addVertex((float) pos2.x(), (float) pos2.y(), (float) pos2.z()).setColor(color.r, color.g, color.b, color.a).setLineWidth(lineWidth);

        try {
            MeshData meshData = builder.build();

            if (meshData != null) {
                ctx.draw(meshData, false, true);
                meshData.close();
            }

            ctx.close();
        } catch (Exception ignored) {
        }
    }

    public static void drawOutlineBox(Vec3 pos, double expend, Color4f color) {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        pos = pos.subtract(camPos);
        RenderContext ctx = new RenderContext(
                () -> "masa_gadget:outline_box",
                MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL
        );
        BufferBuilder builder = ctx.getBuilder();
        RenderUtils.drawBoxAllEdgesBatchedLines(
                (float) (pos.x() - expend),
                (float) (pos.y() - expend),
                (float) (pos.z() - expend),
                (float) (pos.x() + expend),
                (float) (pos.y() + expend),
                (float) (pos.z() + expend),
                color,
                1.0f,
                builder
        );

        try {
            MeshData meshData = builder.build();

            if (meshData != null) {
                ctx.draw(meshData, false, true);
                meshData.close();
            }

            ctx.close();
        } catch (Exception ignored) {
        }
    }
}
