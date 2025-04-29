package Validation;

import Sample.Sampler;
import Validation.BitMap.BitMapValidator;
import Validation.BitMap.TupleBinaryGenerator;
import Validation.HashMap.HashValidator;
import test.Sample;
import test.Validation;

import javax.swing.text.LayoutQueue;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ValidatorSelector {

    private double HIGH_CARDINALITY_THRESHOLD = 0.7;
    // 阈值：由唯一值占比得出


    private double SMALL_CLUSTER_TOTAL_THRESHOLD = 1000;

    //private  List<Integer> unqiueNum;

    private  List<String>  columnFiles;

    private List<Integer>  clustersNum;
    private int fileLength;

    private Sampler sampler;
//    private Validation validation;
//    private Sample sample;

    private ClusterFileCache cache;

    private List<Integer> candidateCluster;

    private List<Boolean> colHash;

    public ValidatorSelector(Sampler sampler, List<Integer> clustersNum, List<Boolean> colHash, List<String> columnFiles, int fileLength,Sample sample, Validation validation){
        this.sampler = sampler;
        this.clustersNum = clustersNum;
        //this.unqiueNum = unqiueNum;
        this.columnFiles = columnFiles;
        this.fileLength = fileLength;
        this.cache = new ClusterFileCache();
        this.colHash = colHash;

        //this.validation=validation;
//        this.sample = sample;
    }

    /**
     * 根据属性基数信息选择验证器
     */
//    public int test_valid(List<Integer> ucc) {
//        Stack<List<List<Integer>>> intersection_stack = new Stack<>();
//        intersection_stack.add(validation.PLI[ucc.remove(0)]);
//        LinkedList<Integer> tointersect_queue = new LinkedList<>();
//        for (int i = 0; i < ucc.size(); i++) {
//            tointersect_queue.add(ucc.get(i));
//        }
//        validation.pullUpIntersections(intersection_stack, tointersect_queue);
//        if (!intersection_stack.peek().isEmpty()) {
//            //得到新增边的条数
//            return sample.sample(intersection_stack.peek());
//            //添加uncov
//
//        }
//        return  0;
//    }
    //选择合适的文件
    private List<String> selectFiles(List<Integer> candidateUCC){
        List<String> files = new ArrayList<>();
        candidateCluster = new ArrayList<>();
        List<Integer> set = cache.findSubsets(candidateUCC);
        if(cache.isHash){
            files.add("Validation/h"+set.toString()+".dat");
            Set<Integer> redis = new HashSet<>(candidateUCC);

            redis.removeAll(set);
            for(int ver : redis){
                files.add(columnFiles.get(ver));
            }
        }else{
            for (int i =0 ;i<cache.result.size();i++)  {
                files.add( "Validation/"+cache.result.get(i).toString()+".dat");
                candidateCluster.add(cache.result_clusternum.get(i));
            }
            boolean isH = false;
            for (int ver :set){
                if(!colHash.get(ver)||isH){
                    files.add(columnFiles.get(ver));
                    candidateCluster.add(clustersNum.get(ver));
                }else {
                    isH =true;
                    files.add(0,"V" + columnFiles.get(ver));
                    cache.isHash =true;
                }
            }
        }
        cache.result.clear();
        cache.result_clusternum.clear();
        cache.result_isHash.clear();
        return files;
    }
    public  int selectValidator(List<Integer> candidateUCC) throws IOException, NoSuchAlgorithmException {
        // 如果存在某个属性的基数大于等于 HIGH_CARDINALITY_THRESHOLD，则选用 DirectValidator

        //System.out.println(candidateUCC);
        List<String>  validFile = selectFiles(candidateUCC);
        List<Double> threshold = new ArrayList<>();
        String masterFile = null;
        if(cache.isHash){
            //System.out.println("hash-哈希");
            masterFile = validFile.remove(0);
            //System.out.println(masterFile);
            //System.out.println(validFile);
            return  sampler.validSample(new HashValidator(validFile,masterFile,fileLength).processTuples());
        }
        //System.out.println(validFile);

        // 计算每个属性的簇需要占几位，若总数小于32位则使用基于位图的验证器
        String outFile = "Validation/"+candidateUCC.toString()+".dat";
        boolean flag = false;
        for (Integer num : candidateCluster) {
            if (num > SMALL_CLUSTER_TOTAL_THRESHOLD) {
                flag = true;
                break;
            }
        }
        if(!flag){
            TupleBinaryGenerator tupleBit = new TupleBinaryGenerator(candidateCluster);
            if(tupleBit.judge_useBitMap()<=20){
                Map<Integer,List<Integer>> valid=new BitMapValidator(validFile, tupleBit.getMaxIndexValue(candidateCluster),outFile,tupleBit,fileLength).processTuples();
                judgeHash(valid,candidateUCC,outFile,false);
                return  sampler.validSample(valid);
            }
        }


        Map<Integer,List<Integer>> valid = new BitHashValidator(candidateCluster,validFile,fileLength,outFile).Validator(outFile);
        judgeHash(valid,candidateUCC,outFile,false);
//        List<List<Integer>> list = new ArrayList<>();
//        for (Map.Entry<Integer,List<Integer>> cl :valid.entrySet()){
//          list.add(cl.getValue());
//        }
        return  sampler.validSample(valid);
    }

    public void  judgeHash(Map<Integer,List<Integer>> valid,List<Integer> candidateUCC, String outFile,boolean isHash) throws IOException {
        if(valid.size()>0){
            int out_unique = 0;
            for (Map.Entry<Integer,List<Integer>> entry : valid.entrySet()){
                out_unique +=entry.getValue().size();
            }

            //System.out.println((1-out_unique/fileLength));
            if((1-out_unique/fileLength) >HIGH_CARDINALITY_THRESHOLD){
                //重写文件
                rewritefile(outFile,"Validation/h"+candidateUCC.toString()+".dat",out_unique );
                cache.insert(candidateUCC,valid.size(),true);
            }else cache.insert(candidateUCC,valid.size(),isHash);
        }
        else Files.delete(Paths.get(outFile));

    }

    public void rewritefile(String outFile,String fina,int sum) throws IOException {
        DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(outFile)));
        DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fina)));
        int tuple = -1,value;
        output.writeInt(sum);
        while (++tuple<fileLength){
            value  = input.readInt();
            if(value!= -1){
                output.writeInt(tuple);
                output.writeInt(value);
            }
        }
        input.close();
        output.close();
    }


}

