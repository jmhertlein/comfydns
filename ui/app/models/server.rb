class Server < ApplicationRecord
  has_many :server_authority_state
  has_many :task
end
