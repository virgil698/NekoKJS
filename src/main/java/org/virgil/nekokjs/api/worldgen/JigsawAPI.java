package org.virgil.nekokjs.api.worldgen;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.*;
import java.util.logging.Logger;

/**
 * 拼图系统 API
 * 提供类似 Minecraft 拼图系统的结构生成功能
 */
public class JigsawAPI {
    
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Jigsaw");
    private static final Map<String, JigsawPool> pools = new HashMap<>();
    private static final Map<String, JigsawPiece> pieces = new HashMap<>();
    
    /**
     * 注册拼图池
     * 
     * @param poolId 池 ID
     * @param config 配置
     * @return 是否注册成功
     */
    public static boolean registerPool(String poolId, Map<String, Object> config) {
        try {
            JigsawPool pool = new JigsawPool(poolId);
            
            // 解析配置
            if (config.containsKey("pieces")) {
                Object piecesObj = config.get("pieces");
                if (piecesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> pieceIds = (List<String>) piecesObj;
                    for (String pieceId : pieceIds) {
                        pool.addPiece(pieceId);
                    }
                }
            }
            
            if (config.containsKey("fallback")) {
                pool.fallbackPool = config.get("fallback").toString();
            }
            
            pools.put(poolId, pool);
            LOGGER.info("Registered jigsaw pool: " + poolId);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register jigsaw pool: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 注册拼图片段
     * 
     * @param pieceId 片段 ID
     * @param config 配置
     * @return 是否注册成功
     */
    public static boolean registerPiece(String pieceId, Map<String, Object> config) {
        try {
            JigsawPiece piece = new JigsawPiece(pieceId);
            
            // 解析配置
            if (config.containsKey("size")) {
                Object sizeObj = config.get("size");
                if (sizeObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Number> size = (List<Number>) sizeObj;
                    if (size.size() >= 3) {
                        piece.sizeX = size.get(0).intValue();
                        piece.sizeY = size.get(1).intValue();
                        piece.sizeZ = size.get(2).intValue();
                    }
                }
            }
            
            if (config.containsKey("blocks")) {
                Object blocksObj = config.get("blocks");
                if (blocksObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> blocks = (List<Map<String, Object>>) blocksObj;
                    for (Map<String, Object> block : blocks) {
                        int x = ((Number) block.get("x")).intValue();
                        int y = ((Number) block.get("y")).intValue();
                        int z = ((Number) block.get("z")).intValue();
                        String material = block.get("block").toString();
                        piece.addBlock(x, y, z, material);
                    }
                }
            }
            
            if (config.containsKey("connectors")) {
                Object connectorsObj = config.get("connectors");
                if (connectorsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> connectors = (List<Map<String, Object>>) connectorsObj;
                    for (Map<String, Object> connector : connectors) {
                        int x = ((Number) connector.get("x")).intValue();
                        int y = ((Number) connector.get("y")).intValue();
                        int z = ((Number) connector.get("z")).intValue();
                        String targetPool = connector.get("targetPool").toString();
                        String direction = connector.getOrDefault("direction", "NORTH").toString();
                        piece.addConnector(x, y, z, targetPool, direction);
                    }
                }
            }
            
            pieces.put(pieceId, piece);
            LOGGER.info("Registered jigsaw piece: " + pieceId);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register jigsaw piece: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 生成拼图结构
     * 
     * @param world 世界
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param startPool 起始池 ID
     * @param maxDepth 最大深度
     * @return 是否生成成功
     */
    public static boolean generateStructure(Object world, int x, int y, int z, String startPool, int maxDepth) {
        try {
            if (!(world instanceof World)) {
                LOGGER.warning("Invalid world object");
                return false;
            }
            
            World bukkitWorld = (World) world;
            JigsawPool pool = pools.get(startPool);
            
            if (pool == null) {
                LOGGER.warning("Pool not found: " + startPool);
                return false;
            }
            
            // 生成结构
            Location startLocation = new Location(bukkitWorld, x, y, z);
            generateRecursive(bukkitWorld, startLocation, pool, 0, maxDepth, new HashSet<>());
            
            LOGGER.info("Generated jigsaw structure at " + x + ", " + y + ", " + z);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to generate jigsaw structure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 递归生成拼图结构
     */
    private static void generateRecursive(World world, Location location, JigsawPool pool, 
                                         int depth, int maxDepth, Set<String> placedPieces) {
        if (depth >= maxDepth) {
            return;
        }
        
        // 从池中随机选择一个片段
        JigsawPiece piece = pool.getRandomPiece();
        if (piece == null) {
            return;
        }
        
        // 放置片段
        piece.place(world, location);
        placedPieces.add(piece.id + "@" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
        
        // 处理连接器
        for (JigsawConnector connector : piece.connectors) {
            JigsawPool targetPool = pools.get(connector.targetPool);
            if (targetPool != null) {
                // 计算连接位置
                Location connectorLocation = location.clone().add(connector.x, connector.y, connector.z);
                
                // 根据方向调整位置
                Location nextLocation = getNextLocation(connectorLocation, connector.direction);
                
                // 检查是否已经放置过
                String locationKey = nextLocation.getBlockX() + "," + nextLocation.getBlockY() + "," + nextLocation.getBlockZ();
                if (!placedPieces.contains(locationKey)) {
                    generateRecursive(world, nextLocation, targetPool, depth + 1, maxDepth, placedPieces);
                }
            }
        }
    }
    
    /**
     * 根据方向获取下一个位置
     */
    private static Location getNextLocation(Location current, String direction) {
        Location next = current.clone();
        switch (direction.toUpperCase()) {
            case "NORTH":
                next.add(0, 0, -1);
                break;
            case "SOUTH":
                next.add(0, 0, 1);
                break;
            case "EAST":
                next.add(1, 0, 0);
                break;
            case "WEST":
                next.add(-1, 0, 0);
                break;
            case "UP":
                next.add(0, 1, 0);
                break;
            case "DOWN":
                next.add(0, -1, 0);
                break;
        }
        return next;
    }
    
    /**
     * 拼图池
     */
    public static class JigsawPool {
        private final String id;
        private final List<String> pieceIds = new ArrayList<>();
        private String fallbackPool;
        private final Random random = new Random();
        
        public JigsawPool(String id) {
            this.id = id;
        }
        
        public void addPiece(String pieceId) {
            pieceIds.add(pieceId);
        }
        
        public JigsawPiece getRandomPiece() {
            if (pieceIds.isEmpty()) {
                return null;
            }
            String pieceId = pieceIds.get(random.nextInt(pieceIds.size()));
            return pieces.get(pieceId);
        }
    }
    
    /**
     * 拼图片段
     */
    public static class JigsawPiece {
        private final String id;
        private int sizeX = 5;
        private int sizeY = 5;
        private int sizeZ = 5;
        private final List<BlockInfo> blocks = new ArrayList<>();
        private final List<JigsawConnector> connectors = new ArrayList<>();
        
        public JigsawPiece(String id) {
            this.id = id;
        }
        
        public void addBlock(int x, int y, int z, String material) {
            blocks.add(new BlockInfo(x, y, z, material));
        }
        
        public void addConnector(int x, int y, int z, String targetPool, String direction) {
            connectors.add(new JigsawConnector(x, y, z, targetPool, direction));
        }
        
        public void place(World world, Location location) {
            for (BlockInfo blockInfo : blocks) {
                Location blockLocation = location.clone().add(blockInfo.x, blockInfo.y, blockInfo.z);
                Block block = world.getBlockAt(blockLocation);
                
                try {
                    Material material = Material.getMaterial(
                        blockInfo.material.toUpperCase().replace("MINECRAFT:", "")
                    );
                    if (material != null) {
                        block.setType(material);
                    }
                } catch (Exception e) {
                    // 忽略无效的材质
                }
            }
        }
    }
    
    /**
     * 方块信息
     */
    private static class BlockInfo {
        final int x, y, z;
        final String material;
        
        BlockInfo(int x, int y, int z, String material) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
        }
    }
    
    /**
     * 拼图连接器
     */
    private static class JigsawConnector {
        final int x, y, z;
        final String targetPool;
        final String direction;
        
        JigsawConnector(int x, int y, int z, String targetPool, String direction) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.targetPool = targetPool;
            this.direction = direction;
        }
    }
}
