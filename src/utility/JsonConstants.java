package utility;

/**
 * Created by lstan on 31-10-2017.
 */
public class JsonConstants {

    // json keys constants
    public static final String KEYREQUEST = "request";
    public static final String KEYDATA = "data";
    public static final String KEYBLOCKNUMBER = "block";
    public static final String KEYNUMBEROFBLOCKS = "numberofblocks";
    public static final String KEYBLOCKSIZE = "blocksize";
    public static final String KEYVERSION = "version";
    public static final String KEYFILE = "file";
    public static final String KEYFILETYPE = "filetype";
    public static final String KEYFILEENCODE = "fileencode";
    public static final String KEYFILESIZE = "filesize";
    public static final String KEYPROTOCOL = "protocol";
    public static final String KEYCHECKSUM = "checksum";
    public static final String KEYRESUME = "resume";
    public static final String KEY_IS_RESUME = "isResume";

    // json request values
    public static final String VALUEREQUESTFILE = "filerequest";
    public static final String VALUEREQUESTBLOCK = "blockrequest";

    // json protocol values
    public static final int VALUEPROTOCOL = 0;
    public static final int VALUEPROTOCOLFAULT = 1;
    public static final int VALUEPROTOCOLFAULTQUICK = 2;

    // json file paths
    public static final String RESUME_FILE = "C:\\Git\\Fault-Tolerant-Download\\json\\resume.json";
    public static final String CONFIG_FILE = "C:\\Git\\Fault-Tolerant-Download\\json\\config.json";

}
