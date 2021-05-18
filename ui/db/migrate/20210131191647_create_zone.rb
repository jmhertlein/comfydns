class CreateZone < ActiveRecord::Migration[6.0]
  def change
    enable_extension "pgcrypto"

    create_table :zone, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string :name
      t.string :host_mask
      t.boolean :gen_ptrs
      t.timestamps
    end

    create_table :rr, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string :name
      t.integer :rrtype
      t.integer :rrclass
      t.integer :ttl
      t.jsonb :rdata
      t.uuid :zone_id
      t.timestamps
    end

    add_foreign_key :rr, :zone, on_delete: :cascade
  end
end
