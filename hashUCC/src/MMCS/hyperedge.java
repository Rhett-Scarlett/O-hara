package MMCS;

public class hyperedge{
    int hypeid;//超边id
    int verdex;//由于哪个点而推出,-1代表无意义
    public hyperedge(int hypeid,int verdex){
        this.hypeid=hypeid;
        this.verdex=verdex;
    }
}