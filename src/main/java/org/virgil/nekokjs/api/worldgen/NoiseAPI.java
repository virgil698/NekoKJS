package org.virgil.nekokjs.api.worldgen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * 噪声生成 API
 * 提供多种噪声算法和密度函数支持
 */
public class NoiseAPI {
    
    private static final Logger LOGGER = Logger.getLogger("NekoKJS-Noise");
    private static final Map<String, NoiseGenerator> noiseGenerators = new HashMap<>();
    private static final Map<String, DensityFunction> densityFunctions = new HashMap<>();
    
    // 噪声缓存 - 提高性能
    private static final Map<String, Map<Long, Double>> noiseCache = new HashMap<>();
    private static final int MAX_CACHE_SIZE = 10000; // 每个噪声生成器最多缓存 10000 个值
    private static boolean cacheEnabled = true;
    
    /**
     * 注册噪声生成器
     * 
     * @param noiseId 噪声 ID
     * @param config 配置
     * @return 是否注册成功
     */
    public static boolean registerNoise(String noiseId, Map<String, Object> config) {
        try {
            String type = config.getOrDefault("type", "PERLIN").toString().toUpperCase();
            NoiseType noiseType = NoiseType.valueOf(type);
            
            double frequency = 1.0;
            if (config.containsKey("frequency")) {
                Object freq = config.get("frequency");
                if (freq instanceof Number) {
                    frequency = ((Number) freq).doubleValue();
                }
            }
            
            int octaves = 4;
            if (config.containsKey("octaves")) {
                Object oct = config.get("octaves");
                if (oct instanceof Number) {
                    octaves = ((Number) oct).intValue();
                }
            }
            
            double persistence = 0.5;
            if (config.containsKey("persistence")) {
                Object pers = config.get("persistence");
                if (pers instanceof Number) {
                    persistence = ((Number) pers).doubleValue();
                }
            }
            
            double lacunarity = 2.0;
            if (config.containsKey("lacunarity")) {
                Object lac = config.get("lacunarity");
                if (lac instanceof Number) {
                    lacunarity = ((Number) lac).doubleValue();
                }
            }
            
            long seed = System.currentTimeMillis();
            if (config.containsKey("seed")) {
                Object s = config.get("seed");
                if (s instanceof Number) {
                    seed = ((Number) s).longValue();
                }
            }
            
            NoiseGenerator generator = new NoiseGenerator(noiseId, noiseType, frequency, octaves, persistence, lacunarity, seed);
            noiseGenerators.put(noiseId, generator);
            
            LOGGER.info("Registered noise generator: " + noiseId + " (type=" + noiseType + ")");
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register noise: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取噪声值（带缓存）
     * 
     * @param noiseId 噪声 ID
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 噪声值 (-1.0 到 1.0)
     */
    public static double getNoise(String noiseId, double x, double y, double z) {
        NoiseGenerator generator = noiseGenerators.get(noiseId);
        if (generator == null) {
            LOGGER.warning("Noise generator not found: " + noiseId);
            return 0.0;
        }
        
        // 如果启用缓存，先查找缓存
        if (cacheEnabled) {
            long key = hashCoordinates(x, y, z);
            Map<Long, Double> cache = noiseCache.computeIfAbsent(noiseId, k -> new HashMap<>());
            
            Double cachedValue = cache.get(key);
            if (cachedValue != null) {
                return cachedValue;
            }
            
            // 计算新值
            double value = generator.getValue(x, y, z);
            
            // 缓存大小限制
            if (cache.size() < MAX_CACHE_SIZE) {
                cache.put(key, value);
            }
            
            return value;
        }
        
        return generator.getValue(x, y, z);
    }
    
    /**
     * 启用/禁用噪声缓存
     * 
     * @param enabled 是否启用
     */
    public static void setCacheEnabled(boolean enabled) {
        cacheEnabled = enabled;
        if (!enabled) {
            clearCache();
        }
    }
    
    /**
     * 清除所有噪声缓存
     */
    public static void clearCache() {
        noiseCache.clear();
        LOGGER.info("Noise cache cleared");
    }
    
    /**
     * 清除指定噪声生成器的缓存
     * 
     * @param noiseId 噪声 ID
     */
    public static void clearCache(String noiseId) {
        Map<Long, Double> cache = noiseCache.get(noiseId);
        if (cache != null) {
            cache.clear();
            LOGGER.info("Cleared cache for noise: " + noiseId);
        }
    }
    
    /**
     * 哈希坐标为 long 值
     */
    private static long hashCoordinates(double x, double y, double z) {
        // 将坐标转换为整数（精度到 0.01）
        long ix = (long) (x * 100);
        long iy = (long) (y * 100);
        long iz = (long) (z * 100);
        
        // 组合为单个 long 值
        return (ix & 0x1FFFFF) | ((iy & 0x1FFFFF) << 21) | ((iz & 0x1FFFFF) << 42);
    }
    
    /**
     * 获取 2D 噪声值
     * 
     * @param noiseId 噪声 ID
     * @param x X 坐标
     * @param z Z 坐标
     * @return 噪声值 (-1.0 到 1.0)
     */
    public static double getNoise2D(String noiseId, double x, double z) {
        return getNoise(noiseId, x, 0, z);
    }
    
    /**
     * 注册密度函数
     * 
     * @param functionId 函数 ID
     * @param config 配置
     * @return 是否注册成功
     */
    public static boolean registerDensityFunction(String functionId, Map<String, Object> config) {
        try {
            String type = config.getOrDefault("type", "NOISE").toString().toUpperCase();
            
            DensityFunction function;
            switch (type) {
                case "NOISE":
                    String noiseId = config.get("noise").toString();
                    double scale = config.containsKey("scale") ? 
                        ((Number) config.get("scale")).doubleValue() : 1.0;
                    function = new NoiseDensityFunction(noiseId, scale);
                    break;
                    
                case "CONSTANT":
                    double value = ((Number) config.get("value")).doubleValue();
                    function = new ConstantDensityFunction(value);
                    break;
                    
                case "ADD":
                    String input1 = config.get("input1").toString();
                    String input2 = config.get("input2").toString();
                    function = new AddDensityFunction(input1, input2);
                    break;
                    
                case "MUL":
                    String mul1 = config.get("input1").toString();
                    String mul2 = config.get("input2").toString();
                    function = new MulDensityFunction(mul1, mul2);
                    break;
                    
                case "CLAMP":
                    String input = config.get("input").toString();
                    double min = ((Number) config.get("min")).doubleValue();
                    double max = ((Number) config.get("max")).doubleValue();
                    function = new ClampDensityFunction(input, min, max);
                    break;
                    
                case "Y_CLAMPED_GRADIENT":
                    int fromY = ((Number) config.get("fromY")).intValue();
                    int toY = ((Number) config.get("toY")).intValue();
                    double fromValue = ((Number) config.get("fromValue")).doubleValue();
                    double toValue = ((Number) config.get("toValue")).doubleValue();
                    function = new YClampedGradientFunction(fromY, toY, fromValue, toValue);
                    break;
                    
                case "ABS":
                    String absInput = config.get("input").toString();
                    function = new AbsDensityFunction(absInput);
                    break;
                    
                case "SQUARE":
                    String sqInput = config.get("input").toString();
                    function = new SquareDensityFunction(sqInput);
                    break;
                    
                case "CUBE":
                    String cubeInput = config.get("input").toString();
                    function = new CubeDensityFunction(cubeInput);
                    break;
                    
                case "MIN":
                    String minInput1 = config.get("input1").toString();
                    String minInput2 = config.get("input2").toString();
                    function = new MinDensityFunction(minInput1, minInput2);
                    break;
                    
                case "MAX":
                    String maxInput1 = config.get("input1").toString();
                    String maxInput2 = config.get("input2").toString();
                    function = new MaxDensityFunction(maxInput1, maxInput2);
                    break;
                    
                case "LERP":
                    String lerpInput1 = config.get("input1").toString();
                    String lerpInput2 = config.get("input2").toString();
                    String lerpDelta = config.get("delta").toString();
                    function = new LerpDensityFunction(lerpInput1, lerpInput2, lerpDelta);
                    break;
                    
                case "SPLINE":
                    String splineInput = config.get("input").toString();
                    Object pointsObj = config.get("points");
                    Object valuesObj = config.get("values");
                    
                    // 转换为 double 数组
                    double[] points;
                    double[] values;
                    
                    if (pointsObj instanceof List) {
                        List<?> pointsList = (List<?>) pointsObj;
                        points = new double[pointsList.size()];
                        for (int i = 0; i < pointsList.size(); i++) {
                            points[i] = ((Number) pointsList.get(i)).doubleValue();
                        }
                    } else {
                        LOGGER.warning("Spline points must be an array");
                        return false;
                    }
                    
                    if (valuesObj instanceof List) {
                        List<?> valuesList = (List<?>) valuesObj;
                        values = new double[valuesList.size()];
                        for (int i = 0; i < valuesList.size(); i++) {
                            values[i] = ((Number) valuesList.get(i)).doubleValue();
                        }
                    } else {
                        LOGGER.warning("Spline values must be an array");
                        return false;
                    }
                    
                    function = new SplineDensityFunction(splineInput, points, values);
                    break;
                    
                default:
                    LOGGER.warning("Unknown density function type: " + type);
                    return false;
            }
            
            densityFunctions.put(functionId, function);
            LOGGER.info("Registered density function: " + functionId + " (type=" + type + ")");
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register density function: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 计算密度函数值
     * 
     * @param functionId 函数 ID
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 密度值
     */
    public static double getDensity(String functionId, double x, double y, double z) {
        DensityFunction function = densityFunctions.get(functionId);
        if (function == null) {
            LOGGER.warning("Density function not found: " + functionId);
            return 0.0;
        }
        return function.compute(x, y, z);
    }
    
    /**
     * 噪声类型
     */
    public enum NoiseType {
        PERLIN,      // 柏林噪声
        SIMPLEX,     // 单纯形噪声
        CELLULAR,    // 细胞噪声
        VALUE,       // 值噪声
        RIDGED       // 山脊噪声
    }
    
    /**
     * 噪声生成器
     */
    public static class NoiseGenerator {
        private final String id;
        private final NoiseType type;
        private final double frequency;
        private final int octaves;
        private final double persistence;
        private final double lacunarity;
        private final Random random;
        private final int[] permutation;
        
        public NoiseGenerator(String id, NoiseType type, double frequency, int octaves, 
                            double persistence, double lacunarity, long seed) {
            this.id = id;
            this.type = type;
            this.frequency = frequency;
            this.octaves = octaves;
            this.persistence = persistence;
            this.lacunarity = lacunarity;
            this.random = new Random(seed);
            
            // 初始化置换表
            this.permutation = new int[512];
            int[] p = new int[256];
            for (int i = 0; i < 256; i++) {
                p[i] = i;
            }
            
            // 打乱置换表
            for (int i = 255; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = p[i];
                p[i] = p[j];
                p[j] = temp;
            }
            
            // 复制到双倍大小的数组
            for (int i = 0; i < 512; i++) {
                permutation[i] = p[i & 255];
            }
        }
        
        public double getValue(double x, double y, double z) {
            double total = 0.0;
            double amplitude = 1.0;
            double freq = frequency;
            double maxValue = 0.0;
            
            for (int i = 0; i < octaves; i++) {
                double value;
                switch (type) {
                    case PERLIN:
                        value = perlinNoise(x * freq, y * freq, z * freq);
                        break;
                    case SIMPLEX:
                        value = simplexNoise(x * freq, y * freq, z * freq);
                        break;
                    case CELLULAR:
                        value = cellularNoise(x * freq, y * freq, z * freq);
                        break;
                    case VALUE:
                        value = valueNoise(x * freq, y * freq, z * freq);
                        break;
                    case RIDGED:
                        value = 1.0 - Math.abs(perlinNoise(x * freq, y * freq, z * freq));
                        break;
                    default:
                        value = 0.0;
                }
                
                total += value * amplitude;
                maxValue += amplitude;
                
                amplitude *= persistence;
                freq *= lacunarity;
            }
            
            return total / maxValue;
        }
        
        private double perlinNoise(double x, double y, double z) {
            int X = (int) Math.floor(x) & 255;
            int Y = (int) Math.floor(y) & 255;
            int Z = (int) Math.floor(z) & 255;
            
            x -= Math.floor(x);
            y -= Math.floor(y);
            z -= Math.floor(z);
            
            double u = fade(x);
            double v = fade(y);
            double w = fade(z);
            
            int A = permutation[X] + Y;
            int AA = permutation[A] + Z;
            int AB = permutation[A + 1] + Z;
            int B = permutation[X + 1] + Y;
            int BA = permutation[B] + Z;
            int BB = permutation[B + 1] + Z;
            
            return lerp(w,
                lerp(v,
                    lerp(u, grad(permutation[AA], x, y, z), grad(permutation[BA], x - 1, y, z)),
                    lerp(u, grad(permutation[AB], x, y - 1, z), grad(permutation[BB], x - 1, y - 1, z))
                ),
                lerp(v,
                    lerp(u, grad(permutation[AA + 1], x, y, z - 1), grad(permutation[BA + 1], x - 1, y, z - 1)),
                    lerp(u, grad(permutation[AB + 1], x, y - 1, z - 1), grad(permutation[BB + 1], x - 1, y - 1, z - 1))
                )
            );
        }
        
        private double simplexNoise(double x, double y, double z) {
            // 简化的单纯形噪声实现
            return perlinNoise(x, y, z) * 0.866;
        }
        
        private double cellularNoise(double x, double y, double z) {
            int xi = (int) Math.floor(x);
            int yi = (int) Math.floor(y);
            int zi = (int) Math.floor(z);
            
            double minDist = Double.MAX_VALUE;
            
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int cellX = xi + dx;
                        int cellY = yi + dy;
                        int cellZ = zi + dz;
                        
                        int hash = permutation[(permutation[(permutation[cellX & 255] + cellY) & 255] + cellZ) & 255];
                        double pointX = cellX + (hash & 255) / 255.0;
                        double pointY = cellY + ((hash >> 8) & 255) / 255.0;
                        double pointZ = cellZ + ((hash >> 16) & 255) / 255.0;
                        
                        double dist = Math.sqrt(
                            (x - pointX) * (x - pointX) +
                            (y - pointY) * (y - pointY) +
                            (z - pointZ) * (z - pointZ)
                        );
                        
                        minDist = Math.min(minDist, dist);
                    }
                }
            }
            
            return 1.0 - (minDist * 2.0);
        }
        
        private double valueNoise(double x, double y, double z) {
            int X = (int) Math.floor(x) & 255;
            int Y = (int) Math.floor(y) & 255;
            int Z = (int) Math.floor(z) & 255;
            
            x -= Math.floor(x);
            y -= Math.floor(y);
            z -= Math.floor(z);
            
            double u = fade(x);
            double v = fade(y);
            double w = fade(z);
            
            int A = permutation[X] + Y;
            int AA = permutation[A] + Z;
            int AB = permutation[A + 1] + Z;
            int B = permutation[X + 1] + Y;
            int BA = permutation[B] + Z;
            int BB = permutation[B + 1] + Z;
            
            double v000 = permutation[AA] / 255.0;
            double v100 = permutation[BA] / 255.0;
            double v010 = permutation[AB] / 255.0;
            double v110 = permutation[BB] / 255.0;
            double v001 = permutation[AA + 1] / 255.0;
            double v101 = permutation[BA + 1] / 255.0;
            double v011 = permutation[AB + 1] / 255.0;
            double v111 = permutation[BB + 1] / 255.0;
            
            return lerp(w,
                lerp(v, lerp(u, v000, v100), lerp(u, v010, v110)),
                lerp(v, lerp(u, v001, v101), lerp(u, v011, v111))
            ) * 2.0 - 1.0;
        }
        
        private double fade(double t) {
            return t * t * t * (t * (t * 6 - 15) + 10);
        }
        
        private double lerp(double t, double a, double b) {
            return a + t * (b - a);
        }
        
        private double grad(int hash, double x, double y, double z) {
            int h = hash & 15;
            double u = h < 8 ? x : y;
            double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }
    }
    
    /**
     * 密度函数接口
     */
    public interface DensityFunction {
        double compute(double x, double y, double z);
    }
    
    /**
     * 噪声密度函数
     */
    public static class NoiseDensityFunction implements DensityFunction {
        private final String noiseId;
        private final double scale;
        
        public NoiseDensityFunction(String noiseId, double scale) {
            this.noiseId = noiseId;
            this.scale = scale;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            return getNoise(noiseId, x, y, z) * scale;
        }
    }
    
    /**
     * 常量密度函数
     */
    public static class ConstantDensityFunction implements DensityFunction {
        private final double value;
        
        public ConstantDensityFunction(double value) {
            this.value = value;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            return value;
        }
    }
    
    /**
     * 加法密度函数
     */
    public static class AddDensityFunction implements DensityFunction {
        private final String input1;
        private final String input2;
        
        public AddDensityFunction(String input1, String input2) {
            this.input1 = input1;
            this.input2 = input2;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            return getDensity(input1, x, y, z) + getDensity(input2, x, y, z);
        }
    }
    
    /**
     * 乘法密度函数
     */
    public static class MulDensityFunction implements DensityFunction {
        private final String input1;
        private final String input2;
        
        public MulDensityFunction(String input1, String input2) {
            this.input1 = input1;
            this.input2 = input2;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            return getDensity(input1, x, y, z) * getDensity(input2, x, y, z);
        }
    }
    
    /**
     * 限制密度函数
     */
    public static class ClampDensityFunction implements DensityFunction {
        private final String input;
        private final double min;
        private final double max;
        
        public ClampDensityFunction(String input, double min, double max) {
            this.input = input;
            this.min = min;
            this.max = max;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            double value = getDensity(input, x, y, z);
            return Math.max(min, Math.min(max, value));
        }
    }
    
    /**
     * Y 轴渐变函数
     */
    public static class YClampedGradientFunction implements DensityFunction {
        private final int fromY;
        private final int toY;
        private final double fromValue;
        private final double toValue;
        
        public YClampedGradientFunction(int fromY, int toY, double fromValue, double toValue) {
            this.fromY = fromY;
            this.toY = toY;
            this.fromValue = fromValue;
            this.toValue = toValue;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            if (y <= fromY) return fromValue;
            if (y >= toY) return toValue;
            
            double t = (y - fromY) / (toY - fromY);
            return fromValue + t * (toValue - fromValue);
        }
    }
    
    // ===== 高级密度函数 =====
    
    /**
     * 绝对值密度函数
     */
    public static class AbsDensityFunction implements DensityFunction {
        private final String input;
        
        public AbsDensityFunction(String input) {
            this.input = input;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            return Math.abs(getDensity(input, x, y, z));
        }
    }
    
    /**
     * 平方密度函数
     */
    public static class SquareDensityFunction implements DensityFunction {
        private final String input;
        
        public SquareDensityFunction(String input) {
            this.input = input;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            double value = getDensity(input, x, y, z);
            return value * value;
        }
    }
    
    /**
     * 立方密度函数
     */
    public static class CubeDensityFunction implements DensityFunction {
        private final String input;
        
        public CubeDensityFunction(String input) {
            this.input = input;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            double value = getDensity(input, x, y, z);
            return value * value * value;
        }
    }
    
    /**
     * 最小值密度函数
     */
    public static class MinDensityFunction implements DensityFunction {
        private final String input1;
        private final String input2;
        
        public MinDensityFunction(String input1, String input2) {
            this.input1 = input1;
            this.input2 = input2;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            return Math.min(getDensity(input1, x, y, z), getDensity(input2, x, y, z));
        }
    }
    
    /**
     * 最大值密度函数
     */
    public static class MaxDensityFunction implements DensityFunction {
        private final String input1;
        private final String input2;
        
        public MaxDensityFunction(String input1, String input2) {
            this.input1 = input1;
            this.input2 = input2;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            return Math.max(getDensity(input1, x, y, z), getDensity(input2, x, y, z));
        }
    }
    
    /**
     * 线性插值密度函数
     */
    public static class LerpDensityFunction implements DensityFunction {
        private final String input1;
        private final String input2;
        private final String delta;
        
        public LerpDensityFunction(String input1, String input2, String delta) {
            this.input1 = input1;
            this.input2 = input2;
            this.delta = delta;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            double a = getDensity(input1, x, y, z);
            double b = getDensity(input2, x, y, z);
            double t = getDensity(delta, x, y, z);
            return a + t * (b - a);
        }
    }
    
    /**
     * 样条插值密度函数
     */
    public static class SplineDensityFunction implements DensityFunction {
        private final String input;
        private final double[] points;
        private final double[] values;
        
        public SplineDensityFunction(String input, double[] points, double[] values) {
            this.input = input;
            this.points = points;
            this.values = values;
        }
        
        @Override
        public double compute(double x, double y, double z) {
            double value = getDensity(input, x, y, z);
            
            // 简单的线性插值样条
            if (value <= points[0]) return values[0];
            if (value >= points[points.length - 1]) return values[values.length - 1];
            
            for (int i = 0; i < points.length - 1; i++) {
                if (value >= points[i] && value <= points[i + 1]) {
                    double t = (value - points[i]) / (points[i + 1] - points[i]);
                    return values[i] + t * (values[i + 1] - values[i]);
                }
            }
            
            return 0.0;
        }
    }
    
    // ===== 噪声路由器 =====
    
    private static final Map<String, NoiseRouter> noiseRouters = new HashMap<>();
    
    /**
     * 注册噪声路由器
     * 
     * @param routerId 路由器 ID
     * @param config 配置
     * @return 是否注册成功
     */
    public static boolean registerNoiseRouter(String routerId, Map<String, Object> config) {
        try {
            NoiseRouter router = new NoiseRouter(routerId);
            
            // 配置各个密度函数
            if (config.containsKey("barrierNoise")) {
                router.barrierNoise = config.get("barrierNoise").toString();
            }
            if (config.containsKey("fluidLevelFloodedness")) {
                router.fluidLevelFloodedness = config.get("fluidLevelFloodedness").toString();
            }
            if (config.containsKey("fluidLevelSpread")) {
                router.fluidLevelSpread = config.get("fluidLevelSpread").toString();
            }
            if (config.containsKey("lava")) {
                router.lava = config.get("lava").toString();
            }
            if (config.containsKey("temperature")) {
                router.temperature = config.get("temperature").toString();
            }
            if (config.containsKey("vegetation")) {
                router.vegetation = config.get("vegetation").toString();
            }
            if (config.containsKey("continents")) {
                router.continents = config.get("continents").toString();
            }
            if (config.containsKey("erosion")) {
                router.erosion = config.get("erosion").toString();
            }
            if (config.containsKey("depth")) {
                router.depth = config.get("depth").toString();
            }
            if (config.containsKey("ridges")) {
                router.ridges = config.get("ridges").toString();
            }
            if (config.containsKey("initialDensityWithoutJaggedness")) {
                router.initialDensityWithoutJaggedness = config.get("initialDensityWithoutJaggedness").toString();
            }
            if (config.containsKey("finalDensity")) {
                router.finalDensity = config.get("finalDensity").toString();
            }
            
            noiseRouters.put(routerId, router);
            LOGGER.info("Registered noise router: " + routerId);
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to register noise router: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取噪声路由器
     * 
     * @param routerId 路由器 ID
     * @return 噪声路由器
     */
    public static NoiseRouter getNoiseRouter(String routerId) {
        return noiseRouters.get(routerId);
    }
    
    /**
     * 噪声路由器类
     * 类似 Minecraft 的 NoiseRouter，用于组织多个密度函数
     */
    public static class NoiseRouter {
        public final String id;
        public String barrierNoise;
        public String fluidLevelFloodedness;
        public String fluidLevelSpread;
        public String lava;
        public String temperature;
        public String vegetation;
        public String continents;
        public String erosion;
        public String depth;
        public String ridges;
        public String initialDensityWithoutJaggedness;
        public String finalDensity;
        
        public NoiseRouter(String id) {
            this.id = id;
        }
        
        /**
         * 计算最终密度
         */
        public double computeFinalDensity(double x, double y, double z) {
            if (finalDensity != null) {
                return getDensity(finalDensity, x, y, z);
            }
            return 0.0;
        }
        
        /**
         * 计算温度
         */
        public double computeTemperature(double x, double y, double z) {
            if (temperature != null) {
                return getDensity(temperature, x, y, z);
            }
            return 0.5;
        }
        
        /**
         * 计算植被
         */
        public double computeVegetation(double x, double y, double z) {
            if (vegetation != null) {
                return getDensity(vegetation, x, y, z);
            }
            return 0.5;
        }
    }
}
