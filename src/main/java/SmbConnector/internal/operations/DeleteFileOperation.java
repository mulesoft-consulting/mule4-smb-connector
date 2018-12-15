package SmbConnector.internal.operations;

import SmbConnector.internal.SambaConfiguration;
import SmbConnector.internal.util.SmbjUtils;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.share.DiskShare;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * List all files in connected share and folder
 */
public class DeleteFileOperation {
    Logger logger = LoggerFactory.getLogger(DeleteFileOperation.class);

    @MediaType(value = MediaType.ANY, strict = false)
    @DisplayName("Delete File")
    public boolean DeleteFile(@Config SambaConfiguration smbConfig, String fileName) {
        SMBClient client = null;
        DiskShare diskShare = null;
        boolean deleteStatus = false;
        logger.info("Listing all files ...");
        try {

            client = SmbjUtils.createClient(smbConfig);
            diskShare = SmbjUtils.connectWithShare(client, smbConfig);
            diskShare.rm(SmbjUtils.prepareFilePath(smbConfig, fileName));
            SmbjUtils.closeConnection(client, diskShare);
            deleteStatus = true;
        } catch (Exception e) {
            logger.error("Error in listing files [{}] ", e.getMessage(), e);
            SmbjUtils.closeConnection(client, diskShare);
        }

        return deleteStatus;
    }

}
