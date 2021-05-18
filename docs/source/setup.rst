.. _setup:
Setup
-------
There are two parts to setting up ComfyDNS.

* Router setup: You need to change your router settings to tell your internet devices about your DNS server.
* ComfyDNS server setup: You need to set up your first domain and add hosts to it.

Router
=======

All router models are different. You will be looking for a way to set the DNS servers that DHCP gives out. 
If you have a standard router given to you by your ISP, you can probably find its admin UI at either 
`192.168.1.1 <http://192.168.1.1/>`_ or `10.0.0.1. <http://10.0.0.1>`_ You should be able to find login details
on a sticker attached to your router. This password is very likely NOT the same password you use to connect to
your WiFi.

Once you're in, look for settings for DHCP, and look for a series of text boxes that will let you set multiple
DNS servers. It usually allows for at least two.

When you locate it, put the IP address of your ComfyDNS server in the FIRST slot. The other servers won't matter much,
I use 8.8.8.8 as a backup in case I ever need to take down my local hardware.


ComfyDNS
=========

1. Go to http://your-comfydns-server-ip-here:8080
2. Using the passphrase you picked for COMFYDNS_UI_PASSPHRASE, create an account.
3. You can now add domains, refresh servers, or configure blocklists.