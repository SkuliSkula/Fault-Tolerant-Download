package utility;

import java.io.File;
import org.json.simple.JSONObject;


/**
 * Created by lstan on 07-11-2017.
 */
public class FileData {

    private File file;
    private double blocksize = 30000;

    public FileData(String path){
        file = new File(path);
    }

    public FileData(String path, double blocksize ){
        file = new File(path);
        this.blocksize = blocksize;
    }

    // Returns metadata about the file in a json-object
    public JSONObject getFileData(){
        JSONObject filedata = new JSONObject();
        long filesize = file.length();
        filedata.put(JsonConstants.KEYFILESIZE, filesize);
        filedata.put(JsonConstants.KEYFILE, file.getName());
        filedata.put(JsonConstants.KEYBLOCKSIZE, blocksize);
        filedata.put(JsonConstants.KEYNUMBEROFBLOCKS, (int) Math.ceil(filesize/blocksize));

        return filedata;
    }
}
