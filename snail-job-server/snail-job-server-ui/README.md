## 自定义前端指南

为了在项目中集成自定义前端界面，请按照以下步骤操作：

### 1. 创建前端目录

首先，在 [`snail-job-server-ui`]() 的 [`src/main`](src/main) 目录下创建一个名为 [`frontend`](src/main/frontend) 的新目录：

```
snail-job-server-ui/
└─ src/
   └─ main/
      └─ frontend/  # 新建的前端目录
         └─ snail-job-admin/  # 拉取的前端代码
```

### 2. 拉取前端代码

使用 Git 命令拉取前端代码库到 frontend 目录中：

```bash
cd src/main/frontend
git clone https://gitee.com/opensnail/snail-job-admin.git
```

> 注意：确保你在 [`frontend`](src/main/frontend) 目录中执行 `git clone` 命令，这样代码会被克隆到当前目录而不是创建一个新的子目录。

### 3. 自定义前端

在 [`frontend`](src/main/frontend) 目录中对前端代码进行必要的修改。你可以编辑文件、添加新文件或删除不必要的文件来满足你的自定义需求。

### 4. 打包程序

在 `Maven` 配置中找到 `skipFrontend` 选项，并确保其未被勾选，以便在打包时包含前端代码的修改，然后运行 `Maven` 打包命令来构建项目。
