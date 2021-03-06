# Redis

## Redis Overview
#### Overview
- Remote Dicitionary Server
- NoSQL Not Only SQL, not relation database
- Relationship database not efficient 
    + High concurrency
    + Huge storage
    + High scalability & High Availability
- Product
    + Redis
        + single thread + NIO (select + epull)
        + 100,000 - 120,000 QPS
    + Memcached
        + multi-thread + lock
        + 1,000,000 QPS
    + MongoDB
- Storage
    + Key-Value
    + Row
    + Documents
    + Graph
- Feature
    + easy to scale via cluster
    + free data model, support multiple data structure
    + high currency and huge storage
    + high availability via cluster and master-slave
    + base on memory
    + single thread
    + IO: base on NIO
        + [select and pull] linux 2.6, loop the file description
        + [epoll] linux 2.6+, ae_epoll.c only loop the read/write socket
    + support persist storage
    + support lua and transaction
- Fork thread
    + AOF rewrite, sub thread rewrite aof temp, master append aof temp, replace aof temp to current aof
    + RDB bgsave
    + Sync build rdb: build the full rdb allow slave to sync with master
- BIO thread (Background IO)
    + main thread is producer to put tasks into queue
    + bio thread is consumer to consume task from queue
    + redis start 3 bio thread and init 3 task queue
        + fsync: persist buffer from memory to disk
        + close: close, delete, freeup file
        + lazy free: clean key, slot
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
- Default Setting
    - redis.conf: change daemonize from no to yes
    - default 16 database, default is 0 database
    - select $i to switch database
#### Installation
```
$ wget http://download.redis.io/releases/redis-5.0.5.tar.gz
$ tar xzf redis-5.0.5.tar.gz
$ cd redis-5.0.5
$ make
```
***
    
    
## Redis DataType
- support 8 datatype
    + string, list, set, sorted set, hash
    + bitmap, geo, HyperLoglog
- start up & shutdown & connect
    + redis-server <config_file>
    + redis-cli shutdown
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
    + raw encoding in string, int encoding in number
    + number stored as string as well
    + allocate 2 * space if data less than 1MB, increase 1MB if string size bigger than 1MB
    + commands
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
        + set $k $v
        + incr/decr $k, atomic operation to increase or decrease 1
        + incrby/decrby $k $v, atomic operation
- List
    + double linked list, fast in header and tailer, pool performance based on index
    + case scenario
        + feed timeline
        + news list
    + commands
        + lpush/rpush $list [$val...], lpush add from header, rpush add from tailer
        + lpop/rpop $list, if no element in list, list will be destoryed 
        + llen $list
        + lpushx $list $val..., insert only list exists
        + lrem $list $num $val
            + positive number, delete number of value from header
            + negative number, delete number of value from tailer
            + 0, delete all number of value in list
        + lset $list $index $value, set the value base on index, index starting from -1
        + linsert $list before $val1 $val2, insert val2 before val1
        + lrange $list $start $end
        + lindex $list $index
        + blpop/brpop blocking pop
- Set
    + no duplicated elements, efficient insert and query, O(1) complexity
    + case scenario
        + friends list
        + relationship verification
    + commands
        + sadd $set $val...
        + srem $set $val...
        + smembers $set
        + sismember $set $val, check if contains value
        + sdiff $set1 $set2
        + sinter $set1 $set2
        + sunion $set1 $set2
        + scard $set, return number of elements
        + spop, pop element by random
        + srandmember, pick element by random, not delete element in set
- Hash
    + Map, key-value collection
    + case scenario
        + key to complexy object
    + commands
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
    + score defined order, no duplicated value but allow duplicated score, efficient in insert and query, O(1) complexity
    + case scenario
        + stats sorted item list
        + grade table
    + commands
        + zadd $set $score $value
        + zscore $set $val, 
        + zrem $set $val
        + zrange $set $start $end [withscores], min before max
        + zrevrange $set $start $end [withscores], return list with v1, s1, v2, s2
        + zrangebyscore $set score1 score2
        + zincrby $set $score $val
 - Bitmap
    + bit array, each position is 0 or 1
    + case scenario
        + last n day records
        + news, feed, users flag
    + commands
        + setbit $bitmap $offset $v
        + getbit $bitmap $offset
- GEO
    + wrapper by sorted sort, convert two demssion longitude and latitude to one demssion (52 bit)
    + one demssion as score, efficient in calculation
    + case scenario
        + query location
        + calculate distance between two location
        + find nearby
    + commands
        + geoadd $key $longitude $latitude $member
        + geopos $key $member
        + geodist $key $mem1 $mem2
