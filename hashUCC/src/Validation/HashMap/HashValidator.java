package Validation.HashMap;

import Validation.IntArrayKey;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class HashValidator {
    private DataInputStream master_CL;  // 主列文件的输入流
    private List<DataInputStream> readers;  // 存储每个列文件的输入流
    private Map<IntArrayKey, List<Integer>> hashTable;
    private Map<Integer, List<Integer>> hashMap ;
    private int file_size;
    private int candidateSize;


    /**
     * 构造函数初始化文件读取器、验证器和二进制生成器
     *
     * @param columnFiles  存储其他列文件路径的列表
     * @param master       主列文件路径
     * @throws IOException              如果文件操作失败
     * @throws NoSuchAlgorithmException 如果没有找到指定的算法
     */
    public HashValidator(List<String> columnFiles, String master, int file_size) throws IOException, NoSuchAlgorithmException {
        this.readers = new ArrayList<>();
        hashTable = new HashMap<>();
        this.file_size = file_size;
        this.candidateSize = columnFiles.size()+1;
        initializeReadersAndByteLengths(columnFiles, master);  // 初始化文件读取器

    }

    /**
     * 初始化每个列文件的 DataInputStream，并为主列文件初始化主读取器
     *
     * @param columnFiles  存储列文件路径的列表
     * @param master       主列文件路径
     * @throws IOException 如果文件操作失败
     */
    private void initializeReadersAndByteLengths(List<String> columnFiles, String master) throws IOException {
        // 初始化主列文件读取器
        master_CL = new DataInputStream(new BufferedInputStream(new FileInputStream(master)));

        // 遍历列文件，初始化每个列文件的输入流
        for (String filePath : columnFiles) {
            DataInputStream br = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
            readers.add(br);
        }
    }



    /**
     * 读取单个列文件的对应值
     *
     * @throws IOException 如果读取文件时发生错误
     */
    public int getColumnValue(DataInputStream read, long bytesToSkip) throws IOException {
        long bytesSkipped = 0;

        while (bytesSkipped < bytesToSkip) {
            long remainingBytes = bytesToSkip - bytesSkipped;
            long skipCount = read.skip(remainingBytes);
            if (skipCount == 0) {
                // 如果 skipCount 为 0，表示无法再跳过字节，可能已到达文件末尾
                return -2;
            }
            bytesSkipped += skipCount;
        }
        return read.readInt();
    }


    /**
     * 处理元组并执行哈希验证
     *
     * @throws IOException 如果读取文件时发生错误
     */
    public Map<Integer,List<Integer>> processTuples() throws IOException {
        int masterClusterID, masterTupleID,lastTupleID = 0;
        int current = 0;
        // 逐行读取主列文件和其他列文件
        int length = master_CL.readInt();
        while (current++ < length) {
            // 读取 master_CL 中的元组ID和主簇ID

            masterTupleID = master_CL.readInt();
            masterClusterID = master_CL.readInt();
            long bytesToSkip = (masterTupleID - lastTupleID)*4;
            System.out.println(masterTupleID+" "+masterClusterID+" "+lastTupleID+" " + bytesToSkip/4);

            int[] clusterIDs = new int[readers.size()+1];
            clusterIDs[0] = masterClusterID;
            boolean valid = true;

            // 同步读取其他列文件的元组ID和簇ID
            for (int i =1 ;i<= readers.size();i++) {
                int columnValue = getColumnValue(readers.get(i-1), bytesToSkip);

                if (columnValue < 0) valid = false;
                clusterIDs[i] = columnValue;
            }

            // 如果所有列都有效，生成二进制数据并存储
            if (valid) {
                IntArrayKey key = new IntArrayKey(clusterIDs); // 生成二进制数据
                for (int value : clusterIDs) System.out.print( value+" ");
                System.out.println();
                hashTable.computeIfAbsent(key, k -> new ArrayList<>()).add(masterTupleID);  // 使用验证器检查是否存在重复数据
            }

            lastTupleID = masterTupleID+1;  // 处理下一个元组
        }

        close();
        int i=0;
        hashMap =new HashMap<>();
        List<Integer> cluster;
        for (Map.Entry<IntArrayKey, List<Integer>> entry : hashTable.entrySet()) {
            cluster = entry.getValue();
            if(cluster.size()>1){
                System.out.println("簇"+i+cluster);
                hashMap.put(i++,cluster);
            }
        }

        return hashMap;
    }



    /**
     * 关闭所有打开的流
     *
     * @throws IOException 如果关闭流时发生错误
     */
    public void close() throws IOException {
        // 关闭 master_CL 和其他列文件的输入流
        master_CL.close();
        for (DataInputStream reader : readers) {
            reader.close();
        }
    }
}

