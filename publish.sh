#!/bin/bash

#Import secret key into GPG
gpg --fast-import --no-tty --batch --yes secret-keys.gpg

#Publish and provide necessary details
gradle publish \
-PsonatypeUsername=${sonatype_username} \
-PsonatypePassword=${sonatype_password} \
-Psigning.gnupg.keyName=${sonatype_gpg_keyid} \
-Psigning.gnupg.passphrase=${sonatype_gpg_password}
