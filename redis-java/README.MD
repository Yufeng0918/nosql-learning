# Redis

## Redis Overview
#### Overview
- NoSQL Not Only SQL, not relation database
- Relationship database not efficient 
    + High concurrency
    + Huge storage
    + High scalability & High Availability
- Product
    + Redis: single thread + NIO (select + epull)
    + Memcached: multi-thread + lock
    + MongoDB
- Storage
    + Key-Value
    + Row
    + Documents
    + Graph
- Feature
    + easy to scale via cluster
    + free data model
    + high currency and huge storage
    + high availability
    + base on memory
    + single thread
    + IO: base on NIO
        + [select and pull] linux 2.6, loop the file description
        + [epoll] linux 2.6+, ae_epoll.c only loop the read/write socket
- Developed by C
    + Key-value
    + Row
    + Hash
    + Collection
    + Sequence Collection
- Case Scenario
    + Cache
    + Task Queue
    + State
#### Installation
```
$ wget http://download.redis.io/releases/redis-5.0.5.tar.gz
$ tar xzf redis-5.0.5.tar.gz
$ cd redis-5.0.5
$ make
```
- redis.conf: change daemonize from no to yes
#### Database
- default 16 database, default is 0 database
- select $i to switch database
***
    
    
## Redis DataType
- start up
    + redis-server <config_file>
- shutdown
    + redis-cli shutdown
- connect
    + redis-cli
- standard
    + type key
    + ping
    + select $i, i is database index
    + dbsize, return current key size in select db, max key size is 512MB, handle 2^32 keys
    + flushdb, clear current database
    + flushall, clear all database
    + keys *
    + expire key timeout, -1 forever, -2 expired
    + ttl key, time to expire
    + exists key
- String:
    + set/get/del
    + set $k $v, reset expire to -1
    + getset $k $v, replace one value
    + get $k
    + del $k
    + append $k $v
    + strlen $k
    + setnx $k $v, set the key if does not exist
    + mset $k1 $v1 $k2 $v2 $k3 $v3, same as msetnx
    + mget $k1 $k2 $k3
    + getrange $k1 $start $end, index start from 0, end index is -1 is last index, including start and end
    + setrange $k $offset, set the value from offset
    + setex $k $expire $v, set value and exipre
- Number:
    + set $k $v
    + incr/decr $k, atomic operation to increase or decrease 1
    + incrby/decrby $k $v, atomic operation
- List
    + double linked list, fast in header and tailer, pool performance based on index
    + lpush/rpush $list [$val...], lpush add from header, rpush add from tailer
    + lpop/rpop $list, if no element in list, list will be destoryed 
    + llen $list
    + lpushx $list [$val...], insert only list exists
    + lrem $list $num [$val]
        + positive number, delete number of value from header
        + negative number, delete number of value from tailer
        + 0, delete all number of value in list
    + lset $list $index $value, set the value base on index, index starting from -1
    + linsert $list before [$val1] [$val2], insert val2 before val1
    + lrange $list $start $end
    + lindex $list $index
- Set
    + no duplicated elements
    + sadd $set [$val...]
    + srem $set [$val...]
    + smembers $set
    + sismember $set [$val], check if contains value
    + sdiff $set1 $set2, element in set1 but not in set2, diff set
    + sinter $set1 $set2, element in set1 and set2, interset
    + sunion $set1 $set2, all elements
    + scard $set, return number of elements
    + spop, pop element by random
    + srandmember, pick element by random, not delete element in set
- Hash
    + Map, key-value collection
    + hset $hash $key $value
    + hget $hash $key 
    + hgetall $hash, return list by [k1, v1, k2, v2]
    + hdel $hash $key
    + hincrby/hdecrby $hash $key $value 
    + hexists $hash $key
    + hlen $hash
    + hkeys $hash
    + hvals $hash
- Sorted Set
    + score defined order
    + zadd $set [$score $value, ...]
    + zscore $set $val, 
    + zrem $set $val
    + zrange $set [$start $end] [withscores], min before max
    + zrevrange $set [$start $end] [withscores], return list with v1, s1, v2, s2
    + zrangebyscore $set score1 score2
    + zincrby $set $score $val
***


