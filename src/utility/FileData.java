package utility;

import java.io.File;
import org.json.simple.JSONObject;


/**
 * Created by lstan on 07-11-2017.
 */
public class FileData {

    private File file;


    public FileData(String path){
        file = new File(path);
    }


    // Returns metadata about the file in a json-object
    public JSONObject getFileData(){
        JSONObject filedata = new JSONObject();
        long filesize = file.length();
        filedata.put(JsonConstants.KEYFILESIZE, filesize);
        filedata.put(JsonConstants.KEYFILE, file.getName());

        return filedata;
    }
}
