package Sample;

import test.ProProcess;

import java.io.*;
import java.util.*;

public class ProcessPairs {

    private Map<Integer, List<Integer>> tupleDataMap;
    private ProProcess p;
    public ProcessPairs(List<String> columnFiles,Set<TuplePair> sampledPairs) throws IOException {
        getTuple(columnFiles, sampledPairs);
        p =new ProProcess();
        p.proprocess();
    }
    //1.得到元组数据
    private  void getTuple(List<String> columnFiles,Set<TuplePair> sampledPairs) throws IOException {
        // Step 1: 收集所有需要采样的元组 ID，并排序
        Set<Integer> tupleSet = new HashSet<>();
        for (TuplePair tuplePair : sampledPairs) {
            tupleSet.add(tuplePair.id1);
            tupleSet.add(tuplePair.id2);
        }
        List<Integer> tupleList = new ArrayList<>(tupleSet);
        Collections.sort(tupleList);

        // Step 2: 初始化存储 Map
        tupleDataMap = new HashMap<>(tupleList.size());
        for (Integer tupleId : tupleList) {
            tupleDataMap.put(tupleId, new ArrayList<>());
        }



        // Step 3: 依次读取每个列文件
        for (String columnFile : columnFiles) {
            try (RandomAccessFile file = new RandomAccessFile(columnFile, "r")) {
                for (Integer tupleId : tupleList) {
                    long offset = tupleId * 4L; // 使用 long 避免溢出
                    file.seek(offset);
                    int value = file.readInt();
                    tupleDataMap.get(tupleId).add(value);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading file: " + columnFile, e);
            }
        }

//        for (Map.Entry<Integer,List<Integer>> t: tupleDataMap.entrySet()){
//            System.out.println(t.getKey()+" "+ t.getValue());
//        }

        // 可选：方法结束后 tupleDataMap 可用于后续处理
    }
    //2.处理元组对，生成超边
    public List<Integer> genrateEdge(TuplePair tuplePair) {
        List<Integer> edgeList = new ArrayList<>();
        List<Integer> t1 = tupleDataMap.get(tuplePair.id1);
        List<Integer> t2 = tupleDataMap.get(tuplePair.id2);

        for (int i=0; i<t1.size(); i++) {
            if (t1.get(i)==-1||(!t1.get(i).equals(t2.get(i)))){
                edgeList.add(i);
            }
        }


        return edgeList;
    }

}