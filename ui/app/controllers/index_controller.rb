class IndexController < ApplicationController
  def index
    @current_rr_count = CachedRR.count
    @zone_count = Zone.count

    
  end
end
