package Sample;

import java.util.Objects;

public class TuplePair {
    int id1;
    int id2;

    public TuplePair(int id1, int id2) {
        // 保证无序对：小的在前，大的在后
        if (id1 < id2) {
            this.id1 = id1;
            this.id2 = id2;
        } else {
            this.id1 = id2;
            this.id2 = id1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TuplePair pair = (TuplePair) o;
        return id1 == pair.id1 && id2 == pair.id2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }

    @Override
    public String toString() {
        return "(" + id1 + ", " + id2 + ")";
    }
}