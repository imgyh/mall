package com.imgyh.mall.product.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;

/**
 * @ClassName : ListValueConstraintValidator
 * @Package : com.imgyh.mall.product.valid
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/2/26 17:29
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
// ConstraintValidator<A, T> A 校验的注解 T注解里面值的类型
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {
    HashSet<Integer> set = new HashSet<>();
    // 初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {

        for (int val : constraintAnnotation.vals()) {
            set.add(val);
        }
    }

    // 判断是否校验成功
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(integer);
    }
}
