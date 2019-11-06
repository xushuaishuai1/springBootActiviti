package com.xtm.listener;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * 全局监听,线上也可以使用
 */
public class MyExecutionListener implements ExecutionListener {
    private static final long serialVersionUID = 7960387497099642910L;
    private static ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

//    String getEventName(); 这个比较有用 主要就是start、end、take
//    EngineServices getEngineServices(); 这个非常有用吧。当拿到EngineServices 对象所有的xxxService都可以拿到。
    public void notify(DelegateExecution execution) throws Exception {
        String eventName = execution.getEventName();

//        System.out.println("execution Id="+execution.getId());
//        System.out.println("流程实例id="+execution.getProcessInstanceId());
//        System.out.println("业务id已经废弃="+execution.getBusinessKey());
//        System.out.println("业务id="+execution.getProcessBusinessKey());
//        System.out.println("流程定义id="+execution.getProcessDefinitionId());
//        System.out.println("获取父id，并发的时候有用="+execution.getParentId());
//        System.out.println("获取当前的.Activityid="+execution.getCurrentActivityId());
//        System.out.println("获取当前的.Activity name="+execution.getCurrentActivityName());
//        System.out.println("获取TenantId 当有多个TenantId 有用="+execution.getTenantId());

        if ("start".equals(eventName)) {
            System.out.println("start=========");
        }else if ("end".equals(eventName)) {
            System.out.println("end=========");
        }else if ("take".equals(eventName)) {
            System.out.println("take=========");
        }

    }
}


