FROM ubuntu:20.04
MAINTAINER Francisco Chicano <chicano@lcc.uma.es>

RUN apt-get update
RUN apt-get upgrade -y

# Download and install wkhtmltopdf
COPY wkhtmltox_0.12.6-1.focal_amd64.deb . 
RUN apt-get install -y ./wkhtmltox_0.12.6-1.focal_amd64.deb && rm wkhtmltox_0.12.6-1.focal_amd64.deb

# Show the extended help
CMD ["bash"]
