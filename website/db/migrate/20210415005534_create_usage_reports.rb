class CreateUsageReports < ActiveRecord::Migration[6.0]
  def change
    enable_extension "pgcrypto"
    
    create_table :usage_reports, id: :uuid, default: -> {"gen_random_uuid()"} do |t|
      t.inet :ip, null: false
      t.uuid :fingerprint, null: false
      t.datetime "created_at", precision: 6, null: false
    end
  end
end
