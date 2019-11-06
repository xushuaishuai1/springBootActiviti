//package com.xtm.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.servlet.NoHandlerFoundException;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
///**
// * 全局异常捕获
// */
//@RestControllerAdvice
//public class ExceptionHandle {
//    Log log = LogFactory.getLog(ExceptionHandle.class);
//
//    @Autowired
//    ObjectMapper mapper;
//    /**
//     * 404异常处理
//     */
//    @ExceptionHandler(value = NoHandlerFoundException.class)
//    @ResponseBody
//    public Forwad NoHandlerFoundExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {
//        Common.setCorsMappings(request,response);
//        Forwad forwad = new Forwad();
//        forwad.setState("error");
//        forwad.setSimpleErrorMsg("404未找到访问的方法！");
//        forwad.setErrorMsg(errorTrackSpace(e));
//        log.error(e);
//        return forwad;
//    }
//
//
//    /**
//     *  默认异常处理，前面未处理
//     */
//    @ExceptionHandler(value = Throwable.class)
//    @ResponseBody
//    public Forwad  defaultHandler(HttpServletRequest req, Exception e) throws Exception {
//        String errorMessage = (String)req.getAttribute("errorMessage");
//        Forwad forwad = new Forwad();
//        forwad.setState("error");
//        forwad.setSimpleErrorMsg((errorMessage==null||"".equals(errorMessage))?"参数不匹配或参数类型错误":errorMessage);
//        forwad.setErrorMsg(errorTrackSpace(e));
//        log.error(e);
//        return forwad;
//    }
//
////    /**
////     *  默认异常处理，前面未处理
////     */
////    @ExceptionHandler(value = RequestLimitException.class)
////    public Forwad RequestLimitContract(HttpServletRequest req, Exception e) throws Exception {
////        String errorMessage = (String)req.getAttribute("errorMessage");
////        Forwad forwad = new Forwad();
////        forwad.setState("error");
////        forwad.setSimpleErrorMsg("HTTP请求超出设定的限制");
////        forwad.setErrorMsg(Common.errorTrackSpace(e));
////        log.error(e);
////        return forwad;
////    }
//
//
//    /**
//     * 打印异常详细信息
//     * @param e
//     * @return
//     */
//    public static String errorTrackSpace(Exception e) {
//        StringBuffer sb = new StringBuffer();
//        if (e != null) {
//            for (StackTraceElement element : e.getStackTrace()) {
//                sb.append("\r\n\t").append(element);
//            }
//        }
//        return sb.length() == 0 ? null : sb.toString();
//    }
//}
