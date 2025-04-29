import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessInput {

    Map<String, Integer>  hashMap;

    int columnNum;

    int fileLength;
    public  ProcessInput(String inputFilePath) throws IOException {


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
        reader.close();


        Path outputDir = Paths.get("Input");
        Files.createDirectories(outputDir);
        for(int i = 0; i<numColumns;i++){
            String outFile = "Input/["+i+"].dat";
            processSingleCol(i,inputFilePath,splitChar,outFile);
        }

    }


    // 处理某一列的数据：读取原始数据文件，提取指定列，将该列的值映射为等价类 ID（int），并写入输出文件
    private void processSingleCol(int col, String inputFilePath, String splitChar, String outFile) throws IOException {

        // 打开输入文件用于读取
        BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));

        // 用于记录该列每个不同取值的映射编号（等价类 ID）
        hashMap = new HashMap<>();

        // 创建输出文件（写入压缩编码后的等价类 ID）
        DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));

        int rowId = 0; // 行号计数器
        AtomicInteger count = new AtomicInteger(0); // 计数器：为每个新值分配一个唯一 ID，从 0 开始

        // 特殊处理第一列：需要完整扫描每一行，并记录总行数（fileLength）
        if (col == 0) {
            String line;
            while ((line = reader.readLine()) != null) {
                rowId++; // 行计数

                // 解析该行第 col 列的值（splitChar 支持保留空字符串）
                String value = line.split(splitChar, -1)[col];

                // 如果该值未见过，则分配新编号；否则使用已有编号
                hashMap.computeIfAbsent(value, k -> count.getAndIncrement());

                // 写入该值对应的等价类 ID
                writer.writeInt(hashMap.get(value));
            }

            // 注意此处 rowId++ 可能多加了一次，建议改为 fileLength = rowId;
            fileLength = rowId;

        } else {
            // 处理非第0列：只需按 fileLength 行读取即可
            while (rowId++ < fileLength) {

                // 从当前行读取并提取指定列
                String value = reader.readLine().split(splitChar, -1)[col];

                // 为该值分配 ID 并写入
                hashMap.computeIfAbsent(value, k -> count.getAndIncrement());
                writer.writeInt(hashMap.get(value));
            }
        }

        // 关闭文件资源
        reader.close();
        writer.close();
    }

}
