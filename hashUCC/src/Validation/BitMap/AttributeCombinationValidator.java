package Validation.BitMap;

import Validation.BitMap.BitmapChecker;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

//HashMap记录重复的二进制数据及对应元组ID
public class AttributeCombinationValidator {
    private BitmapChecker bitmapChecker;
    private DataOutputStream write;  // 使用二进制输出
    // 用拼接后得到的数据作为键存储重复二进制数据对应的元组ID列表
    private Map<Integer, Integer> clustersMap;


    public AttributeCombinationValidator(int bitmapSize, DataOutputStream write, Map<Integer, Integer> clustersMap) throws IOException {
        this.bitmapChecker = new BitmapChecker(bitmapSize);
        this.write = write;
        this.clustersMap = clustersMap;
    }



    // 处理一个元组数据,检查其唯一性（tupleID 对应元组的序号）
    // idx 是拼接后的整数索引（由候选列组合构造的位编码）
    public void isUniqueTuple(int idx ) throws IOException {
        if (bitmapChecker.checkAndMark(idx)) {
            clustersMap.computeIfAbsent(idx, k -> clustersMap.size());
        }
        // **二进制写入（4 字节整数）**
        write.writeInt(idx);
    }

    public Map<Integer, Integer> getClustersMap() {
        return clustersMap;
    }
}