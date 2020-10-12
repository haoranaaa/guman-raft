package com.guman.raft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author duanhaoran
 * @since 2020/4/23 10:44 PM
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pair implements Serializable {

    /**
     * Key of this <code>Pair</code>.
     */
    private String key;

    /**
     * Value of this this <code>Pair</code>.
     */
    private String value;

}
