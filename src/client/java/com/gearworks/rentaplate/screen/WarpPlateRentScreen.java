package com.gearworks.rentaplate.screen;

import com.gearworks.rentaplate.WarpPlates;
import com.gearworks.rentaplate.WarpPlatesConfig;
import com.gearworks.rentaplate.menu.WarpPlateRentMenu;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("DataFlowIssue")
public class WarpPlateRentScreen extends AbstractContainerScreen<WarpPlateRentMenu> {
	private static final ResourceLocation SCREEN_LOCATION = WarpPlates.id("textures/gui/container/rent_screen.png");
	private static final ItemStack RENT_ITEM = new ItemStack(WarpPlatesConfig.INSTANCE.getRentItem(), WarpPlatesConfig.INSTANCE.getRentPrice());
	private static final int ROW_ONE_X_OFFSET = 11;
	private static final int EDIT_BOX_Y_OFFSET = 24;
	private static final int EDIT_BOX_WIDTH = 154;
	private static final int EDIT_BOX_HEIGHT = 16;
	private static final int EDIT_BOX_BACKGROUND_X_OFFSET = 8;
	private static final int EDIT_BOX_BACKGROUND_Y_OFFSET = 19;
	private static final int EDIT_BOX_BACKGROUND_WIDTH = 161;
	private static final int EDIT_BOX_BACKGROUND_HEIGHT = 17;
	private static final int EXPIRY_TEXT_Y_OFFSET = 16;
	private static final int BUTTON_X_OFFSET = 69;
	private static final int BUTTON_Y_OFFSET = 48;
	private static final int BUTTON_WIDTH = 100;
	private static final int BUTTON_HEIGHT = 18;
	private static final int ITEM_X_OFFSET = 15;
	private Button rentButton;
	@Nullable
	private EditBox warpTitle;

	public WarpPlateRentScreen(WarpPlateRentMenu menu, Inventory playerInventory, Component ignoredTitle) {
		super(menu, playerInventory, Component.translatable("text.warp_plates.screen.title"));
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;

		if (this.menu.getExpiryTime() == -1L) {
			this.warpTitle = new EditBox(this.font, i + ROW_ONE_X_OFFSET, j + EDIT_BOX_Y_OFFSET, EDIT_BOX_WIDTH, EDIT_BOX_HEIGHT, Component.translatable("text.warp_plates.screen.edit_box"));
			this.warpTitle.setCanLoseFocus(false);
			this.warpTitle.setTextColor(-1);
			this.warpTitle.setBordered(false);
			this.warpTitle.setMaxLength(50);
			this.warpTitle.setValue(this.menu.getWarpTitle());
			this.warpTitle.setHint(Component.translatable("text.warp_plates.screen.edit_box"));
			this.addRenderableWidget(this.warpTitle);
			this.setInitialFocus(this.warpTitle);
		}

		this.rentButton = this.addRenderableWidget(Button.builder(this.menu.getExpiryTime() == -1L ? Component.translatable("text.warp_plates.screen.rent") : Component.translatable("text.warp_plates.screen.renew"), (button) -> {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			if (this.warpTitle != null) {
				buf.writeBoolean(false);
				buf.writeUtf(this.warpTitle.getValue());
			} else {
				buf.writeBoolean(true);
			}

			ClientPlayNetworking.send(WarpPlates.RENT_SCREEN_RENT_PACKET, buf);
			this.minecraft.player.closeContainer();
		}).pos(i + BUTTON_X_OFFSET, j + BUTTON_Y_OFFSET).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());
	}

	private Component getExpiryText() {
		long time = this.menu.getExpiryTime() - System.currentTimeMillis();
		if (time < 0) {
			this.minecraft.player.closeContainer();
			return Component.empty();
		}

		String dur = DurationFormatUtils.formatDuration(time, "d'd' H'h' m'm' s's'");
		return Component.translatable("text.warp_plates.screen.expiry_time", dur);
	}


	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		String string = this.warpTitle == null ? "" : this.warpTitle.getValue();
		this.init(minecraft, width, height);

		if (this.warpTitle != null) {
			this.warpTitle.setValue(string);
		}
	}

	@Override
	protected void containerTick() {
		super.containerTick();

		if (this.warpTitle != null) {
			this.rentButton.active = this.menu.hasValidPayment() && !this.warpTitle.getValue().trim().isEmpty();
			this.warpTitle.tick();
		} else {
			this.rentButton.active = this.menu.hasValidPayment() && this.menu.getExpiryTime() - System.currentTimeMillis() < WarpPlatesConfig.INSTANCE.getRentRenewalTime();
		}
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		guiGraphics.blit(SCREEN_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);

		if (this.warpTitle != null) {
			guiGraphics.blit(SCREEN_LOCATION, i + EDIT_BOX_BACKGROUND_X_OFFSET, j + EDIT_BOX_BACKGROUND_Y_OFFSET, 0, this.imageHeight, EDIT_BOX_BACKGROUND_WIDTH, EDIT_BOX_BACKGROUND_HEIGHT);
		} else {
			int y = j + EXPIRY_TEXT_Y_OFFSET;
			for (FormattedCharSequence text : font.split(this.getExpiryText(), EDIT_BOX_WIDTH)) {
				guiGraphics.drawString(this.font, text, i + this.imageWidth / 2 - this.font.width(text) / 2, y, 4210752, false);
				y += this.font.lineHeight;
			}
		}

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
		guiGraphics.renderItem(RENT_ITEM, i + ITEM_X_OFFSET, j + BUTTON_Y_OFFSET);
		guiGraphics.renderItemDecorations(this.font, RENT_ITEM, i + ITEM_X_OFFSET, j + BUTTON_Y_OFFSET);
		guiGraphics.pose().popPose();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			this.minecraft.player.closeContainer();
		}

		if (this.warpTitle != null) {
			return this.warpTitle.keyPressed(keyCode, scanCode, modifiers) || this.warpTitle.canConsumeInput() || super.keyPressed(keyCode, scanCode, modifiers);
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}
}
