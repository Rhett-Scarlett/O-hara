package Validation.BitMap;

import Validation.IntArrayKey;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BitMapValidator {


    private List<String> columnFiles; // 存储列文件路径的列表
    private List<DataInputStream> readers; // 存储每个列文件的 BufferedReader
    private AttributeCombinationValidator validator; // 用于重复检查的验证器
    private TupleBinaryGenerator generator;
    private DataOutputStream write;

    private Map<Integer, List<Integer>> hashMap ;

    private Map<Integer, Integer> clustersMap ;

    private int fileLength;

    private DataOutputStream writef;
    private String outputFile;

    /**
     * 构造函数，初始化文件读取器、计算字节长度，并初始化验证器。
     *
     * @param columnFiles 列文件路径列表
     * @param bitmapSize  位图大小，用于重复检测
     * @param outputFile  处理后的数据存储文件
     * @throws IOException              文件操作失败时抛出
     * @throws NoSuchAlgorithmException 如果哈希算法不可用，则抛出异常
     */
    public BitMapValidator(List<String> columnFiles, int bitmapSize, String outputFile,TupleBinaryGenerator generator,int fileLength) throws IOException, NoSuchAlgorithmException {
        hashMap = new HashMap<>();
        clustersMap = new HashMap<>();

        this.columnFiles = columnFiles;
        this.readers = new ArrayList<>();
        this.writef = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        this.write = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("temp")));
        initializeReadersAndByteLengths();
        this.validator = new AttributeCombinationValidator(bitmapSize, this.write,this.clustersMap);
        this.generator=generator;
        this.fileLength = fileLength;



    }

    /**
     * 初始化每个列文件的 BufferedReader，并计算所需的字节长度。
     * @throws IOException 如果打开文件时发生错误
     */
    private void initializeReadersAndByteLengths() throws IOException {
        for (String filePath : columnFiles) {
            DataInputStream br = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
            readers.add(br);
        }
    }
    /**
     * 读取一行所有列文件，生成二进制数据，并检查唯一性。
     * @throws IOException 如果读取文件时发生错误
     */
    private List<Integer> getTuple() throws IOException {
        List<Integer> tupleValues = new ArrayList<>(readers.size());
        int i ;
        for (i = 0; i < readers.size(); i++) {
            int value = readers.get(i).readInt();
            if (value == -1) {
                tupleValues.add(0, -1);
                i++;
                break;
            }

            tupleValues.add(value);
        }
        for( ; i < readers.size(); i++) readers.get(i).readInt();
        return tupleValues;
    }

    /**
     * 读取所有列文件，逐行生成二进制数据，并检查唯一性。
     * @throws IOException 如果读取文件时发生错误
     */
    public Map<Integer,List<Integer>> processTuples() throws IOException {
        int tupleID = 0;
        while (tupleID<fileLength) {
            List<Integer> tupleValues = getTuple();

            if (tupleValues.isEmpty()) break;
            tupleID++;
            if (tupleValues.get(0) == -1) {
                write.writeInt(-1);
                continue;  // 提前处理结束标记，继续读取下一个元组
            }

            int idx = generator.generateTupleIndex(tupleValues); // 生成组合的“二进制索引”
            validator.isUniqueTuple(idx); // 检查是否唯一，记录重复元组


        }
        close();
        genrateWrite();

        for (Map.Entry<Integer,List<Integer>> entry : hashMap.entrySet()){
            System.out.println("cluster"+entry.getKey()+" : "+entry.getValue());
        }
        return hashMap;
    }

    public void genrateWrite() throws IOException {
        DataInputStream input = new DataInputStream((new BufferedInputStream(new FileInputStream("temp"))));
        for (int tupleID = 0; tupleID < fileLength; tupleID++) {
            int value = input.readInt();

            if (value == -1 || !clustersMap.containsKey(value)) {
                writef.writeInt(-1);
                continue;
            }
            value = clustersMap.get(value);
            hashMap.computeIfAbsent(value, k->new ArrayList<>()).add(tupleID);
            writef.writeInt(value);
        }

        input.close();
        writef.close();
        File file = new File("temp");
        file.delete();

    }



    public void close() throws IOException {
        for (DataInputStream reader : readers) {
            reader.close();
        }
        write.close();
    }


}

