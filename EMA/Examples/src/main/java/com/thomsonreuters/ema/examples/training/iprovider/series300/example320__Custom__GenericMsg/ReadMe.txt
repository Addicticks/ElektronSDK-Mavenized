Summary
=======

320__Custom__GenericMsg is an example of an OMM Interactive Provider application 
written to the EMA library.

This application demonstrates the basic usage of the EMA library in providing
of custom domain data to the Thomson Reuters Advanced Distribution Hub.

320__Custom__GenericMsg illustrates how to create and publish a single
streaming item of custom domain. This application uses source directory configured in the EmaConfig.xml
file.


Detailed Description
====================

320__Custom__GenericMsg implements the following high-level steps:

+ Instantiates and modifies an OmmIProviderConfig object.
  - Set the operation model to use user dispatch
+ Instantiates an OmmProvider object which:
  - listens on the port from the EmaConfig.xml file
+ Accepts a login request
+ Processes an item request of custom domain.
 - Creates streaming item (refresh and updates) and publishes them
 - Publishes updates 1 per second for 60 seconds
 - Updates are published with a user dispatch loop
+ Rejects subsequent item requests.
+ Receive and pulish generic msg on the custom domain stream.
+ Exits

Note: If needed, these and other details may be modified to fit your local
      environment. For details on standard configuration, refer to the EMA library
      ReadMe.txt file and EMA Configuration Guide.
