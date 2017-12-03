/**
 * Created by lstan on 23-11-2017.
 */
package utility;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utility.FileData;

import java.io.*;

public class CustomProtocol implements Serializable {

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

    public void blockRequest(String filename, int blocksize, double blocknumber){
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

    public void fileResponse(String filename, double numberOfBlocks) {
        FileData fileData = new FileData(filename);
        overhead = fileData.getFileData();
        overhead.put(JsonConstants.KEYNUMBEROFBLOCKS, numberOfBlocks);

    }

    public void blockResponse(JSONObject blockRequest, byte[] data){
        overhead = blockRequest;
        overhead.remove(JsonConstants.KEYREQUEST);
        overhead.put(JsonConstants.KEYDATA,data);
    }

    public void simpleSend(byte[] data, double blockNumber){
        overhead = new JSONObject();
        overhead.put(JsonConstants.KEYBLOCKNUMBER, blockNumber);
        overhead.put(JsonConstants.KEYDATA, data);
    }

    public JSONObject getOverhead(){
        return overhead;
    }

    public void writeJsonToFile(String filePath, JSONObject jsonObject) throws IOException{
        try(FileWriter file = new FileWriter(filePath)) {
            file.write(jsonObject.toJSONString());
        }
    }

    public JSONObject readJsonFromFile(String jsonFilePath) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try{
            Object obj = parser.parse(new FileReader(jsonFilePath));
            jsonObject = (JSONObject) obj;
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
