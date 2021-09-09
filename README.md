# furms
FENIX User Management System

### CLI BUILD
To be able to build and create CLI application you have to meet these requirements:

- download GraalVM from https://www.graalvm.org/downloads/
- install `native-image` plugin: 
  
        `gu install native-image` 
  (more info available at https://www.graalvm.org/reference-manual/native-image/),
- force Maven installation to use GraalVM JDK e.g.:
        
        export JAVA_HOME=/path/to/your/graalvm/installation

- run maven package with `native` profile enabled:

        mvn clean package -Pnative

Binary application will be available in `target` module as `furms`.

More information about running using CLI, type `furms --help`