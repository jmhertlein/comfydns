require 'open-uri'
require 'json'

class ServerController < ApplicationController
  def index
    @servers = Server.all
  end

  def edit
    @server = Server.find(params[:id])
  end

  def refresh
    @server = Server.find(params[:id])
    Task.create(action: "RELOAD_ZONES", server_id: @server.id)
    redirect_to "/server/"    
  end

  def update
    @server = Server.find(params[:id])

    unless params[:ip_address].nil? || /()|((\d+\.){3}\d+)/.match?(params[:ip_address])
      redirect_to "/server/#{@server.id}", alert: "Invalid IP address: #{params[:ip_address]}"
      return
    end

    @server.ip_address = params[:ip_address].nil? || params[:ip_address].empty? ? nil : params[:ip_address]
    @server.hostname = params[:hostname].nil? || params[:hostname].strip.empty? ? nil : params[:hostname]

    @server.save
    redirect_to "/server/", notice: "Updated #{@server.id}"
  end
end
