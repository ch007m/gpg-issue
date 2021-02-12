watch ps -ef \| grep gpg-agent


https://www.debuntu.org/how-to-importexport-gpg-key-pair/
https://d-k-ivanov.github.io/docs/#Security/GPG/Common_Usage/
https://github.com/stephenc/git-timestamp-maven-plugin/blob/1973be25c750b6ba98b6fb8200cecd7920b05b80/Jenkinsfile#L22-L54
https://www.cloudbees.com/blog/continuous-deployment-maven-central-apache-maven


```
10072  rm -rf .gnupg2; mkdir -p .gnupg2; chmod 700 .gnupg2\ngpg -a --export > .gnupg2/key.pub
10073  gpg -a --export-secret-keys -a 4BD5F787F27F97744BC09E019C1CA69653E98E56 > .gnupg2/private.key
10074* pkill gpg-agent
10075  pwd
10076  unset GNUPGHOME
10077  env
10078  unset GNUPGHOME\nrm -rf .gnupg2; mkdir -p .gnupg2; chmod 700 .gnupg2\ngpg -a --export > .gnupg2/key.pub\ngpg -a --export-secret-keys -a 4BD5F787F27F97744BC09E019C1CA69653E98E56 > .gnupg2/private.key
10079  ls -la .gnupg2
10080  GNUPGHOME=.gnupg2 gpg --import key.pub\nGNUPGHOME=.gnupg2 gpg --import .gnupg2/private.key
10081* pkill gpg-agent
10082  unset GNUPGHOME\nrm -rf .gnupg2; mkdir -p .gnupg2; chmod 700 .gnupg2\ngpg -a --export > .gnupg2/key.pub\ngpg -a --export-secret-keys -a 4BD5F787F27F97744BC09E019C1CA69653E98E56 > .gnupg2/private.key
10083  ls -la .gnupg2
10084  GNUPGHOME=.gnupg2 gpg --import .gnupg2/key.pub
10085  GNUPGHOME=.gnupg2 gpg --import .gnupg2/private.key
10086  unset GNUPGHOME\nrm -rf .gnupg2; mkdir -p .gnupg2; chmod 700 .gnupg2\ngpg -a --export > .gnupg2/key.pub\ngpg -a --export-secret-keys -a 4BD5F787F27F97744BC09E019C1CA69653E98E56 > .gnupg2/private.key
10087  GNUPGHOME=.gnupg2 gpg --import .gnupg2/key.pub
10088  GNUPGHOME=.gnupg2 gpg --allow-secret-key-import --import .gnupg2/private.key
10089  GNUPGHOME=.gnupg2 gpg --list-secret-keys --keyid-format LONG
10090  GNUPGHOME=.gnupg2 gpg --edit-key 4BD5F787F27F97744BC09E019C1CA69653E98E56
10091  cat > .gnupg2/gpg-agent.conf << EOF\nallow-loopback-pinentry\nEOF\ncat > .gnupg2/gpg.conf << EOF\nuse-agent\npinentry-mode loopback\nEOF
10092  cat > .gnupg2/gpg-agent.conf << EOF\nallow-loopback-pinentry\nEOF
10093  cat > .gnupg2/gpg-agent.conf << EOF\nallow-loopback-pinentry\nEOF
10094  cat > .gnupg2/gpg.conf << EOF\nuse-agent\npinentry-mode loopback\nEOF
10095  export GNUPGHOME=.gnupg2\ngpgconf --kill gpg-agent\ngpgconf --launch gpg-agent
10096* pkill gpg-agent
10097  gpgconf --kill gpg-agent
10098  gpgconf --launch gpg-agent
10099  export gpg.passphrase="secret-passphrase"
10100  export gpg.passphrase=secret-passphrase
10101  echo "secret-passphrase" | gpg --use-agent --batch --pinentry-mode loopback --local-user 4BD5F787F27F97744BC09E019C1CA69653E98E56 --armor --detach-sign --no-default-keyring --output dummy.txt.asc dummy.txt
10102  gpg --use-agent --batch --pinentry-mode loopback --local-user 4BD5F787F27F97744BC09E019C1CA69653E98E56 --armor --detach-sign --no-default-keyring --output dummy.txt.asc dummy.txt
10103  rm dummy.txt.asc
10104  gpg --use-agent --batch --pinentry-mode loopback --local-user 4BD5F787F27F97744BC09E019C1CA69653E98E56 --armor --detach-sign --no-default-keyring --output dummy.txt.asc dummy.txt
10105  echo "secret-passphrase" | gpg --use-agent --pinentry-mode loopback --local-user 4BD5F787F27F97744BC09E019C1CA69653E98E56 --armor --detach-sign --no-default-keyring --output dummy.txt.asc dummy.txt
10106  gpg --use-agent --pinentry-mode loopback --local-user 4BD5F787F27F97744BC09E019C1CA69653E98E56 --armor --detach-sign --no-default-keyring --output dummy.txt.asc dummy.txt
10107  echo "secret-passphrase" | gpg --use-agent --batch --passphrase-fd 0 --local-user 4BD5F787F27F97744BC09E019C1CA69653E98E56 --armor --detach-sign --no-default-keyring --output dummy.txt.asc dummy.txt
10108  rm dummy.txt.asc
10109  echo "secret-passphrase" | gpg --use-agent --batch --passphrase-fd 0 --local-user 4BD5F787F27F97744BC09E019C1CA69653E98E56 --armor --detach-sign --no-default-keyring --output dummy.txt.asc dummy.txt
```