## Redis Config
- protected-mode: if access by other ip
	+ bind: specify the ip accesable
- tcp-backlog: client connection queue size
- tcp-keeplive: client connection live timeout
- database: number of database
***


## Redis Transaction
- atomic operation in multi if error in composite command
- not atomic operation in exec if error in execution command
- not advice for rollback
- Transaction
    - watch $k
        + if return nil means transaction has been break
        + exec unwatch command if commit "exec" or "discard"
    - multi: open transaction
    - exec:  commit transaction
    - discard: rollback transaction
- features
    + all command serialized and exec in sequence
    + no isolation
    + not atomic in exec operation
- flash sales case
    + sk:prod-id:qt = String(quantity)
    + sk:prod-id:user = Set(user_id)
- LUA script is recommanded
    + exec LUA script is atomic, the executions is sequence
***


## Redis Persistence
#### RDB Persist
+ persist data time interval
+ RDB conf in "save" session, periodic snapshot
+ high performance
+ single file backup, fork the sub process with write-on-copy tech
+ may loss data in HA
+ load rdb file to restore the data
+ auto save in shutdown, debug reload
+ manual save
	+ save: blocking save
	+ bgsave: fork thread to save
   		+ check if sub thread is running aof or rdb
       	+ Yes, return error
        	+ No invoke rdbSaveBackgroud to fork sub thread to save
+ implementation: redis servercron schedule task to execute save

```
#### save "" to disallow RDB
save 900 1 # after 900 sec, at least 1 keys change
save 300 10, after/ 300 sec, at least 10 keys change
save 60 10000, after 60 sec, at least 10000 keys change

stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
```

#### AOF
+ persist data by by appending log
+ syn by per operation
+ append to log file before shutdown

```
# main config to open aof
appendonly yes

# file name
appendfilename "appendonly.aof"

# define the append sequence, value is  [always|everysec|no]
# always: slow but safe, IO consumption high
# everysec: sync every sec, fork sub thread to write command into aof_buf, flush into disk every sec
# no: os backup if buffer is full
appendfsync

# ll prevent fsync() from being called in the main process while a BGSAVE or BGREWRITEAOF is in progress
no-appendsync-on-rewrite no

# trigger rewrite via calling BGREWRITEAOF
# rewrite by percentage or min file zie
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64m

# handle error logging, if aof file error
# yes, logging and continue
# no, do not execute and wait for repair
aof-load-truncated yes
aof-rewrite-incremental-fsync yes
```

#### AOF vs RDF
- sequence
    - if aof exist, load aof
    - otherwise, load rdf
- implementation
    + rdf: snapshot of data
    + aof: records every operation
- rdb
    + pos
        + higher performance, master thread does not participant IO
        + faster bootstrap
    + cons
        + rdf may lost data due to frequence
        + fork may takes longer time if data size is huge
- aof
    + pos
        + safe in data backup
        + if aof file is too big, rewrite the file to minimize file size
        + human readable 
    + cos
        + size is bigger
        + aof may slow than rdb
***        
        
  
## Redis Master Slave
#### Separation of Read & Write
- Master-Slaver
    + master: write
    + slave: read
    + disaster recovery   
- Config
    + slave include master config
    + daemonize yes
    + pidfile
    + port
    + log file name
    + dump.rdb file name
    + appendonly
    + slaveof master port
- Slave
    + slaveof master_host master_port, e.g. slaveof localhost 6379
    + copy the full set of data
    + slave may have slave to unload the master sync operation
    + connect master and sent sync command
    + master send the rdb file to slave
    + slave reload rdb file
    + synchronization
        + SYNC
            + before redis2.8
            + master execute BSSAVE to generate RDB, consume master CPU, memory, IO
            + master send the RDB to slave, consume bandwidth
            + slave receive RDB and load RDB, block IO
        + PSYNC 
            + after redis2.8
            + master and slave maintains replication offset
            + master write data into replication backlog, default size 1M
            + slave connect to master to compare the master ID connected and disconnected before
            + master received slaveof command, slave compare master ID
                + if this is 1st time connect to master, slave send PSYNC?-1 to do the FULLREPSYNC
                + if this is not 1st time connect to master, slave send PSYNC runid offset to do the PartialSYNC
                    + but if backlog is over 1M, slave need to do the FULLRESYNC
                + repl-disable-tcp-nodelay
                    + set to yes, delay in sync but save bandwidth, default is 40ms
                    + set to no, less delay but increase bandwidth
    + info replication to check master slave
