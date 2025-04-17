package ProProcess;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class ColumnSorterID {
    public void ExternalSortID(String inputFilePath, String outputFilePath, int chunkSize, int length) {
        // 用于存放所有临时文件的列表
        List<File> tempFiles = new ArrayList<>();
        //存放临时文件大小的列表
        List<Integer> tempFilesSize = new ArrayList<>();
        int chunkCount = 0;//  统计块数量
        try {
            // 打开输入文件进行读取
            DataInputStream reader = new DataInputStream(new FileInputStream(inputFilePath));
            List<int[]> chunk = new ArrayList<>();
            int len=0;
            // 逐行读取文件，避免一次加载整个文件
            while (len++ < length) {
                // 将当前行的值和行号存入当前块中
                int[] elements = {reader.readInt(), reader.readInt()};
                chunk.add(elements);
                // 当当前块中的行数达到指定大小时，进行排序并写入一个临时文件
                if (chunk.size() >= chunkSize) {
                    // 调用辅助函数排序当前块并写入临时文件
                    File tempFile = sortAndSaveChunkID(chunk, ++chunkCount);
                    // 将临时文件加入列表
                    tempFiles.add(tempFile);
                    tempFilesSize.add(chunk.size());
                    // 清空当前块，准备下一批数据
                    chunk.clear();
                }
            }
            // 处理剩余不足一块的数据
            if (!chunk.isEmpty()) {
                File tempFile = sortAndSaveChunkID(chunk, ++chunkCount);
                tempFiles.add(tempFile);
                tempFilesSize.add(chunk.size());
                chunk.clear();
            }
            // 关闭输入流
            reader.close();

            // 调用辅助函数，将所有临时文件归并为一个排序后的输出文件
            mergeSortedIDFiles(tempFiles, outputFilePath, tempFilesSize);
            // 临时排序完成后，删除所有临时文件
            for (File file : tempFiles) {
                file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private File sortAndSaveChunkID(List<int[]> chunk, int count) throws IOException {
        // 对数据块进行排序，按记录中的 value (第0个元素) 比较
        chunk.sort((a, b) -> Integer.compare(a[0], b[0]));
        // 创建一个临时文件，文件名前缀为 "sortChunk"，后缀为 ".csv"
        File tempFile = File.createTempFile("sortChunkID"+count, ".csv");
        // 确保程序退出时自动删除该临时文件
        tempFile.deleteOnExit();
        // 使用 BufferedWriter 将排序后的数据写入临时文件
        DataOutputStream writer = new DataOutputStream(new FileOutputStream(tempFile));
        // 遍历当前块中的所有记录
        for (int[] record : chunk) {
            writer.writeInt(record[0]);
            writer.writeInt(record[1]);
        }
        // 关闭写入流
        writer.close();
        // 返回生成的临时文件
        return tempFile;
    }
    private void mergeSortedIDFiles(List<File> files, String outputFilePath, List<Integer> filesSize) throws IOException {
        // 为每个临时文件创建一个 BufferedReader 用于逐行读取数据
        List<DataInputStream> readers = new ArrayList<>();
        for (File file : files) {
            readers.add(new DataInputStream(new BufferedInputStream(new FileInputStream(file))));
        }

        // 定义内部类，用于存放每个读取到的记录及其来源文件的索引
        class RecordEntry {
            int value;    // 排序依据的值
            int rowID;    // 原始行号（元组ID）
            int fileIndex;   // 记录来自哪个临时文件（对应 readers 的索引）
            int fileSize;

            RecordEntry(int value, int rowID, int fileIndex,int fileSize) {
                this.value = value;
                this.rowID = rowID;
                this.fileIndex = fileIndex;
                this.fileSize = fileSize;
            }
        }

        // 创建优先队列，用于归并多个文件中的数据，按照 id 排序

        PriorityQueue<RecordEntry> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.rowID));

        // 初始化优先队列，从每个临时文件中读取第一条记录
        for (int i = 0; i < readers.size(); i++) {
            int id = readers.get(i).readInt();
            int value = readers.get(i).readInt();
            pq.add(new RecordEntry(value, id, i, filesSize.get(i)-1));
        }

        // 打开输出文件写入归并后的数据
        DataOutputStream writer = new DataOutputStream(new FileOutputStream(outputFilePath));
//        BufferedWriter bw = new BufferedWriter(new FileWriter("test_" + outputFilePath));

        // 归并过程：每次从优先队列中取出最小的记录，写入输出文件，并从该记录所在的文件中读取下一条记录

        while (!pq.isEmpty()) {
            // 取出队列中排序最小的记录
            RecordEntry entry = pq.poll();
            // 将记录写入输出文件
            writer.writeInt(entry.value);
//            bw.write( Integer.toString(entry.rowID)+","+Integer.toString(entry.value));
//            bw.newLine();
            // 从该临时文件中读取下一行记录
            if (entry.fileSize > 0) {
                DataInputStream reader = readers.get(entry.fileIndex);
                entry.rowID = reader.readInt();
                entry.value = reader.readInt();
                entry.fileSize--;
                pq.add(entry);
            }
        }
        // 归并完成后，关闭所有 BufferedReader
        for (DataInputStream r : readers) {
            r.close();
        }
        // 关闭输出写入流
        writer.close();
//        bw.close();
    }
}
