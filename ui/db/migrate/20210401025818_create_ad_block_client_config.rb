class CreateAdBlockClientConfig < ActiveRecord::Migration[6.0]
  def change
    create_table :ad_block_client_config, id: :uuid, default: -> {"gen_random_uuid()"}  do |t|
      t.inet :ip, null: false
      t.boolean :block_ads, null: false
      t.timestamps
    end
  end
end
