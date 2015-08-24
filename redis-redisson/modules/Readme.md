Hi All,

I developed this plugin as an alternative to atmosphere-redis due to the following issues in jedis:
https://github.com/xetorthio/jedis/issues/997
It's quite bad, thread blocking stuff.

So i did quite a lazy man's fix of replacing jedis with redisson, seems to work well.
So all everything works the same if your on a single redis instance however if you go to 
Master/slave, clusters or sentinels there's a bit more config.

First you have to specifiy redis type: defualt is single redis instance
you do this with the 
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.type=
one of these single(or nothing)/sentinel/master/cluster

if single then it's just the default redis config of 
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.server
the line above is always required no matter which options you pic, it's your master

otherwise you'll need a bit more

Sentinel has the following options:
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.type=sentinel
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.master.name=SomeName
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.others=the other nodes in comman seperated list e.g. http://bhahds:123,http://fsdf:12312

Clusters have the following:
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.type=cluster
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.scan.interval=default is 2000, which is the reconnect if lost time
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.others=the other nodes in comman seperated list e.g. http://bhahds:123,http://fsdf:12312

The Master/Slave has the following:
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.type=master
org.atmosphere.plugin.redis.redisson.RedissonBroadcaster.others=the other nodes in comman seperated list e.g. http://bhahds:123,http://fsdf:12312

Enjoy
