class Zone < ApplicationRecord
  has_one :start_of_authority
  has_many :rr
  
end
