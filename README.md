# 思潮博客
主要模块：用户模块、话题模块、博客模块、私信博客、OSS模块

使用技术：springBoot、mybatis-plus、springCloud、MySQL、Redis、rabbitMQ、Elasticsearch、webSocket、JWT
```
1、1、token续签与注销：续签拦截器拦截请求、AOP切面增强方法、ThreadLocal线程变量保存用户信息
2、变化数修改：redis缓存+定时任务落盘的实现（redisson实现分布式锁、lua脚本保证原子性）（自增自减、查询、落盘）
3、@用户与#话题#的实现、rabbitMQ消息可靠投递、向被@用户在私信中发送提示消息
4、博客缓存（redis操作）：综合博客（zset）与实时博客（zset）
	1）查询博客或评论缓存时，使用zSet类型保存排序列表（综合博客以评论数与点赞数之和为分值，实时博客与评论使用时间戳为分值）
	2）博客和评论的排序列表与信息要分开存储到redis中，排序列表设置一个长时间的生存时长，但是具体的博客与评论信息只需要设置一个短的生存时长，因为排序列表要一开始就知道，但是信息不需要立即知道全部，有些可能很难被浏览到，所以详细信息要随用随查。
	3）查询时，以指定的起始位置索引start为开头，根据升序或倒序先前或先后查询，（因为使用的是以创建时间的时间戳为分值的zSet类型，所以插入数据时，不会影响用户浏览）（start>=0表示 以start为起始索引查询 ，start==-1表示对于当前用户数据以确保查出，start==-2表示当前用户第一次查询数据，根据key的size-1为起始索引查询）
	3）发布博客或评论是，在对应的实时博客或实时评论的排序列表上以时间戳为分值插入id
	4）删除博客或评论时，不能直接删除排序列表中的value，不然会改变zSet的结果，出现查询出重复数据问题。（先查询该value的分值score，再删除该value，在以delete+value值为值score为分值保存数据到zSet中，查询时如果id是delete开头的不去查询数据，而后通过查询多拿1位数据，保证拿满limit条目数）
5、feed流：推拉结合（在线推，离线拉），使用aop+redis的zSet以时间戳为分值维护用户在线列表，缓存每个用户的关注列表、粉丝列表、发件箱、收件箱
6、消息模块：websocker实时通信+rabbitMq实现session共享
7、使用Elasticsearch实现搜索博客
```

### 用户模块

##### 1.用户中心

注册、登录 、根据token信息获取用户信息（密码除外）、注销、修改密码

根据用户id查看用户信息（密码除外）、修改头像url、修改用户个人信息（头像、密码除外）
![图片](https://github.com/jzmr/sichao_parent/assets/81701868/80f465bb-905e-4954-bccd-1eaef28ec712)

##### 2. 用户关注

关注用户、取关用户、查看当前用户是否关注某位其他用户

分页倒序查看用户关注列表、分页倒序查看用户粉丝列表

查询当前用户关注的用户昵称列表、获取用户关注列表、获取用户粉丝列表
![图片](https://github.com/jzmr/sichao_parent/assets/81701868/b8773067-d59c-46db-bc25-57c41353eae7)

### 话题与博客模块

##### 1.话题模块

发布话题、查询热门话题（热搜榜）、获取某个话题的信息
![图片](https://github.com/jzmr/sichao_parent/assets/81701868/ff9e802d-be1a-422a-af2a-2133042f08db)

##### 2.博客模块

发布博客、根据博客id获取博客信息、删除博客

分页查询指定话题id下的博客、分页查询指定话题id下的实时博客、查询用户博客、查询我的关注用户的博客、根据关键字全文检索博客

发布评论、删除评论、分页查询指定博客id下的评论（升序或降序）

点赞博客、取消点赞博客
![图片](https://github.com/jzmr/sichao_parent/assets/81701868/74ac98a0-2bad-41de-9187-b2c4980afe16)
![图片](https://github.com/jzmr/sichao_parent/assets/81701868/1dfb1fca-323a-43a1-9540-91ee0c4ea041)

### 私信博客

加载用户聊天列表、加载聊天记录、当前用户发送消息给目标用户、查询聊天列表项及聊天消息列表
![图片](https://github.com/jzmr/sichao_parent/assets/81701868/330ba3e0-68c1-4796-98b5-cc360acbbed7)
![图片](https://github.com/jzmr/sichao_parent/assets/81701868/0c0f1e93-5596-43eb-a52c-62bc128de746)

### OSS模块

上传头像、上传博客图片
