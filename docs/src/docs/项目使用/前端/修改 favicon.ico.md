# 修改 favicon.ico 

## 方法一

打开控制端/管理/系统/网站设置，找到网站图标，填写图标路径

![image-20250709140251177](./assets/image-20250709140251177.png)




## 方法二

**第一步：** 将图标转换为 `favicon.ico` 格式，可以在这个网站转换：https://www.bitbug.net/

**第二步：** 打开前端项目，找到根目录中的 `public` ，把刚刚转换后的图标  `favicon.ico`  上传到这里。

随后打开 `src/layout.tsx` 文件找到如下代码

```html
<link rel="icon" href={data?.favicon || '/favicon.ico'} />
```

注释或替换为

```html
<link rel="icon" href="/favicon.ico" />
```

![image-20250709135416413](./assets/image-20250709135416413.png)

**第三步：** 重新项目打包并将 `public` 目录上传到服务器