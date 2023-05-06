#!/usr/bin/env ruby

require 'subprocess'
require 'fileutils'
require 'pathname'

PG_CUR_VER = 12

PG_BASE_DATA_DIR_PN = Pathname.new "/opt/comfydns/pg"
PG_DATA_PN = PG_BASE_DATA_DIR_PN + PG_CUR_VER.to_s + "main"

PG_BASE_CONF_DIR_PN = Pathname.new "/opt/comfydns/pgetc"
PG_CONF_PN = PG_BASE_CONF_DIR_PN + PG_CUR_VER.to_s + "main"


PG_HBA_PN = Pathname.new "/static/pg_hba.conf"


# pg db setup, if needed
unless PG_DATA_PN.exist?
  puts "initdb"
  PG_DATA_PN.mkpath
  Subprocess.check_call(["chown", "-R", "postgres:postgres", PG_BASE_CONF_DIR_PN.to_s, PG_BASE_DATA_DIR_PN.to_s])
  Subprocess.check_call(["sudo", "-u", "postgres", "pg_createcluster", PG_CUR_VER.to_s, "main"])
  puts "write pg_hba.conf"
  (PG_CONF_PN + "pg_hba.conf").write(PG_HBA_PN.read)
  puts "start pg"
  Subprocess.check_call(["service", "postgresql", "start"])
  puts "createuser comfydns"
  Subprocess.check_call(["sudo", "-u", "postgres", "createuser", "-w", "-s", "comfydns"])
  Subprocess.check_call(["chown", "-R", "postgres:postgres", PG_BASE_CONF_DIR_PN.to_s, PG_BASE_DATA_DIR_PN.to_s])
end

unless PG_CONF_PN.exist?
  # the old dockerfile didn't have the config rehomed... let's fix it
  puts "fixing missing pgetc from old docker image"
  PG_CONF_PN.mkpath
  Subprocess.check_call(["bash", "-c", "cp -r /savedetcpg/#{PG_CUR_VER}/main/* #{PG_CONF_PN.to_s}"])
  FileUtils.cp PG_HBA_PN.to_s, (PG_CONF_PN + "pg_hba.conf")
  Subprocess.check_call(["chown", "-R", "postgres:postgres", PG_BASE_CONF_DIR_PN.to_s, PG_BASE_DATA_DIR_PN.to_s])
end

# start pg
puts "start pg"
Subprocess.check_call(["service", "postgresql", "start"])


Dir.chdir("/app/ui")

rails_secret_pn = Pathname.new "/opt/comfydns/rails_secret.txt"

if rails_secret_pn.exist?
  puts "Using existing rails secret_key_base"
  skb = rails_secret_pn.read.strip
else
  puts "Generating new rails secret_key_base"
  skb = Subprocess.check_output(["rails", "secret"])
  rails_secret_pn.write skb
end

# Dir.chdir("/app/ui")
puts "db migrate"
new_env = ENV.to_h
new_env["SECRET_KEY_BASE"] = skb
Subprocess.check_call(["rails", "db:create", "db:migrate"], env: new_env)

Dir.chdir("/app")
puts "setup done, starting supervisor"
Subprocess.check_call(["/usr/bin/supervisord"], env: new_env)