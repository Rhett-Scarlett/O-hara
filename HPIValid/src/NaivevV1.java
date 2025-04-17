import java.io.*;
import java.util.*;

public class NaivevV1 {
    public static   int numVer;//属性总和
    public static   Map<String, ArrayList<Integer>>[] preliminaryPLI;//初始结构
    public static   List<List<Integer>>[] PLI;
    public static   List<String>[] PLIid;//初始结构
    public static   int[][] reversePLI;//逆PLI
    public static List<List<Integer>> hyperedges; // 超边集合 下标就是id

    public static  int[] min_column;

    /*
        public static void main(String args[]) throws IOException {
            /*
            FileInputStream filein=new FileInputStream("data.txt");
            InputStream bfin=new BufferedInputStream(filein);
            DataInputStream in=new DataInputStream(bfin);


            hashMap//每一列 创建一个从值到各自簇的初始数据结构
            inverseMap	//创建逆映射
            for each row/recode in table do
                for each column in table do //对于每个属性a,都有
                    if ci not in hashMap[a] then	//如果是未出现过的值
                         add ci to hashMap[a] //将该值加入哈希映射
                    end if
                    add recordID to hashMap[a][ci] 	//将记录的ID加入哈希映射
                    inverseMap[recodeID][a]<- ci	//将ci的簇Id写入逆映射
                end for
            end for


            FileOutputStream fileout=new FileOutputStream("data.txt");

            OutputStream bfout=new BufferedOutputStream(fileout);
            DataOutputStream out=new DataOutputStream(bfout);
            //输出数据
            int[] tuple=new int[8];//元组
            Random random=new Random();
            char c='\n';
            out.writeInt(8);
            out.writeInt(30);
            for(long i=0;i<30;i++){
                for(int j=0;j<8;j++){
                    tuple[j]=random.nextInt(20);
                    out.writeInt(tuple[j]);
                }
            }
            out.flush();
            out.close();
            bfout.close();
            fileout.close();
        }

     */
    public static void main(String args[]) throws IOException {
        /*long st= System.currentTimeMillis();
       proprocess();
        long end=System.currentTimeMillis();
        System.out.print(end-st);
*/
       ProProcess pr=new ProProcess();
       pr.proprocess();
        Sample sample=new Sample(pr.numVer,pr.PLI,pr.reversePLI);
        hyperedges=sample.hyperedges;
        for(int i=0;i<pr.numVer;i++) sample.sample(pr.PLI[i]);
        Validation validation=new Validation(pr.numVer,pr.PLI,pr.reversePLI);
        // 测试验证
//        LinkedList<Integer> ucc = new LinkedList<>();
//        ucc.add(18); ucc.add(21); ucc.add(0); ucc.add(4); ucc.add(7); ucc.add(9);
//        Stack<List<List<Integer>>> stack = new Stack<>();
//        stack.add(pr.PLI[24]);
//        validation.pullUpIntersections(stack,ucc);
//        for (int i =0;i<stack.peek().size();i++){
//            System.out.println("Cluster " + i + ": " + stack.peek().get(i));
//        }
//        ucc.add(18); ucc.add(21); ucc.add(0); ucc.add(4); ucc.add(7); ucc.add(9);
        List<int[]> reversePLI = pr.reversePLI;
        for (int i =0;i<reversePLI.get(0).length;i++){
            System.out.print(i+" ");
            for (int j =0;j<reversePLI.size();j++){
                System.out.print(" "+reversePLI.get(j)[i]);
            }
            System.out.println();

        }
        MMCS_v2 mmcs=new MMCS_v2(pr.numVer,hyperedges,validation,sample,pr.min_column,pr.PLI);

       mmcs.mmcs(new ArrayList<>());
    }


}