class CreateServer < ActiveRecord::Migration[6.0]
  def change
    create_table :server, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.string :hostname
      t.string :ip_address
      t.boolean :use_https_for_api
      t.timestamps
    end
  end
end
