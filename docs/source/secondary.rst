Setting Up a Secondary Server
------------------------------

It's common to have 2 DNS servers, since DNS not resolving is pretty inconvenient and sometimes you need to do
maintenance on your primary DNS server.

ComfyDNS is a bit odd in that it has a relational database and a web UI, so it doesn't make a ton of sense to
run a whole entire copy of it just to have it be a secondary. You could technically just run the resolver part of
ComfyDNS, but that's not a supported deployment method yet.

The best thing to do is run a less-interesting DNS server, like bind9, and have it zone transfer from ComfyDNS.

As an example, this is a bind9 config file that will do nothing but transfer the "mytld" zone from a ComfyDNS server.

.. code-block::
  zone "mytld" {
    type slave;
    masters  { 127.0.0.1; };
  };

You should also configure it to allow recursive queries and all that too, to use it as a full secondary.