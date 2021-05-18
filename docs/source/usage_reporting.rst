Usage Reporting & Privacy Policy
---------------------------------

ComfyDNS only collects a very small amount of data. What and how is detailed here.

Usage Reporting
================

When you run ComfyDNS for the first time, it generates a type 4 UUID and saves it to persistent storage. This is
called the "install ID".

Every hour, with an initial delay of a few minutes after first start, a POST request is made with the following information:

* An empty request body
* a URL parameter that is your install ID
* a User Agent header that contains the current version of ComyDNS

Server-side, this is stored in a database. Eventually it will be rolled up daily into just a Daily Active Users count,
since that's the main thing I'm interested in.

You can opt out of usage reporting by setting :code:`USAGE_REPORTING_DISABLED=1`.

Privacy Policy
===============

ComfyDNS doesn't collect any PII, and the only reason I collect any of this is to hopefully provide motivation
to work on the project. 