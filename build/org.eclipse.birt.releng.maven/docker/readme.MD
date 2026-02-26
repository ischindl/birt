* tool to run CBI aggregator in local workspace
create image 
```
docker build . -t birt-cbi-aggregator
```
* after succesfull tycho build run in birt parent folder 
```
docker run  --rm -v $(pwd):/workspace -e SNAPSHOT=true birt-cbi-aggregator
```