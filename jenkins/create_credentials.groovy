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
pubKeyFile = new StringBuffer().append(groovyDir).append("/jenkins/pubring.gpg").toString()
secretKeyFile = new StringBuffer().append(groovyDir).append("/jenkins/secring.gpg").toString()

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
file = new File(pubKeyFile)
Files.copy(file.toPath(), out)
out.flush()
pubKeyFile = new FileCredentialsImpl(
        CredentialsScope.GLOBAL,
        "GPG_KEY_PUB_FILE",
        "File with the public GPG key to sign the release files with.",
        dfi, // Don't use FileItem
        "pubring.gpg",
        ""
)

dfi = (DiskFileItem) new DiskFileItemFactory().createItem("", "application/octet-stream", false, "filename")
out = dfi.getOutputStream()
file = new File(secretKeyFile)
Files.copy(file.toPath(), out)
out.flush()
secretKeyFile = new FileCredentialsImpl(
        CredentialsScope.GLOBAL,
        "GPG_KEY_SEC_FILE",
        "File with the secret GPG key to sign the release files with",
        dfi, // Don't use FileItem
        "secring.gpg",
        ""
)

store.addCredentials(domain, GITHUB_CREDENTIALS)
store.addCredentials(domain, GPG_KEY)
store.addCredentials(domain, GPG_KEY_PASSPHRASE)
store.addCredentials(domain, pubKeyFile)
store.addCredentials(domain, secretKeyFile)
