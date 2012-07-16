package de.doridian.yiffbukkit.spawning.fakeentity;

import de.doridian.yiffbukkitsplit.util.PlayerHelper;
import net.minecraft.server.MathHelper;
import net.minecraft.server.Packet23VehicleSpawn;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FakeVehicle extends FakeEntity {
	public int vehicleTypeId;

	public FakeVehicle(Location location, int vehicleType) {
		super(location);

		this.vehicleTypeId = vehicleType;
	}

	@Override
	public void send(Player player) {
		final Packet23VehicleSpawn p23 = new Packet23VehicleSpawn();

		final Location position = player.getLocation();

		p23.a = entityId;
		p23.b = MathHelper.floor(position.getX() * 32.0D);
		p23.c = MathHelper.floor(position.getY() * 32.0D);
		p23.d = MathHelper.floor(position.getZ() * 32.0D);
		p23.h = vehicleTypeId;
		p23.i = 0; //dataValue;

		PlayerHelper.sendPacketToPlayer(player, p23);
	}
}