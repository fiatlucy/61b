package gitlet;


import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Lucy Chen
 */

public class Main implements Serializable {


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        try {
            new Main(args);
            return;
        } catch (GitletException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(0);

    }

    /** Returns a byte array containing the serialized contents of OBJ.
     *  @param p fdgfdggf*/
    List<File> getFilesList(Path p) throws IOException {
        List<File> filesInFolder = Files.walk(p)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        return filesInFolder;
    }


    /** Returns a byte array containing the serialized contents of OBJ. */
    @SuppressWarnings("unchecked")
    void refresh() throws IOException {
        _addblobs = new HashMap<String, Blob>();
        _rmblobs = new HashMap<String, Blob>();
        Path tree = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "tree").toPath();
        Path stagingadd = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "staging", "add").toPath();
        Path stagingrm = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "staging", "remove").toPath();


        List<File> sa = getFilesList(stagingadd);
        for (int i = 0; i < sa.size(); i++) {
            File a = sa.get(i);
            Blob b = Utils.readObject(a, Blob.class);
            _addblobs.put(b.getName(), b);
        }


        File[] trepo = tree.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().equals("repo");
            }
        });


        File repof = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "tree/repo");
        File commitsf = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "tree/commits");

        _tree = Utils.readObject(repof, CommitTree.class);



        _commits = Utils.readObject(commitsf, HashMap.class);


        List<File> sr = getFilesList(stagingrm);
        for (int i = 0; i < sr.size(); i++) {
            File a = sr.get(i);
            Blob b = Utils.readObject(a, Blob.class);
            _rmblobs.put(b.getName(), b);
        }

        File source = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "currCommit");

        File f = Utils.join(System.getProperty("user.dir"), ".gitlet",
                "temp", "currCommit");
        Path target = Paths.get(f.getAbsolutePath());
        Files.copy(source.toPath(), target,
                StandardCopyOption.REPLACE_EXISTING);
        _currCom = Utils.readObject(f, Commit.class);
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param folder fdgfdg*/
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f: files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }


    /** Returns a byte array containing the serialized contents of OBJ.
     * @param args fdgdfgf */
    Main(String[] args) throws IOException {
        if (!args[0].equals("init")) {
            refresh();
        }
        if (args[0].equals("init")) {
            init();
        } else if (args[0].equals("log")) {
            log(_currCom.getBranch());
        } else if (args[0].equals("branch")) {
            branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            rmbranch(args[1]);
        } else if (args[0].equals("merge")) {
            merge(args[1]);
        } else if (args[0].equals("global-log")) {
            globallog();
        } else if (args[0].equals("status")) {
            status();
        } else if (args[0].equals("find")) {
            find(args[1]);
        } else if (args[0].equals("reset")) {
            reset(args[1]);
        } else if (args[0].equals("add")) {
            File f = Utils.join(System.getProperty("user.dir"), args[1]);
            if (!f.exists()) {
                System.out.println("File does not exist.");
                System.exit(0);
            }
            add(f);
        } else if (args[0].equals("commit")) {
            if (args.length < 2 || (args.length > 2
                    && Pattern.matches("(?:^|\\s)'([^']*?)'(?:$|\\s)",
                    args.toString().substring(5)))) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            commit(args[1]);
        } else if (args[0].equals("checkout")) {
            checkout1(args);
        } else if (args[0].equals("rm")) {
            rm(Utils.join(System.getProperty("user.dir"), ".gitlet",
                    "commits", Integer.toString(_currCom.getNode()),
                    "commit",  args[1]));
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        end();
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param args */
    void checkout1(String[] args) throws IOException {
        if (args.length == 3) {
            checkout(Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(_currCom.getNode()), "commit", args[2]));
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            if (!_commits.containsKey(args[1])) {
                System.out.println("No commit with that id exists");
                System.exit(0);
            }
            Commit c = Utils.readObject(_commits.get(args[1]), Commit.class);
            File p = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(c.getNode()), "commit", args[3]);
            checkout(args[1], p);
        } else if (args.length == 2) {
            checkout(args[1]);
        }
    }

    /** Returns a byte array containing the serialized contents of OBJ. */
    void end() throws IOException {
        File addp = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "staging", "add");
        deleteFolder(addp);
        File rmp = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "staging", "remove");

        deleteFolder(rmp);
        addp.mkdir();
        rmp.mkdir();

        for (Map.Entry<String, Blob> add : _addblobs.entrySet()) {
            Blob b = add.getValue();
            File out = Utils.join(System.getProperty("user.dir"),
                    ".gitlet",  "staging", "add", b.getName());

            Utils.writeObject(out, b);
        }

        for (Map.Entry<String, Blob> rm : _rmblobs.entrySet()) {
            Blob b = rm.getValue();
            File out = Utils.join(System.getProperty("user.dir"),
                    ".gitlet",  "staging", "remove", b.getName());

            Utils.writeObject(out, b);
        }
        File a = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "tree", "repo");
        File b = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "tree", "commits");
        Files.deleteIfExists(a.toPath());
        Files.deleteIfExists(b.toPath());


        File outtt = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "tree", "repo");
        File l = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "tree", "repo");
        _tree.setPath(l.getAbsolutePath());
        Utils.writeObject(outtt, _tree);

        File outt = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "tree", "commits");

        Utils.writeObject(outt, _commits);



        File cCom = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "currCommit");
        Files.deleteIfExists(cCom.toPath());
        File ccout = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "currCommit");
        Utils.writeObject(ccout, _currCom);


    }


    /** Returns a byte array containing the serialized contents of OBJ. */
    private void init() throws IOException {

        File file = Utils.join(System.getProperty("user.dir"), ".gitlet");
        boolean bool = file.mkdir();
        if (!bool) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
        Commit first = new Commit();
        _currCom = first;
        _tree = new CommitTree<Commit>(first);
        _addblobs = new HashMap<String, Blob>();
        _rmblobs = new HashMap<String, Blob>();
        _tree.setHead(first);
        _tree.addCommit(first);

        _tree.setBranch(_currCom.getBranch());
        first.setPlace(_tree);

        File repo = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "tree");
        repo.mkdir();
        File staging = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "staging");

        staging.mkdir();
        File rm = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "staging", "remove");

        rm.mkdir();
        File add = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "staging", "add");

        add.mkdir();
        File temp = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "temp");

        temp.mkdir();
        File cout = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "commits");

        cout.mkdir();

        File commit = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "commits", Integer.toString(0));

        commit.mkdir();


        File out = Utils.join(System.getProperty("user.dir"), ".gitlet",
                "commits", Integer.toString(0), _currCom.getSha1());
        Files.write(out.toPath(), Utils.serialize(_currCom));
        if (!_commits.containsKey(_currCom.getName())) {
            _commits.put(_currCom.getName(), out);
            _tree.addCommit(_currCom);
        }

    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param f dsfdsf */
    private void add(File f) throws IOException {
        Blob file = new Blob(f.getAbsolutePath(), f.getName(), f);
        if (_addblobs.containsKey(file.getName())
                && !_currCom.getFiles().containsKey(f.getName())) {
            _addblobs.remove(file.getName());
            _addblobs.put(file.getName(), file);
        } else if (_currCom.getFiles().containsKey(f.getName())) {
            File l = _currCom.getFiles().get(f.getName()).getFile();
            byte[] a = Utils.readContents(l);
            byte[] b = Utils.readContents(f);
            File r = _currCom.getFiles().get(f.getName()).getFile();
            String s = Utils.readContentsAsString(r);
            if (!Arrays.equals(a, b)
                    || !Utils.readContentsAsString(f).equals(s)) {

                if (_addblobs.containsKey(file.getName())) {

                    _addblobs.remove(file.getName());
                }
                _addblobs.put(file.getName(), file);
            }
        } else {
            _addblobs.put(file.getName(), file);
        }
        if (_rmblobs.containsKey(file.getName())) {
            _rmblobs.remove(file.getName());

        }
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param msg vxcvxc*/
    @SuppressWarnings("unchecked")
    private Commit commit(String msg) throws IOException {
        if (msg.length() == 0) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        _currCom = new Commit(_currCom, msg);
        _currCom.setPlace(_tree.addChild(_currCom));
        _tree.setHead(_currCom);
        if (_addblobs.size() == 0 && _rmblobs.size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        for (Map.Entry<String, Blob> entry : _rmblobs.entrySet()) {
            if (_currCom.getFiles().containsKey(entry.getKey())) {
                _currCom.getFiles().remove(entry.getKey());
                Files.deleteIfExists(Paths.get(entry.getValue().getPath()));
            }
        }
        _currCom.setSha1(Utils.sha1(Utils.serialize(_currCom)));
        File repo = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "commits",
                Integer.toString(_currCom.getNode()), "commit");
        repo.mkdirs();
        for (Map.Entry<String, Blob> e : _currCom.getFiles().entrySet()) {
            File data = e.getValue().getFile();
            File out = Utils.join(System.getProperty("user.dir"), ".gitlet",
                    "commits", Integer.toString(_currCom.getNode()),
                    "commit",  e.getValue().getName());
            Files.write(out.toPath(), Files.readAllBytes(data.toPath()));
        }
        for (Map.Entry<String, Blob> e : _addblobs.entrySet()) {
            File data = e.getValue().getFile();
            File out = Utils.join(System.getProperty("user.dir"), ".gitlet",
                    "commits", Integer.toString(_currCom.getNode()),
                    "commit", e.getValue().getName());
            Files.write(out.toPath(), Files.readAllBytes(data.toPath()));
            if (_currCom.getFiles().containsKey(e.getKey())) {
                _currCom.getFiles().remove(e.getKey());
            }
            e.getValue().setFile(out);
            _currCom.addFile(e.getValue());
        }
        File com = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "commits",
                Integer.toString(_currCom.getNode()), _currCom.getSha1());
        Files.write(com.toPath(), Utils.serialize(_currCom));
        _commits.put(_currCom.getName(), com);
        _tree.addCommit(_currCom);
        File comm = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "commits", Integer.toString(_currCom.getNode()),
                _currCom.getBranch(), _currCom.getParent());
        comm.mkdir();
        r();
        return _currCom;
    }

    /** Returns a byte array containing the serialized contents of OBJ.*/
    private void r() {
        File a = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "staging", "add");
        File b = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "staging", "remove");
        deleteFolder(a);
        deleteFolder(b);
        a.mkdir();
        b.mkdir();
        _addblobs = new HashMap<String, Blob>();
        _rmblobs = new HashMap<String, Blob>();
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param file fsdfsdf */
    private void checkout(File file) throws IOException {
        if (!_currCom.getFiles().containsKey(file.getName())) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob b = _currCom.getFiles().get(file.getName());
        cohelp(b, file.toPath());
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param file dfgdf
     * @param id jnjnkjns*/
    private void checkout(String id, File file) throws IOException {
        if (!_commits.containsKey(id)) {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
        File f = _commits.get(id);
        Commit here = Utils.readObject(f, Commit.class);
        if (!here.getFiles().containsKey(file.getName())) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob b = here.getFiles().get(file.getName());
        cohelp(b, Paths.get(file.getAbsolutePath()));
    }


    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch dfd*/
    private void checkout(String branch) throws IOException  {
        Commit here = _tree.findBranchhead(branch);
        if (here == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branch.equals(_currCom.getBranch())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        File f = Paths.get(System.getProperty("user.dir")).toFile();
        File[] files = f.listFiles();

        here = Utils.readObject(_commits.get(here.getName()), Commit.class);
        for (Map.Entry<String, Blob> b : _currCom.getFiles().entrySet()) {
            if (!here.getFiles().containsKey(b.getKey())) {
                Files.deleteIfExists(Paths.get(b.getValue().getPath()));
            }
        }
        for (Map.Entry<String, Blob> b : here.getFiles().entrySet()) {
            cohelp(b.getValue(), Paths.get(b.getValue().getPath()));
        }
        _currCom = here;

        if (_currCom.getBranch() != branch) {
            _addblobs = new HashMap<String, Blob>();
            _rmblobs = new HashMap<String, Blob>();
        }
        _currCom.setBranch(branch);
        _currCom.getPlace().setBranch(branch);
        _tree.setHead(here);
    }


    /** Returns a byte array containing the serialized contents of OBJ.
     * @param b sdf
     * @param p sdfsdf*/
    private void cohelp(Blob b, Path p) throws IOException {
        Files.deleteIfExists(Paths.get(b.getPath()));
        FileOutputStream f = new FileOutputStream(b.getPath());
        f.write(Files.readAllBytes(b.getFile().toPath()));
        f.close();

    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch fsfsdf*/
    void branch(String branch) {
        if (_tree.getBranches().containsKey(branch)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        _tree.setBranch(branch);
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param c */
    void deleteCommit(Commit c) throws IOException {
        _tree.getCommits().remove(c.getName());
        _commits.remove(c.getName());
        Path p = Paths.get(System.getProperty("user.dir") + "/" + ".gitlet"
                + "/" + "commits" + "/" + c.getNode() + "/" + c.getSha1());
        Files.deleteIfExists(p);
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch */
    void rmbranch(String branch) throws IOException {
        if (!_tree.getBranches().containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (_currCom.getBranch().equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        Commit here = _tree.findBranchhead(branch);
        _tree.getBranches().remove(here.getBranch());
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param message */
    void find(String message) throws IOException {
        boolean hass = false;
        for (Map.Entry<String, File> b : _commits.entrySet()) {
            Commit now = Utils.readObject(b.getValue(), Commit.class);
            if (now.getMsg().equals(message)) {
                hass = true;
                System.out.println(now.getSha1());
            }
        }
        if (!hass) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }


    /** Returns a byte array containing the serialized contents of OBJ.
     * @param commit sdfdf*/
    void reset(String commit) throws IOException {
        if (!_commits.containsKey(commit)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File f = Paths.get(System.getProperty("user.dir")).toFile();
        File[] files = f.listFiles();


        File g = _commits.get(commit);
        Commit c = Utils.readObject(g, Commit.class);
        HashMap<String, Blob> p  = c.getFiles();

        for (Map.Entry<String, Blob> l : p.entrySet()) {
            cohelp(l.getValue(), l.getValue().getFile().toPath());
        }
        for (Map.Entry<String, Blob> l : _currCom.getFiles().entrySet()) {
            if (!c.getFiles().containsKey(l.getKey())) {
                Files.deleteIfExists(Paths.get(l.getValue().getPath()));
            }
        }


        _currCom = c;
        _tree.setHead(_currCom);
        _addblobs.clear();
        _rmblobs.clear();

    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch fdgdf
     */
    void checkmerge(String branch) {

        Commit given = _tree.getBranches().get(branch);
        if (given == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (given == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Commit split = _tree.findSplitt(given, _currCom);
        if (branch.equals(_currCom.getBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        if (split.equals(given)) {
            System.out.println("Given branch "
                    + "is an ancestor of the current branch");
            return;
        }
        if (split.equals(_currCom)) {
            _currCom = given;
            System.out.println("Current branch fast-forwarded");
            return;
        }

    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch dsfsdf */
    void merge(String branch) throws IOException {
        HashMap<String, File> mergee = new HashMap<>();
        Commit given = _tree.getBranches().get(branch);
        checkmerge(branch);
        Commit split = _tree.findSplitt(given, _currCom);

        mergee = mergehelp2(mergee, given, split, branch);
        mergee = mergehelp3(mergee, given, split, branch);

        for (Map.Entry<String, Blob> l : split.getFiles().entrySet()) {
            Blob splitb = l.getValue();
            Blob givenb = given.getBlob(l.getKey());
            Blob current = _currCom.getBlob(l.getKey());
            File givenf = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(given.getNode()), "commit", l.getKey());
            File currentf = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(_currCom.getNode()), "commit", l.getKey());

            File splitf = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(split.getNode()), "commit", l.getKey());
            byte [] givenarr = {0};
            byte [] splitarr = {0};
            byte [] currarr = {0};
            if (givenf.exists()) {
                givenarr = Utils.readContents(givenf);
            }
            if (splitf.exists()) {
                splitarr = Utils.readContents(splitf);
            }
            if (currentf.exists()) {
                currarr = Utils.readContents(currentf);
            }
            if (Arrays.equals(currarr, splitarr) && givenb == null) {
                Path p = Paths.get(splitb.getPath());
                Files.deleteIfExists(p);
                _addblobs.remove(splitb.getName());
                _rmblobs.remove(splitb.getName());
                _currCom.getFiles().remove(splitb.getName());
            }
            if (currarr == null && givenarr == null) {
                Files.deleteIfExists(Paths.get(splitb.getPath()));
            }
            if (Arrays.equals(givenarr, splitarr) && current == null) {
                continue;
            }
            if (!Arrays.equals(currarr, splitarr)
                    && !Arrays.equals(givenarr, splitarr)
                    && !Arrays.equals(currarr, givenarr)) {
                if (!mergee.containsKey(splitf.getName())) {
                    mergee.put(splitf.getName(), splitf);
                }
            }
        }
        mergehelp1(mergee, given, split, branch);
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch dsffsg
     * @param merge dfgdfg
     * @param given fdgfdg
     * @param split fdgfdgfd*/
    HashMap<String, File> mergehelp3(HashMap<String, File> merge,
                                     Commit given, Commit split, String branch)
            throws IOException {
        HashMap<String, File> mergee = merge;
        for (Map.Entry<String, Blob> l : _currCom.getFiles().entrySet()) {
            Blob current = l.getValue();
            Blob givenb = given.getBlob(current.getName());
            Blob splitb = split.getBlob(current.getName());
            File givenf = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(given.getNode()), "commit", l.getKey());

            File currentf = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(_currCom.getNode()), "commit", l.getKey());

            File splitf = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(split.getNode()), "commit", l.getKey());

            byte [] givenarr = {0};
            byte [] splitarr = {0};
            byte [] currarr = {0};

            if (givenf.exists()) {
                givenarr = Utils.readContents(givenf);
            }
            if (splitf.exists()) {
                splitarr = Utils.readContents(splitf);
            }
            if (currentf.exists()) {
                currarr = Utils.readContents(currentf);
            }

            if (splitb == null && givenb == null) {

                continue;
            }
            if (!Arrays.equals(currarr, splitarr)
                    && Arrays.equals(splitarr, givenarr)) {
                continue;
            }
            if (Arrays.equals(currarr, givenarr)) {
                continue;
            }


            if (!Arrays.equals(currarr, splitarr) && givenb == null) {
                if (!mergee.containsKey(currentf.getName())) {
                    mergee.put(currentf.getName(), currentf);
                }
            }
            if (!Arrays.equals(currarr, givenarr) && splitb == null) {
                if (!mergee.containsKey(currentf.getName())) {
                    mergee.put(currentf.getName(), currentf);
                }
            }
        }
        return mergee;
    }
    /** Returns a byte array containing the serialized contents of OBJ.
     * @param split dfgdf
     * @param given dfgdfg
     * @param branch dfgdf
     * @param merge dfgdfg*/
    HashMap<String, File> mergehelp2(HashMap<String, File> merge,
                                     Commit given, Commit split, String branch)
            throws IOException {
        HashMap<String, File> mergee = merge;
        for (Map.Entry<String, Blob> l : given.getFiles().entrySet()) {
            Blob givenb = l.getValue();
            Blob splitb = split.getBlob(givenb.getName());
            Blob current = _currCom.getBlob(givenb.getName());
            File givenf = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(given.getNode()), "commit", l.getKey());
            File currentf = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(_currCom.getNode()), "commit", l.getKey());

            File splitf = Utils.join(System.getProperty("user.dir"),
                    ".gitlet", "commits",
                    Integer.toString(split.getNode()), "commit", l.getKey());
            byte [] givenarr = {0};
            byte [] splitarr = {0};
            byte [] currarr = {0};
            if (givenf.exists()) {
                givenarr = Utils.readContents(givenf);
            }
            if (splitf.exists()) {
                splitarr = Utils.readContents(splitf);
            }
            if (currentf.exists()) {
                currarr = Utils.readContents(currentf);
            }
            if (splitb == null && current == null) {
                checkout(given.getSha1(), givenb.getFile());
                addSA(givenb);
                continue;
            }
            if (!Arrays.equals(splitarr, givenarr)
                    && Arrays.equals(splitarr, currarr)) {
                checkout(given.getSha1(), givenb.getFile());
                addSA(givenb);
            }


            if (Arrays.equals(givenarr, splitarr) && current == null) {
                continue;
            }


            if (!Arrays.equals(givenarr, splitarr) && current == null) {
                if (!mergee.containsKey(givenf.getName())) {
                    mergee.put(givenf.getName(), givenf);
                }
            }

            if (!Arrays.equals(currarr, givenarr) && splitb == null) {
                if (!mergee.containsKey(givenf.getName())) {
                    mergee.put(givenf.getName(), givenf);
                }
            }
        }
        return mergee;
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch dfgdfg
     * @param given dfgdfg
     * @param mergee dfgdfg
     * @param split dfgdfg*/
    void mergehelp1(HashMap<String, File> mergee, Commit given,
                    Commit split, String branch) throws IOException {
        if (mergee.size() != 0) {
            System.out.println("Encountered a merge conflict.");
        }
        for (Map.Entry<String, File> l : mergee.entrySet()) {
            writeFile1(l.getValue(), _currCom, given);
        }

        if (!split.equals(branch) && !split.equals(_currCom)) {
            Commit c = commit("Merged " + given.getBranch()
                    + " into " + _currCom.getBranch() + ".");
            c.setMparent(given);
            _tree.addCommit(c);
            _tree.addDefSplit(branch, c.getBranch(), given);
        }
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param curr sdfs
     * @param fout dffdg
     * @param given sdfsf*/
    public static void writeFile1(File fout, Commit curr, Commit given)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        bw.write("<<<<<<< HEAD");
        bw.newLine();
        File f = curr.getBlob(fout.getName()).getFile();
        String l = Utils.readContentsAsString(f);
        bw.write(l);
        bw.newLine();
        bw.write("=======");
        bw.newLine();
        String s = "";
        if (given == null) {
            s = "";
        } else {
            if (given.getFiles().containsKey(fout.getName())) {
                File a = given.getBlob(fout.getName()).getFile();
                s = Utils.readContentsAsString(a);
            } else {
                s = "";
            }
        }
        bw.write(s);
        bw.newLine();
        bw.write(">>>>>>>");
        bw.close();
    }


    /** Returns a byte array containing the serialized contents of OBJ.
     * @param f sfddfs*/
    private void rm(File f) throws IOException {
        if (!_addblobs.containsKey(f.getName())
                && !_currCom.getFiles().containsKey(f.getName())) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (_addblobs.containsKey(f.getName())) {
            _addblobs.remove(f.getName());
        }
        if (_currCom.getFiles().containsKey(f.getName())) {
            if (_rmblobs.containsKey(f.getName())) {
                _rmblobs.remove(f.getName());
            }
            Blob b = _currCom.getFiles().get(f.getName());
            _rmblobs.put(f.getName(), b);
            Files.deleteIfExists(Paths.get(b.getPath()));
        }
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param b sdfsf*/
    void addSA(Blob b) {
        if (_rmblobs.containsKey(b.getName())) {
            _rmblobs.remove(b.getName());
        }
        if (_addblobs.containsKey(b.getName())) {
            _addblobs.remove(b.getName());
        }
        _addblobs.put(b.getName(), b);
    }


    /** Returns a byte array containing the serialized contents of OBJ. */
    private void status() {
        System.out.println("=== Branches ===");

        for (Map.Entry<String, Commit> l : _tree.getBranches().entrySet()) {
            if (_currCom.getBranch().equals(l.getKey())) {
                System.out.print("*");
            }
            System.out.println(l.getKey());
        }
        System.out.println("    ");
        System.out.println("=== Staged Files ===");
        for (Map.Entry<String, Blob> l : _addblobs.entrySet()) {
            System.out.println(l.getKey());
        }
        System.out.println(" ");
        System.out.println("=== Removed Files ===");
        for (Map.Entry<String, Blob> l : _rmblobs.entrySet()) {
            System.out.println(l.getKey());
        }
        System.out.println(" ");
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println(" ");
        System.out.println("=== Untracked Files ===");
        System.out.println("  ");
    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param branch  sdfsd*/
    private void log(String branch) {
        Path p = Utils.join(System.getProperty("user.dir"),
                ".gitlet", "commits").toPath();
        File dir = p.toFile();
        File[] listOfFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isHidden();
            }
        });

        System.out.println(logh(_currCom));

        String pa = _currCom.getParent();
        while (pa.length() > 0) {
            Commit parent = Utils.readObject(_commits.get(pa), Commit.class);
            System.out.println(logh(parent));
            pa = parent.getParent();
        }
        System.out.println("");

    }

    /** Returns a byte array containing the serialized contents of OBJ.
     * @param c dfgdff*/
    private String logh(Commit c) {
        StringBuilder l = new StringBuilder();
        l.append("=== \n");
        l.append("commit " + c.getSha1() + " \n");
        SimpleDateFormat d = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        if (c.getMparent().length() > 0) {
            l.append("Merge: " + c.getParent().substring(0, 7)
                    + " " + c.getMparent().substring(0, 7));
        }
        String ts = d.format(c.getTime());
        l.append("Date: " + ts + " \n");
        l.append(c.getMsg() + " \n");
        return l.toString();
    }

    /** Returns a byte array containing the serialized contents of OBJ. */
    void globallog() {
        log("");
    }

    /** Returns a byte array containing the serialized contents of OBJ. */
    CommitTree<Commit> tree() {
        return _tree;
    }

    /** Returns a byte array containing the serialized contents of OBJ. */
    Commit currCom() {
        return _currCom;
    }

    /** Returns a byte array containing the serialized contents of OBJ. */
    private CommitTree<Commit> _tree;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private Commit _currCom;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private ArrayList<Commit> _check = new ArrayList<Commit>();
    /** Returns a byte array containing the serialized contents of OBJ. */
    private HashMap<String, File> _commits = new HashMap<String, File>();
    /** Returns a byte array containing the serialized contents of OBJ. */
    private HashMap<String, Blob> _addblobs;
    /** Returns a byte array containing the serialized contents of OBJ. */
    private HashMap<String, Blob> _rmblobs;
}
