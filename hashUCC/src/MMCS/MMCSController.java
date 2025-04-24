package MMCS;

import Validation.ValidatorSelector;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

//负责搜索主流程控制
public class MMCSController {

    private final Stack<Integer> searchPath;
    private final Stack<Integer> backtrackLevel;

    private final SearchState state;

    private VertexSelector verSelect;

    private List<Integer> S;//当前候选UCC

    private ValidatorSelector validation;

    private ResultCollector result;

    private boolean is_minimal;

    public MMCSController(Hypergraph hypergraph,List<Integer> unique, ValidatorSelector validation) {
        state = new SearchState(unique.size(),hypergraph);
        searchPath = new Stack<>();
        backtrackLevel = new Stack<>();
        result = new ResultCollector();
        verSelect = new VertexSelector(searchPath,backtrackLevel, hypergraph, unique, state.uncovs, unique.size());
        this.validation = validation;

        is_minimal = true ;
        S = new ArrayList<>();
    }

    public void return_position_status(int lastVer){
        //将比ver层数低的cand状态返回
        verSelect.return_position_ver(lastVer);
        //将S、uncov和crit状态返回
        state.recover_Crit_Uncov(S.remove(S.size()-1));
    }
    public void return_position_status(int pos,int ver){
        //将比ver层数低的cand状态返回
        verSelect.return_position_ver(pos,ver);

        //将S状态饭返回
        List<Integer> delete = new ArrayList<>();
        for(int i=S.size();i>pos;i--) delete.add(S.remove(S.size()-1));

        //将uncov和crit状态返回
        state.recover_Crit_Uncov(delete);
    }

    public void goback(int ver){
        if(backtrackLevel.peek() == S.size()-1 ){
            System.out.println("只返回点"+ver);
            //System.out.println("下一个位置"+backtrackLevel.peek()+"下一个点 "+searchPath.peek());
            return_position_status(ver);
        }
        else return_position_status(backtrackLevel.peek(), searchPath.peek());
    }

    public void run() throws IOException, NoSuchAlgorithmException {
        // 执行主逻辑
        //1、初始化

        int ver=0;//当前判断的顶点
        //进入迭代
        while(true){
            if (state.uncovs_num == 0){
                //是最小覆盖集
                List<Integer> minUCC= new ArrayList<>();
                for (int v : S)  minUCC.add(v);
                minUCC.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1<o2? -1:1;
                    }
                });


                int newH = validation.selectValidator(minUCC);//得到新增边的条数
                //验证失败
                if(newH>0){
                    //添加uncov
                    for(int i=0;i<newH;i++){
                        state.uncovs.add(false);

                    }
                    //System.out.println("当前超边数量 "+ state.uncovs.size());
                    state.uncovs_num+=newH;
                    System.out.println("验证失败");
                    continue;

                }else{//验证成功
                    result.addMinUCC(minUCC);
                    //System.out.println("当前超边数量 "+ state.uncovs.size());
                    //这一段是用来解决，mmcs返回的问题
                    if(!backtrackLevel.isEmpty()) goback(ver);
                }

            }else if(is_minimal){
                if(!verSelect.selectEdge(S.size())&&!backtrackLevel.isEmpty()) goback(S.get(S.size()-1));
            }
            if(searchPath.isEmpty()) break;

            //3、选择一个点ver,判断是否能将v加入当前击中集S
            ver = verSelect.selectVertex();
            //System.out.println("ver "+ ver);

            S.add(ver);
            //满足极小性则进行下一步,将当前判断顶点加入S
            is_minimal = state.update_Crit_Uncov(ver);
            if(!is_minimal){
                //判断是否应该推出循环
                //System.out.println(ver);

                if(!backtrackLevel.isEmpty()) goback(ver);
            }
            System.out.println("S "+S);
            //
        }
        result.print();
    }
}


