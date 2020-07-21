### 一款不错的选词填空题的自定义view，样式可以自己定制。
![simple](https://github.com/xiangcman/BlankView/blob/master/images/simple.gif)

### 使用:
- 如果想直接有流失布局的选项样式直接用:
```
<com.xc.blank.BlankRootView
    android:id="@+id/blank_root_view"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```
如果想自己定义选项的view那你可以直接定义BlankView:
```
<com.xc.blank.BlankView
    android:id="@+id/blankView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginLeft="@dimen/dp_23"
    android:layout_marginTop="20dp"
    android:layout_marginRight="@dimen/dp_16"/>
```
后期会考虑加入自定义属性!!!

如果想第一时间看demo效果扫描下面二维码:

![image](https://upload-images.jianshu.io/upload_images/2528336-6141c1e15ee83177?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
