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
public class ListFileOperation {
    Logger logger = LoggerFactory.getLogger(ListFileOperation.class);

    @MediaType(value = MediaType.ANY, strict = false)
    @DisplayName("List All")
    public List<String> listFiles(@Config SambaConfiguration smbConfig, @Optional(defaultValue = "*.*") String searchPattern) {
        List<String> files = new ArrayList<>();
        SMBClient client = null;
        DiskShare diskShare = null;
        logger.info("Listing all files ...");
        try {

            client = SmbjUtils.createClient(smbConfig);
            diskShare = SmbjUtils.connectWithShare(client, smbConfig);
            List<FileIdBothDirectoryInformation> list = diskShare.list(smbConfig.getFolder(), searchPattern);
            for (FileIdBothDirectoryInformation fileInfo : list) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found file {} ", fileInfo.getFileName());
                }
                files.add(fileInfo.getFileName());
            }
            SmbjUtils.closeConnection(client, diskShare);

        } catch (Exception e) {
            logger.error("Error in listing files [{}] ", e.getMessage(), e);
            SmbjUtils.closeConnection(client, diskShare);
        }

        return files;
    }

}
