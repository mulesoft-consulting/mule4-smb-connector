package SmbConnector.internal.util;

import SmbConnector.internal.SambaConfiguration;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
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
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.api.util.StringUtils;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SmbjUtils {
    protected static Logger logger = LoggerFactory.getLogger(SmbjUtils.class);

    public static SMBClient createClient(SambaConfiguration sambaConfiguration) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating SMBJ client for host [{}], share [{}], folder [{}]", sambaConfiguration.getHost(),
                    sambaConfiguration.getShare(), sambaConfiguration.getFolder());
        }
        SmbConfig smbConfig = SmbConfig.builder()
                .withTimeout(sambaConfiguration.getTimeout(), TimeUnit.SECONDS)
                .withSoTimeout(sambaConfiguration.getSocketTimeout(), TimeUnit.SECONDS)
                .build();
        return new SMBClient(smbConfig);
    }

    public static DiskShare connectWithShare(SMBClient smbClient, SambaConfiguration smbConfig) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Connecting with Share [{}], domain [{}], folder [{}] ", smbConfig.getShare(),
                    smbConfig.getDomain(), smbConfig.getFolder());
        }
        String shareName = smbConfig.getShare();
        if (StringUtils.isBlank(smbConfig.getShare())) {
            throw new IllegalArgumentException("Share name can't be empty or null");
        }
        if (shareName.startsWith("/")) {
            shareName = shareName.replace("/", "").trim();
        }
        Connection connect = smbClient.connect(smbConfig.getHost());
        AuthenticationContext authenticationContext = new AuthenticationContext(smbConfig.getUserName(), smbConfig.getPassword().toCharArray(), smbConfig.getDomain());
        Session authenticate = connect.authenticate(authenticationContext);
        DiskShare share = (DiskShare) authenticate.connectShare(shareName);
        return share;
    }

    public static File openFile(SambaConfiguration configuration, String fileName, DiskShare diskShare, boolean fileOverwrite) {
        //set access
        Set<AccessMask> accessMasks = new HashSet<>();
        accessMasks.add(AccessMask.MAXIMUM_ALLOWED);
        accessMasks.add(AccessMask.GENERIC_ALL);
        Set<FileAttributes> fileAttributes = new HashSet<>();
        fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
        Set<SMB2ShareAccess> shareAccesses = new HashSet<>();
        shareAccesses.add(SMB2ShareAccess.FILE_SHARE_DELETE);
        shareAccesses.add(SMB2ShareAccess.FILE_SHARE_WRITE);
        shareAccesses.add(SMB2ShareAccess.FILE_SHARE_READ);
        Set<SMB2CreateOptions> smb2CreateOptions = new HashSet<>();
        smb2CreateOptions.add(SMB2CreateOptions.FILE_RANDOM_ACCESS);
        //create dest file path
        if (!diskShare.folderExists(configuration.getFolder())) {
            logger.info("Destination directory doesn't exist, creating new directory");
            diskShare.mkdir(configuration.getFolder());
        }

        String dest = prepareFilePath(configuration, fileName);
        if (diskShare.fileExists(dest) && fileOverwrite) {
            logger.info("file name {} exists, overwriting existing file ", fileName);
            diskShare.rm(dest);
        }
        return diskShare.openFile(dest, accessMasks, fileAttributes, shareAccesses, SMB2CreateDisposition.FILE_OVERWRITE_IF, smb2CreateOptions);
    }

    public static File readFile(SambaConfiguration configuration, String fileName, DiskShare diskShare) {
        Set<AccessMask> accessMasks = new HashSet<>();
        accessMasks.add(AccessMask.FILE_READ_DATA);

        Set<FileAttributes> fileAttributes = new HashSet<>();
        fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_READONLY);
        Set<SMB2ShareAccess> shareAccesses = new HashSet<>();
        shareAccesses.add(SMB2ShareAccess.FILE_SHARE_READ);

        Set<SMB2CreateOptions> smb2CreateOptions = new HashSet<>();
        smb2CreateOptions.add(SMB2CreateOptions.FILE_RANDOM_ACCESS);
        String dest = prepareFilePath(configuration, fileName);
        return diskShare.openFile(dest, accessMasks, fileAttributes, shareAccesses, SMB2CreateDisposition.FILE_OPEN_IF, smb2CreateOptions);

    }

    public static void closeFile(File file) {
        IOUtils.closeQuietly(file);

    }

    public static String prepareFilePath(@Config SambaConfiguration configuration, String fileName) {
        String formattedName = fileName.startsWith("/") ? fileName.replace("/", "") : fileName;
        StringBuilder dest = new StringBuilder();
        dest.append(configuration.getFolder());
        if (configuration.getFolder().endsWith("/")) {
            dest.append(formattedName.trim());
        } else {
            dest.append("/").append(formattedName.trim());
        }
        logger.info("File path {} ", dest.toString());
        return dest.toString();
    }

    public static void closeConnection(SMBClient smbClient, DiskShare diskShare) {
        try {
            if (diskShare != null) {
                diskShare.close();
            }
            if (smbClient != null) {
                smbClient.close();

            }
        } catch (Exception ignored) {
            logger.warn("Error while closing smb connention {} ", ignored.getMessage());
        }
    }
}
