.. _adblocking:
Ad Blocking
------------------------

ComfyDNS supports DNS-based ad-blocking, similar to PiHole.


DNS-Based Ad Blocking
======================

DNS-based ad blocking is different from the ad blocking you're likely familiar with from extensions like AdBlock 
and UBlock in browsers. Browser-based ad blocking is, generally speaking, better because it can block certain parts
of a web page instead of the whole thing, and is also able to detect more elements as ads (and then block them). In
short, browser-based ad blocking interacts with the web page itself.

DNS-based ad blocking, however, operates completely outside the browser at the DNS level. It blocks ads and tracking by,
when a device tries to download an ad, the server will purposefully fail the DNS lookup, and the ad will fail
to load. It is a much coarser-grained ad blocking mechanism, but it can be useful because:

* it can block ads that appear in contexts other than a browser, which highly unfortunately is quite common nowadays. Examples: your TV, your refrigerator, etc. 
* it can block ads in contexts where you are in a browser, but a company named after a popular red fruit or a very large number won't let you use ad blocking extensions.
* it can block tracking software that's running in devices you don't even know are doing user tracking.

One large downside is that DNS-based ad blocking is much more likely to break websites. LinkedIn is an example,
where they apparently serve assets (like CSS) off the same domain that someone thinks does tracking or serves
ads, as I've noticed it's broken by many blocklists I've tried. Which leads in to the next topic...

Block Lists and Block List Types
=================================

A block list is just a list of domains that are considered to be used to serve ads or perform user tracking.
There are quite a few different options here, these are some I've found:

* `Easylist <https://justdomains.github.io/blocklists/lists/easylist-justdomains.txt>`_
* `AdGuard <https://justdomains.github.io/blocklists/lists/adguarddns-justdomains.txt>`_
* `EasyPrivacy <https://justdomains.github.io/blocklists/lists/easyprivacy-justdomains.txt>`_
* `NoTracking <https://github.com/notracking/hosts-blocklists/raw/master/dnsmasq/dnsmasq.blacklist.txt>`_

There are several different styles of block list, but the two currently supported by ComfyDNS are Line-Separated 
(also referred to as PiHole style) and Dnsmasq-style (because they could be passed as arguments to dnsmasq).

Using Ad Blocking in ComfyDNS
==============================

1. Click "Ad Blocking" in the navigation tabs.
2. Check the box for "ad blocking enabled". 
3. Add (a) block list(s) of your choosing.
4. Go ahead and click "Refresh" on your block lists.
5. Decide what you want to do in the section below.

Block Ads For All Clients Or Just Some?
========================================

It's configurable whether ads are blocked by default or not, and then on top of that you can configure ad blocking
on a per-client basis.  So really, your two options are:

* On by default, configure some clients to opt-out.
* Off by default, configure some clients to opt-in.

Personally, I've been having good results with #2, where I turn it off globally and then turn it on for certain
clients like TVs and phones. The default being "off" accomplishes two things: a) clients with more sophisticated
ad blockers don't have sites unnecessarily broken, and b) random IOT devices don't randomly not work because
the same domain is used for both normal functioning and tracking. Mind you, that probably means we shouldn't
be using said IOT devices at all if they track so much, but I digress....

Anyway, use the "Block By Default" checkbox to control the default, and then click "Add" to bring up
a dialog box to specify an IP address and then whether ad-blocking should be on or not. 

IP address can be an IP Address and a bit count (e.g. `192.168.1.1/24`), wherein anything with that many
matching bits in the IP will have the configuration applied to it. Where multiple configurations overlap,
the most-specific configuration is used. For example, if the default is ON, and it's OFF for `192.168.1.1/24`,
but it's ON for `192.168.1.42/32` (which is just 1 IP address), then it will be ON for 192.168.1.42.  