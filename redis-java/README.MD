# Redis

## Redis Overview
#### Overview
- NoSQL Not Only SQL, not relation database
- Relationship database not efficient 
    + High concurrency
    + Huge storage
    + High scalability & High Availability
- Product
    + Redis
    + MongoDB
- Storage
    + Key-Value
    + Row
    + Documents
    + Graph
- Feature
    + easy to scale
    + free data model
    + high currency and huge storage
    + high availability
    + base on memory
    + single thread
    + IO: base on NIO
        + [select and pull] linux 2.6, loop the file description
        + [epoll] linux 2.6+, ae_epoll.c only loop the read/write socket
- Memcache
    + multithread + lock
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
$ wget http://download.redis.io/releases/redis-5.0.5.tar.gz
$ tar xzf redis-5.0.5.tar.gz
$ cd redis-5.0.5
$ make
- update redis.conf 
    + change daemonize from no to yes
#### Command
- ping
- shutdown
- redis-cli shutdown
#### Database
- default 16 database, default is 0 database
- select <i> to switch database
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
    + select <i>, i is database index
    + dbasize, max key size is 512MB, handle 2^32 keys
    + flushdb, clear current database
    + flushall, clear all database
    + keys *
    + expire key timeout
    + ttl key, time to expire
    + exists key
    + key status: -1 forever, -2 expired
- String:
    + set/get/del
    + set key value, reset expire to -1
    + getset key value, replace one value
    + get key
    + del key
    + append key value
    + strlen key
    + setnx key value, set the key if does not exist
    + mset/mget/msetnx
    + getrange key start end, index start from 0, end index is -1 is last index
    + setrange key offset, set the value from offset
    + setex key expire value, set value and exipre
- Number:
    + set key value
    + incr/decr key, atomic operation
    + incrby/decrby key value, atomic operation
- List
    + double linked list, fast in header and tailer, pool performance based on index
    + lpush/rpush list [val...], lpush add from header, rpush add from tailer
    + lpop/rpop list, if no element in list, list will be destoryed 
    + llen list_name
    + lpushx list [val...], insert only list exists
    + lrem list <num> [val]
        + positive number, delete number of value from header
        + negative number, delete number of value from tailer
        + 0, delete all number of value in list
    + lset list index value, set the value base on index, index starting from -1
    + linsert list before [val1] [val2], insert val2 before val1
    + lrange list start end
    + lindex list index
- Set
    + no duplicated elements
    + sadd set [val...]
    + srem set [val...]
    + smembers set
    + sismember set [val], check if contains value
    + sdiff set1 set2, element in set1 but not in set2, diff set
    + sinter set1 set2, element in set1 and set2, interset
    + sunion set1 set2, all elements
    + scard set, return number of elements
    + sdiffstore | sunion
    + spop, pop element by random
    + srandmember, pick element by random, not delete element in set
- Hash
    + Map, key-value collection
    + hset hash key value
    + hget hash key 
    + hgetall hash
    + hdel hash key
    + hincrby/hdecrby hash_name key value 
    + hexists hash key
    + hlen hash
    + hkeys hash
    + hvals hash
- Sorted Set
    + score defined order
    + zadd <set> [score value, ...]
    + zscore <set> val, 
    + zrem <set> val
    + zrange <set> [start end] [withscores], min before max
    + zrevrange <set> [start end] [withscores]
    + zrangeby[rank|score] <set> score1 score2
    + zincrby <set> <score> <val>
***


## Redis Config
- protected-mode: if access by other ip
- tcp-backlog: client connection queue size
- tcp-keeplive: client connection live timeout
- database: number of database
***


## Redis Transaction
- atomic operation in multi if error in composite command
- not atomic operation in exec if error in execution command
- not advice for rollback
- Transaction
    - watch <key>
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
- RDB Persist
    + persist data time interval
    + RDB conf in "save" session
        + save 900 1, after 900 sec, at least 1 keys change
        + save 300 10, after 300 sec, at least 10 keys change
        + save 60 10000, after 60 sec, at least 10000 keys change
    + high performance
    + single file backup, fork the sub process with write-on-copy tech
    + may loss data in HA
    + config
        + stop-writes-on-bgsave-error yes
        + rdbcompression yes
        + rdbchecksum yes
    + load rdb file to restore the data
- AOF
    + "appendonly yes" in conf file
    + "appendfsync" define the append sequence, value is  [always|everysec|no]
    + "auto-aof-rewrite-percentage": rewrite file compare to last file
    + "auto-aof-rewrite-min-size": min file size
    + persist data by by appending log
    + syn by per operation
    + append to log file before shutdown
***        
        
  
## Redis Cluster
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
- Sentinel
    + sentinel.conf: sentinel monitor mymaster 127.0.0.1 6379 1
    + redis-sentinel sentienl.conf
- Cluster
    + at least 3 master, 3 slave
    + conf
        + cluster-enabled yes
        + cluster
***


## Distributed Lock
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
        
