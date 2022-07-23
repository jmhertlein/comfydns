# ComfyDNS

The world's most comfortable local DNS server.

ComfyDNS gives you a fully-standards-compliant DNS server with a simple, modern web UI. Pull a docker image, run the container, and visit the URL. It's that easy.

# Features

* Simple UI
* Easy deployment - it's just a docker container
* Featureful - ComfyDNS includes local DNS with custom internal TLDs, DNS-based ad blocking (supporting both pihole and dnsmasq-format blocklists), and query tracing to visualize DNS lookups. 


# Quickstart

```
docker pull comfydns/comfydns:latest

docker run -it --name comfydns \
  -v comfydns-data:/opt/comfydns/ \
  -p 53:53/tcp \
  -p 53:53/udp \
  -p 8080:3000/tcp \
  -e COMFYDNS_UI_PASSPHRASE=changeme \
  comfydns/comfydns:latest
```

Change “changeme” to be a simple passphrase of your choosing. You’ll use this to both create user accounts and recover your account if you forget your password.

Now visit http://localhost:8080/ and create a login using the passphrase. 

# How is this different from PiHole?

PiHole is an ad-blocker that also supports local DNS. ComfyDNS is a local DNS server that also supports ad-blocking. It's a matter of different priorities. 

Also, not that it matters, but ComfyDNS is a full implementation of a recursive resolver, while PiHole uses dnsmasq.

# License

AGPLv3