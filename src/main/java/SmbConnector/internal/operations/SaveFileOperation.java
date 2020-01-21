package SmbConnector.internal.operations;

import SmbConnector.internal.SambaConfiguration;
import SmbConnector.internal.util.SmbjUtils;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.hierynomus.smbj.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

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
    public boolean saveFile(@Config SambaConfiguration configuration, InputStream content,
                            String fileName,
                            @Optional(defaultValue = "false") boolean fileOverwrite , @Optional(defaultValue = "false") boolean append) {
        logger.info("Saving file {}", fileName);
        OutputStream out = null;
        SMBClient smbClient = null;
        DiskShare diskShare = null;
        boolean fileCreate = false;
        try {
            smbClient = SmbjUtils.createClient(configuration);
            diskShare = SmbjUtils.connectWithShare(smbClient, configuration);
            File fileToWrite =SmbjUtils.openFile(configuration, fileName, diskShare, fileOverwrite);
            if(append)
            out = fileToWrite.getOutputStream(true);
            else
                out = fileToWrite.getOutputStream();
            out.write(IOUtils.toByteArray(content));
            out.flush();
            out.close();
            SmbjUtils.closeFile(fileToWrite);
            SmbjUtils.closeConnection(smbClient, diskShare);
            fileCreate = true;
        } catch (Exception e) {
            logger.error("Something went wrong while writing the file", e);
            try {
                if (out != null)
                    out.close();
                SmbjUtils.closeConnection(smbClient, diskShare);
            } catch (IOException ignored) {
            }
        }

        return fileCreate;
    }


}
