package gitlet;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.io.File;
import java.io.Serializable;
import java.util.List;

/** Returns a byte array containing the serialized contents of OBJ.
 * @author Lucy Chen*/
public class Commit implements Serializable {

    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _msg = "";
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _branch;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _sha1;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _time;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private int _node = 0;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _path;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private HashMap<String, Blob> _files = new HashMap<String, Blob>();
    /** Returns a byte array containing the serialized contents of OBJ. */
    private Calendar _timestamp;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _parent;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _parent2 = "";
    /** Returns a byte array containing the serialized contents of OBJ. */
    private CommitTree<Commit> _place;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private List<CommitTree<Commit>> _children;

    /** Returns a byte array containing the serialized contents of OBJ. */
    Commit() {
        _files = new HashMap<String, Blob>();
        Formatter fmt = new Formatter();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        _node = 0;
        fmt = new Formatter();
        fmt.format("%ta %tb %te %tT %tY %tz", cal, cal, cal, cal, cal, cal);
        _time = fmt.toString();
        _timestamp = cal;
        _msg = "initial commit";
        _branch = "master";
        _parent = "";
        setSha1(Utils.sha1(Utils.serialize(this)));
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param c */
    Commit(Commit c) {
        copy(c);
    }


    /** Returns a byte array containing the serialized contents of OBJ.
     * @param parent gdgdfg
     * @param msg fgdfg*/
    Commit(Commit parent, String msg) {
        _node = new File(System.getProperty("user.dir") + "/"
                + ".gitlet" + "/" + "commits").list().length;
        _files = parent.getFiles();
        _parent = parent.getSha1();
        _branch = parent.getBranch();
        _msg = msg;
        Formatter fmt = new Formatter();
        Calendar cal = Calendar.getInstance();
        fmt = new Formatter();
        fmt.format("%ta %tb %te %tT %tY %tz", cal, cal, cal, cal, cal, cal);
        _time = fmt.toString();
        _timestamp = cal;
        _sha1 = Utils.sha1(_msg + _timestamp + "com*mit");
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    int getNode() {
        return _node;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param m dfgdfg*/
    void setMparent(Commit m) {
        _parent2 = m.getSha1();
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    String getMparent() {
        return _parent2;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    Commit get() {
        return this;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param tree dfgdfg*/
    void setPlace(CommitTree<Commit> tree) {
        _place = tree;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    CommitTree<Commit> getPlace() {
        return _place;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch sdfsdf */
    void setBranch(String branch) {
        _branch = branch;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    String getBranch() {
        return _branch;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param parent sdfsdf*/
    void setParent(Commit parent) {
        _parent = parent.getSha1();
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    String getParent() {
        return _parent;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param child sdfsdf*/
    void setChild(Commit child) {
        _place.addChild(child);
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    List<CommitTree<Commit>> getChildren() {
        _children = _place.getChildren();
        return _children;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param files sdfsdf*/
    void setFiles(HashMap<String, Blob> files) {
        _files = files;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param b sdfsdf */
    void addFile(Blob b) {
        String name = b.getName();
        if (_files.containsKey(b.getName())) {
            _files.remove(b.getName());
        }
        _files.put(name, b);
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    HashMap<String, Blob> getFiles() {
        return _files;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    Calendar getTime() {
        return _timestamp;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param msg dsf*/
    void setMsg(String msg) {
        _msg = msg;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param name */
    Blob getBlob(String name) {
        if (_files.containsKey(name)) {
            return _files.get(name);
        }
        return null;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    String getName() {
        return _sha1;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    String getMsg() {
        return _msg;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param sha1 sdfsdf*/
    void setSha1(String sha1) {
        _sha1 = sha1;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    String getSha1() {
        return _sha1;
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param c */
    void copy(Commit c) {
        _files = getFiles();
        _msg = c.getMsg();
        _timestamp = c.getTime();
        _sha1 = c.getSha1();
        _children = c.getChildren();
        _parent = c.getParent();
    }

}
