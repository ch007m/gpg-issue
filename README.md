## GPG Issue on a laptop

### Table of Contents

  * [Introduction](#introduction)
  * [References](#references)
  * [Use case: using user's GPG](#use-case-using-users-gpg)
  * [Use case: Export keys and use another gnupg folder](#use-case-export-keys-and-use-another-gnupg-folder)
    * [Export/Import keys](#exportimport-keys)
    * [Sign the files using the exported keys &amp; mvn gpg:sign](#sign-the-files-using-the-exported-keys--mvn-gpgsign)
    * [Sign this project using jenkins job](#sign-this-project-using-jenkins-job)
    * [Sign a file using the exported keys (optional)](#sign-a-file-using-the-exported-keys-optional)
  * [Trick](#trick)
  
### Introduction

The description of the gpg issue is available [here](https://gist.github.com/cmoulliard/e5c56d34f690b719c66e95ec79a676ef#gistcomment-3628065) like also the gpg errors
reported when we try to sign a file using exported public/private keys (e.g. `invalid packet (ctb=00), failed: Invalid keyring` ...).

It occurs when we try to use on the same machine different `gpg` folders; your home folder, the one created within the workspace by jenkins, ...

Such a situation could become conflictual if it not managed correctly as several `gpg-agent` will be launched but pointing to different `.gnupg` folders !!!

### References

The following references will help you to correctly export/import your private/public keys

- [Export/import properly  your gpg keys](https://www.debuntu.org/how-to-importexport-gpg-key-pair/)

like also the following link to design properly the jenkins job:

- [Jenkins CI/CD using GPG](https://www.cloudbees.com/blog/continuous-deployment-maven-central-apache-maven)
- [Jenkins job to import zipped gnupg folder](https://github.com/stephenc/git-timestamp-maven-plugin/blob/1973be25c750b6ba98b6fb8200cecd7920b05b80/Jenkinsfile#L22-L54)

### Use case: using user's GPG 

- The `maven gpg plugin` has been configured as defined hereafter to use the `passphrase` passed as ENV var and `--batch --passphrase fd 0` args to avoid to get a UI prompt
  ```xml
  <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-gpg-plugin</artifactId>
      <executions>
          <execution>
              <id>sign-artifacts</id>
              <phase>verify</phase>
              <goals>
                  <goal>sign</goal>
              </goals>
          </execution>
      </executions>
      <configuration>
          <useAgent>true</useAgent>
          <passphrase>${env.GPG_PASSPHRASE}</passphrase>
          <homeDir>${env.GPG_HOMEDIR}</homeDir>
          <gpgArguments>
              <arg>--pinentry-mode</arg>
              <arg>loopback</arg>
          </gpgArguments>
      </configuration>
  </plugin>
  ```
- Package the Spring Boot project
  ```bash
  mvn clean package
  ```
- Sign the files
  ```bash
  export GPG_PASSPHRASE="xxxx"
  export KEYNAME=<YOUR KEYID>
  mvn package gpg:sign -Dgpg.keyname=${KEYNAME} -Dgpg.passphrase=${GPG_PASSPHRASE} -X
  ```
- The `maven gpg plugin` will use the following parameters according to the configuration defined within the pom.xml and passed as ENV var and `gpg.property`
  ```bash
  [DEBUG]   (f) ascDirectory = /Users/cmoullia/code/ch007m/gpg-issue/target/gpg
  [DEBUG]   (f) defaultKeyring = true
  [DEBUG]   (f) gpgArguments = [--batch, --passphrase-fd, 0]
  [DEBUG]   (f) interactive = true
  [DEBUG]   (f) keyname = 4BD5F787F27F97744BC09E019C1CA69653E98E56
  [DEBUG]   (f) passphrase = xxxxxxxxxx
  [DEBUG]   (f) passphraseServerId = gpg.passphrase
  [DEBUG]   (f) project = MavenProject: dev.snowdrop:gpg:1.0.0-SNAPSHOT @ /Users/cmoullia/code/ch007m/gpg-issue/pom.xml
  [DEBUG]   (f) settings = org.apache.maven.execution.SettingsAdapter@616a06e3
  [DEBUG]   (f) skip = false
  [DEBUG]   (f) useAgent = true
  ```
- Verify under `target`, that files having extensions `*.asc` exist
  ```bash
  ls -la target/*.asc
  -rw-r--r--  1 cmoullia  staff  833 Feb 12 09:32 target/gpg-1.0.0-SNAPSHOT.jar.asc
  -rw-r--r--  1 cmoullia  staff  833 Feb 12 09:32 target/gpg-1.0.0-SNAPSHOT.jar.asc.asc
  -rw-r--r--  1 cmoullia  staff  833 Feb 12 09:32 target/gpg-1.0.0-SNAPSHOT.pom.asc
  -rw-r--r--  1 cmoullia  staff  833 Feb 12 09:32 target/gpg-1.0.0-SNAPSHOT.pom.asc.asc
  ```

### Use case: Export keys and use another gnupg folder

When we want to test the scenario to release a project (or sign the files) using a GPG Key with a Jenkins Job, then the process will certainly fail 
due to wrong format used to export the keys or bad configuration of the `GNUPGHOME` env variable resulting in several `gpg-agent` launched and pointing to your 
HOME gpg folder, and the one created by then jenkins job under the workspace.

The instructions defined hereafter will help you to :
- Export/import correctly the keys and
- Set up the `maven GPG plugin` to point to the correct new `GnuPG folder`

#### Export/Import keys

- First, export the public keys and your private key
  ```bash
  unset GNUPGHOME
  pkill gpg-agent
  
  export KEYNAME=<YOUR KEYID>
  export GPG_PASSPHRASE="xxxx"
  
  rm -rf tmp; mkdir tmp
  gpg -a --export --export ${KEYNAME} > tmp/pubring.gpg
  echo ${GPG_PASSPHRASE} | gpg --batch --passphrase-fd 0 --pinentry-mode loopback -a --export-secret-keys ${KEYNAME} > tmp/secring.gpg
  ```
- Next, import the files into a newly gnupg folder created
- **WARNING**: Set the env variable to the new folder to let the agent to deal correctly with the keys !!
  ```bash
  pkill gpg-agent
  rm -rf .job_gnupg; mkdir -p .job_gnupg; chmod 700 .job_gnupg 
  export GNUPGHOME=.job_gnupg
  
  gpg --import tmp/pubring.gpg
  echo ${GPG_PASSPHRASE} | gpg --batch --passphrase-fd 0 --pinentry-mode loopback --allow-secret-key-import --import tmp/secring.gpg
  ```
- Next edit your key to trust it (optional)
  ```bash
  gpg --edit-key ${KEYNAME}
  trust
  5
  y 
  quit
  ```
- Zip the gnupg files as we will import them for the step where we play with a jenkins job
  ```bash
  cd .job_gnupg && zip -r -X "../gnupg.zip" . && cd ..
  ```
#### Sign the files using the exported keys & mvn gpg:sign

```bash
export GPG_PASSPHRASE="xxxx"
pkill gpg-agent
export GNUPGHOME=.job_gnupg

mvn package gpg:sign -Dgpg.keyname=${KEYNAME} -Dgpg.passphrase=${GPG_PASSPHRASE} -X
```

#### Sign this project using jenkins job

- Start a local jenkins instance. See the instructions of the project [jenkins_job_dsl](https://gitlab.cee.redhat.com/snowdrop/jenkins-jobs-dsl).
  ```bash
  export JENKINS_DIR=~/code/snowdrop/infra-jobs-productization/jenkins-jobs-dsl
  java -Djenkins.install.runSetupWizard=false -jar $JENKINS_DIR/tmp/jenkins.war
  ```
- Create a new pipeline using the UI and this groovy [pipeline DSL](jenkins/10_Sign_Project.groovy) file or import it wih the help of the jenkins-cli
  ```bash
  jenkins-cli -s http://localhost:8080 create-job 10_Sign_Project < jenkins/10_Sign_Project.xml
  ```

- Create the following credentials using the UI - `http://localhost:8080/credentials/store/system/domain/_/`
  - `GITHUB_CREDENTIALS`: Username & password github token
  - `GPG_KEY`: String text of your GPG Key
  - `GPG_ZIPPED_KEYS_FILE`: Secret file containing the zipped keys file
  - `GPG_KEY_PASSPHRASE`: String text of your `PASSPHRASE`
  
  or execute the following script where you will, of course, replace within the `create_credentials.groovy` file the: 
  - passphrase,
  - gpg_keyid, 
  - github credentials
  The groovy file will import the `gnupg.zip` from the home directory of this project
  
  ```bash
  cat jenkins/create_credentials.groovy | jenkins-cli -s http://localhost:8080 groovy = <PATH_OF_THE_PROJECT>
  ```
- Launch the Job :-)

#### Sign a file using the exported keys (optional)

- Kill and restart the gpg-agent to point to the new folder
  ```bash
  pkill gpg-agent
  export GNUPGHOME=.job_gnupg
  ```
- Sign a file locally
  ```bash
  export GNUPGHOME=.job_gnupg
  rm target/pom.xml.asc
  echo ${GPG_PASSPHRASE} | gpg --use-agent --batch --passphrase-fd 0 --pinentry-mode loopback --local-user ${KEYNAME} --armor --detach-sign --no-default-keyring --output target/pom.xml.asc pom.xml

  or to be prompted
  
  gpg --local-user ${KEYNAME} --armor --detach-sign --no-default-keyring --output target/pom.xml.asc pom.xml
  ```
- Check if the file has been signed
  ```bash
  cat target/pom.xml.asc
  -----BEGIN PGP SIGNATURE-----
  iQIyBAABCAAdFiEES9X3h/J/l3RLwJ4BnBymllPpjlYFAmAmSKQACgkQnBymllPp
  jlZkVw/3c8IXrFIhoq4NWea+9qS5VWmFY7X9BLSMizHIWmFs5bdFOE1vsTOa1/E3
  eATmFyuZGM9zJ8Nl14ifnvGx5NNx+J+8+GsPsQgoTOU7dmQU7L0EyLG7mv88XmS3
  C0PYOrPoYd4UzMaAeAlKlWHpF2qhHNwCGt4lEakLiPvR7eu6K7eRKQHHmkcQOONy
  WLykfqXeNiDDn1JzZWYvJfxEpdKsrmIjDz6S4khZgdz/8dHEssla95fxi8ORwjPW
  RSBI/lFo3PuZjU4+0NUOEJv3sdl9VSCzgKAb1PFNKD2S/qmODNgF/5ek6c+5x9Cw
  cKcJAwfHhE9L8rqsQafmz5fdpTOzpXV+EOUppNnrmF5qfdYeb/mhGbpkKmdLvwI3
  AVta3nhD0qMATvr96I2Pd+QP0uQt9f6Ja3juWFfskncjtKlL+/pc81PUOYO8Kh+D
  mmBMMXtEDKhDjrgZSgGFUvzQE2HNbEB54YAFYjSF11RHtLytrmctdokapuxc8zIq
  I5mTrtRRXw+Z8j1WI1yzTr/uwoayMcVqSLMApuSuB5efwj82wwb+U/BaQmd3sxs4
  Wh0TOiGdNBOSlyX1PyiVKn3qmm5sbxGXrB0CYVouIJoemOF3pwTYDetTI19ZBf7c
  c//NIyPKhBkWdAZI9KSSsabCV9VMig8cvLBPAYS/EoZjsKVqbw==
  =eliS
  -----END PGP SIGNATURE-----
  ```

### Tricks

- List jobs, credentials
  ```bash
  jenkins-cli -s http://localhost:8080 list-jobs
  jenkins-cli -s http://localhost:8080 list-credentials "system::system::jenkins"
  ============================================
  Domain             (global)
  Description
  # of Credentials   4
  ============================================
  Id                 Name
  ================== =========================
  GITHUB_CREDENTIALS GITHUB_CREDENTIALS/******
  GPG_KEY_PASSPHRASE GPG_KEY_PASSPHRASE
  GPG_KEY_SEC_FILE   secring.gpg
  GPG_KEY_PUB_FILE   .pub
  ============================================
  ```
- Export Jenkins job, credentials
  ```bash
  jenkins-cli -s http://localhost:8080 list-credentials "system::system::jenkins" > jenkins/jenkins_credentials.xml
  jenkins-cli -s http://localhost:8080 get-job 10_Sign_Project > jenkins/10_Sign_Project.xml
  ```
- Delete credentials
  ```bash
  jenkins-cli -s http://localhost:8080 delete-credentials system::system::jenkins _ GPG_ZIPPED_KEYS_FILE &
  jenkins-cli -s http://localhost:8080 delete-credentials system::system::jenkins _ GPG_KEY &         
  jenkins-cli -s http://localhost:8080 delete-credentials system::system::jenkins _ GITHUB_CREDENTIALS &
  jenkins-cli -s http://localhost:8080 delete-credentials system::system::jenkins _ GPG_KEY_PASSPHRASE &
  ```
- Delete job
  ```bash
  jenkins-cli -s http://localhost:8080 delete-job 10_Sign_Project
  ```

- To watch the agent running
  ```bash
  watch "ps -ef | grep gpg-agent"
  ```
- To list the keys
  ```bash
  gpg --list-secret-keys --keyid-format LONG
  ```