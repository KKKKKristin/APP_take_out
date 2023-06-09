package com.itheima.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.itheima.common.BaseContext;
import com.itheima.entity.Orders;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.OrderService;
import com.itheima.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping
public class PayController {

    @Autowired
    private OrderService ordersService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    //应用的APPID
    private final String APP_ID = "2021000121613404";
    //生成的应用私钥
    private final String APP_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCZJjQt5hLsXVhwVr8VLLAOFuuHux6TOX978ZnLhpwq3bSBJbUPPGaRJuMYgZcOIxNLLgwbba06jPpp8hAeBugyoVHyX4TVyZ8BJztxLXWvH1Hu3kHxoxMQCpoZSDmksy6XtqPzYCPfm7pZwpAWAMuFHUgZ54xc41zJZesX8KecVPExl3LRO3E4qXYH1RlUqlh5FBkYDHNTtwVhW3AEosqIOL6D7bRf/TmYwn2ZdcZhgbQGwhKWElrIHNLCujzVZdtxTOZXDWj4BqCP7KDMe7sIpqM7U8EiOARuDTlJEkjRwCdSGeIQnVbhj4se78+m9CLcicgnKVs6cJ8TC8dXOfUrAgMBAAECggEAcktuuapxCPGJJR24MYdORNWaJYvif6FzoP5n9tqZBey7335Sp/I94aoR5aIhJ63JolvVMUsi8wVvhU3f9WjWNaLrU2Ux75MvFV9AH3D72RwYu5onwLwhZ+Fhq+iZDG+lIBhwNxvS6fiAlKh9e85QFSILQznpKRGNl8h2MUGq9SHP3AzzD16Pqf/bBpooTKXpKB+JdCXB5lf+WYQc3H7aHgmBkIrJvHS9VNSv+NNtYkoaS5MhRmdfoBmTbZcwKUZeT5wvHUmPgh3gUpRcpf3M8d4OPXdNphcinAW8P6/jw1afqPu5tjmlL45zWBKLvjlgU48GtOFuLOh4ge/4cLJWAQKBgQDKD2xRzrplNeFUSAEvk/RhsnXFBdxGLqUz+fnKiCtTeqqpu+OTnJaMQ0gzL6hy8mJbYiwbY+1r664XHxzv9k95aFFCP1xnjkxO4UIU5nAbumoJflnqhE9eNBmjoTzvk8XvYBgdJDU2hZ8WdBqqo/arp9ngZfA/mkI9KUFmmTQkgQKBgQDCCEFjlf4dmuB5u3IlUrhiFdPoKpqrTp5UP57qNPBb8jQb6PXRdcmM59gU5FM5QStxx1075ARC+dIsRVsBpPxx5QBLqO8sCo3ZC1ZwQA8lKhBXJFdluGuvycsXlwqqgsW/JPAdvxfep4WKCmhsOQ1ynae9G5gqBzdjYGyaCrATqwKBgHxda80aUU1UEWE/hENVbFfpcGUYy1ADC7lnBWCr2QbjfEFGHLdQ7LReAQVIMwS0vOcEEKO3KH2BLMCcbMIhNx9BnqoIt4YhZkQyjuqwPpZCcVtwgkU+tF8F8DXvWsMWkABag90D2SC6s4UtZvBaRMu4XvinpWxwNI7SYqeUaiyBAoGADBgho0h1J3lA5rWVXtgyIAMGS2FkdKsY2V2B1U1pcgG918OElwLFhl/pKxw0R8xVHel0WUhUL6yMui+0hXy4M403oH+uMMuTneTEBYkoqINfs17Th8agB7wHSbgjdQ9jrg9qXCBOJ5W/kCQn8B76RJupGfN/X5dFj48yFcP2x7MCgYB+CH9itLjoEn+ZF+XV52L0yjK15ilJe7knpREqMxXl2jtPaLwZ1T21t63gYSra8j6Mrhklkfy/Docl5TjoB2J4te1yndiM8gI4MiId2nWI1qcj2V5n8ENU8t9ctDrXbgjD81fohUYWhv0kScyhwoX5gq+L07eO7/0H6k3Z1ZfGvA==";

