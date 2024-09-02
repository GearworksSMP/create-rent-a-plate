package io.github.jamalam360;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class WarpPlatesConfig {
	public static final WarpPlatesConfig INSTANCE = new WarpPlatesConfig(FabricLoader.getInstance().getConfigDir().resolve("warp_plates.properties").toFile());
	public static final String RENT_ITEM = "rent_item";
	public static final String RENT_PRICE = "rent_price";
	private final Properties properties;

	public WarpPlatesConfig(File file) {
		this.properties = new Properties();
		
		this.properties.setProperty(RENT_ITEM, "minecraft:emerald");
		this.properties.setProperty(RENT_PRICE, "1");

		try {
			if (file.exists()) {
				this.properties.load(new FileReader(file));
			} else {
				file.createNewFile();
				this.properties.store(new FileWriter(file), "Warp Plates Config");
			}
		} catch (IOException e) {
			WarpPlates.LOGGER.error("Failed to create config file", e);
		}
		
		if (this.getRentPrice() > this.getRentItem().getMaxStackSize()) {
			throw new IllegalStateException("Rent price cannot be greater than the max stack size of the rent item");
		}
	}
	
	public void set(String key, String value) {
		this.properties.setProperty(key, value);
	}

	public Item getRentItem() {
		return BuiltInRegistries.ITEM.get(new ResourceLocation(this.properties.getProperty(RENT_ITEM, "minecraft:emerald")));
	}
	
	public int getRentPrice() {
		return Integer.parseInt(this.properties.getProperty(RENT_PRICE, "1"));
	}
	
	public FriendlyByteBuf toSyncPacket() {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeInt(this.properties.size());
		this.properties.forEach((key, value) -> {
			buf.writeUtf((String) key);
			buf.writeUtf((String) value);
		});
		return buf;
	}
}
