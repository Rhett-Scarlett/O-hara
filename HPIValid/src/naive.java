import java.io.*;
import java.util.*;

public class naive {
    public static   int numVer;//属性总和
    public static   Map<String, ArrayList<Integer>>[] preliminaryPLI;//初始结构
    public static   List<List<Integer>>[] PLI;
    public  static List<int[]> reversePLI;//逆PLI
    public static List<int[]> hyperedges; // 超边集合 下标就是id

//    public  static void proprocess() throws IOException {
//        //1.读数据并生成初步PLI
//        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("STRUCT_SHEET_RANGE.csv")));
//        String line=br.readLine();
//        String[] s=line.split(";");
//        //初始化
//        int length=0;
//        numVer=s.length ;
//        preliminaryPLI=new Map[numVer];//PLI结构
//        PLIid=new  List[numVer];
//        PLI=new List[numVer];
//        System.out.println(numVer);
//        for(int i=0;i<numVer;i++) {
//            preliminaryPLI[i]=new HashMap<>();
//            PLIid[i]=new ArrayList<>();
//            PLI[i]=new ArrayList<>();
//
//        }
//
//        int i;
//        List<Integer> mid;
//        while (true){
//            for(i=0;i<numVer;i++){
//                //如果PLI[j]不包含该值
//                if(preliminaryPLI[i].get(s[i])==null) {
//                    preliminaryPLI[i].put(s[i],new ArrayList<Integer>());
//                    PLIid[i].add(s[i]);
//                }
//
//                preliminaryPLI[i].get(s[i]).add(length);//将记录的ID加入哈希映射
//                //System.out.print(PLIid[j].indexOf(record.get(j))+" ");
//            }
//            if((line= br.readLine())==null) break;
//            length++;
//            s=line.split(";");
//        }
//        length++;
//
//
//
//        //4.去除平凡簇并生成逆PLI
//        String value;
//        for( i=0;i<numVer;i++){
//
//            for(int j=0;j<PLIid[i].size();j++){
//                value=PLIid[i].get(j);
//                if(preliminaryPLI[i].get(value).size()>1){//非平凡簇
//                    //得到属性i的PLI的一个簇
//                    mid=preliminaryPLI[i].get(value);
//                    int[] cluster=new int[mid.size()];
//
//                    for(int t=0;t<cluster.length;t++) {
//                        cluster[t]=mid.get(t);
//
//                    }
//                    PLI[i].add(cluster);//将簇加入PLI
//                    mid.clear();//清理初始结构
//                    System.out.println(i+" +"+value);
//                }
//            }
//            //清理属性i的初始结构 String结构没法清除
//        }
//
//    }
    public static void main(String args[]) throws IOException {


        long st1= System.currentTimeMillis();
        ProProcess pro=new ProProcess();
        pro.proprocess();
        numVer= pro.numVer;
        PLI=pro.PLI;
        reversePLI=pro.reversePLI;
        Sample sample=new Sample(numVer,PLI,reversePLI);
        for (int i=0;i<numVer;i++) sample.sample(PLI[i]);
        long end1=System.currentTimeMillis();
        System.out.println(end1-st1);

    }
}
