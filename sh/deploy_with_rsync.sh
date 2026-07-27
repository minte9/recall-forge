#!/bin/sh

rsync -avz --progress --exclude-from=.rsyncignore . catalin@72.62.152.27:/srv/docker/recall-forge/
