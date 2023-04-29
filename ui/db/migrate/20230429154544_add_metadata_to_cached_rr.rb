class AddMetadataToCachedRR < ActiveRecord::Migration[6.1]
  def change
    add_column :cached_rr, :original_qname, :string, default: nil
    add_column :cached_rr, :original_query_id, :uuid, default: nil
  end
end
