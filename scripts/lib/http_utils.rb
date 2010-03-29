require 'net/https'
require 'uri'

module Y60Utils
  
  def self.fetch_wiki_page uri
    response = get_page uri
    html = response.body

    doc = Hpricot html
    (doc/"div.wikipage").to_s
  end

  def self.http_get uri
    #puts "get trac page: #{uri}"

    # https certificate handling code taken from:
    #   http://notetoself.vrensk.com/2008/09/verified-https-in-ruby/
    #
    uri = (URI.parse uri)
    #puts "uri: #{uri.inspect}"
    http = Net::HTTP.new(uri.host, uri.port)
    if uri.scheme == "https"  # enable SSL/TLS
      http.use_ssl = true
      http.verify_mode = OpenSSL::SSL::VERIFY_NONE
    end
    #puts "http: #{http}"
    #puts "uri.path: #{uri.path}"
    http.start { http.request_get(uri.request_uri) }
  end

end