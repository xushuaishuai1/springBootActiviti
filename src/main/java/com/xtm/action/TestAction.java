package com.xtm.action;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtm.util.BlockChain;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
//@RequestMapping("/testAction")
public class TestAction {

    @Value("${server.port}")
    private String port;

    @Autowired
    ProcessEngine processEngine;
    @Autowired
    ObjectMapper objectMapper;

    @RequestMapping("/test")
    @ResponseBody
    public  String  test(){
        return "sfsdfsdf";
    }


    //https://blog.51cto.com/zero01/2086195

    /**
     * 创建一个交易并添加到区块
     * @return
     */
    @RequestMapping("/transactions/new")
    @ResponseBody
    public  String  transactions_new(HttpServletRequest request){
//        {
//            "sender": "my address",
//            "recipient": "someone else's address",
//            "amount": 5
//        }
        String sender = request.getParameter("sender");
        String recipient = request.getParameter("recipient");
        Long amount = new Long(request.getParameter("amount"));
        // 新建交易信息
        BlockChain blockChain = BlockChain.getInstance();
        int index = blockChain.newTransactions(sender,recipient,amount);
        return index+"";
    }

    /**
     * 告诉服务器去挖掘新的区块
     * @return
     */
    @RequestMapping("/mine")
    @ResponseBody
    public  Object  mine(HttpServletRequest request){
        BlockChain blockChain = BlockChain.getInstance();
        Map<String, Object> lastBlock = blockChain.lastBlock();
        long lastProof = Long.parseLong(lastBlock.get("proof") + "");
        long proof = blockChain.proofOfWork(lastProof);

        // 给工作量证明的节点提供奖励，发送者为 "0" 表明是新挖出的币

        blockChain.newTransactions("0", port, 1);

        // 构建新的区块
        Map<String, Object> newBlock = blockChain.newBlock(proof, null);
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("message", "New Block Forged");
        response.put("index", newBlock.get("index"));
        response.put("transactions", newBlock.get("transactions"));
        response.put("proof", newBlock.get("proof"));
        response.put("previous_hash", newBlock.get("previous_hash"));

        return response;

    }


    /**
     * 返回整个区块链
     * @return
     */
    @RequestMapping("/chain")
    @ResponseBody
    public  Object  chain(){
        BlockChain blockChain = BlockChain.getInstance();
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("chain", blockChain.getChain());
        response.put("length", blockChain.getChain().size());
        return response;
    }



    /**
     * 用于注册节点
     * @return
     */
    @RequestMapping("/nodes/register")
    @ResponseBody
    public  Object  nodes_register(HttpServletRequest request) throws Exception{
        //{"nodes":["http://localhost:1234"]}
        //{"nodes":["http://localhost:12345"]}
        // 获得节点集合数据，并进行判空
        JSONArray nodes = JSONArray.parseArray(request.getParameter("nodes"));
//        // 注册节点
        BlockChain blockChain = BlockChain.getInstance();
        for (int i=0;i<nodes.size();i++) {
            blockChain.registerNode(nodes.get(i).toString());
        }

        // 向客户端返回处理结果
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("message", "New nodes have been added");
        response.put("total_nodes", JSONArray.toJSONString(blockChain.getNodes()));

        return response;
    }

    /**
     * 用于解决冲突
     * @return
     */
    @RequestMapping("/nodes/resolve")
    @ResponseBody
    public  Object  nodes_resolve(HttpServletRequest request) throws Exception{
        BlockChain blockChain = BlockChain.getInstance();
        boolean replaced = blockChain.resolveConflicts();
        Map<String, Object> response = new HashMap<String, Object>();
        if (replaced) {
            response.put("message", "Our chain was replaced");
            response.put("new_chain", blockChain.getChain());
        } else {
            response.put("message", "Our chain is authoritative");
            response.put("chain", blockChain.getChain());
        }
        return response;
    }




    @RequestMapping("/hello")
    public String hello(HttpServletRequest request, @RequestParam(value = "name", defaultValue = "水电费水电费springboot-thymeleaf") String name) {
        request.setAttribute("name", name);
        return "hello";
    }




