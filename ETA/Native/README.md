# Thomson Reuters ETA native libs


If your application uses either of the below features:

* Reliable Multicast Transport or Shared Memory Transport. 
* ValueAdd Cache

then you must have the native libraries (i.e. `.so` or `.dll` as appropriate) on your LD_LIBRARY_PATH / PATH, when your application executes.




### Artifacts provided by this project

| Name | groupId | artifactId
| -- | -- | --
| Native libs for Value Add Cache | com.thomsonreuters.elektron.eta | valueAddCacheNative
| Native Libs for Transport | com.thomsonreuters.elektron.eta | transportNative

Artifacts are provided as ZIP files which contain native libs for all platforms.