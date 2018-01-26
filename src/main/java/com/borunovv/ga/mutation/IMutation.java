/**
 * 
 */
package com.borunovv.ga.mutation;

import com.borunovv.ga.Genome;

/**
 * @author borunovv
 * @date 26-01-2017
 */
public interface IMutation {
    boolean mutate(Genome genome);
}