- Mannul Recovery
    + slaveof no one: slave become master
    + slaveof host port: slave to new master
#### High Availability
- Reserve Proxy: ngnix cluster via keeplived
- Web Server: ngnix auto detect the server unavaliable and redirect request to live sever
- Cache: redis master to slave and sentinel
- Database: master to slave
#### Redis HA
- Seninel monitor all servers
- Sentinel
    + configure sentinel.conf
    + redis-sentinel sentienl.conf
```
# sentinel monitor master and agree master failure by number of sentinel
sentinel monitor mymaster 127.0.0.1 6379 1
sentinel down-after-milliseconds mymaster 10000
# failover headbeat confirm
sentinel failover-timeout mymaster 6000
# number of slave to synchronized with master
sentinel parallel-syncs mymaster 1
```
   + Subjectively Down adjustment
        + if server diid not response in  "down-after-milliseconds"  period
        + sentinel send ping, node return pong or loading or masterdown as node is still online
        + slave only applicable to subjectively down
        + once down master join back, the original master become slave of new master
   + Objectively Down
        + only applicable to master
        + multi sentinel confirm node is SDOWN
        + base strong quorum algorithm
#### Backup
- cool backup
    + backup during downtime in certain point
    + low cost
    + high security
- hot backup
    + archivelog mode to backup data
    + short duration
    + backup data available
    + high cost
    + recover in second level
#### Springboot Integration
- connect via sentinel as redis cluster proxy
```
spring.redis.sentinel.master=mymaster
spring.redis.sentinel.nodes=127.0.0.1:26379
```
***
   
   
## Redis Cluster   
#### Cluster
- key hash by CRC16 then mod for 16384
- only after redis 3.0
- at least 3 master, 3 slave
- conf
    + cluster-enabled yes
    + cluster
- if master failed, slave become master
- failure when A and A' down since a set of slots unavailable
#### Config
```
daemonize yes
port 7000
cluster-enabled yes
cluster-config-file node-7000.conf
cluster-node-timeout 5000
appendonly yes
bind <internet ip>
```
#### Command
- redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 --cluster-replicas 1
- redis-cli -p 7000 cluster nodes 
#### Sharding
- arrange 16384 slot
- key calculate base CRC16
- reshard command: redis-cli --cluster reshard 127.0.0.1:7000
#### Spring Integration
```
spring.redis.cluster.nodes=127.0.0.1:7000,127.0.0.1:7001,127.0.0.1:7002,127.0.0.1:7003,127.0.0.1:7004,127.0.0.1:7005
spring.redis.cluster.max-redirects=3
spring.redis.lettuce.pool.max-active=1000
spring.redis.lettuce.pool.max-idle=10
spring.redis.lettuce.pool.min-idle=5
spring.redis.lettuce.pool.max-wait=-1
```
#### Cluster Implementation
- redis 
    - redis cluster only after 3.0
    - Gossip generate ping/pong
    - blocking IO may counter issue when dynamic scale up
- principal
    - horizontal sharding or vetical sharding
        + horizontal sharding to distrubted data in different server
        + Twemproxy from twitter
            + hash key and store data in different server
    - disaster recover
        + gossip to implment slave and master and replace master by slave in disaster
#### Constant Hash
- hash 
    + hash value and mod by 2^32
    + there is n nodes, each node holds 2^32/4
    + master1 -> key2 -> master2 -> key3 -> master3 -> key4 -> master4 -> key1 -> master1
        + master1 store key1, master2 store key2
        + if master2 is down, the key2 flow in clock into master3
- features
    + monotonicity: original request map to original server or new server, but not other server
    + spread
        + if client only knows part of server, same user request may route to differnt server, this should be avoid
        + ratio of this case should be relative low
    + balance: balance of all hash distribute to all server
- visual node distribution
    + server has serval visual node
    + visual node distributed equaly in hash ring
