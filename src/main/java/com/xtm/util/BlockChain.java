package com.xtm.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.RequestEntity;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.Date;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class BlockChain {
    // 存储区块链
    private List<Map<String, Object>> chain;
    // 该实例变量用于当前的交易信息列表
    private List<Map<String, Object>> currentTransactions;
    //节点
    private Set<String> nodes;
    private static BlockChain blockChain = null;

    private BlockChain() {
        // 初始化区块链以及当前的交易信息列表
        chain = new ArrayList<Map<String, Object>>();
        currentTransactions = new ArrayList<Map<String, Object>>();
        // 用于存储网络中其他节点的集合
        nodes = new HashSet<String>();
        // 创建创世区块
        newBlock(100, "0");
    }

    // 创建单例对象
    public static BlockChain getInstance() {
        if (blockChain == null) {
            synchronized (BlockChain.class) {
                if (blockChain == null) {
                    blockChain = new BlockChain();
                }
            }
        }
        return blockChain;
    }


    public List<Map<String, Object>> getChain() {
        return chain;
    }

    public void setChain(List<Map<String, Object>> chain) {
        this.chain = chain;
    }

    public List<Map<String, Object>> getCurrentTransactions() {
        return currentTransactions;
    }

    public void setCurrentTransactions(List<Map<String, Object>> currentTransactions) {
        this.currentTransactions = currentTransactions;
    }
    public Set<String> getNodes() {
        return nodes;
    }

    /**
     * 注册节点
     *
     * @param address
     *            节点地址
     * @throws MalformedURLException
     */
    public void registerNode(String address) throws MalformedURLException {
        URL url = new URL(address);
        String node = url.getHost() + ":" + (url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
        nodes.add(node);
    }

    /**
     * 检查是否是有效链，遍历每个区块验证hash和proof，来确定一个给定的区块链是否有效
     *
     * @param chain
     * @return
     */
    public boolean validChain(List<Map<String, Object>> chain) {
        Map<String, Object> lastBlock = chain.get(0);
        int currentIndex = 1;
        while (currentIndex < chain.size()) {
            Map<String, Object> block = chain.get(currentIndex);
            System.out.println(lastBlock.toString());
            System.out.println(block.toString());
            System.out.println("\n-------------------------\n");

            // 检查block的hash是否正确
            if (!block.get("previous_hash").equals(hash(lastBlock))) {
                return false;
            }

            lastBlock = block;
            currentIndex++;
        }
        return true;
    }

    /**
     * 共识算法解决冲突，使用网络中最长的链. 遍历所有的邻居节点，并用上一个方法检查链的有效性， 如果发现有效更长链，就替换掉自己的链
     *
     * @return 如果链被取代返回true, 否则返回false
     * @throws IOException
     */
    public boolean resolveConflicts() throws Exception {
        Set<String> neighbours = this.nodes;
        List<Map<String, Object>> newChain = null;

        // 寻找最长的区块链
        long maxLength = this.chain.size();

        // 获取并验证网络中的所有节点的区块链
        for (String node : neighbours) {

//            URL url = new URL("http://" + node + "/chain");
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.connect();
//            if (connection.getResponseCode() == 200) {
//                BufferedReader bufferedReader = new BufferedReader(
//                        new InputStreamReader(connection.getInputStream(), "utf-8"));
//                StringBuffer responseData = new StringBuffer();
//                String response = null;
//                while ((response = bufferedReader.readLine()) != null) {
//                    responseData.append(response);
//                }
//                bufferedReader.close();
//
//                System.out.println(bufferedReader.toString());
//                JSONObject jsonData = JSONObject.parseObject(bufferedReader.toString());
//                long length = jsonData.getLong("length");
//                List<Map<String, Object>> chain = (List)jsonData.getJSONArray("chain");
////                List<Map<String, Object>> chain = (List) jsonData.getJSONArray("chain").toList();
//
//                // 检查长度是否长，链是否有效
//                if (length > maxLength && validChain(chain)) {
//                    maxLength = length;
//                    newChain = chain;
//                }
//            }

            String result = doPost("http://" + node + "/chain","");
            System.out.println("http://" + node + "/chain");
            System.out.println("result="+result);
            JSONObject jsonData = JSONObject.parseObject(result);
            long length = jsonData.getLong("length");
            List<Map<String, Object>> chain = (List)jsonData.getJSONArray("chain");

            // 检查长度是否长，链是否有效
            if (length > maxLength && validChain(chain)) {
                maxLength = length;
                newChain = chain;
            }

        }
        // 如果发现一个新的有效链比我们的长，就替换当前的链
        if (newChain != null) {
            this.chain = newChain;
            return true;
        }
        return false;
    }

    /**
     * @return 得到区块链中的最后一个区块
     */
    public Map<String, Object> lastBlock() {
        return getChain().get(getChain().size() - 1);
    }

    /**
     * 在区块链上新建一个区块
     *
     * @param proof
     *            新区块的工作量证明
     * @param previous_hash
     *            上一个区块的hash值
     * @return 返回新建的区块
     */
    public Map<String, Object> newBlock(long proof, String previous_hash) {

        Map<String, Object> block = new HashMap<String, Object>();
        block.put("index", getChain().size() + 1);
        block.put("timestamp", System.currentTimeMillis());
        block.put("transactions", getCurrentTransactions());
        block.put("proof", proof);
        // 如果没有传递上一个区块的hash就计算出区块链中最后一个区块的hash
        block.put("previous_hash", previous_hash != null ? previous_hash : hash(getChain().get(getChain().size() - 1)));

        // 重置当前的交易信息列表
        setCurrentTransactions(new ArrayList<Map<String, Object>>());

        getChain().add(block);

        return block;
    }

    /**
     * 生成新交易信息，信息将加入到下一个待挖的区块中
     *
     * @param sender
     *            发送方的地址
     * @param recipient
     *            接收方的地址
     * @param amount
     *            交易数量
     * @return 返回该交易事务的块的索引
     */
    public int newTransactions(String sender, String recipient, long amount) {

        Map<String, Object> transaction = new HashMap<String, Object>();
        transaction.put("sender", sender);
        transaction.put("recipient", recipient);
        transaction.put("amount", amount);

        getCurrentTransactions().add(transaction);

        return (Integer) lastBlock().get("index") + 1;
    }

    /**
     * 生成区块的 SHA-256格式的 hash值
     *
     * @param block
     *            区块
     * @return 返回该区块的hash
     */
    public static Object hash(Map<String, Object> block) {
        return new Encrypt().getSHA256(new JSONObject(block).toString());
    }

    /**
     * 简单的工作量证明:
     *   - 查找一个 p' 使得 hash(pp') 以4个0开头
     *   - p 是上一个块的证明, p' 是当前的证明
     *
     * @param last_proof
     *               上一个块的证明
     * @return
     */
    public long proofOfWork(long last_proof) {
        long proof = 0;
        while (!validProof(last_proof, proof)) {
            proof += 1;
        }
        return proof;
    }

    /**
     * 验证证明: 是否hash(last_proof, proof)以4个0开头?
     *
     * @param last_proof
     *            上一个块的证明
     * @param proof
     *            当前的证明
     * @return 以4个0开头返回true，否则返回false
     */
    public boolean validProof(long last_proof, long proof) {
        String guess = last_proof + "" + proof;
        String guess_hash = new Encrypt().getSHA256(guess);
        return guess_hash.startsWith("0000");
    }


    /**
     * 调用post方法
     * @param url
     * @param data
     * @return
     */
    public static String doPost(String url,String data) {
        // 获取输入流
        InputStream is = null;
        BufferedReader br = null;
        String result = null;
        // 创建httpClient实例对象
        HttpClient httpClient = new HttpClient();
        // 设置httpClient连接主机服务器超时时间：15000毫秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
        // 创建post请求方法实例对象
        PostMethod postMethod = new PostMethod(url);
        try {
            StringRequestEntity se = new StringRequestEntity(data, "application/json", "UTF-8");
            postMethod.setRequestEntity(se);
            postMethod.setRequestHeader("Content-Type", "application/json");
            //默认的重试策略
            postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);//设置超时时间
            //设置请求的编码
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            httpClient.executeMethod(postMethod);
            result = postMethod.getResponseBodyAsString();

        }catch (Exception e){
            e.fillInStackTrace();
        }

        return result;
    }

}
