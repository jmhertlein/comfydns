# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# This file is the source Rails uses to define your schema when running `bin/rails
# db:schema:load`. When creating a new database, `bin/rails db:schema:load` tends to
# be faster and is potentially less error prone than running all of your
# migrations from scratch. Old migrations may fail to apply correctly if those
# migrations use external dependencies or application code.
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 2023_04_29_154544) do

  # These are extensions that must be enabled in order to support this database
  enable_extension "pgcrypto"
  enable_extension "plpgsql"

  create_table "ad_block_client_config", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.inet "ip", null: false
    t.boolean "block_ads", null: false
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.index ["ip"], name: "ad_block_client_config_ip_idx"
  end

  create_table "block_list", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.string "name", null: false
    t.string "url", null: false
    t.boolean "auto_update", default: false, null: false
    t.string "update_frequency"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.string "list_type", default: "newline-list", null: false
    t.index ["url"], name: "block_list_url_key", unique: true
  end

  create_table "block_list_snapshot", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.uuid "block_list_id", null: false
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.index ["block_list_id"], name: "block_list_snapshot_block_list_id_key", unique: true
  end

  create_table "blocked_name", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.string "name", null: false
    t.uuid "block_list_snapshot_id", null: false
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.index ["name"], name: "blocked_name_name_idx"
  end

  create_table "cached_negative", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.string "qname"
    t.integer "qtype"
    t.integer "qclass"
    t.integer "r_ttl"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "expires_at", precision: 6, null: false
    t.text "r_name"
    t.integer "r_class"
    t.text "r_mname"
    t.text "r_rname"
    t.bigint "r_serial"
    t.bigint "r_refresh"
    t.bigint "r_retry"
    t.bigint "r_expire"
    t.bigint "r_minimum"
    t.integer "r_rcode", null: false
    t.index ["qname"], name: "cached_negative_name_idx"
  end

  create_table "cached_rr", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.string "name"
    t.integer "rrtype"
    t.integer "rrclass"
    t.integer "ttl"
    t.jsonb "rdata"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "expires_at", precision: 6, null: false
    t.string "original_qname"
    t.uuid "original_query_id"
    t.index ["name", "rrtype", "rrclass", "rdata"], name: "cached_rr_name_rrtype_rrclass_rdata_key", unique: true
    t.index ["name"], name: "cached_rr_name_idx"
  end

  create_table "flag", primary_key: "name", id: :string, force: :cascade do |t|
    t.boolean "value"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
  end

  create_table "rr", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.string "name"
    t.integer "rrtype"
    t.integer "rrclass"
    t.integer "ttl"
    t.jsonb "rdata"
    t.uuid "zone_id"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
  end

  create_table "task", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.string "action", null: false
    t.boolean "started", default: false
    t.boolean "done", default: false
    t.boolean "failed", default: false
    t.jsonb "args"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
  end

  create_table "trace", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.uuid "task_id", null: false
    t.string "qname", null: false
    t.integer "qtype", null: false
    t.integer "qclass", null: false
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
  end

  create_table "trace_event", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.integer "event_index", null: false
    t.uuid "trace_id", null: false
    t.string "event_type", null: false
    t.jsonb "event", null: false
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
  end

  create_table "user", force: :cascade do |t|
    t.string "email", default: "", null: false
    t.string "encrypted_password", default: "", null: false
    t.string "reset_password_token"
    t.datetime "reset_password_sent_at"
    t.datetime "remember_created_at"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.index ["email"], name: "index_user_on_email", unique: true
    t.index ["reset_password_token"], name: "index_user_on_reset_password_token", unique: true
  end

  create_table "zone", id: :uuid, default: -> { "gen_random_uuid()" }, force: :cascade do |t|
    t.string "name"
    t.boolean "gen_ptrs"
    t.datetime "created_at", precision: 6, null: false
    t.datetime "updated_at", precision: 6, null: false
    t.uuid "soa_rr_id"
  end

  add_foreign_key "block_list_snapshot", "block_list", on_delete: :cascade
  add_foreign_key "blocked_name", "block_list_snapshot", on_delete: :cascade
  add_foreign_key "rr", "zone", on_delete: :cascade
  add_foreign_key "trace", "task", on_delete: :cascade
  add_foreign_key "trace_event", "trace", on_delete: :cascade
  add_foreign_key "zone", "rr", column: "soa_rr_id"
end
