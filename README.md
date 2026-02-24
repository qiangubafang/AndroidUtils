# AndroidUtils

AndroidUtils 功能库 <br/>
作者： 千古八方<br/>
地址： https://github.com/qiangubafang/AndroidUtils<br/>
简述： 该功能库为android 7.1.2 智能终端常用功能的集合，同样适用于手机端。<br/>
文档： https://rangotec.com/blog/177.html<br/>

Gitee部分内容看不到， 迁移回Github。<br/>
本代码完全开源不受任何版权、权力约束！！！可用于任意用途！！！<br/>

### 目录说明： 
```
 src/main/java-public        应用程序代码 <br/>
         /res-public         应用程序资源路径 <br/>
         /jni-libs-public    应用程序引用的native libs 路径 <br/>        
         /java-utils         工具功能常用代码 <br/>
         /res-utils          工具功能资源路径
         /jni-source-utils   native 源码 <br/>
```            
           
### 工具功能列表
#### websocket 功能
#### 蓝牙GATT通信，自动组装包。
BTUtil.java
```agsl
        // 二次组装数据结构，可选。
        PacketUtil packetUtil = new PacketUtil((byte) 0x68, (byte) 0x55, (byte) 0x16, 512);
        btUtil = new BTUtil(null,
                "0000fff1-0000-1000-8000-00805f9b34fb",
                "0000fff2-0000-1000-8000-00805f9b34fb",
                1000,
                packetUtil);
        btUtil.setDEBUG(true);
        btUtil.setActivity(this);
        
        btUtil.searchBT(); // 搜索并连接蓝牙
        btUtil.addObserver(cb); // 接收信息
        btUtil.sendData(bytes); // 发送信息
        
```
#### AES加密
AESEncryptUtil.java

#### 图片模糊工具
BlurBuilder.java
#### 相机工具
CameraUtils.java
#### 校验和工具
CRC16.java 
#### 16进制工具
Hex.java HexDump.java
#### https 证书
HttpsUtils.java
#### 日志工具，http日志工具
HttpLogger.java
#### HttpLogger 日志
``` MyAndroidLogAdapter.getInstance();
    HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLogger());
    logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client = new OkHttpClient.Builder()
    .cookieJar(cookieJar)
    .addInterceptor(new HttpLogInterceptor(true))
    .addNetworkInterceptor(logInterceptor)
    .build();
    HttpApi.setClient(client);
    HttpApi.setDebug(true);
    Logger.e("here");
    Logger.i("here");
    Logger.i("here----");
```
#### 权限工具类
PermissionHelper.java
#### 打开系统设置功能
SettingHelper.java
#### 网络工具类，包含cookie（内存及硬盘的持久化）
HttpApi.java 
#### 沉浸式工具 
ImmerseUtil.java
#### 执行shell命令工具
ShellUtils.java
#### Wifi 工具, 搜索、打开、连接等
WiFiUtil.java
#### 验证用的正则表达式
Validator.java
#### 生成随机数的工具类
RandomUtils.java
#### 应用升级工具类
UpdateUtil.java
#### 系统栏提示通知的工具类
Notify.java
#### 获取手机想看的信息
PhoneInfo.java
#### 相机工具类
CameraUtils.java
#### 相机预览类
CameraSufaceView.java
#### 贝塞尔曲线相关
DotsTextView.java // 贝塞尔曲线实现的等待点动画，
BeizerEvaluator.java // 贝塞尔曲线计算工具
效果示例： Android widget - 直播右下角点击刷礼物特效 https://rangotec.com/blog/60.html

#### 侧滑消失的行为，列表里的某个条目，侧滑删除
MySwipeDismissBehavior.java
#### 可展开列表数据适配器
ExpandableRecyclerViewAdapter.java
#### 网页浏览器
WebViewFragment.java
#### 捕获应用崩溃信息用 
CrashHandler.java 

#### dialog
LoadingDialog.java
MsgDialogUtil.java
### 自定义的widget挂件
#### Android widget - 完全可配的炫酷仪表盘
GangeView.java 
效果展示： https://rangotec.com/blog/57.html
#### 雷达搜索图，根据信号强度，显示点的远近
RadarView.java
效果展示： https://rangotec.com/blog/59.html

#### 九宫格选择图片
```
安卓11 之后 manifest 根节点下需要添加：
    <queries>
        <intent>
            <action android:name="android.media.action.IMAGE_CAPTURE" />
        </intent>
    </queries>
```

