package Validation.BitMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

public class TupleBinaryGenerator {
    // 保存每个簇的有效位数
    private final List<Integer> bitLengths;

    // 计算所有属性需要的总位数
    public int judge_useBitMap() {
        int sum = 0;
        for (int len : bitLengths) {
            sum += len;
        }
        return sum;
    }

    // 构造函数：根据每个 clusterID 计算其实际存储所需的有效位数
    public TupleBinaryGenerator(List<Integer> clusterIDs) {
        bitLengths = clusterIDs.stream()
                .map(id -> id == 0 ? 1 : (32 - Integer.numberOfLeadingZeros(id)))
                .collect(Collectors.toList());
    }

    /**
     * 直接生成拼接后的整数索引（如果总位数 <= 32）
     *
     * @param tupleValues 每个属性的整数值
     * @return 拼接后的整数索引
     */
    public int generateTupleIndex(List<Integer> tupleValues) {
        int result = 0;
        int offset = judge_useBitMap();
        for (int i = 0; i < tupleValues.size(); i++) {
            int bitLength = bitLengths.get(i);
            offset -= bitLength;
            int clusterID = tupleValues.get(i);
            result |= (clusterID << offset);
        }
        return result;
    }

    /**
     * 如果你担心位数超过 32，提供一个 64 位版本
     */
    public long generateTupleIndex64(List<Integer> tupleValues) {
        long result = 0L;
        int offset = judge_useBitMap();
        for (int i = 0; i < tupleValues.size(); i++) {
            int bitLength = bitLengths.get(i);
            offset -= bitLength;
            int clusterID = tupleValues.get(i);
            result |= ((long) clusterID << offset);
        }
        return result;
    }

    /**
     * 保留 BitSet 版本，方便你后续做位运算用，但建议主流程用 int/long
     */
    public BitSet generateTupleBits(List<Integer> tupleValues) {
        BitSet result = new BitSet();
        int offset = 0;
        for (int i = 0; i < tupleValues.size(); i++) {
            int bitLength = bitLengths.get(i);
            int clusterID = tupleValues.get(i);
            for (int j = 0; j < bitLength; j++) {
                if (((clusterID >> (bitLength - 1 - j)) & 1) == 1) {
                    result.set(offset + j);
                }
            }
            offset += bitLength;
        }
        return result;
    }

    public int getMaxIndexValue(List<Integer> clusterNum) {
        return  generateTupleIndex(clusterNum)+1;
    }


}
