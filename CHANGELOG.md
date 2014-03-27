1.3
* Updated to newspaper-parent 1.2
* Updated to Batch event framework 1.6
* Restructuring and renaming after review
* Log which fedora server you are using
* Maven now spins up a fedora instance as part of the integration test.
* The stand alone doms test server have been moved to a profile so that it can be ignored

1.2.2
* Update to newspaper-batch-event-framework 1.4.4, to make the component respect a maximum of reported failures

1.2.1
* Update to newspaper-batch-event-framework 1.4.2, which makes the component stop talking on stderr

1.2
* Updated to newspaper-batch-event-framework 1.4. This version keeps the locking by:
  - Keeping the component lock for the entire duration of the process
  - Double checks in doms if batches are ready for work. 
* Updated the component to handle partial dates, which will be found in some newspapers

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
