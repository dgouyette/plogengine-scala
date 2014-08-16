play clean
play dist
bees app:deploy -a blog -t play2 -Rjava_version=1.7 target/universal/*.zip proxyBuffering=false