    /**
     * 登陆通过安全框架
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) {
        // 从SecurityUtils里边创建一个 subject
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        // 执行认证登陆
        try {
            subject.login(token);
        } catch (UnknownAccountException uae) {
            return "未知账户";
        } catch (IncorrectCredentialsException ice) {
            return "密码不正确";
        } catch (LockedAccountException lae) {
            return "账户已锁定";
        } catch (ExcessiveAttemptsException eae) {
            return "用户名或密码错误次数过多";
        } catch (AuthenticationException ae) {
            return "用户名或密码不正确！";
        }
        if (subject.isAuthenticated()) {
            return "登录成功";
        } else {
            token.clear();
            return "登录失败";
        }
    }


    /**
     * 没有登陆跳转
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String unLogin() {
        return "login";
    }

    /**
     * 没有权限跳转
     * @return
     */
    @RequestMapping(value = "/noRole", method = RequestMethod.GET)
    public String noRole() {
        return "noRole";
    }








    /*****************************************************************************************工作流部署相关****************************************************************************************/

    //act_re_deployment（部署对象表）
    // 存放流程定义的显示名和部署时间，每部署一次增加一条记录
    //act_re_procdef（流程定义表）
    // 存放流程定义的属性信息，部署每个新的流程定义都会在这张表中增加一条记录。注意：当流程定义的key相同的情况下，使用的是版本升级
    //act_ge_bytearray（资源文件表）
    // 存储流程定义相关的部署信息。即流程定义文档的存放地。每部署一次就会增加两条记录，一条是关于bpmn规则文件的，一条是图片的（如果部署时只指定了bpmn一个文件，activiti会在部署时解析bpmn文件内容自动生成流程图）。两个文件不是很大，都是以二进制形式存储在数据库中。


    /**
     * 根据流程文件的key部署流程
     * @return
     */
    @RequestMapping("/deploymentProcessDefinition")
    @ResponseBody
    public String deploymentProcessDefinition(){

        Deployment deployment = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createDeployment()     //创建一个部署对象
                .name("dempProcess入门程序")//添加部署的名称
                .addClasspathResource("bpmn/dempProcess.bpmn")//从classpath的资源中加载，一次只能加载一个文件
                .addClasspathResource("bpmn/dempProcess.png")//从classpath的资源中加载，一次只能加载一个文件
                .deploy();//完成部署
        System.out.println("部署ID："+deployment.getId());
        System.out.println("部署名称："+deployment.getName());
        return "success";
    }

