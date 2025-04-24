import MMCS.MMCS_v2;
import ProProcess.MultiColumnExternalDictionaryEncoder;
import MMCS.Hypergraph;
import MMCS.MMCSController;
import Sample.Sampler;

import Validation.ValidatorSelector;
import test.MMCS;
import test.ProProcess;
import test.Sample;
import test.Validation;


import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) throws Exception {
        MultiColumnExternalDictionaryEncoder encoder = new MultiColumnExternalDictionaryEncoder();

        encoder.Proprocess("horse.csv");
        Path outputDir = Paths.get("Validation");
        Files.createDirectories(outputDir);

        ProProcess pr =new ProProcess();
        pr.proprocess();
        Sample sample=new Sample(pr.numVer,pr.PLI,pr.reversePLI);
        Validation  validation=new Validation(pr.numVer,pr.PLI,pr.reversePLI);


        Sampler sampler = new Sampler(encoder.columnFiles,encoder.columnDict,encoder.fileLength);

        ValidatorSelector validatorSelector = new ValidatorSelector(sampler,encoder.clustersNum,encoder.uniqueNum,encoder.columnFiles,encoder.fileLength,validation,sample);
 //       sampler.hypeGraph.hyperedges =sample.hyperedges;
        for(int i=0;i<pr.numVer;i++) sample.sample(pr.PLI[i]);

//        Hypergraph  graph = new Hypergraph(sampler.hypeGraph.hyperedges);
//        MMCSController mmcs = new MMCSController(graph, encoder.uniqueNum,validatorSelector);
//        mmcs.run();

        MMCS_v2 mmcs = new MMCS_v2(encoder.columnFiles.size(),sampler.hypeGraph.hyperedges, validatorSelector);
        sampler.initateSample();
        mmcs.mmcs(new ArrayList<>());

        
//        List<Integer> ucc = new ArrayList<>();
//        ucc.add(24); ucc.add(2);
////        ucc.add(18); ucc.add(21); ucc.add(0); ucc.add(4); ucc.add(7); ucc.add(9);
//        validatorSelector.selectValidator(ucc);
//        List<DataInputStream> read = new ArrayList<>();
//        for(int i= 0;i<ucc.size();i++){
//            read.add(new DataInputStream(new FileInputStream(encoder.columnFiles.get(ucc.get(i)))));
//        }
//        for (int i=0;i<encoder.fileLength;i++){
//            System.out.print(i);
//            for(int j=0;j<read.size();j++){
//                System.out.print(" "+read.get(j).readInt());
//            }
//            System.out.println();
//        }



    }
}