- HyperLogLog
    + no store row data, store only stats, allow certain error range
    + case scenario
        + total visit users
        + stats search words by user
    + commands
        + pfadd $key $v1 $v2 $v3
        + pfcount $key
 - Internal Data Structure
    + redisDB
        + main dict
            + core dict: store all key/value
            + expire dict: store expire key
        + sub dict
            + blocking dict: store blocking key or clients, e.g. blpop
            + ready dict: store ready key or client, e.g. list insert item after blpop
            + watched dict: watched key and clients
        + all data store in redisObject
    + redisObject
        + Type: OBJ_STRING, OBJ_LIST, OBJ_SET, OBJ_ZSET, OBJ_HASH, OBJ_MODULE, OBJ_STREAM
        + Encoding: OBJ_ENCODING_RAW, OBJ_ENCODING_INT, OBJ_ENCODING_HT
        + lua
        + Refcount
        + Ptr: point to real object
    + real oject
        + dict
            + ht[0]: daily access
            + ht[1]: rehash
            + every dictht contains dictEntry to resolve key hash conflict
            + dictEntry
                + key: sds
                + value: redisObject
            + real object for set or hash
        + sds 
            + simple dynamic string
            + len: store lenght of string, get length is O(1)
            + buf: dynamic extendable, binary safe
            + real object for string, bitmap, hyperloglog
        + ziplist
            + zlbytes: total bytes
            + zltail: bytes from tailer to header
            + zllen: number of block
            + entry: entry 1 to entry n
                + prevRawLen
                + len: current node length
                + entryData
            + zlend: 255 to indicate end of tailer
            + relocate the space if ziplist growth too fast, good for small item and small list size
            + real object for
                + geo
                + hash
                    + hash_max_ziplist_entries 512
                    + has_max_ziplist_value 64
                + zset
                    + zset_max_ziplist_entries 128
                    + zset_max_ziplist_value 64
        + quicklist
            + quicklist + quicklistnode
            + double linked list
            + head/tail
            + count
            + len: ziplist node number
            + compress: LZF compress algo
            + real object for list
        + zskiplist
            + sorted collection, zset, geo
            + zskiplist + zskiplistnode
            + zskiplistnode has reference to other zskiplistnode
            + real object for geo, zset that number is greater than threshold 
***


## Redis Protocal
#### RESP
- Redis Serialization Protoal
- binary safe for client-server, binary safe means no parse operation to manipulate binary data
#### Request & Response Model
- Ping-Pong
- Pipeline: one request, multiple response
- Pub/Sub
    + client sub channel
    + server push data to channel
#### Request & Response Format
- request format
    + inline command, e.g. comand param1 param2
    + Array command, e.g. mget $k1 $k2
- response format
    + string
        + start with "+"
        + e.g. +OK\r\rn
    + error
        + start with "-"
        + -UNKNOW
    + integer
        + start with ":"
        + e.g. :1000
    + bulk strings
        + start with "$"
        + e.g. "$6\r\nfoobar\r\n", length is 6
    + list
        + start with "*"
        + e.g. "*3\r\n:1\r\n:2\r\n:3\r\n", return list of 3 items, "-1\r\n" is empty list
#### Java Libraries
- Jedis: simple
- Redisson: seperation read and write
#### Architecture
![arch](./src/test/resources/redis_arch.png)
- Event Handling (Blue)
    + eventLoop
        + time event
            + time event linked list
                + one time event
                + perodic event
            + serverCron
            + moduleTimerHandler
        + file event
            + server listen coresponding event via open socket, below is Demultiplexer impelmentation
                + Evport, Solaries
                + Epoll, Linux
                + Kqueue, Unix
                + Select
            + Dispatcher event to event handler
                + collec event and then put into firedEvents
                + EPOLLIN to AE_READABLE
                + EPOLLOUT to AE_WRITABLE
            + Event Handler
                + new connection
                    + acceptCommandHandler, maximum handle 1000 connection one time
                    + create client and register
                + request arrived
                    + readQueryFromClient to read data into client's query buffer
                    + parse command
                    + get redisCommand from command table
                    + execute command via redisCommand.proc()
                    + write response into write buffer
                + response echo
                    + sendReplyToClient to handle response
                    + flush client write buffer to IO
            
    + client
        + maintain client status 
    + redisCommands
        + commands parse, validation and execution
- Storage       
   + redis contains multiple DB, each DB is redisDB structure
   + all data stored in one of eight data type
   + persist
        + aof: append to buffer, buffer write into file
        + rdb: snapshot of memory
   + bio thread handling blocking IO operation, close(), fsync()
   + eviction pool to garbage collection for expired keys  
- Extendable Plugin
    + RedisModule to handle system extendsion
    + support cluster, master-slave
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
+ persist data time interval by auto or manual
+ high performance but may loss data in HA
+ fork the sub process with write-on-copy tech to create single file backup
+ trigger point
    + master-slave
        + load rdb file to restore the data
    + auto save
        + shutdown
        + flushall
    + manual save
        + save: blocking save
        + bgsave: fork thread to save
    + redis servercron schedule task to execute save

```
#### save "" to disallow RDB
save 900 1 # after 900 sec, at least 1 keys change
save 300 10, after/ 300 sec, at least 10 keys change
save 60 10000, after 60 sec, at least 10000 keys change

stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
```
+ rdb file
    + version
    + rd0, rd1.., rdx
        + db-header
        + data
    + tailer
        + lua
        + magic
        + chksum
