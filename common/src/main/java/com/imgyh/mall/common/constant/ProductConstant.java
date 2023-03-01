package com.imgyh.mall.common.constant;

/**
 * @ClassName : ProductConstant
 * @Package : com.imgyh.mall.common.constant
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/3/1 14:58
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class ProductConstant {
    public enum AttrType{
        BASE_TYPE(1,"基本属性"),SALE_TYPE(0,"销售属性");
        private Integer code;
        private String msg;

        AttrType(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
