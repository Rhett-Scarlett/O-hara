package MMCS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

//负责搜索主流程控制
public class MMCSController {

    private final Hypergraph hypergraph;
    private final SearchState state;
    private final List<int[]> minimalHittingSets = new ArrayList<>();

    public MMCSController(Hypergraph hypergraph, SearchState state) {
        this.hypergraph = hypergraph;
        this.state = state;
    }

    public void run() {
        // 执行主逻辑
        //1、初始化
        List<Integer> S=new ArrayList<>();
        Stack<Integer> stack=new Stack<>();//用来记录搜索树的每个节点
        Stack<Integer> position=new Stack<>();//用来记录搜索树中每个节点所处位置，如果找到最小/找不到时，应当将hittingset返还到第几层

        int ver=0;//当前判断的顶点
        int pos=-1;//当前顶点所在位置,根顶点层数是0
        boolean is_Minimal=false;
        //进入迭代
        while(true){
            if (state.uncovs_num==0){
                //是最小覆盖集
                List<Integer> add_min= (List<Integer>) S.clone();
                add_min.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1<o2? -1:1;
                    }
                });


                int newH = validation.selectValidator(add_min);//得到新增边的条数


                //验证失败
                if(newH>0){

                    //添加uncov
                    for(int i=hyperedges.size()-newH;i<hyperedges.size();i++){
                        uncovs.add(new hyperedge(i,-1));
                    }
                    continue;

                }else{//验证成功

                    int[] ucc=new int[add_min.size()];
                    for(int i=0;i<add_min.size();i++) ucc[i]=add_min.get(i);
                    minimalHittingSets.add(ucc);
                    add_min.clear();

                    //这一段是用来解决，mmcs返回的问题
                    if(!position.isEmpty()) return_position_status(position.peek(),stack.peek());
                }

                //验证通过后，判断是否重复

            }else if(is_Minimal==false){
                //1. 选择一个未覆盖的超边.需要剪枝找到一个最小化| F∩CAND |f和cand相交的 点最少
                int minF=select_minF();
                if ((minF<0)){
                    if(!position.isEmpty()) return_position_status(position.peek(), stack.peek());
                }else{
                    //2. 从候选顶点集合中删除这些顶点，并得到c
                    ArrayList<Integer> C=getC(minF);

                    for (int v=0;v<C.size();v++) {//将C中顶点加入栈，并将其加入复原CAND集合中
                        stack.push(C.get(v));
                        position.push(pos+1);
                        cand[C.get(v)]=false;
                        recoverCand[C.get(v)]=pos+1;
                    }
                }

            }
            //判断是否应该推出循环
            if(stack.isEmpty()) break;

            //选择一个点ver
            ver=stack.pop();
            pos=position.pop();
            cand[ver]=false;
            recoverCand[ver]=pos;

            //3、判断是否能将v加入当前击中集S
            update_Crit_Uncov(ver);//更新cirt和uncov
            S.add(ver);//S∪v
            System.out.println(S+" "+ver+" "+ pos);



            //判断将该点加入候选集是否满足极小性条件
            if(!isMinimal()) {//不满足极小性条件,进行下一个点的判断
                if(!position.isEmpty()) return_position_status(position.peek(), stack.peek());
                else recover_Crit_Uncov(ver);
                is_Minimal=true;
            }else {
                is_Minimal=false;
            }
            //满足极小性则进行下一步,将当前判断顶点加入S
        }
        print();
    }
}


