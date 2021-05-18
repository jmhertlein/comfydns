class CreateCachedRR < ActiveRecord::Migration[6.0]
  def change
    create_table :cached_rr, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string "name"
      t.integer "rrtype"
      t.integer "rrclass"
      t.integer "ttl"
      t.jsonb "rdata"
      t.datetime "created_at", precision: 6, null: false
      t.datetime "expires_at", precision: 6, null: false
    end
  end
end
