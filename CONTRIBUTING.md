
<p align="center">
  <a href="https://www.easyretry.com">
   <img alt="snail-job-Logo" src="doc/images/logo.png">
  </a>
</p>

# Contributing to snail-job
> 欢迎来到 snail-job！本文档是有关如何向 snail-job 做出贡献的指南。如果您发现不正确或遗漏的内容，请留下评论/建议。
> 希望各位热爱开源的同学来共同维护 snail-job，让 snail-job 变的越来越好。

## Before you get started

### 设置您的开发环境

You should have JDK 1.8 or later installed in your system.

## Contributing

我们总是很高兴收到贡献，无论是拼写错误、错误修复还是重要的新功能。请随时提出问题或发送拉取请求。

我们非常重视文档以及与其他项目的集成. 我们非常乐意接受这些方面的改进.


## How can I become a Committer
需要你对开源有热情，技术扎实，有一颗用爱发电的心。
你需要加入snail-job的社区群，如何加入请参考[加入社区讨论]，然后私聊我们，表示有意成为开发者。
你需要熟读源码，理解核心代码的逻辑。
然后可以去issue页 (opens new window)查看issue列表并进行认领。
或者你有相关建议自己进行创建issue并进行修复也是可以的。
在相关issue下回复“认领”就可以了，然后私聊告诉我。如果已经有相关同学已经认领了，请不要重复认领。
完成3个issue并成功通过审核并合并，我会邀请你成为Committer。


## Gitee workflow
snail-job的PR统一在Gitee平台上进行提交。
我们使用 dev 分支作为开发分支，这表明这是一个不稳定的分支.

以下是contributors的工作流程:

1. Fork to your own
2. Clone fork to local repository
3. Create a new branch and work on it
4. Keep your branch in sync
5. Commit your changes (make sure your commit message concise)
6. Push your commits to your forked repository
7. Create a pull request

请确保 PR 有相应的问题。


#### Special Note

- 所有的PR提交到dev分支，这个分支为开发分支。
- 如果你作了功能性的变动，请带上你的测试用例，测试用例规范可以参考之前的测试用例。
- 所有的PR必须关联至少一个issue，如果没有相关issue，请自行创建一个。
- 正式提交PR之前，请确保所有的测试用例都通过。
- 提交信息，均要符合要求，下面有讲述。
- 创建 PR 后，将向拉取请求分配一名或多名审阅者。 审核者将审核代码.
- 在合并 PR 之前，请压缩任何修复审查反馈、拼写错误、合并和重新设置基础类型的提交。
最终的提交信息应该清晰简洁.


#### Open an issue / PR

We use [Gitee Issues](https://gitee.com/aizuda/snail-job/issues) and [Pull Requests](https://gitee.com/aizuda/snail-job/pulls) for trackers.

如果您在文档中发现拼写错误，在代码中发现错误，或者想要新功能，或者想要提供建议，
您可以通过[在 Gitee 上打开问题](https://gitee.com/aizuda/snail-job/issues/new) 进行提交。
请遵循问题模板中的指导消息。

如果您想做出贡献，请遵循[贡献工作流程](#gitee-workflow)并创建新的拉取请求。
如果您的 PR 包含较大的更改，例如组件重构或者新增组件，请写详细文档
关于它的设计和使用。

注意单个PR不宜太大。如果需要进行大量更改，最好将更改分开
一些个人 PR.

## Requirements for long-term Committers
- 需要对开源有热情，技术扎实，有一颗用爱发电的心。
- 理解核心代码逻辑。时刻保持学习的心。
- 需要每个月贡献至少1个PR并成功通过审核并合并。
- 需要参与社区群的建设，积极回答问题和进行宣传。

## Comments and Comment requirements
在文件头上，一定得有相关头注释信息，请按照规范，如下所示：
```
 /**
  * 这是你对这个类的描述，如比较长，多行也行
  * @author opensnail
  * @since 1.5.1
  */
 public class YourClass{
 	...
 }
```
相关重要代码，为了保证阅读性，也请加上必要的注释
提交的时候comment也要按照规范来填写：
```
#bug/feature/enhancement/ #issue号 这是你issue的中文描述
```
#### 示例：
enhancemnet #I7EIC7 增加路由剔除机制,提高节点的可达性


## Code review

所有代码都应由一名或多名提交者仔细审查. 一些原则:

- Readability（可读性）: 重要的代码应该有详细的文档记录. 符合我们的代码风格.
- Elegance（够优雅）: 新的函数、类或组件应该经过精心设计.
- Testability（可测试性）: 重要的代码应该经过充分的测试（high unit test coverage）.

## Community

### Contact us

#### Mailing list

如果您有任何疑问或建议，请到官网联系我们 https://www.easyretry.com/pages/bb982b/.

#### Gitter

Our Gitter room: [https://www.easyretry.com/pages/bb982b/](https://www.easyretry.com/pages/bb982b/).
