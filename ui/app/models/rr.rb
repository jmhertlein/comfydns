class RR < ApplicationRecord
  self.table_name = "rr"
  belongs_to :zone
end
