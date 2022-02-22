class BlockListController < ApplicationController
  def index
    @lists = BlockList.all
    @refresh_running = Hash.new
    @last_updates = Hash.new
    @entries = Hash.new

    @lists.each do |l|
      @refresh_running[l.id] = false
      @last_updates[l.id] = "n/a"
      @entries[l.id] = 0
    end

    @supported_update_frequencies = ["daily", "weekly"]
    @supported_list_types = [["Simple List (Pihole-style)", "newline-list"], ["address= (dnsmasq)", "dnsmasq"]]
    

    Task.where(action: "REFRESH_BLOCK_LIST", done: false).each do |t|
      @refresh_running[t.args["block_list_id"]] = true
    end

    @snapshot_entries = BlockedName.group(:block_list_snapshot_id).count

    BlockListSnapshot.all.each do |snap|
      @last_updates[snap.block_list_id] = snap.updated_at
      @entries[snap.block_list_id] = @snapshot_entries[snap.id]
    end

    @client_configs = AdBlockClientConfig.all
  end

  def create
    name = params[:name]
    url = params[:url]
    auto_update = params[:auto_update]
    list_type = params[:list_type]
    if auto_update
      update_frequency = params[:update_frequency]
    else
      update_frequency = nil
    end

    case update_frequency
    when "daily"
      update_frequency = "P1D"
    when "weekly"
      update_frequency = "P7D"
    else
      redirect_to "/block_list", alert: "Invalid update frequency: #{update_frequency}"
      return
    end

    unless ["newline-list", "dnsmasq"].include? list_type
      redirect_to "/block_list", alert: "Invalid list type: #{list_type}"
      return
    end

    BlockList.create name: name, url: url, list_type: list_type, auto_update: auto_update, update_frequency: update_frequency
    redirect_to "/block_list", notice: "Added #{name}."
  end

  def destroy
    id = params[:id]
    
    l = BlockList.find(id)
    l.delete
    redirect_to "/block_list", notice: "Deleted #{l.name}"
  end

  def refresh
    id = params[:id]
    l = BlockList.find(id)
    Task.create!(action: "REFRESH_BLOCK_LIST", args: {block_list_id: id})
    redirect_to "/block_list/"
  end

  def set_flag
    flag = params[:flag]
    enabled = params[:enabled]

    unless ["adblock", "adblock_client_default_on"].include? flag
      redirect_to "/", alert: "Invalid flag."
    end

    f = Flag.find_by(name: flag)
    if f.nil?
      f = Flag.new(name: flag, value: enabled)
      old_value = "n/a"
    else
      old_value = f.value
      f.value = enabled
    end
    f.save!

    logger.info("Set #{flag} flag from #{old_value} to #{f.value}")
    case flag
    when "adblock"
      Task.create!(action: "RELOAD_ADBLOCK_CONFIG")
      redirect_to "/block_list/", notice: f.value ? "Ad blocking enabled!" : nil
    when "adblock_client_default_on"
      Task.create!(action: "RELOAD_ADBLOCK_CONFIG")
      redirect_to "/block_list/", notice: f.value ? "Unconfigured clients now have ads blocked." : "Unconfigured clients now do not have ads blocked."
    end
  end

  def create_client_config
    ip = params[:ip]
    block_ads = params[:block_ads]
    
    AdBlockClientConfig.create!(ip: ip, block_ads: block_ads)
    redirect_to "/block_list", notice: "Added config for #{ip}"
  end

  def destroy_client_config
    id = params[:id]
    c = AdBlockClientConfig.find(id)
    c.delete
    redirect_to "/block_list", notice: "Deleted config for #{c.ip}"
  end
end
