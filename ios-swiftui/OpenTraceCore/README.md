# OpenTraceCore

The glue that holds the project together. This package responds to functions 
coming from the main app bundle and ties into the UI, support, and service packages.

Things that belong in this package include:

* View models that power the higher order containers in the app
* Instantiation and ownership of concrete service objects
* Navigation handling