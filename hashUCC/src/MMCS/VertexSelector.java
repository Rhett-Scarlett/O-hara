package MMCS;

import java.util.List;

//策略类，选择最小交点的超边、构建 C 集合等
public class VertexSelector {

    public List<Boolean> uncovs;
    public  boolean[] cand; // 候选顶点集合

    public Hypergraph graph;

    public VertexSelector(){

    }

    public int select_minF(){
        int minF=-1;
        int min=Integer.MAX_VALUE;
        for(int e=0;e<uncovs.size();e++){
            if(uncovs.get(e)==false) continue;

            int sum=0;
            //统计当前超边和CAND的顶点交集
            for(int ver : graph.getEdge(e)){
                if(cand[ver]) sum++;
            }
            //如果当前超边与cand的顶点交集小于或者等于当前统计的最小值并且大于0
            if (sum<=min&&sum>0){
                //如果等于，需要判断哪一列的簇和小，就选择哪一列
                min=sum;
                if (min == sum) {
                    //如果等于，需要判断哪一列的簇数量小，就选择哪一列













                }

            }
        }
        //如果minF=-1，则说明当前uncov中的超边中没有与cand重合的点
        return minF;
    };
}
