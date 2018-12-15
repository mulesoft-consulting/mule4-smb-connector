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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ReadFileOperation {
    Logger logger = LoggerFactory.getLogger(ReadFileOperation.class);
    @MediaType(value = MediaType.ANY, strict = false)
    @DisplayName("Read File")
    public InputStream readFile(@Config SambaConfiguration sambaConfiguration  , String fileName) {
        logger.info("Reading file {}", fileName);
        SMBClient smbClient = null;
        DiskShare diskShare = null;

        try {
            SMBClient client = SmbjUtils.createClient(sambaConfiguration);
            diskShare = SmbjUtils.connectWithShare(client, sambaConfiguration);
            File sourceFile = SmbjUtils.readFile(sambaConfiguration, fileName, diskShare);
            byte [] bytes  = IOUtils.toByteArray(sourceFile.getInputStream());
            logger.info("Bytes size {} ", bytes.length);
            SmbjUtils.closeFile(sourceFile);
            SmbjUtils.closeConnection(smbClient,diskShare);
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            SmbjUtils.closeConnection(smbClient,diskShare);
            logger.error("Something went wrong while reading the file", e);
        }

        logger.debug("End->readFile");
        return null;

    }
}
