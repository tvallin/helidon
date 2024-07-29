#!/bin/bash -e

for i in $(find target/projects -type d -maxdepth 1); do
  rm -rfd $i/target
  rm -rfd $i/client/target
  rm -rfd $i/server/target
  rm -rfd $i/.idea
  rm -rfd $i/ObjectStore
  rm -rfd $i/PutObjectStoreDirHere
done
