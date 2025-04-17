package Validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IntArrayKey {
    private final int[] data;
    private final int hash;  // 缓存计算好的 hashCode

    public IntArrayKey(int[] data) {
        // 克隆数组以避免外部修改影响键值
        this.data = data.clone();
        // 基于数组内容计算 hashCode，并缓存下来
        this.hash = Arrays.hashCode(this.data);
    }

    @Override
    public boolean equals(Object o) {
        // 判断是否为同一对象
        if (this == o) return true;
        // 判断类型是否一致
        if (!(o instanceof IntArrayKey)) return false;
        IntArrayKey other = (IntArrayKey) o;
        // 使用 Arrays.equals() 比较数组内容是否一致
        return Arrays.equals(this.data, other.data);
    }

    @Override
    public int hashCode() {
        // 直接返回缓存的 hash 值，避免重复计算
        return hash;
    }

}

