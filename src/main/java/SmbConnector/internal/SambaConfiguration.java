package SmbConnector.internal;

import SmbConnector.internal.operations.DeleteFileOperation;
import SmbConnector.internal.operations.ListFileOperation;
import SmbConnector.internal.operations.ReadFileOperation;
import SmbConnector.internal.operations.SaveFileOperation;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations({
        SaveFileOperation.class,
        ReadFileOperation.class,
        ListFileOperation.class,
        DeleteFileOperation.class
})
public class SambaConfiguration {
    @Optional(defaultValue = "mule")
    @Parameter
    private String userName;
    @Optional(defaultValue = "max")
    @Password
    @Parameter
    private String password;
    @Optional(defaultValue = "localhost")
    @Parameter
    private String host;
    @Parameter
    @Optional(defaultValue = "WORKGROUP")
    private String domain;
    @Parameter
    private String share;
    @Parameter
    @Optional(defaultValue = "/home/dms/cj1/consents")
    private String folder;
    @Optional(defaultValue = "120")
    @Parameter
    /**
     * time out in seconds, default 120 secs
     */
    private long timeout;
    @Optional(defaultValue = "0")
    @Parameter
    private long socketTimeout;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(long socketTimeout) {
        this.socketTimeout = socketTimeout;
    }
}
