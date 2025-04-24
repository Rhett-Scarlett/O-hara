package Validation;

import java.util.*;

/**
 * BitSetClusterCache 使用 BitSet 替代前缀树，实现高效的属性组合复用判断。
 * 用于缓存聚类验证文件，判断新的属性组合是否可以复用已有验证文件。
 */
public class BitSetClusterCache {

    // BitSet → 文件名 映射表
    private Map<BitSet, String> fileMap = new HashMap<>();

    /**
     * 插入新的属性组合和对应的缓存文件路径。
     *
     * @param attrs    属性组合（例如：[1,3,5]）
     * @param filename 缓存的聚类文件名（例如："cluster_1.dat"）
     */
    public void insert(Set<Integer> attrs, String filename) {
        BitSet bitset = new BitSet();
        for (int attr : attrs) bitset.set(attr);  // 设置对应属性位
        fileMap.put(bitset, filename);             // 存入缓存映射表
    }

    /**
     * 查询某个候选属性组合是否可以复用已有的聚类文件。
     * 条件是：已有组合是 candidate 的子集。
     *
     * @param candidate 当前候选的属性组合
     * @return 如果存在可复用组合，返回对应的缓存文件名；否则返回 empty
     */
    public Optional<String> query(Set<Integer> candidate) {
        BitSet candBitSet = new BitSet();
        for (int attr : candidate) candBitSet.set(attr);  // 转换成 BitSet 方便子集判断

        for (Map.Entry<BitSet, String> entry : fileMap.entrySet()) {
            BitSet cached = entry.getKey();  // 已有的组合 BitSet
            BitSet temp = (BitSet) cached.clone(); // 克隆一份用于位操作
            temp.and(candBitSet); // 交集

            if (temp.equals(cached)) {
                // 如果交集 == cached，说明 cached ⊆ candidate，可以复用
                return Optional.of(entry.getValue());
            }
        }

        return Optional.empty(); // 没有可复用项
    }
}
