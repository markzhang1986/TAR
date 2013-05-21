package at.ac.tuwien.infosys.www.pixy.analysis.dep.transferfunction;

import at.ac.tuwien.infosys.www.pixy.analysis.LatticeElement;
import at.ac.tuwien.infosys.www.pixy.analysis.TransferFunction;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepAnalysis;
import at.ac.tuwien.infosys.www.pixy.analysis.dep.DepLatticeElement;
import at.ac.tuwien.infosys.www.pixy.conversion.*;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public class DepTfCallPrep extends TransferFunction {
    private List<TacActualParam> actualParams;
    private List<TacFormalParam> formalParams;
    private TacFunction caller;
    private TacFunction callee;
    private DepAnalysis depAnalysis;
    private CfgNode cfgNode;

//  *********************************************************************************
//  CONSTRUCTORS ********************************************************************
//  *********************************************************************************

    public DepTfCallPrep(
        List<TacActualParam> actualParams, List<TacFormalParam> formalParams, TacFunction caller, TacFunction callee,
        DepAnalysis depAnalysis, CfgNode cfgNode
    ) {
        this.actualParams = actualParams;
        this.formalParams = formalParams;
        this.caller = caller;
        this.callee = callee;
        this.depAnalysis = depAnalysis;
        this.cfgNode = cfgNode;
    }

//  *********************************************************************************
//  OTHER ***************************************************************************
//  *********************************************************************************

    public LatticeElement transfer(LatticeElement inX) {
        DepLatticeElement in = (DepLatticeElement) inX;
        DepLatticeElement out = new DepLatticeElement(in);

        // set formal params...

        // use a ListIterator for formals because we might need to step back (see below)
        ListIterator<TacFormalParam> formalIter = formalParams.listIterator();
        Iterator<TacActualParam> actualIter = actualParams.iterator();

        // for each formal parameter...
        while (formalIter.hasNext()) {
            TacFormalParam formalParam = formalIter.next();

            if (actualIter.hasNext()) {
                // there is a corresponding actual parameter; advance iterator
                actualIter.next();

                // set the formal
                out.setFormal(formalParam, cfgNode);
            } else {
                // there is no corresponding actual parameter, use default values
                // for the remaining formal parameters

                // make one step back (so we can use a while loop)
                formalIter.previous();

                while (formalIter.hasNext()) {
                    formalParam = formalIter.next();

                    if (formalParam.hasDefault()) {
                        ControlFlowGraph defaultControlFlowGraph = formalParam.getDefaultControlFlowGraph();

                        // default CFG's have no branches;
                        // start at the CFG's head and apply all transfer functions
                        CfgNode defaultNode = defaultControlFlowGraph.getHead();
                        while (defaultNode != null) {
                            TransferFunction tf = this.depAnalysis.getTransferFunction(defaultNode);
                            out = (DepLatticeElement) tf.transfer(out);
                            defaultNode = defaultNode.getSuccessor(0);
                        }
                    } else {
                        // missing actual parameter;
                        // we have already generated a warning for this during conversion;
                        // simply ignore it (=ok, is exactly what PHP does)
                    }
                }
            }
        }

        // reset all local variables that belong to the symbol table of the
        // caller; shortcut: if the caller is main, we don't have to do
        // this (since there are no real local variables in the main function)
        SymbolTable callerSymTab = this.caller.getSymbolTable();
        if (!callerSymTab.isMain()) {
            // only do this for non-recursive calls;
            // EFF: it might be better to reset everything except the formal params;
            // TODO: also think about correctness
            if (!(callee == caller)) {
                out.resetVariables(callerSymTab);
            }
        } else {
            // for the main function, we can at least reset the temporary variables
            out.resetTemporaries(callerSymTab);
        }

        return out;
    }
}