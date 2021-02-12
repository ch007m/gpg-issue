## GPG Issue on a laptop

The description of the gpg issue is available [here](https://gist.github.com/cmoulliard/e5c56d34f690b719c66e95ec79a676ef#gistcomment-3628065).

It occurs when we try to use on the same machine different `gpg` folders; your home folder, the one created within the workspace by jenkins, ...

Such a situation could become conflictual if it not managed correctly as most several `gpg-agent` will be launched but pointing to different `.gnupg` folders

### References

The following references will help you to correctly export/import your private/public keys

- [Export/import properly  your gpg keys](https://www.debuntu.org/how-to-importexport-gpg-key-pair/)

while the following to design properly the jenkins project:

- [Jenkins CI/CD using GPG](https://www.cloudbees.com/blog/continuous-deployment-maven-central-apache-maven)
- [Jenkins job to import zipped gnupg folder](https://github.com/stephenc/git-timestamp-maven-plugin/blob/1973be25c750b6ba98b6fb8200cecd7920b05b80/Jenkinsfile#L22-L54)

### Use case: using user's GPG 

- Plugin configuration used
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
          <gpgArguments>
              <arg>--batch</arg>
              <arg>--passphrase-fd</arg>
              <arg>0</arg>
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
  mvn verify gpg:sign -Dgpg.keyname=<KEYNAME> -X
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
  
  ```

### Export to a new .gnupg folder

### Commands

```bash
unset GNUPGHOME
rm -rf .new_gnupg; mkdir -p .new_gnupg; chmod 700 .new_gnupg/
gpg -a --export > .new_gnupg/key.pub
gpg -a --export-secret-keys -a 4BD5F787F27F97744BC09E019C1CA69653E98E56 > .new_gnupg//private.key

GNUPGHOME=.new_gnupg/ gpg --import .new_gnupg//key.pub
GNUPGHOME=.new_gnupg/ gpg --allow-secret-key-import --import .new_gnupg//private.key

GNUPGHOME=.new_gnupg/ gpg --list-secret-keys --keyid-format LONG
GNUPGHOME=.new_gnupg/ gpg --edit-key 4BD5F787F27F97744BC09E019C1CA69653E98E56

cat > .new_gnupg/gpg-agent.conf << EOF
allow-loopback-pinentry
EOF

cat > .new_gnupg/gpg.conf << EOF
use-agent
pinentry-mode loopback
EOF

export GNUPGHOME=.new_gnupg
gpgconf --kill gpg-agent
gpgconf --launch gpg-agent

rm dummy.txt.asc
echo "secret-passphrase" | gpg --use-agent --batch --passphrase-fd 0 --local-user 4BD5F787F27F97744BC09E019C1CA69653E98E56 --armor --detach-sign --no-default-keyring --output dummy.txt.asc dummy.txt
```

- To watch the agent running
  ```bash
  watch "ps -ef | grep gpg-agent"
  ```