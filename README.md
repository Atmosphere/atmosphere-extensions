### [Atmosphere Framework](https://github.com/Atmosphere/atmosphere) Extensions and Plugin

You need Atmosphere Runtime version 2.4.0 and higher to use those extension. To install, just do

```java
     <dependency>
         <groupId>org.atmosphere</groupId>
         <artifactId>atmosphere-{atmosphere-module}</artifactId>
         <version>2.4.3</version>
      </dependency>
```
Where module can be one of the listed directory above. Client on the module's pom.xml to get the exact artifact name.

For GWT 2.7

```java
     <dependency>
         <groupId>org.atmosphere</groupId>
         <artifactId>atmosphere-{atmosphere-module}</artifactId>
         <version>2.3.0-GWT27</version>
      </dependency>
```

Easiest way to use extensions is by looking at our [Samples](https://github.com/Atmosphere/atmosphere-samples) 
or jump directly into the [code](https://github.com/Atmosphere/atmosphere-samplesi/extensions-samples) of the extensions you want to use.

[Javadocs](http://atmosphere.github.io/atmosphere-extensions/apidocs/) and [Getting Started](https://github.com/Atmosphere/atmosphere-extensions/wiki)

If you are an Atmosphere GWT 1.0.x users, The GWT module is deprecated and the final version that can be used with Atmosphere 2.0.x is
```java
     <dependency>
         <groupId>org.atmosphere</groupId>
         <artifactId>atmosphere-{gwt-module}</artifactId>
         <version>1.1.0.RC5</version>
      </dependency>
```
It is strongly recommended to upgrade to the GWT2.0 module.

If you are interested, subscribe to our [mailing list](http://groups.google.com/group/atmosphere-framework) for more info!  We are on irc.freenode.net under #atmosphere-comet

[![Analytics](https://ga-beacon.appspot.com/UA-31990725-2/Atmosphere/atmosphere-extensions)]
