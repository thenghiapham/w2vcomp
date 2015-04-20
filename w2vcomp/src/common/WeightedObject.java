package common;

import java.io.Serializable;

/**
 * This is a wrapper for any object which simply traps a double valued weight to it.
 * @author bkievitk
 */

public class WeightedObject<E> implements Serializable, Comparable<WeightedObject<E>> {
    
    private static final long serialVersionUID = 7934819155214251197L;

    // The data object.
    public E object;
    
    // The weight value.
    public double weight;
    
    /**
     * Build with object and weight.
     * @param object
     * @param weight
     */
    public WeightedObject(E object, double weight) {
        this.object = object;
        this.weight = weight;
    }
    
    /**
     * The equals function should act only on the object if possible.
     */
    public boolean equals(Object o) {
        if(o instanceof WeightedObject<?>) {
            return object.equals(((WeightedObject<?>)o).object);
        } else {
            return object.equals(o);
        }
    }
    
    /**
     * If we need to hash, just use the objects hash function.
     */
    public int hashCode() {
        return object.hashCode();
    }
    
    /**
     * Just write the object and weight.
     */
    public String toString() {
        return object + " [" + weight + "]";
    }


    public int compareTo(WeightedObject<E> arg0) {
        if(weight < arg0.weight) {
            return -1;
        } else if(weight == arg0.weight) {
            return 0;
        } else {
            return 1;
        }
    }
}