import javax.swing.text.html.StyleSheet;
import java.io.File;
import java.util.*;

public class MMCS_v2 {
    private List<List<Integer>> hyperedges; // 超边集合 下标就是id,超边顶点从小到大排序
    private  int numVertices; // 顶点数
    private ArrayList<Integer> S; // 当前已覆盖顶点集合
    private  boolean[] cand; // 候选顶点集合
    private  ArrayList<hyperedge> uncovs;//未覆盖边的集合
    private  List<ArrayList<hyperedge>> crits; // 临界超边集合
    private Stack<hyperedge> recoverUncovs;//还原uncovs集合
    private  Stack<hyperedge>[] recoverCrits;//还原crit集合
    private  int[] recoverCand;//还原cand集合,未被删除是-1，否则表示被删除时的层数

    List<int[]> minimalHittingSets = new ArrayList<>();//所有的最小覆盖集
    public  List<List<Integer>>[] PLI;
    public  Validation validation;//验证

    public  int[] min_column;
    public  Sample sample;

    public Stack<List<List<Integer>>> intersection_stack;//跟随迭代记录每一步验证的sPLI
    public LinkedList<Integer> tointersect_queue;//记录下次验证前，未加入sPLI的所有元素

    //选择加入的超边
    public int select_minF(){
        int minF=-1;
        int min=999999;
        int minC=numVertices;
        for(int i=0;i<uncovs.size();i++){

            int sum=0;
            //统计当前超边和CAND的顶点交集
            for(int j=0;j<hyperedges.get(uncovs.get(i).hypeid).size();j++){
                if(cand[hyperedges.get(uncovs.get(i).hypeid).get(j)]) sum++;
                //System.out.println(hyperedges.get(uncovs.get(i).hypeid)[j]);
            }

            //如果当前超边与cand的顶点交集小于或者等于当前统计的最小值并且大于0
            if (sum<=min&&sum>0){
                //如果等于，需要判断哪一列的簇和小，就选择哪一列
                if(sum==min){
                    for(int t=0;t<hyperedges.get(uncovs.get(i).hypeid).size();t++){
                        if(minC>min_column[hyperedges.get(uncovs.get(i).hypeid).get(t)]){
                            minC=hyperedges.get(uncovs.get(i).hypeid).get(t);
                            minF=uncovs.get(i).hypeid;
                        }
                    }
                }else{
                    min=sum;
                    minF=uncovs.get(i).hypeid;
                }

            }
        }
        //如果minF=-1，则说明当前uncov中的超边中没有与cand重合的点
        return minF;
    };
    //得到C并更新cand，C=F∩CAND
    public ArrayList<Integer> getC(int F){
        ArrayList<Integer> C= new ArrayList<>();

        //得到C=F∩CAND
        for(int t=0;t<hyperedges.get(F).size();t++) {
           if(cand[hyperedges.get(F).get(t)]) {
               C.add(hyperedges.get(F).get(t));
           }
        }
        return C;
    }
    //打印uncov和crit
    public  void uncovcrit(){
        for(int i=0;i< crits.size();i++){
            System.out.print(i+" [");
            for(int j=0;j<crits.get(i).size();j++) System.out.print(crits.get(i).get(j).hypeid+" ");
            System.out.print("]     ");

        }
        System.out.println("recover");
        for(int i=0;i<recoverCrits.length;i++){
            System.out.print(i+" [");
            for(int j=0;j<recoverCrits[i].size();j++) System.out.print(recoverCrits[i].get(j).hypeid+"("+recoverCrits[i].get(j).verdex+")"+" ");
            System.out.print("]     ");

        }
        System.out.print("uncov ");
        for(int i=0;i<uncovs.size();i++){
            System.out.print(uncovs.get(i).hypeid+"("+uncovs.get(i).verdex+")"+" ");
        }
        System.out.println();
    }
    // 更新临界顶点集合
    private  void update_Crit_Uncov(int v) {
        for (int f=0;f<hyperedges.size();f++) {
            //对于crit除v以外的更新
            boolean is_contains=false;
            for(int t=0;t<hyperedges.get(f).size();t++) {
                if(hyperedges.get(f).get(t)==v){
                    is_contains=true;
                    break;
                }
            }
            if(is_contains){//超图中每条包含v的边f
                // .如果f原本是S中某点u的超边 ，则在v加入后不是超边，应删去
                for(int j=0;j<S.size();j++){
                    int u=S.get(j);//S中顶点u
                    ArrayList<hyperedge> c=crits.get(u);
                    boolean isHype=false;
                    for(int t=0;t<c.size();t++){//判断crit(u,s)中是否有f
                        if(c.get(t).hypeid==f){
                            //是u的超边
                            c.get(t).verdex=v;
                            recoverCrits[u].push(c.get(t));//将因v删除的超边放入复原crit的结构中
                            c.remove(t);
                            isHype=true;
                            break;
                        }
                    }
                    //每个超边F至多可以是一个顶点的临界超边
                    if (isHype==true) break;

                }
                //更新uncov和uncov[v]
                for(int i=0;i< uncovs.size();i++){
                    if(uncovs.get(i).hypeid==f){//如果uncov中有包含v的超边
                        hyperedge h=uncovs.get(i);
                        h.verdex=v;
                        recoverUncovs.push(h);
                        h=new hyperedge(f,-1);
                        crits.get(v).add(h);
                        uncovs.remove(i);
                        break;
                    }
                }
            }


        }
    }

    // 检查添加顶点后是否满足极小性条件
    private  boolean isMinimal() {
        for (int i=0;i<S.size();i++) {
            if (crits.get(S.get(i)).size()==0) {//crit[f，s+v]为空,则说明没有只属于点f的超边，则f的唯一性被破坏，f和v没有区分度，
                //uncovcrit();
                return false;
            }
        }
        return true;
    }

