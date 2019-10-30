### 项目描述
用 Fragment 代替 Activity，每个 Activity 会维护自己的 Fragment 栈<br>
功能包含：栈管理、onSaveInstanceState、onRestoreInstanceState、startFragmentForResult、四种启动模式、
onNewIntent、自定义出入动画、监听 Fragment 生命周期、监听 Fragment 出入动画结束


### 使用
##### 1. XXXActivity extends OneActivity
##### 2. 提供一个初始的 Fragment

````
protected abstract BaseFragment getDefaultFragment();
````

##### 3. 选择性重写 getLayoutId 和 getContainerId

````
protected int getLayoutId()
protected int getContainerId()
````
getLayoutId() 是为 Activity 设置布局，当然可以不重写，就是代表不设置布局<br>
getContainerId() 是指定哪块区域用来替换 Fragment，不重写的话，默认是 android.R.id.content<br>
所有的 startFragment 都是把 Fragment 放在 container id

##### 4. startFragment

````
FragmentIntent intent = new FragmentIntent(this, PlayFragment.class);
intent.putExtra("key", "value");
//进入动画和当前栈顶的 Fragment 的退出动画
intent.setEnterAnim(R.anim.slide_in_bottom, 0);
//退出动画和即将显示的 Fragment 的进入动画
//回退 (finish) 的时候生效，若是因为 startFragment() 被压入栈，则生效的是目标 Fragment 的动画配置
intent.setExitAnim(R.anim.slide_out_bottom, 0);
startFragmentForResult(intent, 0);
````
<br>
