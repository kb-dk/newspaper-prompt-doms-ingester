1.1

* Logback matches stage
* Updated the config to match new framework
* Multi threaded fedora ingester
* Use the 1.3 version of the framework which cause to component to lock until the SBOI have reindexed
* Do not use the hasFile relation for now
* Quicker way to add Relations
* Handle the collection pid correctly
* Use 1.0 parent pom
* Do not print to std out
* Fixed problem with not using configured timeout values. Main method now uses standard AutonomousComponentUtils to start component

1.0

* Changed event to Metadata_Archived
* Consistent error messages for doms ingester
* Check existence before making new object
* Fix memory leak
* Fix datastream alternative identifiers
* make RunnablePromptDomsIngester get it's component name and version using super class functionality

0.1
Initial release