SelectPictureGridView.java 
#### 选择一张图片工具类
SelectOnePicture.java    

#### 去掉色彩，布局变成灰色的工具
StaturationView.java    
```agsl
    // 使用方法 0:全灰色， 1:全彩色
    SaturationViewUtil.getInstance().saturationView(devContainer, 0f);
```

#### 底部弹出选择列表
BottomListDialog.java   
#### 从左滑动到右侧退出的布局
DragLeft2RightExitFrameLayout.java 
#### 圆形头像； Glide 也可以实现
CircleImageView.java  
#### 左上角斜着的标签样式
LabelView.java  


### 串口|RS485
1. 全双工，串口通信 SerialPort.java， 使用jni打开设备节点，返回文件描述符，进而获取输入输出流。
用法:<br/>
```
    // 全双工，通常用法
    new SerialPort(new File("/dev/ttyS0"), 9600, 0, null);
    
    // 收发前需要额外使能某个节点的用法
    String enable = "/dev/gpio68"; //  手动使能某个节点 system(echo 1 > /dev/gpio68)
    new SerialPort(new File("/dev/ttyS0"), 9600, 0, enable);
    
```
2. RS485SerialPortUtilNew.java 485串口通信。
   如果要使用232功能， 使用 SerialPort.java 类即可。 
   获取到输入输出流，然后实现流操作即可。 （硬件自动使能收发） <br/>
    * 使用示例：
    *    普通串口                     open(9600, "/dev/ttyS1", null, false);                        // 有单独芯片控制自动使能收发；
    *    修改过硬件的设备              open(9600, "/dev/ttyS1", “/sys/class/gpioXXX/value" , false);  // 可以控制gpio， 但无法改内核（如没有源码）
    *    修改过硬件并可以修改内核的设备  open(9600, "/dev/ttyS1", “/dev/gpioXXXX" , true);              // 内核控制使能收发
    *
  
  // ----------------------详细说明 ---------------
  程序读发送寄存器，保证发送完成后，再使能接收<br/>
  1. 如果内核有gpio的驱动则使用ioctl <br/>
  2. 如果内核没有驱动则使用system(echo > 1 /sys/class/gpio/value)来设置
  3. 如果带有自动转换的芯片，则使能路径传null （大多数情况下，成品设备都是这种情况）

3. 该功能与 RS485SerialPortUtilNew.java 的区别是，该类只读。

### USB串口
使用 SerialInputOutputManager.java， 复制自：https://github.com/mik3y/usb-serial-for-android    
    
### 控制状态栏、导航栏的显示隐藏
该功能需要修改android代码, 具体方式见 /framework-modify/动态控制显示隐藏导航栏和状态栏/<br/>
```
    // 隐藏导航栏及状态栏
    sendBroadcast(new Intent("android.intent.action.HIDE_NAVIGATION_BAR"));
    sendBroadcast(new Intent("android.intent.action.HIDE_STATUS_BAR"));
    // 显示导航栏及状态栏
    sendBroadcast(new Intent("android.intent.action.SHOW_NAVIGATION_BAR"));
    sendBroadcast(new Intent("android.intent.action.SHOW_STATUS_BAR"));
      
```
### 动态权限申请
一次请求单个或多个权限
```
    // 1. 申请的权限必须先在manifest中配置， 否则申请结果总是失败
    // 2. ACCESS_FINE_LOCATION 权限包含 ACCESS_COARSE_LOCATION 粗略定位权限
    // 3. 按照权限请求顺序进行申请，遇到失败则返回，之后权限则处于未申请状态: -1
    String permissions[] = new String[]{ Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION};
    int requestCode = 7777;
    PermissionHelper.request(TCMainActivity.this, permissions, requestCode,new PermissionHelper.Callback(){

        @Override
        public void onResult(int requestCode, String[] permissions, int[] grantResult) {
            if(PackageManager.PERMISSION_GRANTED == grantResult[0]){
                // 第一个权限 android.permission.CAMERA 授权成功
            }
            // 授权结果
            Toast.makeText(TCMainActivity.this, "请求权限：" +Arrays.toString(permissions) + "  授权结果:" + Arrays.toString(grantResult), Toast.LENGTH_LONG).show();
        }
    });
```
### server.zip 应用内提供php服务的工具
### pdfjs    pdf 预览工具