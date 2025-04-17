import java.util.*;

class hyperedge{
    int hypeid;//超边id
    int verdex;//由于哪个点而推出,-1代表无意义
    public hyperedge(int hypeid,int verdex){
        this.hypeid=hypeid;
        this.verdex=verdex;
    }
}
public class MMCS {
    private  List<int[]> hyperedges; // 超边集合 下标就是id
    private  int numVertices; // 顶点数
    private  ArrayList<Integer> S; // 当前已覆盖顶点集合
    private  ArrayList<Integer> cand; // 候选顶点集合
    private  ArrayList<hyperedge> uncovs;//未覆盖边的集合
    private  List<ArrayList<hyperedge>> crits; // 临界超边集合
    private  Stack<hyperedge> recoverUncovs;//还原uncovs集合
    private  Stack<hyperedge>[] recoverCrits;//还原crit集合
    private  Stack<Integer>[] recoverCand;//还原cand，一个记录边的下表，一个记录点
    List<int[]> minimalHittingSets = new ArrayList<>();//所有的最小覆盖集
    public  List<List<Integer>>[] PLI;
    public  Validation validation;//验证

    public  int[] min_column;
    public  Sample sample;

    public Stack<List<List<Integer>>> intersection_stack;
    public LinkedList<Integer> tointersect_queue;

    public boolean judge_minimality_condition(){
        boolean judge=false;
        return judge;
    }