#### AOF
+ persist data by by appending log
+ append to log file before shutdown
+ aof in multibulk format
+ aof write op into buffer, then flush buffer into aof based on policy
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
+ aof rewrite
    + bgwrite by manul
    + auto-aof-rewrite by auto
        + sub thread rewrite aof to temp file
        + sub thread notify main thread done
        + main thread rewrite aof to temp file
        + replace temp file to aof file
#### AOF vs RDF
- sequence
    - if aof exist, load aof
    - otherwise, load rdf
- implementation
    + rdf: snapshot of data
    + aof: records every operation
- RDB
    + pos
        + higher performance, master thread does not participant IO
        + faster bootstrap
    + cons
        + rdf may lost data due to frequence
        + fork may takes longer time if data size is huge
- AOF
    + pos
        + safe in data backup
        + if aof file is too big, rewrite the file to minimize file size
        + human readable 
    + cos
        + size is bigger
        + aof may slow than rdb
- hyper RDB + AOF
    + config aof-use-rdb-preamble
    + fork sub thread to execute bgrewriteaof
        + write RDB binary into aof
        + append cmd into aof
     + main thread replace old aof with new aof
  
***        
        
  
## Redis Master Slave
#### Separation of Read & Write
- Master
    + write data
    + config: slaveof no one
- Slave
    + read data
    + slave may have many slaves
    + slaveof master_host master_port, e.g. slaveof localhost 6379
- Synchronization
    + SYNC
        + before redis2.8
        + full sync via rdb + repl buf
        + master echo fullresync to slave
        + master execute BSSAVE to generate RDB
        + master send the RDB and repl buf to slave
        + slave receive RDB and create its own RDB
        + slave clean up old data, load RDB and then connect to master
        + slave receive aof from master and write to its own aof
    + PSYNC 
        + after redis2.8
        + increamental sync via repl buf
        + master and slave maintains replication offset
        + master write data into replication backlog, default size 1M
        + master has replId (itself) and slave has replId (master Id)
        + slave connect to master to send the psync command with replId and offset
        + master received slaveof command, slave compare master ID
            + if replId is same and offset still in replication backlog, master will send command after offset to complete increament sync
            + if replId is differnt or offset is not in replication backlog, master echo fullresync to slave to start full sync
            + repl-disable-tcp-nodelay
                + set to yes, delay in sync but save bandwidth, default is 40ms
                + set to no, less delay but increase bandwidth
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
- client sharding
    + client decide value store into which cache node
    + module hash or consistent hash
    + client connect redis based domain for DR or scale up/down
    + not friendly in resharding
- proxy sharding
    + client connect to proxy
    + proxy hash key and request different redis node, then return response to client
    + Twemproxy
        + simple, stable
        + not efficient in multi key
        + no support scale up/down
    + Codis
        + codis-server is extendable from redis
        + codis-client is proxy
        + zookeeper keep the cluster meta info
        + codis-dashboard monitor redis node and proxy
        + may performance downgrade 5% - 10%
- native redis cluster
    - redis cluster only after 3.0
    - contains 16374 slot
    - easy to scale up
    - resharding by addslots, delslots
    - redis node contains communicate other redis node via gossip protocal
    - client
        + client send request to one redis node
        + redis node parse command and allocate slot via crc16(key) & 0x3FFF
        + redis node return result if slot is in local
        + redis node return host/port and client redirect request to correct redis node
    - scale up
        + cluster meet $host $port
        + cluster setslot $slot importing $desNodeId
        + cluster setslot $slot migrating $srcNodeId
        + cluster getkeysinslot $slot $count
        + migrate $host $port $key $dbid timeout
    - migrate slot block IO in source node
        + may result redis pause
        + no impact to other slot
#### Consistent Hash
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
#### When key clean up
- serverCron
    + check expire dict to pick 20 key to check expire, clean up if key expire
    + if more than 5 key expire, continue pick up samples
    + if less than 5 key expire, stop clean up
- hits memory threshold based on maxmemory setting
    + clean up the key based on policy
#### Eviction Method
- sync
- async
    + used if lazyfree_lazy_expire configured
    + clean up by BIO thread 
    + collection size greater than 64
#### Eviction Policy
- noevicition
    + default policy
    + hit memory threshold
- volatile-lru (least recently used)
    + expire dict to calculate longest idle key
    + put longest idle key to eviction pool
- volatile-lfu (least freqently used)
    + expire dict to calcualte least used key in certain period
    + put least used key into eviction pool
- volatile-ttl
    + expire dict to calcualte earliest expire key
    + put earliest expire key into eviction pool
- volatile-random
- allkeys-lru
- allkeys-lfu    
- allkeys-random
- maxmemory-policy noeviction
***
 
 
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
![design](./src/test/resources/sec_sale.png)
- CDN: web page or javascript preload
- user cache: stored black listed user or zombie user based on history
- access cache: inaccurate access request to second sale
- product cache: sec sale product
- inventory cache: atomic operation to check and decr inventory
- application verify order info and push order into MQ
- application listen order info from queue and persist into DB
#### Redis Design
- skuId_start: indicate if sec sale start
- skuId_count: product inventory, require atomic operation
- skuId_acdess: request to buy the product, number of request is limiting by rounding number, atomic operation does not require
- bloom filter
    + verify if user place order second times
***
    