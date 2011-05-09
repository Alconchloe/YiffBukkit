package de.doridian.yiffbukkit.listeners;

import org.bukkit.entity.Player;

import de.doridian.yiffbukkit.YiffBukkit;
import de.doridian.yiffbukkit.util.PlayerHelper;
import de.doridian.yiffbukkit.util.PlayerHelper.WeatherType;

import net.minecraft.server.PacketListener;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet4UpdateTime;
import net.minecraft.server.Packet70Bed;

public class YiffBukkitPacketListener extends PacketListener {
	private final YiffBukkit plugin;
	private PlayerHelper playerHelper;

	public YiffBukkitPacketListener(YiffBukkit instance) {
		plugin = instance;
		playerHelper = plugin.playerHelper;

		PacketListener.addPacketListener(true, 4, this);
		PacketListener.addPacketListener(true, 70, this);
	}

	@Override
	public boolean onOutgoingPacket(Player ply, int packetID, Packet packet) {
		switch (packetID) {
		case 4:
			Packet4UpdateTime p4 = (Packet4UpdateTime)packet;
			Long frozenTime = playerHelper.frozenTimes.get(ply.getName());

			if (frozenTime != null) {
				p4.a = frozenTime;
			}
			else if (playerHelper.frozenServerTime != null) {
				p4.a = playerHelper.frozenServerTime;
			}
			break;
		case 70:
			Packet70Bed p70 = (Packet70Bed) packet;
			int reason = p70.b;
			final boolean rainState;
			if (reason == 1)
				rainState = true;
			else if (reason == 2)
				rainState = false;
			else
				return true;

			WeatherType frozenWeather = playerHelper.frozenWeathers.get(ply.getName());

			if (frozenWeather != null) {
				final boolean frozenRainState = frozenWeather != WeatherType.CLEAR;
				if (rainState != frozenRainState) {
					System.out.println("rejected packet70 with reason="+reason);
					return false;
				}
			}
			else if (playerHelper.frozenServerWeather != null) {
				final boolean frozenRainState = playerHelper.frozenServerWeather != WeatherType.CLEAR;
				if (rainState != frozenRainState) {
					System.out.println("rejected packet70 with reason="+reason);
					return false;
				}
			}

			break;
		}

		return true;
	}
}
