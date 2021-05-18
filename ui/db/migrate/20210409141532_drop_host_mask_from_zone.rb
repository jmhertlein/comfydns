class DropHostMaskFromZone < ActiveRecord::Migration[6.0]
  def change
    remove_column :zone, :host_mask
  end
end
