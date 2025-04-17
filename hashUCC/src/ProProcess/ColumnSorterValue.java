package ProProcess;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class ColumnSorterValue {
    /**
     * 外部排序方法：当文件过大时，采用分块排序和归并排序来完成排序任务
     *
     * @param inputFilePath  输入文件路径
     * @param outputFilePath 输出文件路径
     * @param chunkSize      每次加载的最大行数（不含表头），用于控制内存使用
     */
    public void ExternalSortValue(String inputFilePath, String outputFilePath, int chunkSize) {
        // 用于存放所有临时文件的列表
        List<File> tempFiles = new ArrayList<>();
        int chunkCount = 0;//  统计块数量
        try {
            // 打开输入文件进行读取
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            String line;
            // 用于暂存一个块的数据，每条记录为字符串数组：[rowID，value]
            List<String[]> chunk = new ArrayList<>();
            // 逐行读取文件，避免一次加载整个文件
            while ((line = reader.readLine()) != null) {
                // 将当前行的值和行号存入当前块中
                chunk.add(line.split(","));
                // 当当前块中的行数达到指定大小时，进行排序并写入一个临时文件
                if (chunk.size() >= chunkSize) {
                    // 调用辅助函数排序当前块并写入临时文件
                    File tempFile = sortAndSaveChunkValue(chunk,++chunkCount);
                    // 将临时文件加入列表
                    tempFiles.add(tempFile);
                    // 清空当前块，准备下一批数据
                    chunk.clear();
                }
            }
            // 处理剩余不足一块的数据
            if (!chunk.isEmpty()) {
                File tempFile = sortAndSaveChunkValue(chunk, ++chunkCount);
                tempFiles.add(tempFile);
                chunk.clear();
            }
            // 关闭输入流
            reader.close();

            // 调用辅助函数，将所有临时文件归并为一个排序后的输出文件
            mergeSortedValueFiles(tempFiles, outputFilePath);

            // 临时排序完成后，删除所有临时文件
            for (File file : tempFiles) {
                file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将内存中的数据块进行排序，然后写入一个临时文件
     *
     * @param chunk 数据块，每条记录为 [value, rowID]
     * @return 返回包含排序后数据的临时文件
     * @throws IOException 如果写入文件时发生错误则抛出异常
     */
    private File sortAndSaveChunkValue(List<String[]> chunk, int count) throws IOException {
        // 对数据块进行排序，按记录中的 value (第0个元素) 比较
        chunk.sort((a, b) -> a[1].compareTo(b[1]));
        // 创建一个临时文件，文件名前缀为 "sortChunk"，后缀为 ".csv"
        File tempFile = File.createTempFile("sortChunkValue"+count, ".csv");
        // 使用 BufferedWriter 将排序后的数据写入临时文件
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        // 遍历当前块中的所有记录
        for (String[] record : chunk) {
            writer.write(record[0] + "," + record[1]);
            writer.newLine(); // 写入换行符
        }
        // 关闭写入流
        writer.close();
        // 返回生成的临时文件
        return tempFile;
    }

    /**
     * 将多个已排序的临时文件归并成一个最终排序的输出文件
     * 采用优先队列实现多路归并排序
     *
     * @param files          临时文件列表，每个文件内的数据均已排序
     * @param outputFilePath 最终输出文件的路径
     * @throws IOException 如果文件读写操作出错则抛出异常
     */
    private void mergeSortedValueFiles(List<File> files, String outputFilePath) throws IOException {
        // 为每个临时文件创建一个 BufferedReader 用于逐行读取数据
        List<BufferedReader> readers = new ArrayList<>();
        for (File file : files) {
            readers.add(new BufferedReader(new FileReader(file)));
        }

        // 定义内部类，用于存放每个读取到的记录及其来源文件的索引
        class RecordEntry {
            String value;    // 排序依据的值
            String rowID;    // 原始行号（元组ID）
            int fileIndex;   // 记录来自哪个临时文件（对应 readers 的索引）

            RecordEntry(String value, String rowID, int fileIndex) {
                this.value = value;
                this.rowID = rowID;
                this.fileIndex = fileIndex;
            }
        }

        // 创建优先队列，用于归并多个文件中的数据，按照 value 字典序排序

        PriorityQueue<RecordEntry> pq = new PriorityQueue<>(Comparator.comparing(e -> e.value));

        // 初始化优先队列，从每个临时文件中读取第一条记录
        for (int i = 0; i < readers.size(); i++) {
            String line = readers.get(i).readLine();
            if (line != null) {
                // 按逗号分割成 value 和 rowID（这里只有两部分）
                String[] parts = line.split(",", 2);
                pq.add(new RecordEntry(parts[1], parts[0], i));
            }
        }

        // 打开输出文件写入归并后的数据
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));

        // 归并过程：每次从优先队列中取出最小的记录，写入输出文件，并从该记录所在的文件中读取下一条记录
        while (!pq.isEmpty()) {
            // 取出队列中排序最小的记录
            RecordEntry entry = pq.poll();
            // 将记录写入输出文件
            writer.write(entry.rowID + "," + entry.value);
            writer.newLine();
            // 从该临时文件中读取下一行记录
            BufferedReader reader = readers.get(entry.fileIndex);
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(",", 2);
                entry.value = parts[1];
                entry.rowID = parts[0];
                // 将新读取的记录加入优先队列，保持队列中始终有每个文件当前的最小记录
                pq.add(entry);
            }
        }

        // 归并完成后，关闭所有 BufferedReader
        for (BufferedReader r : readers) {
            r.close();
        }
        // 关闭输出写入流
        writer.close();
    }
}
