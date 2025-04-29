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

        boolean is_Set = false ;

        int clusternum = 0;
        boolean isHash = false;
    }

    private TrieNode root = new TrieNode(); // 前缀树的根节点

    private  List<Integer> candidate;

    public   List<List<Integer>> result = new ArrayList<>(); //仅保存叶子节点的集合，即各个有效分支的 你  最大覆盖的子集

    public   List<Integer> result_clusternum = new ArrayList<>();

    public   List<Boolean> result_isHash = new ArrayList<>();

    public   boolean isHash ;


    public void insert(List<Integer> attrs,int clusternum, boolean isHash) {
        // 对属性组合排序，便于在 Trie 中形成稳定路径

        TrieNode node = root;
        // 构建前缀路径
        for (int attr : attrs) {
            node = node.children.computeIfAbsent(attr, k -> new TrieNode());
        }
        node.is_Set = true;
        node.clusternum = clusternum;
        node.isHash = isHash;
    }
    public List<Integer> findSubsets(List<Integer> candidate) {
        isHash = false;
        this.candidate = candidate;
        List<Integer> pre = new ArrayList<>();
        dfs(root, 0, pre);
        if(isHash){
            for (int i =0;i<result_isHash.size();i++){
                if(result_isHash.get(i)) return  result.get(i);
            }
        }
        if(result.size() == 0) return candidate;
        filterMaximalSubsets();

        return isComplete();
    }
    // DFS 查找所有子集路径
    private boolean dfs(TrieNode node, int idx, List<Integer> pre) {
        if(isHash == true) return false;
        boolean isMaximal = true;
        for (int i = idx ; i< candidate.size();i++){
            int attr = candidate.get(i);
            if(node.children.containsKey(attr)){
                pre.add(attr);
                if(dfs(node.children.get(attr),i+1,pre)) isMaximal = false ;
                pre.remove(pre.size()-1);
            }

        }
        if(isMaximal&&node.is_Set) {//如果往下找不到集合，这就是最大的
           if (node.isHash) isHash = true;
            result.add(new ArrayList<>(pre));
            result_clusternum.add(node.clusternum);
            result_isHash.add(node.isHash);
            return true;
        }
        return false;
    }
    //过滤掉子集
    private void filterMaximalSubsets() {
        for (int i = 0; i < result.size(); i++) {
            List<Integer> current = result.get(i);
            for (int j = 0; j < i; j++) {
                List<Integer> prev = result.get(j);
                if (isSuperset(prev, current)) {
                    result.remove(i);
                    result_isHash.remove(i);
                    result_clusternum.remove(i);
                    i--;
                    break;
                }
            }
        }
    }

    private boolean isSuperset(List<Integer> a, List<Integer> b) {
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i) < b.get(j)) {
                i++;
            } else if (a.get(i).equals(b.get(j))) {
                i++;
                j++;
            } else {
                return false;
            }
        }
        return j == b.size();
    }
    //收集到的子集是否是全覆盖，如果不是就返回未覆盖的集合
    private List<Integer>  isComplete(){
        Set<Integer> covered = new HashSet<>();
        for (List<Integer> combo : result) {
            covered.addAll(combo);
        }

        List<Integer> uncovered = new ArrayList<>();
        for (int attr : candidate) {
            if (!covered.contains(attr)) {
                uncovered.add(attr);
            }
        }

        return uncovered;
    }



}
