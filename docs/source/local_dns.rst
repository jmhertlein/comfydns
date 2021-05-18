.. _local-dns:
Local DNS
------------------------

Local DNS is the primary feature of ComfyDNS. 

Description
============

Normally, DNS is controlled by ICANN, and registering a top-level domain has both a monetary cost and requires
a certain amount of reputation. However, that's only required if you want public DNS servers, like 8.8.8.8 or
1.1.1.1 to resolve your domain names. 

If you just want your local network, like your home or small business network, to resolve your domain names,
that's where ComfyDNS can help. "Local DNS" is what this feature is usually called, and it's where you have your 
local DNS server also act as an authoritative name server for your own custom domain(s), in addition to
acting as a normal recursive DNS resolver (which is all 8.8.8.8 is).

Only computers who are using your special DNS server will be able to resolve these names, but there are a few
methods for making computers do so.

The easiest thing to do is configure your router's DNS servers that it will hand out when internet devices contact
it to get an IP address via DHCP. How this is done varies from router to router, but it isn't usually too 
complicated. See slightly more in-depth instructions in :ref:`Router Setup <setup>`. 

Alternatively, you can usually specify the IP address of your DNS server on a per-computer basis in the WiFi or
Network settings.

Adding Custom Domains
=======================

1. Navigate to the ComfyDNS web UI in your browser.
2. Log in (if necessary). 
3. Click "Domains" at the top. 
4. Click "Add Domain".
5. Pick a domain name. Valid examples: ".mytld", ".local", or "mything.mytld".
6. Click "Add". You will be redirected to the page for your new domain.

Adding Hosts to Domains
=========================
1. While viewing the page for a domain, fill out the "Hostname" and "IP Address" fields
2. Click "Add".
3. Click the "Servers" tab
4. If you have a server running, you'll see it listed here. Click "refresh" next to your server.
5. If you had a server running, you should see it say "refreshing". Hit F5 to refresh the page. The "Server Status" should now show that it is running the current version (serial) of the domain.
6. If your server isn't running, the next time it starts it will load the new version of your zone, and there's no need to refresh.


Current Limitations
====================
Right now, the UI only supports A records, though this is a UI-only limitation and the backend supports all the
well-known RR types. UI support for other records will be added soon.