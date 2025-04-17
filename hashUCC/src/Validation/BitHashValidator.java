package Validation;

import java.io.*;
import java.util.*;

/**
 * AttributeCombinationDuplicateDetector 用于验证指定属性组合的重复情况。
 * 这里的属性组合只针对需要验证的属性子集，而非元组中所有属性。
 *
 * 每个属性在验证过程中都维护一个 BitSet，记录该属性中某个簇（cluster）的出现情况。
 * 当处理一个元组时，如果属性组合中所有属性对应的簇均已出现，则认为该属性组合可能重复，
 * 并记录当前元组ID。
 */
public class BitHashValidator {
    // candidateSize 表示属性组合的大小（即要验证的属性个数）
    private int candidateSize;
    private List<Integer> clusterSizes;
    // 每个需要验证的属性对应一个 BitSet，用来记录该属性中某个簇是否已出现过
    private List<BitSet> bitSets;
    // 记录可能重复的元组ID列表（即属性组合均已出现过的元组）
    private Map<IntArrayKey,Integer> hashMap;
    private Map<Integer,List<Integer>> hashM;
    private List<DataInputStream> readers;
    private List<Boolean> clusterCheck;
    private DataOutputStream write;
    private int fileLength;


    /**
     * 构造函数，初始化属性组合验证器
     */
    public BitHashValidator(List<Integer> clusterSizes, List<String> columnFiles, int fileLength, String outfile) throws FileNotFoundException {
        this.clusterSizes = clusterSizes;
        this.candidateSize = columnFiles.size();
        this.fileLength = fileLength;
        this.bitSets = new ArrayList<>(candidateSize);
        this.readers = new ArrayList<>(candidateSize);
        this.clusterCheck = new ArrayList<>();
        this.hashMap = new HashMap<>();
        this.write = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outfile)));
        // 初始化 BitSet 和 readers
        for (String filePath : columnFiles) {
            this.bitSets.add(new BitSet());
            this.readers.add(new DataInputStream(new BufferedInputStream(new FileInputStream(filePath))));
        }

    }


    /**
     * 读取一行所有列文件
     *
     * @throws IOException 如果读取文件时发生错误
     */
    private boolean getTuple(int[] tuple) throws IOException {
        boolean flag = true;
        for (int i = 0; i < readers.size(); i++) {
            int value = readers.get(i).readInt();

            if (value == -1) {
                for(++i;i<readers.size();i++) readers.get(i).readInt();
                return false;
            }
            tuple[i] = value;
        }
        return true;
    }

    /**
     * 第一次遍历：读取所有列文件，得到所有可能的簇
     * @throws IOException 如果读取文件时发生错误
     */
    public void processTuples(String outfile) throws IOException {
        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outfile)));
        //用来分配新的簇 ID，初始为 0
        int clusterId = 0;

        //遍历所有的元组 ID，candidateSize 是候选属性组合的长度。
        for(int tupleID = 0; tupleID < fileLength; tupleID++) {
            int[] tupleValues = new int[candidateSize];

            //读取元组数据填入 tupleValues。 如果 getTuple() 返回 false，说明元组无效，写入 -1 作为标记
            if (!getTuple(tupleValues)) {
                output.writeInt(-1);
                //System.out.println(tupleID+" -1");
                continue;
            }
            //System.out.print(tupleID+" ");
            //for (int i: tupleValues) System.out.print(i+" ");
            //System.out.println();
            if(checkBitset(tupleValues)) {//可能重复元组
                IntArrayKey key = new IntArrayKey(tupleValues);

                //哈希表中存在该键则说明一定是重复属性组合值
                if(!hashMap.containsKey(key)) {
                    //不存在
                        //System.out.println( "pro "+tupleID+" "+ key.hashCode());
                    clusterCheck.add(false);
                    hashMap.put(key, -2-clusterId);
                    clusterId++;
                }else{
                    clusterCheck.set(-hashMap.get(key)-2, true);
                    //System.out.println( "true "+tupleID+" "+ hashMap.get(key));
                }

                output.writeInt(hashMap.get(key));
            }else for (int value : tupleValues)   output.writeInt(value);//存入各个属性的值
        }

        output.close();
    }



    /**
     * 检查当前元组是否可能重复
     */
    public boolean checkBitset(int[] tuple) throws IOException {
        // 标记是否所有属性的簇均已出现过
        boolean allAttributesSeen = true;

        // 遍历属性组合中的每个属性
        for (int i = 0; i < candidateSize; i++) {
            BitSet bitSet = bitSets.get(i);
            // 如果当前属性的该簇还未出现，则更新位图，并标记当前属性为首次出现
            if (!bitSet.get(tuple[i])) {
                bitSet.set(tuple[i]);
                allAttributesSeen = false;
            }
        }
        return allAttributesSeen;
    }

    /**
     * 第二次遍历：检查所有可能簇，处理后得到最终确定的结果
     */
    public void processClusters(String infile) throws IOException {
        DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(infile)));

        // 存储新的 hash 映射关系
        hashM = new HashMap<>();


        // 新簇的编号递增器
        int clusterId = 0;
        Map<Integer, Integer> newClusters = new HashMap<>();
        for (int tupleID = 0; tupleID < fileLength; tupleID++) {
            int value = input.readInt();// 读取当前 tuple 对应的值

            // 情况 1：无效元组，直接写入 -1 表示无效
            if (value == -1)  write.writeInt(-1);
            else if (value > -1){
                // 情况 2：value > -1，表示当前是一个新的 tuple，接下来还需要读取属性值
                int[] tupleValues = new int[candidateSize];
                tupleValues[0] = value;// 第一个属性值

                // 继续读取剩余的属性值
                for(int i = 1; i < candidateSize; i++) tupleValues[i] = input.readInt();

                // 使用属性值构造键值
                IntArrayKey key = new IntArrayKey(tupleValues);

                // 如果 hashMap 中存在该键，说明这个组合之前已经见过
                if(hashMap.containsKey(key)) {
                    hashM.computeIfAbsent(clusterId, k -> new ArrayList<>()).add(tupleID);

                    write.writeInt(clusterId);// 分配新的 clusterId 并写入
                    newClusters.put(hashMap.get(key), clusterId++); // 记录旧簇 id -> 新簇 id 的映射关系
                }else write.writeInt(-1);
            }else{
                // 情况 3：value < -1，表示这是之前的 clusterId（负数表示）
                //System.out.println(tupleID+" "+newClusters.containsKey(value)+" "+clusterCheck.get(-value-2));
                if(newClusters.containsKey(value)) {

                    hashM.get(newClusters.get(value)).add(tupleID);

                    write.writeInt(newClusters.get(value));// 如果该 cluster 已经分配了新的 clusterId，直接写入
                }
                else if(clusterCheck.get(-value-2)){

                    hashM.computeIfAbsent(clusterId, k -> new ArrayList<>()).add(tupleID);

                    // 如果原 cluster 被标记为“有效 cluster”，分配一个新的 clusterId
                    newClusters.put(value, clusterId);
                    write.writeInt(clusterId++);
                }else write.writeInt(-1);  // 否则，说明这个 cluster 已经无效，写入 -1 表示无效
            }
        }
        input.close();
        write.close();

    }

    public Map<Integer,List<Integer>> Validator(String outfile) throws IOException {
        String tempFile = "temp_clusters.dat";
        processTuples(tempFile);
        processClusters(tempFile);
        for (Map.Entry<Integer, List<Integer>> entry : hashM.entrySet()) {
            Integer clusterId = entry.getKey();
            List<Integer> tupleIds = entry.getValue();

//            // 你可以在这里对 clusterId 和 tupleIds 做处理
//            System.out.println("Cluster " + clusterId + ": " + tupleIds);
        }
        return hashM;


    }

}

