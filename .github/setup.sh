#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/ci-setup-github-actions.sh
sh ci-setup-github-actions.sh

# NB: Needed to avoid CI test failure: java.lang.UnsatisfiedLinkError: /home/runner/.javacpp/cache/opencv-4.1.2-1.5.2-linux-x86_64.jar/org/bytedeco/opencv/linux-x86_64/libjniopencv_highgui.so: libgtk-x11-2.0.so.0: cannot open shared object file: No such file or directory
sudo apt-get -y install libgtk2.0-0
