package gitlet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;


/** Returns a byte array containing the serialized contents of OBJ.
 * @author Lucy chen*/
public class CommitTree<T> extends LinkedList<T> implements Serializable {

    /** Returns a byte array containing the serialized contents of OBJ. */
    private Commit _head;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private Commit _chead;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _branch;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _path;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private HashMap<String, Commit> _splits = new HashMap<String, Commit>();
    /** Returns a byte array containing the serialized contents of OBJ. */
    private HashMap<String, Commit> _commits = new HashMap<String, Commit>();
    /** Returns a byte array containing the serialized contents of OBJ. */
    private List<CommitTree<Commit>> _children;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private TreeMap<String, Commit> _branches = new TreeMap<String, Commit>();

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param data */
    CommitTree(Commit data) {
        this._head = data;
        this._chead = data;
        addBranch(_chead.getBranch());

        this._children = new LinkedList<CommitTree<Commit>>();
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param c dgdgdfg*/
    void addCommit(Commit c) {
        addBranch(c.getBranch());
        if (_commits.containsKey(c.getSha1())) {
            _commits.remove(c.getSha1());
        }
        _commits.put(c.getName(), c);
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param path dfgdfg*/
    void setPath(String path) {
        _path = path;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    String getPath() {
        return _path;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch dsfds*/
    void addBranch(String branch) {

        if (_branches.containsKey(branch)) {
            _branches.remove(branch);
        } else {
            addSplit(_head.getBranch(), branch);
        }
        _branches.put(branch, _head);
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param x  sdf
     * @param y dsfdsf*/
    void addSplit(String x, String y) {
        if (x.compareTo(y) < 0) {
            if (_splits.containsKey(x + "_" + y)) {
                _splits.remove(x + "_" + y);
            }
            _splits.put(x + "_" + y, _head);
        } else if (x.compareTo(y) > 0) {
            if (_splits.containsKey(y + "_" + x)) {
                _splits.remove(y + "_" + x);
            }
            _splits.put(y + "_" + x, _head);
        } else {
            return;
        }
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param x dsfsf
     * @param y sdfdf
     * @param c sdfdsf*/
    void addDefSplit(String x, String y, Commit c) {
        if (x.compareTo(y) < 0) {
            if (_splits.containsKey(x + "_" + y)) {
                _splits.remove(x + "_" + y);
            }
            _splits.put(x + "_" + y, c);
        } else if (x.compareTo(y) > 0) {
            if (_splits.containsKey(y + "_" + x)) {
                _splits.remove(y + "_" + x);
            }
            _splits.put(y + "_" + x, c);
        }
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param x sdfsdf
     * @param y dsfsfd*/
    Commit findSplitt(Commit x, Commit y) {
        HashMap<String, Commit> a = new HashMap<>();
        HashMap<String, Commit> b = new HashMap<>();
        ArrayList<Commit> c = new ArrayList<>();
        String p = x.getParent();
        String q = y.getParent();
        while (!p.equals("")) {
            if (!a.containsKey(p)) {
                a.put(p, _commits.get(p));
            }
            p = _commits.get(p).getParent();
        }
        p = x.getMparent();
        while (!p.equals("")) {
            if (!a.containsKey(p)) {
                a.put(p, _commits.get(p));
            }
            p = _commits.get(p).getMparent();
        }
        while (!q.equals("")) {
            Commit here = _commits.get(q);
            if (a.containsKey(q) && !c.contains(here)) {
                if (c.size() != 0) {
                    if (c.get(0).getNode() <= here.getNode()) {
                        c.clear();
                        c.add(here);
                    }
                } else {
                    c.add(here);
                }
            }
            if (!b.containsKey(q)) {
                b.put(q, _commits.get(q));
            }
            q = _commits.get(q).getParent();
        }
        q = y.getMparent();
        while (!q.equals("")) {
            Commit here = _commits.get(q);
            if (a.containsKey(q) && !c.contains(here)) {
                if (c.size() != 0) {
                    if (c.get(0).getNode() <= here.getNode()) {
                        c.clear();
                        c.add(here);
                    }
                } else {
                    c.add(here);
                }
            }
            if (!b.containsKey(q)) {
                b.put(q, _commits.get(q));
            }
            q = _commits.get(q).getMparent();
        }
        return c.get(0);
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param y sefsf
     * @param x fdsdfsd*/
    Commit findSplit(String x, String y) {
        if ((x.equals(y) && !x.equals(""))
                || (!_splits.containsKey(y + "_" + x)
                && !_splits.containsKey(x + "_" + y))) {
            if (_branches.get(x).getParent().length() == 0) {
                return _branches.get("master");
            }
            x = _commits.get(_branches.get(x).getParent()).getBranch();
            String s;
            Commit b = null;
            Commit a = null;
            if (_branches.get(x).getMparent().length() != 0) {
                s = _commits.get(_branches.get(x).getMparent()).getBranch();
                b = findSplit(s, y);
            } else {
                a = findSplit(x, y);
            }

            if (a != null && b != null) {
                if (a.getPlace().getChildren().size()
                        < b.getPlace().getChildren().size()) {
                    return a;
                }
                return b;
            }
            if (a == null) {
                return b;
            } else {
                return a;
            }
        }
        if (x.compareTo(y) < 0) {
            return _splits.get(x + "_" + y);
        } else if (x.compareTo(y) > 0) {
            return _splits.get(y + "_" + x);
        }
        return null;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    HashMap<String, Commit> getSplits() {
        return _splits;
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch sdfsdf*/
    Commit findBranchhead(String branch) {
        return _branches.get(branch);
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch fdsfd*/
    void setBranch(String branch) {
        _branch = branch;
        addBranch(branch);
    }

    /** Returns a byte array containing the serialized contents of OBJ. */
    String getBranch() {
        return _branch;
    }

    /** Returns a byte array containing the serialized contents of OBJ. */
    TreeMap<String, Commit> getBranches() {
        return _branches;
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param head sdfdsff*/
    void changeHead(Commit head) {
        _head = head;
        _branch = head.getPlace().getBranch();
        _children = head.getChildren();
    }

    /** Returns a byte array containing the serialized contents of OBJ. */
    public Commit getHead() {
        return _head;
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param head sdfsdf*/
    public void setHead(Commit head) {
        _head = head;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    public Commit getChead() {
        return _chead;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    List<CommitTree<Commit>> getChildren() {
        return _children;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    HashMap<String, Commit> getCommits() {
        return _commits;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param child sdfsdf*/
    public CommitTree addChild(Commit child) {
        CommitTree<Commit> childCommits = new CommitTree<Commit>(child);
        this._children.add(childCommits);
        return childCommits;
    }


}
