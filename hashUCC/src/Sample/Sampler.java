package Sample;

import java.io.*;
import java.util.*;




public class Sampler {


    private List<String> columnFiles;

    private List<String> columnDicts;

    // 最终结果：去重后的元组对
    private Set<TuplePair> sampledPairs;

    private GenarateSamplePairs genarateSamplePairs;

    private ProcessPairs processPairs;

    public Hypedges hypeGraph;

    private int fileLength;



    public Sampler(List<String> columnFiles,List<String> columnDicts,int fileLength) {
        this.fileLength = fileLength;
        this.columnDicts = columnDicts;
        this.columnFiles = columnFiles;
        this.hypeGraph = new Hypedges();
    }
    //初始采样
    public void initateSample() throws IOException {

        sampledPairs = new HashSet<>();
        genarateSamplePairs = new GenarateSamplePairs(fileLength,sampledPairs);

        //遍历每个属性生成采样对
        for(int i=0; i<columnFiles.size(); i++) {
            genarateSamplePairs.buildClusterSizeToClusterIds(columnDicts.get(i),i);
        }
        //处理采样对并形成超边
        processPairs = new ProcessPairs(columnFiles,sampledPairs);
        for(TuplePair tuplePair : sampledPairs) {
            hypeGraph.addEdgeAndMinimizeInclusion(processPairs.genrateEdge(tuplePair));

        }

    }

    //后续采样
    public int validSample(Map<Integer,List<Integer>> clusters) throws IOException {
        Set<TuplePair> validSampledPairs = new HashSet<>();
        if (clusters.size()==0) return 0;
        double x = 0.2;
        int p = 0;
        List<Integer> probablily = new ArrayList<>();

        //1.得到可取样的采样对数
        for (Map.Entry<Integer,List<Integer>> entry : clusters.entrySet()){
            int num = entry.getValue().size();
            p += num * (num - 1) / 2;
            probablily.add(p);
        }
        int sum= (int) StrictMath.pow((double) p, x);
        sum = sum>0?sum:1;
        Random random = new Random();
        for (int i=0;i<sum;i++){
            // ----- a. 随机选择一个元组对（按概率分布定位簇）
            int selectedCi = random.nextInt(p) + 1;  // 1 ~ p 中的随机数

            int t = 0;
            while (probablily.get(t) < selectedCi) t++;  // 找到对应簇
            selectedCi = t;
            List<Integer> cluster = clusters.get(selectedCi);
            int clusterSize = cluster.size();
            if (clusterSize == 2) {
                // 特殊情况：簇内只有 2 个元组，直接采样成一对
                validSampledPairs.add(new TuplePair(cluster.get(0), cluster.get(1)));
            } else {
                // 通用情况：簇内有多个元组，进行多次随机采样

                    int tuple1 = random.nextInt(cluster.size());
                    int tuple2 = random.nextInt(cluster.size());

                    // 防止采样到相同的元组，做简单修正
                    if (tuple1 == tuple2) {
                        tuple2 = (tuple2 + 1 == clusterSize) ? tuple2 - 1 : tuple2 + 1;
                    }
                    // 添加采样对
                    validSampledPairs.add(new TuplePair(cluster.get(tuple1), cluster.get(tuple2)));

            }
        }
//        for(Map.Entry<Integer,List<Integer>> entry : clusters.entrySet()) {
//            List<Integer> cluster = entry.getValue();
//            if (random.nextDouble() <= 0.3) {
//                int clusterSize = cluster.size();
//                if (clusterSize == 2) {





//                    // 特殊情况：簇内只有 2 个元组，直接采样成一对
//                    validSampledPairs.add(new TuplePair(cluster.get(0), cluster.get(1)));
//                } else {
//                    // 通用情况：簇内有多个元组，进行多次随机采样
//                    int sample_count = (int) Math.pow(clusterSize * (clusterSize - 1), 0.3); // 采样次数，按规模开 0.3 次方
//                    if (sample_count < 1) sample_count = 1; // 至少采样一次
//
//                    for (int i = 0; i < sample_count; ++i) {
//                        int tuple1 = random.nextInt(cluster.size());
//                        int tuple2 = random.nextInt(cluster.size());
//
//                        // 防止采样到相同的元组，做简单修正
//                        if (tuple1 == tuple2) {
//                            tuple2 = (tuple2 + 1 == clusterSize) ? tuple2 - 1 : tuple2 + 1;
//                        }
//                        // 添加采样对
//                        validSampledPairs.add(new TuplePair(cluster.get(tuple1), cluster.get(tuple2)));
//                    }
//                }
//            }
//        }
        //处理采样对并形成超边
        // 初始化 ProcessPairs 对象，参数为包含列文件路径的 columnFiles（假设在上下文中已有定义）和采样对集合
        processPairs = new ProcessPairs(columnFiles, validSampledPairs);

        // 创建一个临时的 Hypedges 对象，用来处理采样后的超边
        Hypedges hypedGraphSon = new Hypedges();

        // 遍历每个采样对，生成超边，并利用 addEdgeAndMinimizeInclusion 方法合并或更新超边集合
        for (TuplePair tuplePair : validSampledPairs) {
            hypedGraphSon.addEdgeAndMinimizeInclusion(processPairs.genrateEdge(tuplePair));
        }
        // 将得到的新超边添加到全局的超边集合 hypeGraph.hyperedges 中
        for (List<Integer> edge : hypedGraphSon.hyperedges) {
            hypeGraph.hyperedges.add(edge);
        }
        return hypedGraphSon.hyperedges.size();
    }
}
