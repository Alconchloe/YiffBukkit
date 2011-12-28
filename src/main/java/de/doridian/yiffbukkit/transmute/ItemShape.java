package de.doridian.yiffbukkit.transmute;


import net.minecraft.server.MathHelper;
import net.minecraft.server.Packet21PickupSpawn;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.server.Packet;
import org.bukkit.util.Vector;

public class ItemShape extends EntityShape {
	private int type = 81; // cactus
	private int data = 0;
	private int count = 1;

	public ItemShape(Transmute transmute, Entity entity, int mobType) {
		super(transmute, entity, mobType);

		//yOffset = 1.62;
		dropping = true;
	}

	@Override
	protected Packet createSpawnPacket() {
		Location location = entity.getLocation();

		final Packet21PickupSpawn p21 = new Packet21PickupSpawn();

		p21.a = entityId;

		p21.b = MathHelper.floor(location.getX() * 32.0D);
		p21.c = MathHelper.floor((location.getY()+yOffset) * 32.0D);
		p21.d = MathHelper.floor(location.getZ() * 32.0D);

		Vector velocity = entity.getVelocity();
		p21.e = (byte) ((int) (velocity.getX() * 128.0D));
		p21.f = (byte) ((int) (velocity.getY() * 128.0D));
		p21.g = (byte) ((int) (velocity.getZ() * 128.0D));

		p21.h = type;
		p21.i = count;
		p21.m = data;

		return p21;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;

		deleteEntity();
		createTransmutedEntity();
	}

	public int getDataValue() {
		return data;
	}

	public void setData(int data) {
		this.data = data;

		deleteEntity();
		createTransmutedEntity();
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;

		deleteEntity();
		createTransmutedEntity();
	}

	public void setType(int type, int data) {
		this.type = type;
		this.data = data;

		deleteEntity();
		createTransmutedEntity();
	}

	public void setType(int type, int data, int count) {
		this.type = type;
		this.data = data;
		this.count = count;

		deleteEntity();
		createTransmutedEntity();
	}
}
