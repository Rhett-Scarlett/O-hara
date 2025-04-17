package Sample;

import java.util.ArrayList;
import java.util.List;

public class Hypedges {
    public List<List<Integer>> hyperedges;
    public Hypedges() {
        hyperedges = new ArrayList<>();
    }
    public boolean is_subset_of(List<Integer> a, List<Integer> b) {
        if (a.size() > b.size()) {
            return false;
        } else {
            int a_l = 0;

            for(int i = 0; i < b.size() && a_l < a.size(); ++i) {
                if ((Integer)b.get(i) > (Integer)a.get(a_l)) {
                    return false;
                }

                if (b.get(i) == a.get(a_l)) {
                    ++a_l;
                }
            }

            return a_l == a.size();
        }
    }

    public void addEdgeAndMinimizeInclusion(List<Integer> edge) {
        boolean is_supset = false;
        List<Integer> supsets_indeces = new ArrayList();

        for(int i_e = 0; i_e < this.hyperedges.size(); ++i_e) {
            if (this.is_subset_of((List)this.hyperedges.get(i_e), edge)) {
                is_supset = true;
                break;
            }

            if (this.is_subset_of(edge, (List)this.hyperedges.get(i_e))) {
                supsets_indeces.add(i_e);
            }
        }

        if (!is_supset) {
            for(int i = 0; i < supsets_indeces.size(); ++i) {
                this.hyperedges.remove(supsets_indeces.get(i));
            }

            this.hyperedges.add(edge);
        }

    }
}
