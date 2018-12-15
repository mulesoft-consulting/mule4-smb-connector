package SmbConnector.internal.operations;

import SmbConnector.internal.SambaConfiguration;
import SmbConnector.internal.util.SmbjUtils;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class SaveFileOperation {

    Logger logger = LoggerFactory.getLogger(SaveFileOperation.class);

    /**
     * Save file from
     */
    @MediaType(value = ANY, strict = false)
    @DisplayName("Save File")
    public boolean saveFile(@Config SambaConfiguration configuration, InputStream payload, String fileName) {
        logger.info("Saving file {}", fileName);
        OutputStream out = null;
        SMBClient smbClient = null;
        DiskShare diskShare = null;
        try {
            smbClient = SmbjUtils.createClient(configuration);
            diskShare = SmbjUtils.connectWithShare(smbClient, configuration);
            File fileToWrite = SmbjUtils.openFile(configuration, fileName, diskShare);
            out = fileToWrite.getOutputStream();
            out.write(IOUtils.toByteArray(payload));
            out.flush();
            out.close();
            SmbjUtils.closeFile(fileToWrite);
            SmbjUtils.closeConnection(smbClient, diskShare);
            return true;
        } catch (Exception e) {
            logger.error("Something went wrong while writing the file", e);
            try {
                if (out != null)
                    out.close();
                SmbjUtils.closeConnection(smbClient, diskShare);
            } catch (IOException ignored) {
            }
        }

        return false;
    }


}
