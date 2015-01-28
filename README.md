<a href="http://projectdanube.org/" target="_blank"><img src="http://projectdanube.github.com/xdi2/images/projectdanube_logo.png" align="right"></a>
<img src="http://projectdanube.github.com/xdi2/images/logo64.png"><br>

XDI Cloud Manager [https://cloud-manager.xdi2.org/](https://cloud-manager.xdi2.org/)

This web application allows you to configure your personal cloud.

* Personal Profile - Fill your personal information
* Connection - Manage your cloud connections
* Cloud Cards - Cloud Card editor (can be viewed by [Cloud Card Viewer](https://github.com/projectdanube/xdi2-cloudcards))
* Facebook Connector - Link Facebook account to your personal cloud

### Information

* [Walkthrough](https://github.com/projectdanube/xdi2-manager/wiki/Walkthrough)
* [Screencast](https://github.com/projectdanube/xdi2-manager/wiki/Screencast)

### How to build

First, you need to build the main [XDI2](https://github.com/projectdanube/xdi2) and the [xdi2-connector-facebook](https://github.com/projectdanube/xdi2-connector-facebook) projects.

After that, just run

    mvn clean install

This will generate a .war file ready for deployment.

### Community

Website: https://xdi2.org/

Google Group: http://groups.google.com/group/xdi2
