module DNS
  RAW_TYPE_TEXT = <<-EOF
  A               1 a host address
  NS              2 an authoritative name server
  MD              3 a mail destination (Obsolete - use MX)
  MF              4 a mail forwarder (Obsolete - use MX)
  CNAME           5 the canonical name for an alias
  SOA             6 marks the start of a zone of authority
  MB              7 a mailbox domain name (EXPERIMENTAL)
  MG              8 a mail group member (EXPERIMENTAL)
  MR              9 a mail rename domain name (EXPERIMENTAL)
  NULL            10 a null RR (EXPERIMENTAL)
  WKS             11 a well known service description
  PTR             12 a domain name pointer
  HINFO           13 host information
  MINFO           14 mailbox or mail list information
  MX              15 mail exchange
  TXT             16 text strings
  EOF

  RRTYPE_TO_VALUE = Hash.new
  VALUE_TO_RRTYPE = Hash.new
  RRTYPE_TO_DESC = Hash.new
  RAW_TYPE_TEXT.strip
  .split(/\n/)
  .map{|l| l.strip.split(/\s+/, 3)}
  .each do |k, v, desc|
    RRTYPE_TO_VALUE[k] = v.to_i 
    VALUE_TO_RRTYPE[v.to_i] = k
    RRTYPE_TO_DESC[k] = desc
  end

  SUPPORTED_RRTYPES = [
    "A",
    "NS",
    "CNAME",
    "SOA",
    "WKS",
    "PTR",
    "MX",
    "TXT",
  ]

  RRCLASS_TO_VALUE = {
    "IN"=> 1,
    "CH"=> 3, 
    "HS"=> 4,
  }

  VALUE_TO_RRCLASS = {
    1 => "IN",
    3 => "CH",
    4 => "HS",
  }
end