    public int select_minF(){
        int minF=-1;
        int min=999999;
        int minC=numVertices;
        for(int i=0;i<uncovs.size();i++){
            int sum=0;
            for(int j=0;j<hyperedges.get(uncovs.get(i).hypeid).length;j++){
                if(cand.contains(hyperedges.get(uncovs.get(i).hypeid)[j])) sum++;
            }
            if (sum<=min&&sum>0){
                if(sum==min){
                    for(int t=0;t<hyperedges.get(uncovs.get(i).hypeid).length;t++){
                        if(minC>min_column[hyperedges.get(uncovs.get(i).hypeid)[t]]){
                            minC=hyperedges.get(uncovs.get(i).hypeid)[t];
                            minF=uncovs.get(i).hypeid;
                        }
                    }
                }else{
                    min=sum;
                    minF=uncovs.get(i).hypeid;
                }

            }
        }
        return minF;
    };
    //public MMCS
    MMCS(int numVertices1, List<List<Integer>> hyperedges1, Validation validation1, Sample sample1, int[] min_column1, List<List<Integer>>[] PLI1) {
        hyperedges=new ArrayList<>();
        for(int i=0;i<hyperedges1.size();i++){
            int[] f=new int[hyperedges1.get(i).size()];
            for (int j=0;j<hyperedges1.get(i).size();j++) f[j]=hyperedges1.get(i).get(j);
            hyperedges.add(f);
        }
        numVertices=numVertices1;
        validation=validation1;
        sample=sample1;
        min_column=min_column1;
        //初始化cand，包含所有点
        cand = new ArrayList<Integer>();
        recoverCrits=new Stack[numVertices];
        for(int i=0;i<numVertices;i++) recoverCrits[i]=new Stack<>();
        recoverUncovs=new Stack<>();
        uncovs = new ArrayList<hyperedge>();
        crits=new ArrayList<>();
        recoverCand=new Stack[2];
        recoverCand[0]=new Stack<>();
        recoverCand[1]=new Stack<>();
        intersection_stack=new Stack<>();
        tointersect_queue=new LinkedList<>();
        PLI=PLI1;
    }
    public  void print(){
        System.out.println("minimalHittingSets");
        for(int i=0;i<minimalHittingSets.size();i++){
            System.out.print(i+"[");
            for(int j=0;j<minimalHittingSets.get(i).length;j++) System.out.print(" " +minimalHittingSets.get(i)[j]);
            System.out.println("]");
        }
        System.out.println(minimalHittingSets.size());
    }
    public void run(){
        for (int i=0;i<numVertices;i++) recoverCrits[i].clear();
        for (int i = 0; i < numVertices; i++) {
            cand.add(i);
        }
        for (int i = 0; i < hyperedges.size(); i++) {
            uncovs.add(new hyperedge(i,-1));
        }
        for (int i = 0; i < numVertices; i++) {
            crits.add(new ArrayList<hyperedge>());
        }
        Stack<Integer> stack=new Stack<>();
        Stack<Integer> layer=new Stack<>();//用来记录如果找到最小/找不到时，应当将hittingset返还到第几层
    }
    public  void mmcs(ArrayList<Integer> hittingSet) {
        long startTime=System.currentTimeMillis();
        System.out.println("____________________________________");
        System.out.println("MMCS");
        System.out.println("____________________________________");
        //清理

        for (int i=0;i<numVertices;i++) recoverCrits[i].clear();
        for (int i = 0; i < numVertices; i++) {
            cand.add(i);
        }
        for (int i = 0; i < hyperedges.size(); i++) {
            uncovs.add(new hyperedge(i,-1));
        }
        for (int i = 0; i < numVertices; i++) {
            crits.add(new ArrayList<hyperedge>());
        }
        Stack<Integer> stack=new Stack<>();
        Stack<Integer> layer=new Stack<>();//用来记录如果找到最小/找不到时，应当将hittingset返还到第几层
        S=hittingSet;
        int ver,lay=-1,minF,min,minC;
        ArrayList<Integer> c,c1;
        boolean is_Minimal=false;
        boolean ist=false;
        while (true) {
            // 如果所有超边都被与S有交集，则找到一个最小覆盖集
            if (uncovs.isEmpty()) {
                //是最小覆盖集
                ArrayList<Integer> add_min= (ArrayList<Integer>) S.clone();
                System.out.println(intersection_stack.size());
                System.out.println(tointersect_queue.size());
                //排序,降序emh
                //验证
                if(intersection_stack.isEmpty()) {
                    intersection_stack.add(PLI[tointersect_queue.removeLast()]);
                }
                validation.pullUpIntersections(intersection_stack,tointersect_queue);

                //验证失败
                if(!intersection_stack.peek().isEmpty()){

                    int newH=sample.sample(intersection_stack.peek());
                    for(int i=hyperedges.size()-newH;i<hyperedges.size();i++){
                        uncovs.add(new hyperedge(i,-1));
                    }
                    System.out.println(" "+hyperedges.size());
                    System.out.println("no Validtion");
                    ist=true;
                    continue;

                }else{//验证成功

                    int[] ucc=new int[add_min.size()];
                    for(int i=0;i<add_min.size();i++) ucc[i]=add_min.get(i);
                    minimalHittingSets.add(ucc);
                    add_min.clear();

                    //这一段是用来解决，mmcs返回的问题
                    if(!layer.isEmpty())lay=layer.peek();//将其返回到上一层
                    //更新cand;
                    for(int i=S.size()-1;i>lay;i--){

                        int j=cand.size()-1;
                        while(recoverCand[0].peek()==i){
                            //由大到小，插入cand
                            int Ver=recoverCand[1].pop();
                            for(;j>=0;j--){
                                if(Ver>cand.get(j)){
                                    cand.add(j+1,Ver);
                                    break;
                                }
                            }

                            if(j==-1) {
                                cand.add(0,Ver);
                                j++;
                            }
                            recoverCand[0].pop();
                        }
                    }

                    int j=0;
                    for(j=0;j<cand.size();j++) {
                        if(S.get(lay)<cand.get(j)) {
                            cand.add(j,S.get(lay));
                            break;
                        }
                    }
                    if(j==cand.size()) cand.add(j,S.get(lay));
                    recoverCand[0].removeLast();
                    recoverCand[1].removeLast();

                    for(int i=S.size()-1;i>=lay;i--){
                        recover_Crit_Uncov(S.get(i));
                        S.remove(i);
                        intersection_stack.removeLast();
                    }


                }

                //验证通过后，判断是否重复


            }
            else if(is_Minimal==false){
                //1. 选择一个未覆盖的超边.需要剪枝找到一个最小化| F∩CAND |f和cand相交的 点最少
                minF=-1;
                min=999999;
                minC=numVertices;
                for(int i=0;i<uncovs.size();i++){
                    int sum=0;
                    for(int j=0;j<hyperedges.get(uncovs.get(i).hypeid).length;j++){
                        if(cand.contains(hyperedges.get(uncovs.get(i).hypeid)[j])) sum++;
                    }
                    if (sum<=min&&sum>0){
                        if(sum==min){
                            for(int t=0;t<hyperedges.get(uncovs.get(i).hypeid).length;t++){
                                if(minC>min_column[hyperedges.get(uncovs.get(i).hypeid)[t]]){
                                    minC=hyperedges.get(uncovs.get(i).hypeid)[t];
                                    minF=uncovs.get(i).hypeid;
                                }
                            }
                        }else{
                            min=sum;
                            minF=uncovs.get(i).hypeid;
                        }

                    }
                }
                System.out.println("minF"+minF);
                if(minF<0){
                    ver=S.removeLast();
                    //如果在这层的最末尾，需要将cand复原
                    if((!layer.empty() )&&layer.peek()!=lay){
                        System.out.println(cand);
                        System.out.println(recoverCand[0]);
                        System.out.println(recoverCand[1]);
                        recover_Crit_Uncov(ver);
                        int j=0;
                        for( j=0;j<cand.size();j++) {
                            if(S.get(layer.peek())<cand.get(j)) {
                                cand.add(j,S.get(layer.peek()));
                                break;
                            }
                        }
                        if(j==cand.size()) cand.add(j,S.get(layer.peek()));
                        for(int i=S.size();i>layer.peek();i--){
                            //将第i层迭代中的cand去掉的集合，重新加入cand
                            j=cand.size()-1;
                            while(recoverCand[0].peek()==i){
                                //由大到小，插入cand
                                int Ver=recoverCand[1].pop();
                                for(;j>=0;j--){
                                    if(Ver>cand.get(j)){
                                        cand.add(j+1,Ver);
                                        break;
                                    }
                                }
                                if(j==-1) {
                                    cand.add(0,Ver);
                                    j++;
                                }
                                recoverCand[0].pop();
                            }
                            recover_Crit_Uncov(S.getLast());
                            S.removeLast();
                            if(tointersect_queue.isEmpty()) intersection_stack.pop();
                            else tointersect_queue.removeLast();
                        }
                        recoverCand[0].removeLast();
                        recoverCand[1].removeLast();
                    }else {
                        recover_Crit_Uncov(ver);
                        cand.add(ver);
                        recoverCand[0].removeLast();
                        recoverCand[1].removeLast();
                    }
                }else{
                    //2. 从候选顶点集合中删除这些顶点，并得到c
                    c= (ArrayList<Integer>) cand.clone();
                    c1= (ArrayList<Integer>) cand.clone();
                    System.out.println("last cand"+c1);
                    int m=0;
                    //得到
                    for(int t=0;t<hyperedges.get(minF).length;t++) {
                        m=c1.indexOf(hyperedges.get(minF)[t]);
                        if(m>=0) c1.remove(m);
                    }
                    c.removeAll(c1);
                    System.out.println("cand "+cand);
                    System.out.println("c "+c);
                    cand.removeAll(c);

                    for (int v=0;v<c.size();v++) {
                        stack.push(c.get(v));
                        layer.push(lay+1);
                        recoverCand[0].add(lay+1);
                        recoverCand[1].add(c.get(v));
                    }
                }

            }
            if(stack.isEmpty()) break;//判断是否应该推出循环
            ver=stack.pop();
            lay=layer.pop();
            //3.将点ver加入候选集

            update_Crit_Uncov(ver);
            System.out.println("S "+S+" v"+ver );
            System.out.println("stack "+stack);
            S.add(ver);

            //4.判断将该点加入候选集是否满足极小性条件
            if(!isMinimal()) {
                //不满足极小性条件,进行下一个点的判断
                S.removeLast();
                //如果在这层的最末尾，需要将cand复原
                if((!layer.empty() )&&layer.peek()!=lay){

                    recover_Crit_Uncov(ver);
                    int j=0;
                    for( j=0;j<cand.size();j++) {
                        //S.get(layer.peek())代表S需删除到这个顶点
                        if(S.get(layer.peek())<cand.get(j)) {
                            //将顶点加入cand
                            cand.add(j,S.get(layer.peek()));
                            break;
                        }
                    }
                    if(j==cand.size()) cand.add(j,S.get(layer.peek()));
                    for(int i=S.size();i>layer.peek();i--){
                        //将第i层迭代中的cand去掉的集合，重新加入cand
                        j=cand.size()-1;
                        while(recoverCand[0].peek()==i){
                            //由大到小，插入cand
                            int Ver=recoverCand[1].pop();
                            for(;j>=0;j--){
                                if(Ver>cand.get(j)){
                                    cand.add(j+1,Ver);
                                    break;
                                }
                            }
                            if(j==-1) {
                                cand.add(0,Ver);
                                j++;
                            }
                            recoverCand[0].pop();
                        }
                        recover_Crit_Uncov(S.getLast());
                        S.removeLast();
                        if(tointersect_queue.isEmpty()) intersection_stack.pop();
                        else tointersect_queue.removeLast();

                    }
                    recoverCand[0].removeLast();
                    recoverCand[1].removeLast();
                }else {
                    recover_Crit_Uncov(ver);
                    cand.add(ver);
                    recoverCand[0].removeLast();
                    recoverCand[1].removeLast();
                }
                is_Minimal=true;
                System.out.println(recoverCand[0]);
                System.out.println(recoverCand[1]);

            }else {
                is_Minimal=false;
                tointersect_queue.add(ver);
            }
            //满足极小性则进行下一步,将当前判断顶点加入S
            System.out.println("S "+S);
        };
        print();
        System.out.println(System.currentTimeMillis()-startTime);
        return ;
    }
    // 更新临界顶点集合
    private  void update_Crit_Uncov(int v) {
        for (int f=0;f<hyperedges.size();f++) {
            //对于crit除v以外的更新
            boolean is_contains=false;
            for(int t=0;t<hyperedges.get(f).length;t++) {
                if(hyperedges.get(f)[t]==v){
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

}