    /**
     * 查询所有部署流程
     * @return
     */
    @RequestMapping("/findProcessDefinition")
    @ResponseBody
    public String findProcessDefinition(){

        // 可以看到流程定义的key值相同的情况下，版本是从1开始逐次升级的，流程定义的Id是【key：版本：生成ID】。
        // 由运行结果可以看出：Key和Name的值为：bpmn文件process节点的id和name的属性值。
        // key属性被用来区别不同的流程定义，带有特定key的流程定义第一次部署时，version为1。
        // 之后每次部署都会在当前最高版本号上加1，Id的值的生成规则为:{processDefinitionKey}:{processDefinitionVersion}:{generated-id},
        // 这里的generated-id是一个自动生成的唯一的数字，重复部署一次，deploymentId的值以一定的形式变化，规则act_ge_property表生成。

        List<ProcessDefinition> list = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery()//创建一个流程定义的查询
                /**指定查询条件,where条件*/
//                        .deploymentId(deploymentId)//使用部署对象ID查询
//                        .processDefinitionId(processDefinitionId)//使用流程定义ID查询
//                        .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
//                        .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

                /**排序*/
                .orderByProcessDefinitionVersion().asc()//按照版本的升序排列
//                        .orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列

                /**返回的结果集*/
                .list();//返回一个集合列表，封装流程定义
//                        .singleResult();//返回惟一结果集
//                        .count();//返回结果集数量
//                        .listPage(firstResult, maxResults);//分页查询
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                System.out.println("流程定义ID:"+pd.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:"+pd.getName());//对应helloworld.bpmn文件中的name属性值
                System.out.println("流程定义的key:"+pd.getKey());//对应helloworld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:"+pd.getVersion());//当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:"+pd.getResourceName());
                System.out.println("资源名称png文件:"+pd.getDiagramResourceName());
                System.out.println("部署对象ID："+pd.getDeploymentId());
                System.out.println("#########################################################");
            }
        }
        return "success";
    }


    /**
     * 查询最新版本的流程定义
     */
    @RequestMapping("/findLastVersionProcessDefinition")
    @ResponseBody
    public String findLastVersionProcessDefinition(){
        List<ProcessDefinition> list = processEngine.getRepositoryService()//
                .createProcessDefinitionQuery()//
                .orderByProcessDefinitionVersion().asc()//使用流程定义的版本升序排列
                .list();
        /**
         * Map<String,ProcessDefinition>
         map集合的key：流程定义的key
         map集合的value：流程定义的对象
         map集合的特点：当map集合key值相同的情况下，后一次的值将替换前一次的值
         */
        Map<String, ProcessDefinition> map = new LinkedHashMap<String, ProcessDefinition>();
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                map.put(pd.getKey(), pd);
            }
        }
        List<ProcessDefinition> pdList = new ArrayList<ProcessDefinition>(map.values());
        if(pdList!=null && pdList.size()>0){
            for(ProcessDefinition pd:pdList){
                System.out.println("流程定义ID:"+pd.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:"+pd.getName());//对应helloworld.bpmn文件中的name属性值
                System.out.println("流程定义的key:"+pd.getKey());//对应helloworld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:"+pd.getVersion());//当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:"+pd.getResourceName());
                System.out.println("资源名称png文件:"+pd.getDiagramResourceName());
                System.out.println("部署对象ID："+pd.getDeploymentId());
                System.out.println("#########################################################");
            }
        }
        return "success";
    }

    /**
     * 根据流程部署的key删除流程
     * @return
     */
    @RequestMapping("/deleteProcessDefinition")
    @ResponseBody
    public String deleteProcessDefinition(){
        //使用部署ID，完成删除 act_re_deployment表id
        String deploymentId = "2501";
        processEngine.getRepositoryService()//
                .deleteDeployment(deploymentId, true);
        System.out.println("删除成功！");
        return "success";
    }


    /**
     * 删除流程定义（删除key相同的所有不同版本的流程定义）
     */
    @RequestMapping("/deleteProcessDefinitionByKey")
    @ResponseBody
    public String deleteProcessDefinitionByKey(){
        //流程定义的key
        String processDefinitionKey = "myProcess_1";
        //先使用流程定义的key查询流程定义，查询出所有的版本
        List<ProcessDefinition> list = processEngine.getRepositoryService()//
                .createProcessDefinitionQuery()//
                .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
                .list();
        //遍历，获取每个流程定义的部署ID
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                //获取部署ID
                String deploymentId = pd.getDeploymentId();
                processEngine.getRepositoryService()//
                        .deleteDeployment(deploymentId, true);
            }
        }
        return "success";
    }


    /**
     * 根据流程部署的key查看流程图
     * @throws IOException
     */
    @RequestMapping("/viewPic")
    @ResponseBody
    public String viewPic() throws IOException {

        // deploymentId为流程部署ID，resourceName为act_ge_bytearray表中NAME_列的值，
        // 使用repositoryService的getDeploymentResourceNames方法可以获取指定部署下得所有文件的名称，
        // 使用repositoryService的getResourceAsStream方法传入部署ID和资源图片名称可以获取部署下指定名称文件的输入流，
        // 最后的有关IO流的操作，使用FileUtils工具的copyInputStreamToFile方法完成流程流程到文件的拷贝，将资源文件以流的形式输出到指定文件夹下。

        /**将生成图片放到文件夹下*/
        String deploymentId = "10001";
        //获取图片资源名称
        List<String> list = processEngine.getRepositoryService()//
                .getDeploymentResourceNames(deploymentId);
        //定义图片资源的名称
        String resourceName = "";
        if(list!=null && list.size()>0){
            for(String name:list){
                if(name.indexOf(".png")>=0){
                    resourceName = name;
                }
            }
        }
        //获取图片的输入流
        InputStream in = processEngine.getRepositoryService()//
                .getResourceAsStream(deploymentId, resourceName);
        //将图片生成到D盘的目录下
        File file = new File("C:/activitiDemo/"+resourceName);
        System.out.println(file.getPath());
        //将输入流的图片写到C盘下
        FileUtils.copyInputStreamToFile(in, file);
        return "success";
    }


    /*****************************************************************************************工作流部署相关****************************************************************************************/




    /*****************************************************************************************工作流运行相关****************************************************************************************/

    //流程实例、执行对象、任务
    //act_ru_execution  正在执行的执行对象表
    //act_hi_procinst 流程实例的历史表
    //act_ru_task 正在执行的任务表（只存UserTask节点数据）
    //act_hi_taskinst 任务历史表（只存UserTask节点数据）
    //act_hi_actinst 活动节点历史表

    /**
     * 根据流程部署文件的key启动流程实例
     */
    @RequestMapping("/startProcessInstance")
    @ResponseBody
    public String startProcessInstance(){

        Map<String, Object> variables = new HashMap<String, Object>();
        List<String> usr = new ArrayList<String>();
        usr.add("提交审批");
        variables.put("judge","false");
        variables.put("user",usr);
        //获取与正在执行的流程示例和执行对象相关的Service
        ProcessInstance processInstance = processEngine.getRuntimeService()
                //使用流程定义的key启动实例，key对应bpmn文件中id的属性值，默认按照最新版本流程启动
                .startProcessInstanceByKey("myProcess_1",variables);
        System.out.println(processInstance.getId());
        System.out.println(processInstance.getProcessDefinitionId());

        //通过流程定义的key启动流程实例，这时打开数据库act_ru_execution表，
        //ID_表示执行对象ID，PROC_INST_ID_表示流程实例ID，如果是单例流程（没有分支和聚合），
        //那么流程实例ID和执行对象ID是相同的。
        return "success";
    }


    /**
     * 查询流程实例状态（判断流程正在执行，还是结束）
     * 根据流程实例act_ru_execution id
     * @return
     */
    @RequestMapping("/isProcessEnd")
    @ResponseBody
    public String isProcessEnd(){
        String processInstanceId = "12501";
        ProcessInstance pi = processEngine.getRuntimeService()//表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        if(pi==null){
            System.out.println("流程已经结束");
        }
        else{
            System.out.println("流程没有结束");
        }
        return "success";
    }


    /**
     * 查询历史流程实例
     */
    @RequestMapping("/findHistoryProcessInstance")
    @ResponseBody
    public String findHistoryProcessInstance(){
        String processInstanceId = "12501";
        HistoricProcessInstance hpi = processEngine.getHistoryService()//与历史数据（历史表）相关的Service
                .createHistoricProcessInstanceQuery()//创建历史流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        return (hpi.getId()+"    "+hpi.getProcessDefinitionId()+"    "+hpi.getStartTime()+"    "+hpi.getEndTime()+"     "+hpi.getDurationInMillis());
    }






    /**
     * 查询当前的个人任务
     */
    @RequestMapping("/findPersonalTask")
    @ResponseBody
    public String findPersonalTask(){
        String assignee = "张三";
        List<Task> list = processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .createTaskQuery()//创建任务查询对象
                /**查询条件（where部分）*/
//                .taskAssignee(assignee)//指定个人任务查询，指定办理人
//                        .taskCandidateUser(candidateUser)//组任务的办理人查询
//                        .processDefinitionId(processDefinitionId)//使用流程定义ID查询
//                        .processInstanceId(processInstanceId)//使用流程实例ID查询
//                        .executionId(executionId)//使用执行对象ID查询
                /**排序*/
                .orderByTaskCreateTime().asc()//使用创建时间的升序排列
                /**返回结果集*/
//                        .singleResult()//返回惟一结果集
//                        .count()//返回结果集的数量
//                        .listPage(firstResult, maxResults);//分页查询
                .list();//返回列表
        if(list != null && list.size() > 0){
            for(Task task : list){
                System.out.println("任务ID:"+task.getId());
                System.out.println("任务名称:"+task.getName());
                System.out.println("任务的创建时间:"+task.getCreateTime());
                System.out.println("任务的办理人:"+task.getAssignee());
                System.out.println("流程实例ID："+task.getProcessInstanceId());
                System.out.println("执行对象ID:"+task.getExecutionId());
                System.out.println("流程定义ID:"+task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
        }
        return "success";
    }

    /**
     * 根据act_ru_task的ID完成我的任务
     */
    @RequestMapping("/completePersonalTask")
    @ResponseBody
    public String completePersonalTask(){
        //这边设置的是下一个环节审批人，优先级低于监听设置
//        Map<String, Object> variables = new HashMap<String, Object>();
//        List<String> usr = new ArrayList<String>();
//        usr.add("总部门");
//        variables.put("user",usr);
//        processEngine.getTaskService()
//                .complete("67519",variables);
        processEngine.getTaskService()
                .complete("72512");

        System.out.println("完成任务：任务ID：72512");
        return "success";
    }


    /**
     * 查询历史任务
     */
    @RequestMapping("/findHistoryTask")
    @ResponseBody
    public String findHistoryTask(){
        String taskAssignee = "张三";
        List<HistoricTaskInstance> list = processEngine.getHistoryService()//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
//                .taskAssignee(taskAssignee)//指定历史任务的办理人
                .list();
        if(list!=null && list.size()>0){
            for(HistoricTaskInstance hti:list){
                System.out.println(hti.getId()+"    "+hti.getName()+"    "+hti.getProcessInstanceId()+"   "+hti.getStartTime()+"   "+hti.getEndTime()+"   "+hti.getDurationInMillis());
                System.out.println("################################");
            }
        }
        return "success";
    }

    /*****************************************************************************************工作流运行相关****************************************************************************************/


    /*****************************************************************************************流程变量相关****************************************************************************************/

    //act_ru_variable 正在执行的流程变量表
    //act_hi_carinst  历史的流程变量表

    /**
     * 根据act_ru_task的ID设置流程变量
     * setVariableLocal 表面该变量只存在该任务环节
     * setVariable 通过该任务id给整个流程设置变量
     */
    @RequestMapping("/setVariables")
    @ResponseBody
    public String setVariables(){
        TaskService taskService = processEngine.getTaskService();
        //任务ID
        String taskId = "12510";
        taskService.setVariableLocal(taskId,"请假天数",3);//local与当前task绑定，下一个task不可见
        taskService.setVariable(taskId,"请假日期",new Date());
        taskService.setVariable(taskId,"请假原因","回家探亲");
        Map<String,String> map = new HashMap<String,String>();
        map.put("pid","-1");
        map.put("proName","所发生的反倒是");
        taskService.setVariables(taskId,map);

        //可是设置javabean类型，不过需要javabean序列话，且值不能改变
        //解决方案：在Person对象中添加：
        //private static final long serialVersionUID = 6757393795687480331L;
        //同时实现Serializable
//        Person p = new Person();
//        p.setId(20);
//        p.setName("翠花");
//        taskService.setVariable(taskId, "人员信息(添加固定版本)", p);

        System.out.println("流程变量设置成功");
        return "success";
    }

    /**
     * 获取流程变量
     */
    @RequestMapping("/getVariables")
    @ResponseBody
    public String getVariables(){
        TaskService taskService = processEngine.getTaskService();
        //任务ID
        String taskId = "12510";
        /**一：获取流程变量，使用基本数据类型*/
        Integer days = (Integer) taskService.getVariable(taskId, "请假天数");
        Date date = (Date) taskService.getVariable(taskId, "请假日期");
        String resean = (String) taskService.getVariable(taskId, "请假原因");
        System.out.println("请假天数："+days);
        System.out.println("请假日期："+date);
        System.out.println("请假原因："+resean);
        System.out.println("pid："+(String) taskService.getVariable(taskId, "pid"));
        System.out.println("proName："+(String) taskService.getVariable(taskId, "proName"));
        /**二：获取流程变量，使用javabean类型*/
//        Person p = (Person)taskService.getVariable(taskId, "人员信息(添加固定版本)");
//        System.out.println(p.getId()+"        "+p.getName());
        return "success";
    }


    /**
     * 查询流程变量的历史表
     */
    @RequestMapping("/findHistoryProcessVariables")
    @ResponseBody
    public String findHistoryProcessVariables(){

        //历史的流程变量查询，指定流程变量的名称，查询act_hi_varinst表（也可以针对，流程实例ID，执行对象ID，任务ID查询）
        List<HistoricVariableInstance> list = processEngine.getHistoryService()//
                .createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询对象
                .variableName("请假天数")
                .list();
        if(list!=null && list.size()>0){
            for(HistoricVariableInstance hvi:list){
                System.out.println(hvi.getId()+"   "+hvi.getProcessInstanceId()+"   "+hvi.getVariableName()+"   "+hvi.getVariableTypeName()+"    "+hvi.getValue());
                System.out.println("###############################################");
            }
        }
        return "success";
    }

    /*****************************************************************************************流程变量相关****************************************************************************************/

}
