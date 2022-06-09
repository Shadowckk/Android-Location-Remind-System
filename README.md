# iRemind
一款基于Android的提醒系统，既可以通过用户设定的某个时间进行任务的提醒，也允许用户设定进入或离开某个地点的任务提醒。

## 〇、关于本项目
本文设计并实现了一款[基于Android的时间与位置服务提醒系统](https://segmentfault.com/a/1190000041962480)——iRemind，它既可以通过用户设定的某个时间进行任务的提醒，也允许用户设定进入或离开某个地点的任务提醒。
本项目源码位于：[https://github.com/Shadowckk/iRemind](https://github.com/Shadowckk/iRemind)

## 一、开发环境
Android Studio 版本：4.1.1
SDK版本：Android 11.0 (API 30)
数据库：SQLite

## 二、使用方法
### （1）创建百度地图SDK项目
1. 参考[注册和获取密钥](https://lbsyun.baidu.com/index.php?title=androidsdk/guide/create-project/ak)，获取开发密钥。
2. 在`AndroidManifest.xml`文件的相应位置填入你的密钥：
	```xml
	<meta-data
		android:name="com.baidu.lbsapi.API_KEY"
		android:value="XXXXX你的密钥XXXXX" />
	```
### （2）运行项目
1. 使用Android Studio打开本项目。
2. 建议使用真机调试，参考：[Android Studio如何进行真机调试](https://blog.csdn.net/qq_44489836/article/details/106318639)
3. 运行该项目。

## 三、系统功能
项目已实现的功能和未实现的功能如下：
1. 待办事项管理
	（1）添加待办事项：用户可点击主界面下方“添加任务”按钮添加待办事项。
	（2）删除待办事项：用户可将待办事项右滑以删除待办事项。
	（3）显示待办事项：在主界面可将所有待办事项显示，其中未完成任务排列于已完成任务之前。（使用适配器实现）
	（4）标记待办事项：用户可点击待办事项左侧复选框，将待办事项进行标记。
2. 提醒管理
	（1）基于时间的任务提醒：用户设定基于时间的任务提醒，使其在设定时间进行任务提醒。（使用Padding Intent实现）
	（2）基于位置的进入提醒：用户设定基于位置的进入提醒，使其在进入提醒范围时进行任务提醒。（使用百度地图定位SDK实现）
	（3）基于位置的离开提醒：用户设定基于位置的离开提醒，使其在离开提醒范围时进行任务提醒。**（未实现）**

## 四、系统完善与改进建议
1. 对于“基于位置的进入提醒”功能，使用[百度地图定位SDK位置提醒](https://lbsyun.baidu.com/index.php?title=android-locsdk/guide/addition-func/loc-alert)实现，调用BDNotifyListener的setNotifyLocation方法实现设置位置消息提醒。
	如果您要实现**“基于位置的离开提醒”功能**，可仔细阅读百度地图定位SDK相关源码，并在此基础上进行修改。或者您可以重写“基于位置的进入提醒”功能并改写“基于位置的离开提醒”功能。
2. 对于**“删除待办事项”功能**，可增加确认删除提醒，因为右滑删除存在误操作可能性。
3. 对于“基于位置的提醒”功能，选择地点时可增加搜索功能。
4. 可增加数据同步与恢复功能。
5. **系统的后台运行存在问题，亟待解决。**

## 五、项目效果
1. 用户主界面模块
	![用户主界面模块.gif](https://github.com/Shadowckk/Mask-face-recognition/blob/main/用户主界面模块.gif)
2. 新建待办事项模块
	![新建待办事项模块.gif](https://github.com/Shadowckk/Mask-face-recognition/blob/main/新建待办事项模块.gif)
3. 位置服务模块
	![位置服务模块.gif](https://github.com/Shadowckk/Mask-face-recognition/blob/main/位置服务模块.gif)
4. 时间提醒服务模块
	![时间提醒服务模块.gif](https://github.com/Shadowckk/Mask-face-recognition/blob/main/时间提醒服务模块.gif)
5. 位置提醒服务模块
	![位置提醒服务模块.gif](https://github.com/Shadowckk/Mask-face-recognition/blob/main/位置提醒服务模块.gif)
