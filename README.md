# MediaPicker
[![](https://jitpack.io/v/xushihai/MediaPicker.svg)](https://jitpack.io/#xushihai/MediaPicker)

# 介绍
高仿Android版微信的图片和视频选择器。

# 集成库
Step 1. Add the JitPack repository to your build file 
```sh
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```sh
	dependencies {
	        compile 'com.github.xushihai:MediaPicker:v1.1'
	}

```
# 使用教程

```sh
 GalleryFinal.selectMedias(this, 10, new GalleryFinal.OnSelectMediaListener() {
            @Override
            public void onSelected(ArrayList<Photo> photoArrayList) {
                
            }
        });
```
```sh
    使用EventBus3.0注册，接收选择好的图片和视频列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendMedia(ArrayList<Photo> photoList) {
        Log.e("多媒体", photoList.toString());
    }
    
    GalleryFinal.selectMedias(this, 10);
```

使用了v7兼容包，EventBus,Glide，RecyclerView组件。如果项目中包含了这些组件可以使用exclude将这几个组件排除。
```sh
 exclude group: 'com.android.support', module: 'appcompat-v7'
 exclude group: 'com.android.support', module: 'recyclerview-v7'
 exclude group: 'org.greenrobot', module: 'eventbus'
 exclude group: 'com.github.bumptech.glide', module: 'glide'
 ```
 
 # 截图
 [![N|Solid](https://github.com/xushihai/MediaPicker/blob/master/shotcuts/device-2017-03-20-112104.png)]
[![N|Solid](https://github.com/xushihai/MediaPicker/blob/master/shotcuts/device-2017-03-20-112114.png)]
[![N|Solid](https://github.com/xushihai/MediaPicker/blob/master/shotcuts/device-2017-03-20-112128.png)]
[![N|Solid](https://github.com/xushihai/MediaPicker/blob/master/shotcuts/device-2017-03-20-112139.png)]
