/**
 * 
 */
package com.borunovv.ga.crossover;

import com.borunovv.ga.Genome;

/**
 * @author borunovv
 * @date 26-01-2017
 */
public interface ICrossover {
    void merge(Genome first, Genome second, Genome result);
}
