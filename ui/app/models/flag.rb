class Flag < ApplicationRecord

  def self.is_enabled? name
    f = Flag.find_by(name: name)
    return f.nil? ? false : f.value
  end

  def self.adblock_enabled?
    return Flag.is_enabled? "adblock"
  end

  def self.adblock_client_default_on?
    return Flag.is_enabled? "adblock_client_default_on"
  end
end
