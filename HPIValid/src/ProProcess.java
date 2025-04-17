import  java.io.*;
import java.security.PublicKey;
import java.sql.Array;
import java.util.*;
public class ProProcess {
    public   int numVer;//属性总和
    public   Map<String,ArrayList<Integer>>[] preliminaryPLI;//初始结构
    public   List<List<Integer>>[] PLI;
    public   List<String>[] PLIid;//初始结构
    public   List<int[]> reversePLI;//逆PLI
    public   List<int[]> hyperedges; // 超边集合 下标就是id

    public   int[] min_column;
    public  void proprocess() throws IOException {
        //1.读数据并生成初步PLI
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("horse.csv")));
        String line=br.readLine();
        String[] s=line.split(";");
        //初始化
        int length=0;
        numVer=s.length ;
        preliminaryPLI=new Map[numVer];//PLI结构
        PLIid=new  List[numVer];
        PLI=new List[numVer];
        min_column=new int[numVer];
        reversePLI=new ArrayList<>();
        for(int i=0;i<numVer;i++) {
            preliminaryPLI[i]=new HashMap<>();
            PLIid[i]=new ArrayList<>();
            PLI[i]=new ArrayList<>();
            min_column[i]=0;
        }
        int i;
        List<Integer> mid;
        while (true){
            for(i=0;i<numVer;i++){
                //如果PLI[j]不包含该值
                if(preliminaryPLI[i].get(s[i])==null) {
                    preliminaryPLI[i].put(s[i],new ArrayList<Integer>());
                    PLIid[i].add(s[i]);
                }

                preliminaryPLI[i].get(s[i]).add(length);//将记录的ID加入哈希映射
                //System.out.print(PLIid[j].indexOf(record.get(j))+" ");
            }
            if((line= br.readLine())==null) break;
            length++;
            s=line.split(";");
        }

        length++;
        br.close();
        //2.逆PLI的初始化


        for(i=0;i<numVer;i++)  reversePLI.add(new int[length]);
        List<Integer> order=new ArrayList<>();
        //4.去除平凡簇并生成逆PLI
        String value;
        for( i=0;i<numVer;i++){
            for(int j=0;j<PLIid[i].size();j++){
                value=PLIid[i].get(j);
                if(preliminaryPLI[i].get(value).size()==1) {//如果为平凡簇
                    mid=preliminaryPLI[i].get(value);
                    reversePLI.get(i)[mid.get(0)]=-1;//将逆PLI置为-1
                    //清理初始结构
                    mid.clear();
                    preliminaryPLI[i].remove(value);
                    PLIid[i].remove(j);

                    j--;
                }else{//非平凡簇
                    //得到属性i的PLI的一个簇
                    mid=preliminaryPLI[i].get(value);
                    List<Integer> cluster=new ArrayList<>();
                    min_column[i]+=mid.size();
                    for(int t=0;t<mid.size();t++) {
                        cluster.add(mid.get(t));
                        reversePLI.get(i)[mid.get(t)]=j;
                    }
                    PLI[i].add(cluster);//将簇加入PLI
                    mid.clear();//清理初始结构
                }
            }
            //清理属性i的初始结构 String结构没法清除
            preliminaryPLI[i].clear();
            PLIid[i].clear();
            // 得到PLI簇数量的顺序
            for(int j=0;j<i;j++){
                if(min_column[i]<min_column[order.get(j)]){
                    order.add(j,i);
                    break;
                }
            }
            if(order.size()==i) order.add(i);
        }
        //将顺序存入
        for(i=0;i<numVer;i++) min_column[i]=order.indexOf(i);

        order.clear();
    }



}