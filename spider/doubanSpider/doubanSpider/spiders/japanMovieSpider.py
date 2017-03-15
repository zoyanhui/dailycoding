# -*- coding: utf-8 -*-

import scrapy

class JapanMovieSpider(scrapy.Spider):
      name = "japanMovie"

      def start_requests(self):
            urls = [
                  # "https://movie.douban.com/subject/26683290/?tag=%E6%97%A5%E6%9C%AC&from=gaia",
                  # "https://movie.douban.com/subject/26609126/?tag=%E6%97%A5%E6%9C%AC&from=gaia"
                  "http://quotes.toscrape.com/page/1/"
            ]
            for url in urls:
                  yield scrapy.Request(url = url, callback=self.parse)


      def parse(self, response):
            page = response.url.split("/")[-2]
            filename = 'quotes-%s.html' % page
            with open(filename, 'wb') as f:
                f.write(response.body)
            self.log('Saved file %s' % filename)
