package gitlet;

import java.io.File;
import java.io.Serializable;

/** Returns a byte array containing the serialized contents of OBJ.
 * @author lucychen*/
public class Blob implements Serializable {

    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _name;

    /** Returns a byte array containing the serialized contents of OBJ. */
    private File _file;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _sha1;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private String _path = "";

    /** Returns a byte array containing the serialized contents of OBJ. */
    Blob() {
        _name = "";
    }


    /** Returns a byte array containing the serialized contents of OBJ.
     * @param path dfgf
     * @param name dfg
     * @param file dfgdf*/
    Blob(String path, String name, File file) {
        _path = path;
        _name = name;
        _file = file;
        _sha1 = Utils.sha1(Utils.readContents(file));
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    String getPath() {
        return _path;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    String getName() {
        return _name;
    }
    /** Returns a byte array containing the serialized contents of OBJ. */
    File getFile() {
        return _file;
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param f dfgdfg*/
    void setFile(File f) {
        _file = f;
    }

    /** Returns a byte array containing the serialized contents of OBJ. */
    String getSha1() {
        return _sha1;
    }
}
