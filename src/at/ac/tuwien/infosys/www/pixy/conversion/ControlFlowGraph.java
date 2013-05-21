package at.ac.tuwien.infosys.www.pixy.conversion;

import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNode;
import at.ac.tuwien.infosys.www.pixy.conversion.nodes.CfgNodeCall;

import java.util.*;

/**
 * @author Nenad Jovanovic <enji@seclab.tuwien.ac.at>
 */
public final class ControlFlowGraph {
    private CfgNode head;
    private final CfgNode tail;
    private final int tailEdgeType;

// CONSTRUCTORS ********************************************************************

    ControlFlowGraph(CfgNode head, CfgNode tail, int tailEdgeType) {
        this.head = head;
        this.tail = tail;
        this.tailEdgeType = tailEdgeType;
    }

    ControlFlowGraph(CfgNode head, CfgNode tail) {
        this.head = head;
        this.tail = tail;
        this.tailEdgeType = CfgEdge.NORMAL_EDGE;
    }

    // dummy constructor, can be used for "getFunction"
    public ControlFlowGraph() {
        this.tail = null;
        this.tailEdgeType = 0;
    }

// GET *****************************************************************************

    public CfgNode getHead() {
        return this.head;
    }

    public CfgNode getTail() {
        return this.tail;
    }

    int getTailEdgeType() {
        return this.tailEdgeType;
    }

    // returns the function that contains the given CFG node;
    // throws an exception if it does not succeed
    public static TacFunction getFunction(CfgNode cfgNode) {
        return cfgNode.getEnclosingFunction();
    }

    // EFF: this function should be called only once, and the result cached;
    // this is what currently happens in ConnectorComputation
    public List<CfgNodeCall> getContainedCalls() {
        List<CfgNodeCall> retMe = new LinkedList<>();
        for (CfgNode cfgNode : this.dfPreOrder()) {
            if (cfgNode instanceof CfgNodeCall) {
                retMe.add((CfgNodeCall) cfgNode);
            }
        }
        return retMe;
    }

    // returns the head of this cfg by walking backwards;
    // only use this for linear cfgs (e.g., default param cfgs)
    public static CfgNode getHead(CfgNode cfgNode) {

        boolean goOn = true;
        while (goOn) {
            List<CfgNode> pre = cfgNode.getPredecessors();
            if (pre.size() == 0) {
                // found the head!
                goOn = false;
            } else if (pre.size() == 1) {
                // continue with predecessor
                cfgNode = pre.get(0);
            } else {
                // more than one predecessor...
                System.out.println(cfgNode.getLoc());
                throw new RuntimeException("SNH");
            }
        }
        return cfgNode;
    }

// SET *****************************************************************************

    void setHead(CfgNode head) {
        this.head = head;
    }

// OTHER ***************************************************************************

    public void assignReversePostOrder() {
        LinkedList<CfgNode> postorder = this.dfPostOrder();
        ListIterator<CfgNode> iter = postorder.listIterator(postorder.size());
        int i = 0;
        while (iter.hasPrevious()) {
            CfgNode cfgNode = iter.previous();
            cfgNode.setReversePostOrder(i);
            i++;
        }
    }

    // returns the number of nodes in this ControlFlowGraph
    public int size() {
        Set<CfgNode> visited = new HashSet<>();
        int s = size(this.head, visited);
        //visited = null;
        return s;
    }

    // helper function for size();
    // recursively calculates the number of successor nodes
    int size(CfgNode node, Set<CfgNode> visited) {

        if (!visited.contains(node)) {
            visited.add(node);
            int size = 1;
            CfgEdge[] outEdges = node.getOutEdges();
            for (CfgEdge outEdge : outEdges) {
                if (outEdge != null) {
                    size += size(outEdge.getDest(), visited);
                }
            }
            return size;
        } else {
            return 0;
        }
    }

// bfIterator **********************************************************************

    // breadth first iterator;
    // NOTE: when iterating over large CFGs, use should better use
    // dfPreOrderIterator; bfIterator tends to produce stack overflows
    public Iterator<CfgNode> bfIterator() {
        // list for the iterator
        LinkedList<CfgNode> list = new LinkedList<>();

        // queue for nodes that still have to be visited
        LinkedList<CfgNode> queue = new LinkedList<>();

        Set<CfgNode> visited = new HashSet<>();

        queue.add(this.head);
        visited.add(this.head);

        this.bfIteratorHelper(list, queue, visited);

        return list.iterator();
    }

// bfIteratorHelper ****************************************************************

    private void bfIteratorHelper(List<CfgNode> list, LinkedList<CfgNode> queue,
                                  Set<CfgNode> visited) {

        CfgNode cfgNode = queue.removeFirst();
        list.add(cfgNode);

        // handle successors
        for (int i = 0; i < 2; i++) {
            CfgEdge outEdge = cfgNode.getOutEdge(i);
            if (outEdge != null) {
                CfgNode succ = outEdge.getDest();
                // for all successors that have not been visited yet...
                if (!visited.contains(succ)) {
                    // add it to the queue
                    queue.add(succ);
                    // mark it as visited
                    visited.add(succ);
                }
            }
        }

        // if the queue is non-empty: recurse
        if (queue.size() > 0) {
            bfIteratorHelper(list, queue, visited);
        }
    }

//  depth first iterators **********************************************************

    // depth first iterator (preorder)
    public LinkedList<CfgNode> dfPreOrder() {
        LinkedList<CfgNode> preorder = new LinkedList<>();
        LinkedList<CfgNode> postorder = new LinkedList<>();
        this.dfIterator(preorder, postorder);
        return preorder;
    }

    // depth first iterator (postorder)
    public LinkedList<CfgNode> dfPostOrder() {
        LinkedList<CfgNode> preorder = new LinkedList<>();
        LinkedList<CfgNode> postorder = new LinkedList<>();
        this.dfIterator(preorder, postorder);
        return postorder;
    }

    // uses the given lists as containers for preorder and postorder
    private void dfIterator(LinkedList<CfgNode> preorder, LinkedList<CfgNode> postorder) {

        // auxiliary stack and visited set
        LinkedList<CfgNode> stack = new LinkedList<>();
        Set<CfgNode> visited = new HashSet<>();

        // visit head:
        // mark it as visited, add it to stack and preorder
        CfgNode current = this.head;
        visited.add(current);
        stack.add(current);
        preorder.add(current);

        // how it works:
        // while there is something on the stack:
        // - mark the top stack element as visited
        // - add it to the preorder list
        // - try to get an unvisited successor of this element
        // - if there is such a successor: push it on the stack and continue
        // - else: pop the stack and add the popped element to the postorder list
        while (!stack.isEmpty()) {

            // inspect the top stack element
            current = stack.getLast();

            // we will try to get an unvisited successor element
            CfgNode next = null;
            for (int i = 0; (i < 2) && (next == null); i++) {
                CfgEdge outEdge = current.getOutEdge(i);
                if (outEdge != null) {
                    next = outEdge.getDest();
                    if (visited.contains(next)) {
                        // try another one
                        next = null;
                    } else {
                        // found it!
                    }
                }
            }

            if (next == null) {
                // pop from stack and add it to the postorder list
                postorder.add(stack.removeLast());
            } else {
                // visit next
                visited.add(next);
                stack.add(next);
                preorder.add(next);
            }
        }
    }
}