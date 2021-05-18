.. _install-guide:
Installation Guide
------------------------

The following sections will help you choose where and how to install ComfyDNS.

Choose a Host
==============

If you just want to test out ComfyDNS, run these on your current computer. If you want to set it up
for other computers to use, you should install it on an always-online device like a raspberry pi or a home server.

Like other DNS servers, it's important to not run an open resolver on the open internet. Don't run this on a VPS
or other cloud host unless you know how to set up your firewall correctly. 

Ideally, ComfyDNS should be installed on a device that's behind your home router's firewall.

Installation Requirements
=========================

ComfyDNS uses Docker to make installation easy. It should be installed and running on your install host. 
Consider using `this guide <https://docs.docker.com/engine/install/debian/>`_ if you are using a raspberry 
pi or Debian.

Install Steps
==============

1. Docker pull::

    docker pull comfydns/comfydns:latest

2. Docker run::

    docker run -it --name comfydns \
      -v comfydns-data:/opt/comfydns/ \
      -p 53:53/tcp \
      -p 33200:33200/tcp \
      -p 53:53/udp \
      -p 8080:3000/tcp \
      -e COMFYDNS_UI_PASSPHRASE=changeme \
      comfydns/comfydns:latest

  Change "changeme" to be a simple passphrase of your choosing. You'll use this to
  both create user accounts and recover your account if you forget your password.
