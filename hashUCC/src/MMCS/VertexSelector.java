package MMCS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

//策略类，选择最小交点的超边、构建 C 集合等
public class VertexSelector {

    public List<Boolean> uncovs;
    public  boolean[] cand; // 候选顶点集合

    public Hypergraph graph;

    public List<Integer> unique;
    private  int[] recoverCand;//还原cand集合,未被删除是-1，否则表示被删除时的层数

    private Stack<Integer> searchPath ;   // 原 stack：表示搜索路径中的节点（即顶点）
    private Stack<Integer> backtrackLevel ; // 原 position：表示回溯时对应的搜索层级


    public VertexSelector(Stack searchPath, Stack backtrackLevel, Hypergraph graph, List<Integer> unique,List<Boolean> uncovs, int vertexNum ){
        this.searchPath = searchPath;
        this.backtrackLevel = backtrackLevel;
        this.graph = graph ;
        this.unique = unique ;
        this.uncovs = uncovs;

        cand = new boolean[vertexNum];
        recoverCand = new int[vertexNum];
        Arrays.fill(cand, true);
        Arrays.fill(recoverCand, -1);

    }
    public void return_position_ver(int lastVer){
        recoverCand[lastVer]=-1;
        cand[lastVer]=true;
    }

    public void return_position_ver(int pos,int ver){
        for(int t=0;t<cand.length;t++){
            if(recoverCand[t]>pos){
                recoverCand[t]=-1;
                cand[t]=true;
            }else if(recoverCand[t]==pos&&ver<t) cand[t]=true;
        }
    }

    public boolean selectEdge(int pos){
        int minF= select_minF();
        if(minF == -1) return false;
        getC(minF,pos);
        return true;
    }
    public int selectVertex(){

        backtrackLevel.pop();
        int t = searchPath.pop();
        if(!backtrackLevel.isEmpty()){
            //System.out.println("下一个位置"+backtrackLevel.peek()+"下一个点 "+searchPath.peek());
        }
        return t;
    }
    public int select_minF(){
        int minF=-1;
        int min=Integer.MAX_VALUE;
        int max_Unique = Integer.MIN_VALUE;
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
                int temp =0;
                for(int ver : graph.getEdge(e)){
                    if(unique.get(ver) > temp)  temp = unique.get(ver);
                }
                //如果等于，需要判断哪一列的唯一性大，就选择哪一列，就选择哪条边
                if (min == sum&&max_Unique > temp)  continue;
                min=sum;
                max_Unique = temp;
                minF = e;

            }
        }
        //如果minF=-1，则说明当前uncov中的超边中没有与cand重合的点
        return minF;
    };


    public void getC(int edge, int pos){

        //得到C=F∩CAND
        System.out.println("pos "+pos);
        System.out.println("所选择边 "+graph.getEdge(edge));
        for(int ver : graph.getEdge(edge)) {
            if(cand[ver]) {
                searchPath.push(ver);
                backtrackLevel.push(pos);
                cand[ver]=false;
                recoverCand[ver]=pos;
                System.out.print (ver+" ");
            }
        }
        System.out.println();
        return ;
    }
}
