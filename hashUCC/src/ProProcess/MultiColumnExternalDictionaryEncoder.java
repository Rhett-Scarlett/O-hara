package ProProcess;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Comparator;

/**
 * ProProcess.MultiColumnExternalDictionaryEncoder
 *
 * 该类实现以下功能：
 * 1. 将多列 CSV 文件拆分成单列文件，并为每行添加行号（即原始元组 ID）。
 * 2. 对每个单列文件：
 *    a. 按 value 外部排序（示例中用内存排序，仅适用于较小数据，实际场景应替换为真正的外部排序算法）。
 *    b. 扫描排序后文件生成字典：统计相同 value 的频次，
 *       如果全局只出现一次则编码为 -1，否则按顺序分配正整数编码。
 *    c. 利用归并扫描（Merge Join）方式，将排序文件中 value 替换为编码，生成中间文件（仍按 value 排序）。
 *    d. 对中间文件按行号（rowId）排序，恢复原始顺序，得到该列最终压缩文件。
 * 3. 将各列最终结果合并为一个完整的压缩表，输出文件格式为：rowId,compressedCol1,compressedCol2,...
 *
 */
public class MultiColumnExternalDictionaryEncoder {

    // 文件命名约定：
    // 分割后的单列文件: "column_<colName>_with_id.csv"
    // 按 value 排序后的文件: "column_<colName>_sorted.csv"
    // 字典文件: "column_<colName>_dict.csv"
    // 按行ID排序恢复顺序后的文件: "column_<colName>_final.csv"
    public int chunksize = 10000;
    public int fileLength ;
    public String outputDir = "output_columns/";
    public List<Integer> clustersNum;//存储簇数量
    public List<Integer> uniqueNum;//存储基数
    public List<String> columnFiles;
    public List<String> columnDict;

    public List<Double> threshold;

    public double HIGH_CARDINALITY_THRESHOLD = 0.7;

    public void Proprocess(String inputFilePath) throws Exception {

        clustersNum = new ArrayList<>();
        uniqueNum = new ArrayList<>();
        columnFiles = new ArrayList<>();
        columnDict = new ArrayList<>();
        threshold = new ArrayList<>();

        // 创建存储输出文件的目录
        Path outputDir = Paths.get("output_columns");
        Files.createDirectories(outputDir);
//        Path test = Paths.get("test_output_columns");
//        Files.createDirectories(test);

        // Step 1: 拆分多列文件为单列文件，并为每行添加行号
        List<String> colFiles = splitColumns(inputFilePath);

        // Step 2: 分别对每个单列文件进行外部排序、生成字典、编码替换、恢复原始行顺序
        for (int i = 0; i < colFiles.size(); i++) {
            processColumn(Integer.toString(i), colFiles.get(i));
        }
    }

