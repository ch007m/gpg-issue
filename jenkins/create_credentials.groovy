import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret
import hudson.plugins.sshslaves.*
import org.apache.commons.fileupload.*
import org.apache.commons.fileupload.disk.*
import java.nio.file.Files

groovyDir = args[0]
zippedFile = new StringBuffer().append(groovyDir).append("/gnupg.zip").toString()

domain = Domain.global()
store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

GITHUB_CREDENTIALS = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        "GITHUB_CREDENTIALS", "GitHub credentials to checkout, tag, ...",
        "GITHUB_CREDENTIALS",
        "<TO_BE_CHANGED>"
)

GPG_KEY = new StringCredentialsImpl(
        CredentialsScope.GLOBAL,
        "GPG_KEY",
        "GPG key to sign the release files with",
        Secret.fromString("<TO_BE_CHANGED>"))

GPG_KEY_PASSPHRASE = new StringCredentialsImpl(
        CredentialsScope.GLOBAL,
        "GPG_KEY_PASSPHRASE",
        "Passphrase for the secret GPG key to sign the release files with.",
        Secret.fromString("<TO_BE_CHANGED>"))

dfi = (DiskFileItem) new DiskFileItemFactory().createItem("", "application/octet-stream", false, "filename")
out = dfi.getOutputStream()
file = new File(zippedFile)
Files.copy(file.toPath(), out)
out.flush()
zippedFile = new FileCredentialsImpl(
        CredentialsScope.GLOBAL,
        "GPG_ZIPPED_KEYS_FILE",
        "zipped keys file",
        dfi, // Don't use FileItem
        "gnupg.zip",
        ""
)

store.addCredentials(domain, GITHUB_CREDENTIALS)
store.addCredentials(domain, GPG_KEY)
store.addCredentials(domain, GPG_KEY_PASSPHRASE)
store.addCredentials(domain, zippedFile)
