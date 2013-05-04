package de.doridian.yiffbukkit.advanced.packetlistener;

import net.minecraft.server.v1_5_R3.Packet;
import org.bukkit.entity.Player;

interface YBPacketListenerInt {
	public boolean onOutgoingPacket(Player ply, int packetID, Packet packet);
	public boolean onIncomingPacket(Player ply, int packetID, Packet packet);
}
