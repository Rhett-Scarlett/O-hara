package Validation;

import java.util.*;

/**
 * ClusterFileCache 类用于管理属性组合与对应聚类缓存文件之间的映射关系，
 * 通过前缀树（Trie）剪枝搜索空间，实现高效的复用验证文件逻辑。
 */
public class ClusterFileCache {

    // 前缀树节点定义
    static class TrieNode {
        Map<Integer, TrieNode> children = new HashMap<>();  // 子节点映射
        List<Set<Integer>> cachedCombos = new ArrayList<>(); // 到达该节点所存的组合（用于复用匹配）
    }

    private TrieNode root = new TrieNode(); // 前缀树的根节点

    /**
     * 插入新的属性组合及其对应的聚类缓存文件路径
     *
     * @param attrs    属性组合（例如：[1,3,5]）
     * @param filename 缓存的聚类文件名（例如："cluster_1.dat"）
     */
    public void insert(Set<Integer> attrs, String filename) {
        // 对属性组合排序，便于在 Trie 中形成稳定路径
        List<Integer> sorted = new ArrayList<>(attrs);
        Collections.sort(sorted);

        TrieNode node = root;
        // 构建前缀路径
        for (int attr : sorted) {
            node = node.children.computeIfAbsent(attr, k -> new TrieNode());
        }

        // 在路径尾部记录完整组合
        node.cachedCombos.add(attrs);
        // 在哈希表中建立文件名索引
        fileMap.put(attrs, filename);
    }

    /**
     * 查询当前属性组合是否可以复用已有缓存文件
     *
     * @param candidate 当前需要验证的属性组合
     * @return 如果找到可复用的组合，返回其对应缓存文件名，否则返回 empty
     */
    public Optional<String> query(Set<Integer> candidate) {
        List<Set<Integer>> possible = findPrefixPaths(candidate);
        for (Set<Integer> cached : possible) {
            // 若已有组合是 candidate 的子集，说明可以复用该缓存
            if (candidate.containsAll(cached)) {
                return Optional.of(fileMap.get(cached));
            }
        }
        return Optional.empty();
    }

    /**
     * 遍历 Trie 中与 candidate 前缀路径相同的所有缓存组合
     *
     * @param attrs 当前候选属性组合
     * @return 可达路径中记录的所有组合（用于复用匹配）
     */
    private List<Set<Integer>> findPrefixPaths(Set<Integer> attrs) {
        List<Integer> sorted = new ArrayList<>(attrs);
        Collections.sort(sorted);

        List<Set<Integer>> result = new ArrayList<>();
        TrieNode node = root;

        for (int attr : sorted) {
            // 收集当前路径上可用的组合
            if (node.cachedCombos != null) {
                result.addAll(node.cachedCombos);
            }
            // 向下遍历
            node = node.children.get(attr);
            if (node == null) break; // 不存在路径则停止
        }

        // 最后一个节点的组合也需要加入
        if (node != null && node.cachedCombos != null) {
            result.addAll(node.cachedCombos);
        }

        return result;
    }
}