    // 恢复临界超边集合
    private  void recover_Crit_Uncov(int v) {
        hyperedge h;
        //通过之前利用栈保存的crit来回复于
        for(int t=0;t< S.size();t++){
            int i=S.get(t);
            while(!recoverCrits[i].isEmpty()){
                h=recoverCrits[i].pop();
                if(h.verdex!=v) {
                    recoverCrits[i].push(h);
                    break;
                }
                crits.get(i).add(h);


            }
        }
        crits.get(v).clear();
        //恢复uncov
        while(!recoverUncovs.isEmpty()){
            h=recoverUncovs.pop();
            if(h.verdex==v) {
                uncovs.add(h);
            }else {
                recoverUncovs.push(h);
                break;
            }
        }
    }

    //初始化函数，对mmcs所有结构进行一个初始化
    public void initial(){
        //复原crit为空
        for (int i=0;i<numVertices;i++) recoverCrits[i].clear();
        //可添加顶点列表为R
        Arrays.fill(cand,true);
        //未被击中超边为整个超图
        for (int i = 0; i < hyperedges.size(); i++) {
            uncovs.add(new hyperedge(i,-1));
        }
        //临界超边为空
        for (int i = 0; i < numVertices; i++) {
            crits.add(new ArrayList<hyperedge>());
        }
        //复原cand为空
        Arrays.fill(recoverCand,-1);

    }
    //解决迭代问题,pos表示下一个位置的


    public void return_position_status(int pos,int ver){
        //将比ver层数低的cand状态返回
        for(int t=0;t<numVertices;t++){
            if(recoverCand[t]>pos){
                recoverCand[t]=-1;
                cand[t]=true;
            }else if(recoverCand[t]==pos&&ver<t) cand[t]=true;
        }
        //将
        for(int i=S.size();i>pos;i--){
            //将第i层迭代中的cand去掉的集合，重新加入cand
            recover_Crit_Uncov(S.getLast());
            S.removeLast();
            if(tointersect_queue.isEmpty()) {
                if(!intersection_stack.isEmpty()) intersection_stack.pop();
            }else tointersect_queue.removeLast();
        }
    }
    //public MMCS
    MMCS_v2(int numVertices1, List<List<Integer>> hyperedges1, Validation validation1, Sample sample1, int[] min_column1, List<List<Integer>>[] PLI1) {
        hyperedges=hyperedges1;
        numVertices=numVertices1;
        validation=validation1;
        sample=sample1;
        min_column=min_column1;
        //初始化cand，包含所有点
        cand = new boolean[numVertices];
        recoverCrits=new Stack[numVertices];
        for(int i=0;i<numVertices;i++) recoverCrits[i]=new Stack<>();
        recoverUncovs=new Stack<>();
        uncovs = new ArrayList<hyperedge>();
        crits=new ArrayList<>();

        recoverCand=new int[numVertices];

        intersection_stack=new Stack<>();
        tointersect_queue=new LinkedList<>();
        PLI=PLI1;
    }
    public  void mmcs(ArrayList<Integer> Hitting_set) {
        //1、初始化
        initial();
        S=Hitting_set;
        Stack<Integer> stack=new Stack<>();//用来记录搜索树的每个节点
        Stack<Integer> position=new Stack<>();//用来记录搜索树中每个节点所处位置，如果找到最小/找不到时，应当将hittingset返还到第几层
        System.out.println("hyperedges");
        for(int i=0;i<hyperedges.size();i++) {
            System.out.print(i+" ");
            for(int j=0;j<hyperedges.get(i).size();j++){
                System.out.print(hyperedges.get(i).get(j)+" ");
            }
            System.out.println();
        }
        int ver=0;//当前判断的顶点
        int pos=-1;//当前顶点所在位置,根顶点层数是0
        boolean is_Minimal=false;
        //进入迭代
        while(true){
            if (uncovs.isEmpty()){
                //是最小覆盖集
                ArrayList<Integer> add_min= (ArrayList<Integer>) S.clone();
                add_min.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1<o2? -1:1;
                    }
                });


                if(intersection_stack.isEmpty()) {
                    //如果记录验证中间结果的栈为空，则加入当前待验证集合的第一个顶点
                    intersection_stack.add(PLI[tointersect_queue.remove(0)]);
                }
                validation.pullUpIntersections(intersection_stack,tointersect_queue);

                //验证失败
                if(!intersection_stack.peek().isEmpty()){
                    //得到新增边的条数
                    int newH=sample.sample(intersection_stack.peek());
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

            tointersect_queue.add(ver);

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
    public  void print() {
        System.out.println("minimalHittingSets");
        int min;
        for(int i=0;i<minimalHittingSets.size();i++){
            min=i;
            for(int j=i;j<minimalHittingSets.size();j++){
                for(int t=0;t<(minimalHittingSets.get(min).length<minimalHittingSets.get(j).length?minimalHittingSets.get(min).length:minimalHittingSets.get(j).length);t++){
                    if(minimalHittingSets.get(min)[t]>minimalHittingSets.get(j)[t]) {
                        min=j;
                        break;
                    }else if(minimalHittingSets.get(min)[t]<minimalHittingSets.get(j)[t]) break;
                }
            }
            System.out.print(i+"[");
            for(int j=0;j<minimalHittingSets.get(min).length;j++) System.out.print(" " +minimalHittingSets.get(min)[j]);
            System.out.println("]");
            minimalHittingSets.set(min,minimalHittingSets.get(i));
        }


        System.out.println(minimalHittingSets.size());
    }
}
