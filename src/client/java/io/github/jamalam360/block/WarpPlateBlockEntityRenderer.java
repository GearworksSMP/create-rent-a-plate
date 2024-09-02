package io.github.jamalam360.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class WarpPlateBlockEntityRenderer implements BlockEntityRenderer<WarpPlateBlockEntity> {
	public WarpPlateBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public boolean shouldRender(WarpPlateBlockEntity blockEntity, Vec3 cameraPos) {
		return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos) && !blockEntity.getWarpTitle().isBlank();
	}

	@Override
	public void render(WarpPlateBlockEntity blockEntity, float partialTick, PoseStack stack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		Font font = Minecraft.getInstance().font;
		Player player = Minecraft.getInstance().player;
		stack.pushPose();
		stack.translate(0.5, 1.65, 0.5);
		float yaw = (float) (-Mth.atan2(blockEntity.getBlockPos().getZ() - player.getPosition(partialTick).z + 0.5, blockEntity.getBlockPos().getX() - player.getPosition(partialTick).x + 0.5) - Math.PI / 2);
		stack.mulPose(Axis.YP.rotation(yaw));
		stack.scale(0.01F, -0.01F, 0.01F);
		stack.scale(2, 2, 2);
		font.drawInBatch(blockEntity.getWarpTitle(), (float) -font.width(blockEntity.getWarpTitle()) / 2, 0, 0xFFFFFF, true, stack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, packedLight);
		stack.popPose();
	}
}
