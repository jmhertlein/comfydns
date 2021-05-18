# ComfyDNS

## Installation

`docker pull comfydns/comfydns:1`

## Usage

`docker run --name comfydns -v comfydns-data:/opt/comfydns/ -p 53:53/tcp 53:53/udp 8080:3000/tcp comfydns/comfydns:1`

Details of volumes and ports can be found in the next two sections.

### Volumes

* `comfydns-data:/opt/comfydns` - stores both configuration and the postgres database that backs the server.

### Ports

* `53/udp` - default DNS UDP port. Majority (99%) of traffic goes here.
* `53/tcp` - default DNS TCP port. Some DNS queries use TCP.
* `3000/tcp` - web UI HTTP port. The example `run` command above remaps it to 8080. 
* `33200` - [optional] http server serving `/metrics` endpoint for prometheus. 

### 
