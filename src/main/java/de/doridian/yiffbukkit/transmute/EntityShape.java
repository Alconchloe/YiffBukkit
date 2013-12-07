package de.doridian.yiffbukkit.transmute;

import com.sk89q.worldedit.blocks.BlockType;
import de.doridian.yiffbukkit.main.YiffBukkitCommandException;
import de.doridian.yiffbukkitsplit.YiffBukkit;
import de.doridian.yiffbukkitsplit.util.PlayerHelper;
import net.minecraft.server.v1_7_R1.MathHelper;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.Packet18ArmAnimation;
import net.minecraft.server.v1_7_R1.Packet28EntityVelocity;
import net.minecraft.server.v1_7_R1.Packet30Entity;
import net.minecraft.server.v1_7_R1.Packet34EntityTeleport;
import net.minecraft.server.v1_7_R1.Packet38EntityStatus;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class EntityShape extends Shape {
	protected static double[] yOffsets = new double[1024];
	protected static float[] yawOffsets = new float[1024];

	protected int mobType;
	private Map<String, ShapeAction> actions;

	protected float yawOffset = 0;
	protected double yOffset = 0;
	protected boolean dropping = false;

	public EntityShape(Transmute transmute, Entity entity, int mobType) {
		super(transmute, entity);

		this.mobType = mobType;
		actions = ShapeActions.get(mobType);

		yOffset = yOffsets[mobType];
		yawOffset = yawOffsets[mobType];

		if (!(entity instanceof CraftEntity))
			return;

		try {
			Class<? extends net.minecraft.server.v1_6_R2.Entity> entityClass = ((CraftEntity) entity).getHandle().getClass();
			int entityMobType = MyEntityTypes.classToId(entityClass);

			yOffset -= yOffsets[entityMobType];
			yawOffset -= yawOffsets[entityMobType];
		}
		catch (EntityTypeNotFoundException e) {
		}

		yOffset += 0.015625D;
	}

	@Override
	public void createTransmutedEntity() {
		sendPacketToPlayersAround(transmute.ignorePacket(createSpawnPacket()));

		// TODO: send datawatcher to players around

		if (entity instanceof Player) {
			try {
				String typeName = MyEntityTypes.classToTypeName(MyEntityTypes.idToClass(mobType));
				YiffBukkit.instance.playerHelper.sendYiffcraftClientCommand((Player) entity, 't', typeName+"|"+yawOffset+"|"+yOffset);
			}
			catch (EntityTypeNotFoundException e) {
			}
		}
	}

	@Override
	public void createTransmutedEntity(Player forPlayer) {
		PlayerHelper.sendPacketToPlayer(forPlayer, transmute.ignorePacket(createSpawnPacket()));
		// TODO: send datawatcher to player
	}

	protected abstract Packet createSpawnPacket();

	private static final Pattern commandPattern = Pattern.compile("^([^ ]+) (.+)?$");

	@Override
	public void runAction(CommandSender commandSender, String action) throws YiffBukkitCommandException {
		final Matcher matcher = commandPattern.matcher(action);

		final String actionName;
		final String argStr;
		final String[] args;
		if (matcher.matches()) {
			actionName = matcher.group(1);
			argStr = matcher.group(2);
			args = argStr.split(" +");
		}
		else {
			actionName = action.trim();
			argStr = "";
			args = new String[0];
		}

		runAction(commandSender, actionName, args, argStr);
	}

	protected void runAction(CommandSender commandSender, final String actionName, final String[] args, final String argStr) throws YiffBukkitCommandException {
		if (actions == null)
			throw new YiffBukkitCommandException("No actions defined for your current shape.");

		ShapeAction mobAction = actions.get(actionName);
		if (mobAction == null) {
			mobAction = actions.get("help");
			if (mobAction == null)
				throw new YiffBukkitCommandException("No action named '"+actionName+"' defined for your current shape.");

			mobAction.run(this, commandSender, new String[] { "" }, "");
			return;
		}

		mobAction.run(this, commandSender, args, argStr);
	}

	@Override
	public boolean onOutgoingPacket(Player ply, int packetID, Packet packet) {
		if (ply == entity)
			return true;

		switch (packetID) {
		case 18:
			return ((Packet18ArmAnimation) packet).b == 2; // v1_6_R2

		case 22:
			return false; // will be overridden in MobShape

		//case 30:
		//case 31:
		case 32:
		case 33:
			Packet30Entity p30 = (Packet30Entity) packet;
			p30.e += (byte) ((int) (yawOffset * 256.0F / 360.0F)); // v1_6_R2

			return true;

		case 34:
			Packet34EntityTeleport p34 = (Packet34EntityTeleport) packet;
			final net.minecraft.server.v1_6_R2.Entity notchEntity = ((CraftEntity) entity).getHandle();
			p34.c = MathHelper.floor((notchEntity.locY+yOffset) * 32.0D); // v1_6_R2
			p34.e = (byte) ((int) ((notchEntity.yaw+yawOffset) * 256.0F / 360.0F)); // v1_6_R2
			//p34.c += (int)(yOffset * 32.0);
			//p34.e += (byte) ((int) (yawOffset * 256.0F / 360.0F));

			return true;

		default:
			return true;
		}
	}

	@Override
	public void tick() {
		if (!dropping)
			return;

		final net.minecraft.server.v1_6_R2.Entity notchEntity = ((CraftEntity) entity).getHandle();
		if (yOffset == 0) {
			if (Math.IEEEremainder(notchEntity.locY, 1.0) < 0.00001) {
				final Block block = entity.getWorld().getBlockAt(Location.locToBlock(notchEntity.locX), Location.locToBlock(notchEntity.locY)-1, Location.locToBlock(notchEntity.locZ));
				if (!BlockType.canPassThrough(block.getTypeId(), block.getData()))
					return;
			}
		}

		sendPacketToPlayersAround(new Packet34EntityTeleport(notchEntity));
		sendPacketToPlayersAround(new Packet28EntityVelocity(entityId, notchEntity.motX, notchEntity.motY, notchEntity.motZ));
	}

	public double getYOffset() {
		return yOffset;
	}

	public void sendEntityStatus(byte status) {
		sendPacketToPlayersAround(new Packet38EntityStatus(entityId, status));
		sendYCData(ShapeYCData.ENTITY_STATUS, status);
	}
}
