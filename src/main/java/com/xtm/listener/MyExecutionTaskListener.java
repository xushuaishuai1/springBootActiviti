package com.xtm.listener;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;

/**
 * 节点监听
 */
public class MyExecutionTaskListener implements ExecutionListener,TaskListener {
    private static final long serialVersionUID = 7960356497099642910L;
    private static ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        System.out.println("111111111111111111111111111111111111111");
    }

    @Override
    public void notify(DelegateTask delegateTask) {

        System.out.println("数据库中的taskId主键="+delegateTask.getId());
//        System.out.println("任务名称="+delegateTask.getName());
//        System.out.println("任务名称="+delegateTask.getName());
//        System.out.println("获取任务的描述信息="+delegateTask.getDescription());
//        System.out.println("任务处理的优先级范围是0-100="+delegateTask.getPriority());
//        System.out.println("获取流程实例id="+delegateTask.getProcessInstanceId());
//        System.out.println("获取执行id="+delegateTask.getExecutionId());
//        System.out.println("获取流程定义id="+delegateTask.getProcessDefinitionId());

        //void setName(String name);修改任务名称
        //void setDescription(String description);修改任务的描述信息
        //void setPriority(int priority);修改优先级
        //void addCandidateUser(String userId);Adds the given user as a candidate user to this task.
        //void addCandidateUsers(Collection<String> candidateUsers);添加候选人
        //void addCandidateGroup(String groupId);添加候选组

        //这边设置的是下一个环节审批人，优先级高于启动事件和完成事件设置
        delegateTask.setAssignee("提交审核");//指定办理人

        String eventName = delegateTask.getEventName();
        if ("create".endsWith(eventName)) {
            System.out.println("create=========");
        }else if ("assignment".endsWith(eventName)) {
            System.out.println("assignment========");
        }else if ("complete".endsWith(eventName)) {
            System.out.println("complete===========");
        }else if ("delete".endsWith(eventName)) {
            System.out.println("delete=============");
        }
    }


}


