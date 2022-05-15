require 'rfc1035'

class CacheController < ApplicationController
  def index
    @supported_qtypes = DNS::RRTYPE_TO_VALUE.each
      .select{|name, value| DNS::SUPPORTED_RRTYPES.include?(name)}
      .map{|l,r| [l, l]}
    @supported_qtypes << ["*", "STAR"]

    @name = params[:name]
    @qtype = params[:qtype]
    @qclass = "IN"

    if @name.nil?
      @search_performed = false
    else
      @search_performed = true
    
      where = {name: @name, rrclass: DNS::RRCLASS_TO_VALUE[@qclass]}
      where[:rrtype] = DNS::RRTYPE_TO_VALUE[@qtype] unless @qtype.eql? "STAR"
      @found_rrs = CachedRR.where(where)

      where = {qname: @name}
      if @qtype.eql? "STAR"
        @found_negatives = CachedNegative.where(where)
      else  
        where[:qtype] = DNS::RRTYPE_TO_VALUE[@qtype]
        @found_negatives = CachedNegative.where(where)
        @found_negatives += CachedNegative.where(qname: @name, qtype: 255)
      end
    end
  end

  def destroy
    cache_type = params[:cache_type]
    id = params[:id]
    name = params[:search_name]
    type = params[:search_type]


    p = {name: name, qtype: type}
    if cache_type.eql? "positive"
      r = CachedRR.find(id)
      r.delete
      redirect_to "/cache?#{p.to_param}", notice: "Deleted cached record for #{r.name}."
    elsif cache_type.eql? "negative"
      r = CachedNegative.find(id)
      r.delete
      redirect_to "/cache?#{p.to_param}", notice: "Deleted cached negative for #{r.name}."
    else
      head 400
    end
  end
end