    private final String CHARSET = "UTF-8";
    //支付宝公钥
    private final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmSY0LeYS7F1YcFa/FSywDhbrh7sekzl/e/GZy4acKt20gSW1DzxmkSbjGIGXDiMTSy4MG22tOoz6afIQHgboMqFR8l+E1cmfASc7cS11rx9R7t5B8aMTEAqaGUg5pLMul7aj82Aj35u6WcKQFgDLhR1IGeeMXONcyWXrF/CnnFTxMZdy0TtxOKl2B9UZVKpYeRQZGAxzU7cFYVtwBKLKiDi+g+20X/05mMJ9mXXGYYG0BsISlhJayBzSwro81WXbcUzmVw1o+Aagj+ygzHu7CKajO1PBIjgEbg05SRJI0cAnUhniEJ1W4Y+LHu/PpvQi3InIJylbOnCfEwvHVzn1KwIDAQAB";
    //这是沙箱接口路径,正式路径为https://openapi.alipay.com/gateway.do
    private final String GATEWAY_URL = "https://openapi.alipaydev.com/gateway.do";
    private final String FORMAT = "JSON";
    //签名方式
    private final String SIGN_TYPE = "RSA2";
    //支付宝异步通知路径,付款完毕后会异步调用本项目的方法,必须为公网地址
    private final String NOTIFY_URL = "http://localhost:8080/front/page/pay-success.html";
    //支付宝同步通知路径,也就是当付款完毕后跳转本项目的页面,可以不是公网地址
    private final String RETURN_URL = "http://localhost:8080/front/page/pay-success.html";

    @RequestMapping("aliapy")
    public void alipay(HttpServletResponse httpResponse, HttpSession session) throws IOException {

        Random r = new Random();
        //实例化客户端,填入所需参数
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //在公共参数中设置回跳和通知地址
        request.setReturnUrl(RETURN_URL);
        request.setNotifyUrl(NOTIFY_URL);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        //生成随机Id
//        Long orderId = (Long) session.getAttribute("orderId");
        String out_trade_no = UUID.randomUUID().toString();//UUID.randomUUID().toString();
//        //付款金额，必填
//        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
//        lqw.eq(orderId != null, Orders::getId, orderId);
//        lqw.eq(Orders::getUserId, BaseContext.getCurrentId());
//        Orders orders = ordersService.getOne(lqw);


        //获得当前用户id
        Long userId = BaseContext.getCurrentId();
        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);
        //订单号
        long orderId = IdWorker.getId();
        //计算订单金额
    /*    AtomicInteger amount = new AtomicInteger(0);
        shoppingCarts.stream().map((item) -> {
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return null;
        });
*/

        BigDecimal amount = new BigDecimal(0);
        for (ShoppingCart shoppingCart : shoppingCarts) {
            amount = amount.add(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())));
        }


//        String total_amount = String.valueOf(orders.getAmount()); //Integer.toString(r.nextInt(9999999) + 1000000);
        String total_amount = String.valueOf(amount.toString()); //Integer.toString(r.nextInt(9999999) + 1000000);
        //订单名称，必填
        String subject = "瑞吉外卖订单";
        //商品描述，可空
//        String body = "买家电话 :" + orders.getPhone();
        String body = "买家电话 :";
        request.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String form = "";
        try {
            form = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    @RequestMapping(value = "/returnUrl", method = RequestMethod.GET)
    public String returnUrl(HttpServletRequest request, HttpServletResponse response)
            throws IOException, AlipayApiException {
        System.out.println("=================================同步回调=====================================");

        // 获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("utf-8"), "utf-8");
            params.put(name, valueStr);
        }

        System.out.println(params);//查看参数都有哪些
        boolean signVerified = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, CHARSET, SIGN_TYPE); // 调用SDK验证签名
        //验证签名通过
        if (signVerified) {
            // 商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");

            // 支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");

            // 付款金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");

            System.out.println("商户订单号=" + out_trade_no);
            System.out.println("支付宝交易号=" + trade_no);
            System.out.println("付款金额=" + total_amount);


            //支付成功，修复支付状态
//             payService.updateById(Integer.valueOf(out_trade_no));
            return "ok";//跳转付款成功页面

        } else {
            return "no";//跳转付款失败页面
        }
    }
}