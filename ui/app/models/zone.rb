class Zone < ApplicationRecord
  has_many :rr
  
  def start_of_authority
    return RR.find self.soa_rr_id
  end

end
