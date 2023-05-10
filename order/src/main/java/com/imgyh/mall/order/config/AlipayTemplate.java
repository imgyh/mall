package com.imgyh.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.imgyh.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000122682902";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDORyYNVqUXvxBgsZkVVeBijJOliqB7Nax4NS3p5PSHLyWFqAXxTapxQJUkRHBvqVLxEPD/vpNxDbC2GjmH3QMUJvJ5yCirdsCsOJuNY5zBiz4D+/v5AsRfu2hj9IaIVSnTjl/og60WkHW53MNBsZ6oxCADBhgDh5kWeqWwb3ruYv/aG//Y66a30x5hmllh1v8j7p3bgftSScLkUiwkWU8WUVJ4CiOM4msRBqrApOYtPW7j+RlK44H2t8ETXYOleP9p2nYcC3pi3nD8UkweRITaKcVB766XNeLBOy51nAlPT+1jLaZNTV8VWFmg7HyeiVBF9Hhr3jUX3PzNzQ+Q3qDlAgMBAAECggEBALkJ39uVPvFnRmTGy6D08RzWJ9gW29DKnBUU90PaS3Q82bbku7fzSJLhMShgcE/qH2n6HYX8ybxfYIZqTfNPUeSw71BJOhpdo4aaHQefNM31f0MJtsVwMwIBVipb/pardqK9E9G61Pjpy4FXxJvYno+bp2+u9rmr2KruiFUWG7tc26SzYbB6OSAo6rPOhltE1NNxXSJ0yocGDQT0hTNeKR4kXSwBMJzoD1+6czfU1PDyk1ZDwff/rUdpr0YFXh3N/aVku326kNsjhbBpbD9cIuU7UCauXUX26OyklLjILUR3gvJpW8MXKxi+8hjSFOoZe6wU7+wrpa48FmoVrusNIiECgYEA+XSGUt5kWUTE5prpg3GXALgdaVgUxXb03Kt7Uk7UJelwK4Qu4E1Dh93MKRFACEvh6GaBXdljfwFNQJt1ajNL3eM/7LYjDFjMIEQUc730FgSTSH2WQAF381O9a6epPW1CwZBtHMmicsw0FiJrVv4KgEYtYtilIAvaEorVft9wyM0CgYEA07CfTUrPO5pQ53Y/gNjUkiTYiAWAerdWteq6r1n783XmIeUzq6lhlTTiUGGyCOicJmurk5duBYyaHaxqWi83P72KAd+ii53k3hhR7PGZEferiAGRW5SI8NhJ9SMg2rY6aKt346xno9Jo/yRG/RpHClNxhqQC/Hhm0rzOnoqumHkCgYBKRN8ZHQ1TZp4Y6lZLxC3pEOfwzUCh6eDNMmDtCHsxLmcvDZPHRDmu4eI+99vQnIcN4jbx8h8lWQXfDmnOzKbx0Q7fMvsMWgOsN5i/PPbG92XCV2G8fLNxxqTGwIwqtbUCXDWgNIghFZsMXAMJS2lk93/bxhSjjePHUX5u36d8oQKBgQDPkX8SZck/szejTdJEv9fnmRNshKCPM5hldk0pRX2EItXN5JU+vC5ABIPot3K/w33hOnAZn0E1VcGEWRxLY8Kz0GEzJxqV9fOFRqmGQPuLn6NlD8x81O9V4W7bXF+JtWceXQuIhfN1sgpcKzEkWpDP8jP+BnIbJuADwTiEQo2iWQKBgCEI7k5oKtYBgkUcOF0PJz6WPDL1SE+QYPu+r6nuF3ICdbF6eO8GxX5YFOWQXh2ajT1InwiGqEssablKbHv5bY4k/ZOsSw3HBJOQ+6Cr4Y9zcAnZ3mvxG0smipZbK5X0FC0Y8b/JCLEPgoI4J+q7SAFra56WNIJ5jQslJ2NyB4BE";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhz9kbm9VuSaQvEOJ+LuIRFKFdG4hTyAhjhMfI+fBfez2e/0LLXy4XKJkkJ7s6iMFl0Bm7SrqC/Gv4LnMScfqcZZdA/jR5KEbVmLtYf0DnJQ1uDiSqdb30NEiQcVe2JQIn64NAypAKlWsntEOXKdXqyKPVCBpltu+oRKCYRzwQr1JeK1ccTH5prDOBY0j9SpimYZrhfLb9e7LsrlzzP388LUQzQ4+nQNc+aFurz42lUEzqPNhwtxpChHn0tLkd6x2yLhASpzT0fWG/F8LHlmPNKv5enBqsq/Xe/VJhq21IBzn/DMgru4d+CYG37hYG0Y5EI6hlzH2+bmNJ0QG1K6KwQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url = "http://order.mall.gyh.im/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://order.mall.gyh.im/list.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    private String timeout = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
