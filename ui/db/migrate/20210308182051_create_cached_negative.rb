class CreateCachedNegative < ActiveRecord::Migration[6.0]
  def change
    create_table :cached_negative, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string "name"
      t.integer "qtype"
      t.integer "qclass"
      t.integer "ttl"
      t.datetime "created_at", precision: 6, null: false
      t.datetime "expires_at", precision: 6, null: false
    end
  end
end
