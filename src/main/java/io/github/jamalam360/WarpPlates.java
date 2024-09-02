package io.github.jamalam360;

import io.github.jamalam360.block.ReturnPlateBlock;
import io.github.jamalam360.block.ReturnPlateBlockEntity;
import io.github.jamalam360.block.WarpPlateBlock;
import io.github.jamalam360.block.WarpPlateBlockEntity;
import io.github.jamalam360.menu.WarpPlateRentMenu;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarpPlates implements ModInitializer {
	public static final String MOD_ID = "warp_plates";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Block WARP_PLATE_BLOCK = new WarpPlateBlock(Block.Properties.copy(Blocks.IRON_BLOCK).noLootTable().strength(-1.0F, 3600000.0F));
	@SuppressWarnings("DataFlowIssue")
	public static final BlockEntityType<WarpPlateBlockEntity> WARP_PLATE_BLOCK_ENTITY = BlockEntityType.Builder.of(WarpPlateBlockEntity::new, WARP_PLATE_BLOCK).build(null);
	public static final BlockItem WARP_PLATE_BLOCK_ITEM = new BlockItem(WARP_PLATE_BLOCK, new Item.Properties());
	public static final Block RETURN_PLATE_BLOCK = new ReturnPlateBlock(Block.Properties.copy(Blocks.IRON_BLOCK));
	@SuppressWarnings("DataFlowIssue")
	public static final BlockEntityType<ReturnPlateBlockEntity> RETURN_PLATE_BLOCK_ENTITY = BlockEntityType.Builder.of(ReturnPlateBlockEntity::new, RETURN_PLATE_BLOCK).build(null);
	public static final BlockItem RETURN_PLATE_BLOCK_ITEM = new BlockItem(RETURN_PLATE_BLOCK, new Item.Properties());
	public static final MenuType<WarpPlateRentMenu> WARP_PLATE_RENT_MENU = new ExtendedScreenHandlerType<>(WarpPlateRentMenu::new);
	public static final ResourceLocation RENT_SCREEN_RENT_PACKET = id("rent_packet");
	public static final ResourceLocation PARTICLE_PACKET = id("particle_packet");
	public static final ResourceLocation CONFIG_SYNC_PACKET = id("config_packet");

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.BLOCK, id("warp_plate"), WARP_PLATE_BLOCK);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("warp_plate"), WARP_PLATE_BLOCK_ENTITY);
		Registry.register(BuiltInRegistries.ITEM, id("warp_plate"), WARP_PLATE_BLOCK_ITEM);
		Registry.register(BuiltInRegistries.BLOCK, id("return_plate"), RETURN_PLATE_BLOCK);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("return_plate"), RETURN_PLATE_BLOCK_ENTITY);
		Registry.register(BuiltInRegistries.ITEM, id("return_plate"), RETURN_PLATE_BLOCK_ITEM);
		Registry.register(BuiltInRegistries.MENU, id("warp_plate_rent"), WARP_PLATE_RENT_MENU);

		ServerPlayNetworking.registerGlobalReceiver(RENT_SCREEN_RENT_PACKET, (server, player, handler, buf, responseSender) -> {
			boolean alreadyRented = buf.readBoolean();
			String title;

			if (alreadyRented) {
				title = "";
			} else {
				title = buf.readUtf();
			}

			if (player.containerMenu instanceof WarpPlateRentMenu menu) {
				menu.rent(title);
			} else {
				LOGGER.warn("Player {} tried to rent a warp plate without being in the rent menu", player.getName().getString());
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(CONFIG_SYNC_PACKET, WarpPlatesConfig.INSTANCE.createSyncPacket()));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register((group) -> group.accept(WARP_PLATE_BLOCK_ITEM));

		LOGGER.info("Warp Plates initialized");
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
