# ComfyDNS

The world's most comfortable local DNS server.

ComfyDNS gives you a fully-standards-compliant DNS server with a simple, modern web UI. Pull a docker image, run the container, and visit the URL. It's that easy.

# Features

* Simple UI
* Easy deployment - it's just a docker container
* Featureful - ComfyDNS includes local DNS with custom internal TLDs, DNS-based ad blocking (supporting both pihole and dnsmasq-format blocklists), and query tracing to visualize DNS lookups. 

# How is this different from PiHole?

PiHole is an ad-blocker that also supports local DNS. ComfyDNS is a local DNS server that also supports ad-blocking. It's a matter of different priorities. 

Also, not that it matters, but ComfyDNS is a full implementation of a recursive resolver, while PiHole uses dnsmasq.

# License

AGPLv3