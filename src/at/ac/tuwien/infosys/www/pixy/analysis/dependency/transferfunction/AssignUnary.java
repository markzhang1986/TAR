package at.ac.tuwien.infosys.www.pixy.analysis.dependency.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dependency.DependencyLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.TacPlace;
import at.ac.tuwien.infosys.www.pixy.conversion.Variable;
import at.ac.tuwien.infosys.www.pixy.conversion.cfgnodes.AbstractCfgNode;

import java.util.Set;

/**
 * Transfer function for unary assignment nodes.
 *
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class AssignUnary extends TransferFunction {
    private Variable left;
    private TacPlace right;
    private int op;
    private Set<Variable> mustAliases;
    private Set<Variable> mayAliases;
    private AbstractCfgNode cfgNode;

// *********************************************************************************
// CONSTRUCTORS ********************************************************************
// *********************************************************************************

    // mustAliases, mayAliases: of setMe
    public AssignUnary(
        TacPlace left, TacPlace right, int op, Set<Variable> mustAliases, Set<Variable> mayAliases, AbstractCfgNode cfgNode
    ) {
        this.left = (Variable) left;  // must be a variable
        this.right = right;
        this.op = op;
        this.mustAliases = mustAliases;
        this.mayAliases = mayAliases;
        this.cfgNode = cfgNode;
    }

// *********************************************************************************
// OTHER ***************************************************************************
// *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {

        DependencyLatticeElement in = (DependencyLatticeElement) inX;
        DependencyLatticeElement out = new DependencyLatticeElement(in);

        // let the lattice element handle the details
        out.assign(left, mustAliases, mayAliases, cfgNode);

        return out;
    }
}