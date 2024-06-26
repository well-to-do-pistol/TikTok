TikTok(kotlin):

零:

> 1.加载动画用progressbar
>
> 2.edittext的setError来判断字符串符合规则
>
> 3.用ActivityMainBinding来绑定xml(每创建一个activity自动生成一个binding )
>
> 4.用Glide库显示图片(直接访问视频uri), 直接用circlecrop就能显示圆图
>
> 5\. 使用firebase ui库,
> 设置FirestoreRecyclerAdapter\<VideoModel,holder\>,
> 直接检索数据库拿到对象进行显示;
>
> 6\. 使用VideoView显示视频, 放在adapter里,
> 在(安卓x)ViewPager2(垂直)里设置adapter实现下滑视频
>
> 7.分割线用MaterialDivider(), google-material库
>
> 8.右击app new一个image assets改变路径设置app图标

1.  登录注册(FirebaseAuth邮箱密码服务):

```{=html}

```

1.  用CloudFirestore, 保存users, 里面有follower和following列表

```{=html}

```

1.  主页面

```{=html}

```

1.  底部导航栏用GoogleMaterial的bottomnavigation中放menu

```{=html}

```

2.  VideoUploadActivity

```{=html}

```

1.  分SDK等级获得并检查权限

2.  设定ActivityResultLauncher,
    用自带mediastore启动ActivityResultLauncher, 获得视频

3.  用CloudFirestore, 保存videos,
    用storage保存videos/视频mp4(名字是uri的lastPathSegment)

```{=html}

```

3.  ProfileActivity

```{=html}

```

1.  用Intent的putExtra传数据(id)

2.  Logout, FirebaseAuth的signout, clear和new的TASK加进Intent.Flag

3.  设定ActivityResultLauncher,
    用自带mediastore启动ActivityResultLauncher, 获得图片.
    用storage保存profilePic图片

4.  直接改整个model重新查询更新ui(其他是onDataChanged)

5.  使用firebase ui, 设置FirestoreRecyclerAdapter\<VideoModel,holder\>,
    直接检索数据库拿到对象进行显示视频图片(分三格)

```{=html}

```

4.  SingleVideoPlayerActivity(用来在profile点击图片单独显示视频)

```{=html}

```

1.  使用VideoView显示视频, 放在adapter里,
    在(安卓x)ViewPager2(垂直)里设置adapter(只检索了一个视频)
