package Sample;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class GenarateSamplePairs {
    //文件长度
    private int fileLength;

    // Map<簇大小, List<簇ID>>
    private List<Map<Integer, List<Integer>>> clusterSizeToClusterIds = new ArrayList<>();

    // Map<元组ID, 出现次数>
    private Map<Integer, Integer> tupleFrequency = new HashMap<>();

    // Map<簇ID, List<元组ID>>
    private Map<Integer, List<Integer>> clusterIdToTuples = new HashMap<>();
    //倒排索引
    Map<Integer, Map<Integer, Integer>> tupleToColumnCluster = new HashMap<>();
    //去重后的元组对
    private Set<TuplePair> sampledPairs;
    private int limit = 20;

    public GenarateSamplePairs(int fileLength,Set<TuplePair> sampledPairs) {
        this.fileLength = fileLength;
        this.sampledPairs = sampledPairs;
    }

    // 1. 读取列文件，建立 "簇ID -> 簇 ID 列表" 的映射
    public void buildClusterSizeToClusterIds(String columnFilePath, int col) throws IOException {
        // 用于读取二进制文件的输入流（DataInputStream 支持直接读 int）
        DataInputStream reader = new DataInputStream(new FileInputStream(columnFilePath));

        // 用于暂存当前簇的所有元组（这里存的是 clusterId，可以看需求改为元组 ID）
        List<Integer> cluster = new ArrayList<>();

        // 当前正在处理的 clusterId，初始值设为 -2，表示未开始
        int currentCluster = -2;

        // 遍历列文件中的所有元组（假设 fileLength 是总行数）
        for (int i = 0; i < fileLength; i++) {
            // 读取一个元组的数据：tupleId 和 clusterId
            int tupleId = reader.readInt();
            int clusterId = reader.readInt();
            // 判断是否进入了新的簇
            if (currentCluster != clusterId) {
                // 如果进入新簇，处理上一个簇的数据
                if(cluster.size()>= 2) getSampledPairs(col, currentCluster, cluster, clusterIdToTuples);

                // 重置 cluster list，用于收集新的簇的元组
                cluster = new ArrayList<>();

                // 更新当前簇 ID
                currentCluster = clusterId;
            }

            // 过滤掉 clusterId == -1 的情况（可能是孤立元组 / 特殊标记）
            if (clusterId != -1) {
                cluster.add(tupleId);
            }
        }

        // 循环结束后，最后一个簇还未处理，需要手动处理一次
        if (cluster.size() > 0) {
            getSampledPairs(col, currentCluster, cluster, clusterIdToTuples);
        }
    }


    // 2. 处理簇：对小簇进行采样，并收集大簇的元组用于后续处理
    private void getSampledPairs(int col, int clusterID, List<Integer> cluster, Map<Integer, List<Integer>> clusterSizeToClusterIds) {
        int clusterSize = cluster.size(); // 当前簇的大小
        Random random = new Random();     // 随机数生成器
        // 情况 1：处理大簇（簇的大小超过设定阈值 limit）
        if (clusterSize > limit) {
            for (int tupleId : cluster) {
                // 统计元组在大簇中的出现次数（用于后续频率分析）
                tupleFrequency.put(tupleId, tupleFrequency.getOrDefault(tupleId, 0) + 1);

                // 记录元组所在的列和簇：tupleId -> (col -> clusterID)
                tupleToColumnCluster
                        .computeIfAbsent(tupleId, k -> new HashMap<>())
                        .put(col, clusterID);
            }

            // 保存当前大簇的元组列表：clusterID -> List<tupleId>
            clusterSizeToClusterIds.put(clusterID, cluster);
        }

        // 情况 2：小簇，且以 30% 概率进行采样（控制采样规模）
        if (random.nextDouble() <= 0.1) {
            if (clusterSize == 2) {
                // 特殊情况：簇内只有 2 个元组，直接采样成一对
                sampledPairs.add(new TuplePair(cluster.get(0), cluster.get(1)));
            } else {
                // 通用情况：簇内有多个元组，进行多次随机采样
                int sample_count = (int) Math.pow(clusterSize * (clusterSize - 1), 0.3); // 采样次数，按规模开 0.3 次方
                if (sample_count < 1) sample_count = 1; // 至少采样一次

                for (int i = 0; i < sample_count; ++i) {
                    int tuple1 = random.nextInt(cluster.size());
                    int tuple2 = random.nextInt(cluster.size());

                    // 防止采样到相同的元组，做简单修正
                    if (tuple1 == tuple2) {
                        tuple2 = (tuple2 + 1 == clusterSize) ? tuple2 - 1 : tuple2 + 1;
                    }
                    // 添加采样对
                    sampledPairs.add(new TuplePair(tuple1, tuple2));
                }
            }
        }
    }


    //3.依据得到的大簇元组生成采样对
    public void generateSamplePairs() {

        // 1. 找出出现次数最多的元组

        int maxFrequency = 0;
        List<Integer> mostFrequentTuples = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : tupleFrequency.entrySet()) {
            int freq = entry.getValue();
            int tupleId = entry.getKey();

            if (freq > maxFrequency) {
                maxFrequency = freq;
                mostFrequentTuples.clear();
                mostFrequentTuples.add(tupleId);
            } else if (freq == maxFrequency) {
                mostFrequentTuples.add(tupleId);
            }
        }

        for (int frequentTuple : mostFrequentTuples) {
            // 根据倒排索引获取该元组在哪些列、哪些簇中
            Map<Integer, Integer> columnClusters = tupleToColumnCluster.get(frequentTuple);
            if (columnClusters == null) continue;

            for (Map.Entry<Integer, Integer> columnEntry : columnClusters.entrySet()) {
                int columnId = columnEntry.getKey();
                int clusterId = columnEntry.getValue();

                // 获取该列簇中的元组列表
                List<Integer> clusterTuples = clusterSizeToClusterIds.get(columnId).get(clusterId);
                // 3.1 确定采样对的数量
                int size = clusterTuples.size();
                int pairCount = (int) Math.pow(size * (size - 1), 0.2); // 可以根据簇大小确定采样数量
                if (pairCount < 1) pairCount = 1;
                // 3.1 在簇中，优先选择出现次数多的元组
                List<Integer> highFrequencyTuples = new ArrayList<>(clusterTuples);
                int maxCount = 0;
                for(int tupleId : clusterTuples) {
                    if (tupleFrequency.get(tupleId) >= maxCount) {
                        highFrequencyTuples.add(tupleId);
                        maxCount = tupleFrequency.get(tupleId);
                    }
                }
                pairCount = highFrequencyTuples.size() < pairCount? highFrequencyTuples.size():pairCount;
                int limit = highFrequencyTuples.size()-pairCount;
                // 3.2 两两配对，优先高频元组
                for (int i = highFrequencyTuples.size()-1 ; i >= limit; i--) {
                    int tupleId = highFrequencyTuples.get(i);
                    if(tupleId!=frequentTuple) sampledPairs.add(new TuplePair(tupleId, frequentTuple));
                }
            }
        }
    }


}
