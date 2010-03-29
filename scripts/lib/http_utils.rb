require 'uri'

module Y60Utils
  
  def self.fetch_wiki_page uri
    response = get_page uri
    html = response.body

    doc = Hpricot html
    (doc/"div.wikipage").to_s
  end

  def self.http_get uri
    uri = (URI.parse uri)
    http = Net::HTTP.new(uri.host, uri.port)
    http.start { http.request_get(uri.request_uri) }
  end

end