#### Twemproxy
- proxy server support redis and client
- shading key into hash and distribute in different redis master and slave
- dynamic delete the failure node
- support hash algo
    + md5, crc16, crc32, crc32a, murmur
- support shading algo
    + ketama, modula, random
- client only communicate with proxy instead of redis master and slave
#### Production Practise
- enable rdb and aof
- turn off persist if no sensitive info
- enable cluster or master slave
***


## Redis Distributed Lock
#### Overview
- Distributed Lock is lock for resourced shared by multiple instance of application
- Only one instance can access the resource at that moment
- Must be ReentrantLock
#### Implementation
- Lock: setnx to create the lock
- Timed: senex to set expire time
- Release: set expire time or delete keys
- Risk
    + redis or server down between setex and setnx
    + key never get release
    + lua script to run as atomic operation
- Improved Solution
    + Lock
        + Lua
            + after redis 2.6.0 version
            + run atomic operation
        + RedisConnection   
            + run the native set key and expire in atomic operation
    + Unlock
        + Lua
            + CompareAndSwap 
            + compare the value
            + only unlock or release key if value is expected
 ***
 
 
 ## Redis Expire Policy
 #### lazy expire
 - verify key expire when get the key
 - CPU friendly but memory consumption
 - expireIfNeed()
 #### period expire
 - create timer to delete key
 #### Global Timer 
 - global timer to check key expire
 - CPU friendly and save memory space
  - activeExpireCycle() by serverCron
 ****
 
 
## RED PACKET Production
#### Database Design
- packet info contains amount and quantity
- packet leger info contains user grab records
```
CREATE TABLE `red_packet_record` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`amount` int(11) NOT NULL DEFAULT '0' COMMENT '抢到红包的金额',
`nick_name` varchar(32) NOT NULL DEFAULT '0' COMMENT '抢到红包的用户的用户名',
`img_url` varchar(255) NOT NULL DEFAULT '0' COMMENT '抢到红包的用户的头像',
`uid` int(20) NOT NULL DEFAULT '0' COMMENT '抢到红包用户的用户标识',
`red_packet_id` bigint(11) NOT NULL DEFAULT '0' COMMENT '红包id，采用timestamp+5位随机
数',
`create_time` timestamp COMMENT '创建时间',
`update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
COMMENT '更新时间', PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='抢红包记录表，抢一个红包插 入一条记录';


CREATE TABLE `red_packet_info` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`red_packet_id` bigint(11) NOT NULL DEFAULT 0 COMMENT '红包id，采用timestamp+5位随机数', `total_amount` int(11) NOT NULL DEFAULT 0 COMMENT '红包总金额，单位分',
`total_packet` int(11) NOT NULL DEFAULT 0 COMMENT '红包总个数',
`remaining_amount` int(11) NOT NULL DEFAULT 0 COMMENT '剩余红包金额，单位分', `remaining_packet` int(11) NOT NULL DEFAULT 0 COMMENT '剩余红包个数',
`uid` int(20) NOT NULL DEFAULT 0 COMMENT '新建红包用户的用户标识',
`create_time` timestamp COMMENT '创建时间',
`update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
COMMENT '更新时间', PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='红包信息表，新建一个红包插 入一条记录';
```


## Google and Redis Bloom Filter
#### Overview
- data structure check if item does not exist or may exist in high probabilities
- efficient query algo and less memory space
- multiple hash function to generate multiple hash, one hash map to multiple bit position
	+ if one of bit position return 0, it means item does not exist
	+ if all hash function return 1, item may exist depends on error rate
#### Implementation
- Google 
    + store data in memory
    + not support restart
    + not support distributed application
    + not support huge data
- Redis
    + IO consume
    + extendable bloom filter
    + support restart
    + command
        + add: BF.ADD key value
        + exist: BF.EXISTS key value
***


## Redis Second Sale
#### Overview
- user order, redis to check inventroy
- application verify order info and push order into MQ
- application listen order info from queue and persist into DB
#### Redis Design
- skuId_start: indicate if sec sale start
- skuId_count: product inventory, require atomic operation
- skuId_acdess: request to buy the product, number of request is limiting by rounding number, atomic operation does not require
- bloom filter
    + verify if user place order second times
***
    