package test;

import java.lang.reflect.Array;
import java.util.*;

public class Validation {
    public  List<int[]> reversePLI;
    public  List<List<Integer>>[] PLI;//vPLI结构
    public  List<List<Integer>> sPLI;
    public  int numVer;
    public  List<List<Integer>> m_clusterid_to_recordindeces;//
    public Validation(int numVer1, List<List<Integer>>[] PLI1, List<int[]> reversePLI1){
        numVer=numVer1;
        PLI=PLI1;
        reversePLI=reversePLI1;
    }
    // 得到svPLI
    public  void intersect_svPLI(int ver){
        //建立svPLI
        List<List<Integer>> Sv=new ArrayList<>();//值对应ID
        List<List<Integer>> cluter=new ArrayList<>();
        List<List<Integer>> cluster=new ArrayList<>();
        for(int i=0;i<PLI[ver].size();i++) cluster.add(new ArrayList<>());
        int[] reversePLIV=reversePLI.get(ver);//v的逆PLI
        List<Integer> meet=new ArrayList<>();//满足条件的v的簇
        long st= System.currentTimeMillis();
        System.out.println(sPLI.size());
        for(int i=0;i<sPLI.size();i++){
            //不需要知道簇的值，只需要知道簇id就可以了
            for(int  scluster_value: sPLI.get(i)){
                int new_cluster=reversePLIV[scluster_value];
                if(new_cluster!=-1) {
                    cluster.get(new_cluster).add(scluster_value);
                }

            }
            //去掉平凡簇
            for(int j=0;j<cluster.size();j++){
                if(cluster.get(j).size()>1){
                    cluter.add(cluster.get(j));
                    cluster.add(j,new ArrayList<>());
                    cluster.remove(j+1);
                }
                cluster.get(j).clear();
            }

        }
        long end=System.currentTimeMillis();
        System.out.println(ver+" "+(end-st));
        //清理结构缓存
        sPLI.clear();
        sPLI=cluter;
    }
    public  List<int[]> validation(ArrayList<Integer> candidateUCC)  {
        System.out.println("____________________________________");
        System.out.println("Validation");
        System.out.println("____________________________________");
        System.out.println(candidateUCC);
        int i,j=1;
        sPLI=new ArrayList<>();

        //读的第一个属性
        j=candidateUCC.get(0);
        System.out.println(0);
        for(i=0;i<PLI[j].size();i++){
            List<Integer> list=new ArrayList<>();
            for(int t=0;t<PLI[j].get(i).size();t++) list.add(PLI[j].get(i).get(t));
            sPLI.add(list);
        }

        //读剩下的属性
        for(i=1;i<candidateUCC.size();i++){
            System.out.println(i);
            intersect_svPLI(candidateUCC.get(i));
        }
        //得到S+VPLI后，判断其是否平凡
        System.out.println(144);
        for(i=0;i<sPLI.size();i++){
            if(sPLI.get(i).size()>1) {
                System.out.println(i+" "+sPLI.get(i));
                //转化成数组
                for(int m=0;m<sPLI.get(i).size();m++){
                    for(int n=0;n<numVer;n++) System.out.print(reversePLI.get(n)[sPLI.get(i).get(m)]+" ");
                    System.out.println();
                }
                List<int[]> s=new ArrayList<>();
                for(int m=0;m<sPLI.size();m++) {
                    int[] array=new int[sPLI.get(m).size()];
                    for(int n=0;n<sPLI.get(m).size();n++) array[n]=sPLI.get(m).get(n);
                    s.add(array);
                    sPLI.get(m).clear();
                }
                sPLI.clear();
                return s;
            }
        }
        return  null;
    }
    /*public  void intersect_svPLI(List<int[]> vPLI,int ver){
        //建立svPLI
        List<List<Integer>> Sv=new ArrayList<>();//值对应ID
        List<List<Integer>> cluter=new ArrayList<>();
        int[] reversePLIV=reversePLI.get(ver);//v的逆PLI
        System.out.println("SPLI___________________________________");
        for(int i=0;i<sPLI.size();i++){
            System.out.println(i+" "+sPLI.get(i));
        }
        List<Integer> meet=new ArrayList<>();//满足条件的v的簇
        for(int i=0;i<sPLI.size();i++){
            //不需要知道簇的值，只需要知道簇id就可以了
            for(int j=0;j<sPLI.get(i).size();j++){
                int new_cluster=reversePLIV[sPLI.get(i).get(j)];
                if(new_cluster==-1) continue;
                if(!meet.contains(new_cluster)){//是新簇
                    cluter.add(new ArrayList<>());
                    cluter.getLast().add(sPLI.get(i).get(j));//将新簇加入svPLI
                    meet.add(new_cluster);//将满足条件的v值加入meet
                    //System.out.println(new_cluster+" new  "+sPLI.get(i).get(j));
                }else {
                    cluter.get(meet.indexOf(new_cluster)).add(sPLI.get(i).get(j));
                    ///System.out.println(meet);
                    //System.out.println(meet.indexOf(new_cluster)+" "+new_cluster+"  "+sPLI.get(i).get(j));
                }

            }
            //去掉平凡簇
            for(int j=0;j<meet.size();j++){
                if(cluter.get(j).size()==1) {
                    cluter.remove(j).clear();
                    meet.remove(j);
                    j--;
                }
            }
            meet.clear();
            Sv.addAll(cluter);
            cluter.clear();
            sPLI.get(i).clear();
        }
        //清理结构缓存
        sPLI.clear();
        sPLI=Sv;
    }
    public  List<int[]> validation(ArrayList<Integer> candidateUCC)  {
        System.out.println("____________________________________");
        System.out.println("Validation");
        System.out.println("____________________________________");
        System.out.println(candidateUCC);
        int i,j=1;
        sPLI=new ArrayList<>();

        //读的第一个属性
        j=candidateUCC.get(0);
        for(i=0;i<PLI[j].size();i++){
            List<Integer> list=new ArrayList<>();
            for(int t=0;t<PLI[j].get(i).length;t++) list.add(PLI[j].get(i)[t]);
            sPLI.add(list);
        }

        //读剩下的属性
        for(i=1;i<candidateUCC.size();i++){
            // System.out.println("validation "+candidateUCC.get(i));
            //for(int t=0;t<50;t++) System.out.print(reversePLI.get(candidateUCC.get(i))[t]);
            intersect_svPLI(PLI[candidateUCC.get(i)],candidateUCC.get(i));
        }
        //得到S+VPLI后，判断其是否平凡
        for(i=0;i<sPLI.size();i++){
            if(sPLI.get(i).size()>1) {
                System.out.println(i+" "+sPLI.get(i));
                //转化成数组
                for(int m=0;m<sPLI.get(i).size();m++){
                    for(int n=0;n<numVer;n++) System.out.print(reversePLI.get(n)[sPLI.get(i).get(m)]+" ");
                    System.out.println();
                }
                List<int[]> s=new ArrayList<>();
                for(int m=0;m<sPLI.size();m++) {
                    int[] array=new int[sPLI.get(m).size()];
                    for(int n=0;n<sPLI.get(m).size();n++) array[n]=sPLI.get(m).get(n);
                    s.add(array);
                    sPLI.get(m).clear();
                }
                sPLI.clear();
                return s;
            }
        }
        return  null;
    }

*/

