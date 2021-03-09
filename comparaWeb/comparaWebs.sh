#!/bin/bash

file=$1

docker run --rm -it -v $(pwd):/data wkhtmltopdf wkhtmltoimage --disable-smart-width --allow /data /data/$file  /data/student.png

#wkhtmltoimage --disable-smart-width --allow . $file student.png
java -jar ImageDiff.jar patron.png student.png
newfile=$(shasum -a 256 output.jpg | awk '{print $1}')
mv output.jpg ${newfile}.jpg
echo ${newfile}.jpg
rm student.png
