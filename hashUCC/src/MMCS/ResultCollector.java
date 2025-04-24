package MMCS;


import java.util.ArrayList;
import java.util.List;

//记录最终的最小覆盖集结果集以及输出
public class ResultCollector {
    private List<List<Integer>> minUCC;
    public ResultCollector(){
        minUCC = new ArrayList<>();
    }
    public void addMinUCC(List<Integer> ucc){
        minUCC.add(ucc);
    }

    public void print(){
        System.out.println("minUCC");
        int min;
        for(int i=0;i<minUCC.size();i++){
            min=i;
            for(int j=i;j<minUCC.size();j++){
                for(int t=0;t<(minUCC.get(min).size()<minUCC.get(j).size()?minUCC.get(min).size():minUCC.get(j).size());t++){
                    if(minUCC.get(min).get(t)>minUCC.get(j).get(t)) {
                        min=j;
                        break;
                    }else if(minUCC.get(min).get(t)<minUCC.get(j).get(t)) break;
                }
            }
            System.out.print(i+"[");
            for(int j=0;j<minUCC.get(min).size();j++) System.out.print(" " +minUCC.get(min).get(j));
            System.out.println("]");
            minUCC.set(min,minUCC.get(i));
        }


        System.out.println(minUCC.size());
    }
}
