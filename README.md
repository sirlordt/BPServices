servicesdaemon (Atlas Middleware)
=================================

All code in this repo is licensed to GNU/GPL version 2

HTTP JSON/XML/CSV Restfull interface middleware for RDMS using JDBC

Business Process Logic Layer

```
  +---------+        +----------------------------------+                     +-----------------------------------------------+
  |         |  JDBC  |         ServicesDaemon           |       HTTP(S)       |           Standard HTTP(S) client             | 
  |  RDBMS  |<------>|  http(s)://server_ip/BPServices/ |<------------------->|  (Java/Curl/Ruby/Perl/Python/Javascript/Php)  |
  |         |        |         (Jetty/Servlet)          |    (XML/CSV/JSON)   |                                               | 
  +---------+        +----------------------------------+                     +-----------------------------------------------+
                                                |
                                                | Managers (Servlet implementations on specific context path)
                                                |
                                                |     +----------------------------+
                                                |     |                            | 
                                                +-----|   CAbstractServicesManager |
                                                      |                            |
                                                      +----------------------------+
                                                                    |
                                                                    |       +--------------------------------------+ 
                                                                    |       |                                      | 
                                                                    +-------|             /DBServices              |
                                                                    |       |   Control all Datatabase Operations  |
                                                                    |       +--------------------------------------+
                                                                    |
                                                                    |       +--------------------------------------+  
                                                                    |       |                                      | 
                                                                    +-------|              /BPServices             | 
                                                                    |       |  Control all business process logic  |
                                                                    |       +--------------------------------------+   
                                                                    |
                                                                    |       +--------------------------------------+  
                                                                    |       |                                      | 
                                                                    +-------|           /RegisterServices          | 
                                                                    |       |       List for avalaibles nodes      |
                                                                    |       +--------------------------------------+   
                                                                    |
                                                                    |       +--------------------------------------+  
                                                                    |       |                                      | 
                                                                    +-------|           /ReplicationServices       | 
                                                                            |    Control the replication of data   |
                                                                            +--------------------------------------+   


```            
More info on http://www.maindataservices.net/servicesdaemon/?q=es/proyecto
