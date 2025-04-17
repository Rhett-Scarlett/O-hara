import java.util.*;

public class Sample {
    //初始化
    public  int numVer ;//超图顶点数=属性和
    public  List<List<Integer>>[] PLI;
    public  List<int[]> reversePLI;//逆PLI
    public  List<Integer> probablily;
    public  List<List<Integer>> hyperedges; // 超边集合 下标就是id
    Sample(int numVer1,List<List<Integer>>[] PLI1,List<int[]> reversePLI1){
        numVer=numVer1;
        PLI=PLI1;
        reversePLI=reversePLI1;
        probablily=new ArrayList<>();
        hyperedges=new ArrayList<>();
    }
    //判断超图是否为最小化
    public boolean is_subset_of(List<Integer> a,List<Integer>  b){
       if(a.size()>b.size()) return false;
       int a_l=0;
       for(int i=0;i<b.size()&&a_l  <a.size();i++){
           if(b.get(i)>a.get(a_l)) return false;
           if(b.get(i)==a.get(a_l)) a_l++;

       }
       if(a_l==a.size()) return true;
       else  return false;
    }
    //判断某边是否加入超图
    public void addEdgeAndMinimizeInclusion(List<Integer> edge) {
        // is newEdge a supset of an edge in m_Edges?
        boolean is_supset = false;
        // list of indeces of supsets of newEdge from m_Edges in descending order
        List<Integer> supsets_indeces = new ArrayList<>();
        for (int i_e=0;i_e<hyperedges.size();i_e++) {

            if (is_subset_of(hyperedges.get(i_e),edge)) {
                is_supset = true;
                break;
            }

            if (is_subset_of(edge,hyperedges.get(i_e))) {
                supsets_indeces.add(i_e);
            }
        }

        if (!is_supset) {
            for (int i=0;i<supsets_indeces.size();i++) hyperedges.remove(supsets_indeces.get(i));
            hyperedges.add(edge);
        }
    }


    //验证失败跳转至采样
    public int sample(List<List<Integer>> cluster){
        double x = 0.2;
        int p = 0;
        int edge_size=hyperedges.size();
        probablily.clear();
//        for(int i=0;i<cluster.size();i++){
//            System.out.println(i+" "+cluster.get(i));
//        }
        //1.得到可取样的采样对数
        for (int i = 0; i< cluster.size(); i++) {
            int num = cluster.get(i).size();
            p += num * (num - 1) / 2;
            probablily.add(p);
        }
        //System.out.println("P "+p);
        int sum= (int) StrictMath.pow((double) p, x);
        Random random=new Random();
        // System.out.println("sum "+sum);
        //2.得到记录对
        for(int i=0;i<sum;i++) {
            int selectedCi = random.nextInt(p );
            selectedCi++;
            // System.out.println("1selectedCi  "+selectedCi);
            int t = 0;
            for (t = 0; probablily.get(t) < selectedCi; t++) ;
            selectedCi = t;//得到随机簇
            //System.out.println("2selectedCi  "+selectedCi);
            t = random.nextInt(cluster.get(selectedCi).size());
            int t1 = random.nextInt(cluster.get(selectedCi).size());
            if (t == t1) {
                if (t1==cluster.get(selectedCi).size()-1) t1 --;
                else t1 ++;
            }
            //得到记录对值
            t = cluster.get(selectedCi).get(t);
            t1 = cluster.get(selectedCi).get(t1);
            //System.out.println(t+" "+t1);
            //System.out.println("t "+t+"    t1 "+t1);
            //3.根据记录对得到超边并判断超边为极小边

            List<Integer> edge=new ArrayList<>();
            for(int m=0;m<numVer;m++){
                if(reversePLI.get(m)[t]==-1||reversePLI.get(m)[t1]!=reversePLI.get(m)[t]) edge.add(m);
            }
            //System.out.println(edge);
            addEdgeAndMinimizeInclusion(edge);

        }

        return hyperedges.size()-edge_size;//返回新增边数量
    }
}