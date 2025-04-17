package Validation.BitMap;

import java.util.*;

// 位图检查器，对二进制数据计算索引
public class BitmapChecker {
    private BitSet bitmap;
    private int size;

    public BitmapChecker(int size) {
        this.size = size;
        this.bitmap = new BitSet(size);
    }
    // 检查并标记，若已存在返回 true，否则标记并返回 false
    public boolean checkAndMark(int idx) {
        if (bitmap.get(idx)) {
            return true;
        } else {
            bitmap.set(idx);
            return false;
        }
    }
}


