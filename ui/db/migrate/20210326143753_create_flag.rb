class CreateFlag < ActiveRecord::Migration[6.0]
  def change
    create_table :flag, id: :string, primary_key: :name do |t|
      t.boolean :value
      t.timestamps
    end
  end
end