    //验证主函数，输入，
    public void pullUpIntersections(Stack<List<List<Integer>>> intersection_stack,LinkedList<Integer> tointersect_queue){
        m_clusterid_to_recordindeces=new ArrayList<>();
        for(int i=0;i<reversePLI.get(0).length;i++) m_clusterid_to_recordindeces.add(new ArrayList<>());

        while(!tointersect_queue.isEmpty()){
            //System.out.println(tointersect_queue.get(0));

            intersection_stack.push(intersectClusterListAndClusterMapping(intersection_stack.peek(),reversePLI.get(tointersect_queue.get(0))));
            tointersect_queue.remove(0);
        }
    }

    //合并sPLI和vPLI，输入sPLI和v的逆PLI
    public List<List<Integer>> intersectClusterListAndClusterMapping(List<List<Integer>> PLI,int[] reversePLI_v){
        List<List<Integer>> intersection=new ArrayList<>();
        List<Integer> clusterIds=new ArrayList<>();
        for(List<Integer> cluster:PLI){
            clusterIds.clear();
            for(int i_r:cluster){
                if(reversePLI_v[i_r]!=-1){
                    List<Integer> map_entry=m_clusterid_to_recordindeces.get(reversePLI_v[i_r]);
                    if(map_entry.size()==0) clusterIds.add(reversePLI_v[i_r]);
                    map_entry.add(i_r);
                }
            }
            for(int clusterid:clusterIds){
                List<Integer> map_entry=m_clusterid_to_recordindeces.get(clusterid);
                if(map_entry.size()!=1){
                    intersection.add(map_entry);
                }
                m_clusterid_to_recordindeces.remove(clusterid);
                m_clusterid_to_recordindeces.add(clusterid,new ArrayList<>());
            }

        }
        return intersection;
    }


}
