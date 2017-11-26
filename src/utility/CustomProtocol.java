/**
 * Created by lstan on 23-11-2017.
 */
package utility;

import org.json.simple.JSONObject;
import utility.FileData;

public class CustomProtocol {

    private JSONObject overhead;

    public CustomProtocol(){
        overhead = new JSONObject();
    }


    public void fileRequest(String filename){
        overhead = new JSONObject();
        overhead.put(JsonConstants.KEYREQUEST,JsonConstants.VALUEREQUESTFILE);
        overhead.put(JsonConstants.KEYFILE,filename);
    }

    public void fileRequest(String filename, int blocksize){
        overhead = new JSONObject();
        overhead.put(JsonConstants.KEYREQUEST,JsonConstants.VALUEREQUESTFILE);
        overhead.put(JsonConstants.KEYFILE,filename);
        overhead.put(JsonConstants.KEYBLOCKSIZE, blocksize);
    }

    public void blockRequest(String filename, int blocksize, int blocknumber){
        overhead = new JSONObject();
        overhead.put(JsonConstants.KEYREQUEST,JsonConstants.VALUEREQUESTBLOCK);
        overhead.put(JsonConstants.KEYFILE,filename);
        overhead.put(JsonConstants.KEYBLOCKSIZE, blocksize);
        overhead.put(JsonConstants.KEYBLOCKNUMBER, blocknumber);

    }

    public void fileResponse(String filename) {
        FileData fileData = new FileData(filename);
        overhead = fileData.getFileData();

    }

    public void fileResponse(String filename, Byte[] data) {
        FileData fileData = new FileData(filename);
        overhead = fileData.getFileData();
        overhead.put(JsonConstants.KEYDATA, data);

    }

    public void blockResponse(JSONObject blockRequest, Byte[] data){
        overhead = blockRequest;
        overhead.remove(JsonConstants.KEYREQUEST);
        overhead.put(JsonConstants.KEYDATA,data);
    }

    public void simpleSend(Byte[] data, int blocknumber){
        overhead = new JSONObject();
        overhead.put(JsonConstants.KEYBLOCKNUMBER, blocknumber);
        overhead.put(JsonConstants.KEYDATA, data);
    }

    public JSONObject getOverhead(){
        return overhead;
    }
}
