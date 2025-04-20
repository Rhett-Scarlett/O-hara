package MMCS;


import java.util.List;

//负责管理超图结构：超边、顶点、cand、crits、uncovs 等
public class Hypergraph {
    private final List<List<Integer>> edges;

    public Hypergraph(List<List<Integer>> edges) {
        this.edges = edges;
    }

    public int size() {
        return edges.size();
    }

    public List<Integer> getEdge(int id) {
        return edges.get(id);
    }

    public List<List<Integer>> getAllEdges() {
        return edges;
    }
}