    /**
     * Step 1: 拆分多列 CSV 文件
     * 读取原始 CSV 文件（第一行为表头），为每列创建一个输出文件，格式为：
     *      rowId,value
     * 返回拆分后所有列的列名列表。
     */
    private List<String> splitColumns(String inputFilePath) throws IOException {
        List<String> colFiles = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));

        // 读取第一行，确定列数
        String firstLine = reader.readLine();
        if (firstLine == null) {
            reader.close();
            throw new IOException("输入文件为空");
        }
        String splitChar = ",";
        String[] firstValues = firstLine.split(splitChar);
        if (firstValues.length < 2) {
            splitChar = ";";
            firstValues = firstLine.split(splitChar);
        }
        int numColumns = firstValues.length;

        // 创建各列的输出文件
        BufferedWriter[] writers = new BufferedWriter[numColumns];
        for (int i = 0; i < numColumns; i++) {
            String colFile = outputDir + "column_col_" + i + "_with_id.csv";
            writers[i] = new BufferedWriter(new FileWriter(colFile));
            colFiles.add(colFile);
        }

        // 处理第一行
        int rowId = 0;
        for (int i = 0; i < numColumns; i++) {
            writers[i].write(rowId + "," + firstValues[i]);
            writers[i].newLine();
        }

        // 逐行读取原始文件，将各列数据写入对应文件
        String line;
        while ((line = reader.readLine()) != null) {
            rowId++;
            String[] values = line.split(splitChar, -1);
            for (int i = 0; i < numColumns; i++) {
                writers[i].write(rowId + "," + values[i]);
                writers[i].newLine();
            }
        }
        fileLength = ++rowId;
        reader.close();
        for (BufferedWriter w : writers) {
            w.close();
        }

        return colFiles;
    }


    /**
     * Step 2: 对单列文件进行处理
     * 包括：
     * 1. 按 value 排序
     * 2. 生成字典文件,利用字典替换 value 为编码（归并扫描）
     * 3. 按 rowId 排序恢复原始顺序
     * @param colFile 单列文件路径（格式：rowId,value）
     */
    private void processColumn(String colName, String colFile) throws Exception {
        // 定义各步骤中使用的文件名称
        String sortedFile = outputDir + "column_" + colName + "_sorted.csv";
        String dictFile = outputDir + "column_" + colName + "_dict.csv";
        String finalFile = outputDir + "column_" + colName + "_final.csv";
        columnFiles.add(finalFile);
        columnDict.add(dictFile);

        // Step 2.1: 外部排序（按 value 排序）
        ColumnSorterValue sorterValue = new ColumnSorterValue();
        sorterValue.ExternalSortValue(colFile, sortedFile, chunksize);
        File file= new File(colFile);
        file.delete();

        // Step 2.2: 生成字典文件
        boolean selected = generateDictionary(sortedFile, dictFile);
        file = new File(sortedFile);
        file.delete();

        // Step 2.3: 按 rowId 排序恢复原始行顺序
        if(selected) {
            new ColumnSorterIDExcludeUnique().ExternalSortID(dictFile, "V"+finalFile, chunksize, fileLength, uniqueNum.get(uniqueNum.size()-1));
        }
        new ColumnSorterID().ExternalSortID(dictFile, finalFile, chunksize, fileLength);




    }

    /**
     * Step 2.2: 生成字典文件
     * 读取按 value 排序后的文件，
     * 若全局只出现一次，则编码为 -1；否则按顺序分配正整数编码。
     * 结果写入 dictFile
     */
    private boolean generateDictionary(String sortedFile, String dictFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(sortedFile));
        DataOutputStream out = new DataOutputStream(new FileOutputStream(dictFile));
//        BufferedWriter bw = new BufferedWriter(new FileWriter("test_" +dictFile));

        String line = reader.readLine();
        String[] parts = line.split(",", 2);
        String currentValue = parts[1];
        out.writeInt(Integer.parseInt(parts[0]));
//        bw.write(parts[0]);
//        bw.write(",");
        boolean flag = false;
        int dictId = -1;

        int unqiueValueCount = 0; //    记录该列唯一值

        while ((line = reader.readLine()) != null) {
            parts = line.split(",", 2);
            if (parts[1].equals(currentValue)) {
                if (!flag) {
                    flag = true;
                    out.writeInt(++dictId);
//                    bw.write(Integer.toString(dictId));
//                    bw.newLine();
                }
                out.writeInt(Integer.parseInt(parts[0]));
//                bw.write(parts[0]);
//                bw.write(",");
                out.writeInt(dictId);
//                bw.write(Integer.toString(dictId));
//                bw.newLine();
            } else {
                if(!flag){
                    out.writeInt(-1);
                    unqiueValueCount++;
//                    bw.write(Integer.toString(-1));
//                    bw.newLine();
                }
                currentValue = parts[1];
                out.writeInt(Integer.parseInt(parts[0]));
//                bw.write(parts[0]);
//                bw.write(",");
                flag = false;
            }
        }
        if(!flag) {
            out.writeInt(-1);
            unqiueValueCount++;
//            bw.write(Integer.toString(-1));
        }
        clustersNum.add(++dictId);
        uniqueNum.add(unqiueValueCount);
        System.out.print(" "+threshold.size());
        threshold.add((double)unqiueValueCount/fileLength);
        System.out.print(" "+threshold.get(threshold.size()-1));
        reader.close();
        out.close();
//        bw.close();
        return threshold.get(threshold.size()-1) > HIGH_CARDINALITY_THRESHOLD ? true : false ;
    }


}
