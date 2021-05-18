Options
--------

Interesting Environment Variables
==================================

* :code:`CDNS_ALLOW_ZONE_TRANSFER_TO`: Allow AXFR requests from these IP addresses. This is a comma-separated list. It's empty by default.

Only-Really-Useful-For-Development Environment Variables
==========================================================

* :code:`CDNS_DNS_SERVER_PORT`: This is the UDP and TCP port that ComfyDNS listens on. It's normally 53 and the majority of DNS clients make it difficult to use a nonstandard port.
* :code:`CDNS_ROOT_PATH`: This is where ComfyDNS stores persistent data. This path needs to match where you mount a Docker volume.
* :code:`CDNS_DOUBLE_CHECK_SERVER`: ComfyDNS can "double-check" its results against a server you define by this environment variable. 
