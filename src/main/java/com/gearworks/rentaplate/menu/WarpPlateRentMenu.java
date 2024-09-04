package com.gearworks.rentaplate.menu;

import com.gearworks.rentaplate.RentAPlate;
import com.gearworks.rentaplate.WarpPlatesConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WarpPlateRentMenu extends AbstractContainerMenu {
	private final PaymentSlot paymentSlot;
	private final ContainerLevelAccess levelAccess;
	private final RentCallback rentCallback;
	private String warpTitle = "";
	private long expiryTime = -2L;
	private final @Nullable UUID renter;

	public WarpPlateRentMenu(int containerId, Container container, FriendlyByteBuf buf) {
		this(containerId, container, ContainerLevelAccess.NULL, null, RentCallback.EMPTY);
		this.warpTitle = buf.readUtf();
		this.expiryTime = buf.readLong();
	}

	public WarpPlateRentMenu(int containerId, Container container, ContainerLevelAccess levelAccess, @Nullable UUID player, RentCallback rentCallback) {
		super(RentAPlate.WARP_PLATE_RENT_MENU, containerId);
		this.levelAccess = levelAccess;
		this.renter = player;
		this.rentCallback = rentCallback;
		this.paymentSlot = new PaymentSlot(new SimpleContainer(1), 0, 44, 49);
		this.addSlot(this.paymentSlot);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(container, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(container, i, 8 + i * 18, 142));
		}
	}

	@Override
	public void removed(Player player) {
		super.removed(player);

		if (!player.level().isClientSide) {
			ItemStack itemStack = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());

			if (!itemStack.isEmpty()) {
				player.getInventory().placeItemBackInInventory(itemStack, false);
			}
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.levelAccess, player, RentAPlate.WARP_PLATE_BLOCK) && (this.renter == null || this.renter.equals(player.getUUID()));
	}

	public boolean hasValidPayment() {
		Item item = WarpPlatesConfig.INSTANCE.getRentItem();
		int count = WarpPlatesConfig.INSTANCE.getRentPrice();
		ItemStack itemStack = this.paymentSlot.getItem();

		if (itemStack.isEmpty()) {
			return false;
		} else return itemStack.is(item) && itemStack.getCount() >= count;
	}

	public String getWarpTitle() {
		return this.warpTitle;
	}

	public long getExpiryTime() {
		return this.expiryTime;
	}

	public void rent(String warpTitle) {
		this.rentCallback.rent(warpTitle);
		int count = WarpPlatesConfig.INSTANCE.getRentPrice();
		ItemStack itemStack = this.paymentSlot.getItem();
		itemStack.shrink(count);
	}

	private static class PaymentSlot extends Slot {
		public PaymentSlot(Container container, int containerIndex, int xPosition, int yPosition) {
			super(container, containerIndex, xPosition, yPosition);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return stack.is(WarpPlatesConfig.INSTANCE.getRentItem());
		}
	}

	public interface RentCallback {
		RentCallback EMPTY = (s) -> {
		};

		void rent(String warpTitle);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (index == 0) {
				if (!this.moveItemStackTo(itemStack2, 1, 37, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemStack2, itemStack);
			} else if (!this.paymentSlot.hasItem() && this.paymentSlot.mayPlace(itemStack2) && itemStack2.getCount() == 1) {
				if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 1 && index < 28) {
				if (!this.moveItemStackTo(itemStack2, 28, 37, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 28 && index < 37) {
				if (!this.moveItemStackTo(itemStack2, 1, 28, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 1, 37, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemStack2);
		}

		return itemStack;
	}
}
