<a href="http://projectdanube.org/" target="_blank"><img src="http://projectdanube.github.com/xdi2/images/projectdanube_logo.png" align="right"></a>
<img src="http://projectdanube.github.com/xdi2/images/logo64.png"><br>

xdi2-manager
============

xdi2-manager allows you to configure your personal cloud.

Website: https://xdi2.org/, sample deployment: https://cloud-manager.xdi2.org/

### Features

* Personal Profile - Fill your personal information
* Facebook Connector - Link Facebook data to your personal cloud
* Connection - Manage your cloud connections
* Cloud Cards - Cloud Card editor (more information in [xdi2-cloudcards](https://github.com/projectdanube/xdi2-cloudcards))


### How to build
First, you need to build the main [XDI2](https://github.com/projectdanube/xdi2) and the [xdi2-connector-facebook](https://github.com/projectdanube/xdi2-connector-facebook) projects.

After that, just run

    mvn clean install

That will generate xdi2-manager.war. Deploy it in your favorite Java Servlet container.

### Community

Website: https://xdi2.org/

Google Group: http://groups.google.com/group/xdi2
