## Running in local
Tested with:
- ruby 3.0.0
- jekyll 4.2.0
- node 16.3.0
- grunt-cli 1.4.3

Make sure to install these dependencies, run `npm install`, and finally `grunt watch` which will run server on `http://127.0.0.1:1984`

## Adding an news post
Add a markdown file to the `_posts/` directory and format it like `<yyyy-dd-mm-title.md>`, at the begining of the file add following lines:
~~~
---
layout: article
title: title here
categories: news
excerpt_separator: <!--more-->
image: image filename here
---
~~~
- Title image should be added to `/media/news/title` directory.
- `excerpt_separator` is the preview text that will appear on `/news` page, by adding `<!--more-->` you can moderate what content gets to "preview"