package at.ac.tuwien.infosys.www.pixy.analysis;

/**
 * Forces lattice element implementations to think about their equality condition,
 * which is needed for the worklist algorithm.
 *
 * Bad: If you do this, you can't access the natural (native) implementations any longer.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public abstract class AbstractLatticeElement implements Recyclable {

    // lubs the given element over *this* element;
    // can be called on every lattice element except Bottom and Top
    public abstract void lub(AbstractLatticeElement element);

    // returns a clone (deep copy) of this object;
    // can be called on every lattice element except Bottom and Top
    public abstract AbstractLatticeElement cloneMe();

    public abstract boolean structureEquals(Object compX);

    public abstract int structureHashCode();

    public abstract void dump();
}