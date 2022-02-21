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

  VALUE_TO_OPCODE = {
      0 => "QUERY",
      2 => "IQUERY",
      3 => "STATUS",
  }

  VALUE_TO_RCODE = {
    0 => "NO ERROR",
    1 => "FORMAT ERROR",
    2 => "SERVER FAILURE",
    3 => "NAME ERROR",
    4 => "NOT IMPLEMENTED",
    5 => "REFUSED",
  }


  def value_to_rrtype value
    if VALUE_TO_RRTYPE.has_key? value
      return VALUE_TO_RRTYPE[value]
    else
      return value.to_s(2)
    end
  end

  def value_to_rrclass value
    if VALUE_TO_RRCLASS.has_key? value
      return VALUE_TO_RRCLASS[value]
    else
      return value.to_s(2)
    end
  end

  def ip_to_in_addr ip
    return ip.split(".").reverse.join(".") + ".in-addr.arpa"
  end

  module_function :value_to_rrclass, :value_to_rrtype, :ip_to_in_addr
end