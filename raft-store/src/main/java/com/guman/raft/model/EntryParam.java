package com.guman.raft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by guman on 2020/3/24.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntryParam implements Serializable,Comparable {

    public EntryParam(long term, Pair pair) {
        this(null, term, pair);
    }

    private Long index;

    private long term;

    private Pair pair;

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        if (this.getIndex() > ((EntryParam) o).getIndex()) {
            return 1;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryParam that = (EntryParam) o;
        return term == that.term &&
                Objects.equals(index, that.index) &&
                Objects.equals(pair, that.pair);
    }

    @Override
    public int hashCode() {

        return Objects.hash(index, term, pair);
    }
}
