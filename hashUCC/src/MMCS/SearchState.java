package MMCS;

import java.util.*;

/**
 * SearchState 类用于表示 MMCS 算法中的搜索状态。
 * 它维护当前搜索过程中使用到的候选集、未覆盖超边、关键边等信息，
 * 并提供对应的初始化与重置函数。
 */
public class SearchState {

    //超图
    public Hypergraph graph;

    //记录关键边和其对应击中该边的点
    public Map<Integer,Stack<Integer>> hitEdgeToVer;

    public Map<Integer,List<Integer>> verToHitEdge;

    //各个顶点关键边数量
    public int[] crit_num;

    //当前未击中边
    public List<Boolean> uncovs;

    public int uncov_num;

    /**
     * 构造函数：初始化 cand 数组、recoverCrits 栈数组、recoverCand 数组。
     * @param numVertices 顶点总数
     */
    public SearchState(int numVertices, Hypergraph graph) {

        hitEdgeToVer = new HashMap<>();
        verToHitEdge = new HashMap<>();
        uncovs = new ArrayList<>();
        this.graph =graph;

        uncov_num = graph.size();
        for (int i=0;i<uncov_num;i++) uncovs.add(true);
        crit_num = new int[numVertices];

    }


    /**
     * 更新关键边集合（crits）和未覆盖边集合（uncovs）：
     * 当顶点 v 被加入到当前解 S 中时，需要从 uncovs 中移除所有被 v 覆盖的超边，
     * 并更新 crits 结构（如果原来有其他顶点负责的临界边被 v 接管）。
     *
     * @param target 被加入当前 hitting set S 的顶点
     */
    private boolean update_Crit_Uncov(int target){

        List<Integer> targetHitEdges = new ArrayList<>();
        verToHitEdge.put(target,targetHitEdges);
        for (int e = 0 ; e < graph.size() ; e++) {
            boolean is_contains = false;
            // 判断超边 f 是否包含顶点 v
            for (int ver : graph.getEdge(e)) {
                if (ver == target) {
                    is_contains = true;
                    break;
                }
            }

            if(is_contains) {//超图中每条包含v的边f
                //1.先判断边e是否已被击中
                if(hitEdgeToVer.containsKey(e)){// 被击中
                    if(isMinimal(hitEdgeToVer.get(e).peek(),target)) return false;
                    crit_num[hitEdgeToVer.get(e).peek()]--; //改变 原被击中超边的数 量
                    hitEdgeToVer.get(e).add(target);//将新增点加入击中
                }else{
                    //未被击中，则将e作为v的临界超边
                    hitEdgeToVer.put(e,new Stack<>()).add(target);
                    crit_num[target]++;
                    uncovs.set(e,false);
                }
                targetHitEdges.add(e);
            }

        }

        return true;
    }

    public  boolean isMinimal(int u, int target){
        if(crit_num[u]==1){
            recover_Crit_Uncov(Collections.singletonList(target));  // 可读性强，性能优
            return false;
        }
        return true;
    }

    private  void  recover_Crit_Uncov(List<Integer> target) {

        // 统计每条超边被多少个点“释放”
        Map<Integer,Integer> count = new HashMap<>();
        for (int ver : target){
            for (int edge : verToHitEdge.remove(ver)){// 移除 ver 的击中记录
                //“把 edge 作为键放入 count 这个 Map 中，如果 edge 不存在，就设置值为 1；如果已经存在，就把旧值和 1 相加后作为新值。
                count.merge(edge, 1, Integer::sum);
            }
        }

        // 遍历每条被 pop 的超边，执行恢复逻辑
        for (Map.Entry<Integer,Integer> edge : count.entrySet()){
            int edgeKey = edge.getKey();
            int num = edge.getValue();
            Stack<Integer> hitVer = hitEdgeToVer.get(edgeKey);//找到这条边被哪些点击中
            for (int i=0; i<num; i++)  hitVer.pop();
            if(hitVer.isEmpty()) {//如果这条边修改后不被击中
                hitEdgeToVer.remove(edgeKey);
                uncovs.set(edgeKey,true);
            }
            else if(hitVer.size()==1) crit_num[hitVer.peek()]++;
        }
    }

}
