package org.savage.skyblock.nms;

import me.trent.WorldAPI;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.savage.skyblock.SkyBlock;
import org.savage.skyblock.Storage;
import org.savage.skyblock.island.Island;

import java.util.List;
import java.util.Map;

public class NMSHandler_v1_13_R2 extends NMSHandler {

    public void generate(String name){
        WorldAPI.generate(name);
    }

    @Override
    public String getVersion() {
        return "1_13_R2";
    }

    public void calculate(Chunk chunk, Island island) {
        List<Material> tileEntities = SkyBlock.getInstance().getReflectionManager().tileEntities;

        final CraftChunk craftChunk = (CraftChunk) chunk;

        final int minX = chunk.getX() << 4;
        final int minZ = chunk.getZ() << 4;
        final int maxX = minX | 15;
        final int maxY = chunk.getWorld().getMaxHeight();
        final int maxZ = minZ | 15;

        //for (int x = minX; x <= maxX; ++x) {
        //    for (int y = 0; y <= maxY; ++y) {
        //        for (int z = minZ; z <= maxZ; ++z) {
        //            try {
        //                org.bukkit.block.Block block = craftChunk.getBlock(x, y, z);
        //                if (block != null && !block.getType().equals(org.bukkit.Material.AIR)) {
        //                    if (!tileEntities.contains(block.getType())) {
        //                        String type = block.getType().name().toUpperCase();
        //                        if (SkyBlock.getInstance().getIslandUtils().hasWorth(type, false)) {
        //                            island.addBlockCount(type, false);
        //                        }
        //                    }
        //                }
        //            }catch(IllegalArgumentException e){
        //                continue;
        //            }
        //        }
        //    }
        //}

        for (final Map.Entry<BlockPosition, net.minecraft.server.v1_13_R2.TileEntity> entry : craftChunk.getHandle().tileEntities.entrySet()) {
            if (island.isBlockInIsland(entry.getKey().getX(), entry.getKey().getZ())) {
                final net.minecraft.server.v1_13_R2.TileEntity tileEntity = entry.getValue();

                String blockType;
                boolean isSpawner = false;


                if (tileEntity instanceof net.minecraft.server.v1_13_R2.TileEntityMobSpawner) {
                    blockType = ((net.minecraft.server.v1_13_R2.TileEntityMobSpawner) tileEntity).getSpawner().getMobName().toString().toUpperCase();
                    isSpawner = true;
                } else {
                    blockType = tileEntity.getBlock().getMaterial().toString().toUpperCase();
                }

                blockType = blockType.replace("MINECRAFT:", "");
               // System.out.print("\n\n type: "+blockType+", spawner: "+isSpawner+"\n\n");

                if (SkyBlock.getInstance().getIslandUtils().hasWorth(blockType, isSpawner)){
                    island.addBlockCount(blockType, isSpawner);
                  //  System.out.print("\n\n type: "+blockType+", spawner: "+isSpawner+"\n\n");
                }
            }
        }
    }

    @Override
    public void removeBlockSuperFast(int X, int Y, int Z, boolean applyPhysics) {
        Storage.getSkyBlockWorld().getBlockAt(X, Y, Z).setType(Material.AIR, applyPhysics);
    }

    @Override
    public void sendBorder(Player p, double x, double z, double radius) {
        final WorldBorder worldBorder = new WorldBorder();
        worldBorder.setCenter(x, z);
        worldBorder.setSize(radius * 2);
        worldBorder.setWarningDistance(0);
        final EntityPlayer entityPlayer = ((CraftPlayer) p).getHandle();
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE));
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER));
        entityPlayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS));
    }

    @Override
    public void sendTitle(Player p, String text, int in, int stay, int out, String type) {
        final PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.valueOf(type),
                IChatBaseComponent.ChatSerializer.a(ChatColor.translateAlternateColorCodes('&', "{\"text\":\"" + text + " \"}")), in, stay, out);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);
    }
}