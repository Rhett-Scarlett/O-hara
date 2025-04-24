package Validation;

import Sample.Sampler;
import Validation.BitMap.BitMapValidator;
import Validation.BitMap.TupleBinaryGenerator;
import Validation.HashMap.HashValidator;
import test.Sample;
import test.Validation;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ValidatorSelector {

    private double HIGH_CARDINALITY_THRESHOLD = 0.7;
    // 阈值：由唯一值占比得出


    private double SMALL_CLUSTER_TOTAL_THRESHOLD = 0.5;

    private  List<Integer> unqiueNum;

    private  List<String>  columnFiles;

    private List<Integer>  clustersNum;
    private int fileLength;

    private Sampler sampler;
    private Validation validation;
    private Sample sample;

    public ValidatorSelector(Sampler sampler, List<Integer> clustersNum, List<Integer> unqiueNum, List<String> columnFiles, int fileLength, Validation validation, Sample sample){
        this.sampler = sampler;
        this.clustersNum = clustersNum;
        this.unqiueNum = unqiueNum;
        this.columnFiles = columnFiles;
        this.fileLength = fileLength;
        this.validation = validation;
        this.sample=sample;
    }

    /**
     * 根据属性基数信息选择验证器
     */
    public int test_valid(List<Integer> ucc) {
        Stack<List<List<Integer>>> intersection_stack = new Stack<>();
        intersection_stack.add(validation.PLI[ucc.remove(0)]);
        LinkedList<Integer> tointersect_queue = new LinkedList<>();
        for (int i = 0; i < ucc.size(); i++) {
            tointersect_queue.add(ucc.get(i));
        }
        validation.pullUpIntersections(intersection_stack, tointersect_queue);
        if (!intersection_stack.peek().isEmpty()) {
            //得到新增边的条数
            return sample.sample(intersection_stack.peek());
            //添加uncov

        }
        return  0;
    }
    public  int selectValidator(List<Integer> candidateUCC) throws IOException, NoSuchAlgorithmException {
        // 如果存在某个属性的基数大于等于 HIGH_CARDINALITY_THRESHOLD，则选用 DirectValidator

        List<String>  validFile =new ArrayList<>();
        List<Double> threshold = new ArrayList<>();
        String masterFile = null;
        int mincol = -1;
        double hash_threshold = HIGH_CARDINALITY_THRESHOLD;
        for (int i =0;i<candidateUCC.size();i++) {
            threshold.add((double)unqiueNum.get(candidateUCC.get(i))/fileLength);
            if (threshold.get(i)>hash_threshold) {
                mincol = i;
                hash_threshold =threshold.get(i);
            }
            validFile.add(columnFiles.get(candidateUCC.get(i)));
        }
        if(mincol>-1){
            System.out.println("hash-哈希");
            masterFile ="V" + validFile.remove(mincol);
            return  sampler.validSample(new HashValidator(validFile,masterFile,fileLength).processTuples());
        }
        System.out.println(validFile);

        // 计算每个属性的簇需要占几位，若总数小于32位则使用基于位图的验证器
        List<Integer> candidata_clusters = new ArrayList<>();
        String outFile = "Validation/"+candidateUCC.toString()+".dat";
        for (Integer col : candidateUCC) {
            if (clustersNum.get(col) > SMALL_CLUSTER_TOTAL_THRESHOLD) break;
            candidata_clusters.add(clustersNum.get(col)-1);
        }
        if(candidata_clusters.size() ==candidateUCC.size()){
            TupleBinaryGenerator tupleBit = new TupleBinaryGenerator(candidata_clusters);
            if(tupleBit.judge_useBitMap()<=31){
                System.out.println("Bit-位图");
                return  sampler.validSample(new BitMapValidator(validFile, tupleBit.getMaxIndexValue(candidata_clusters),outFile,tupleBit,fileLength).processTuples());
            }
        }

        System.out.println("hashBit-哈希位图");
        return  sampler.validSample(new BitHashValidator(candidata_clusters,validFile,fileLength,outFile).Validator(outFile));
    }


}

