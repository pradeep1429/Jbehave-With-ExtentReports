package com.mtaf.framework.utils;


import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean2;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.BigIntegerConverter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.StringConverter;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BeanUtilFactory {

    private static BeanUtilsBean beanUtilsBean;

    private BeanUtilFactory() {
    }

    public static BeanUtilsBean getBeanUtilsInstance() {
        if (beanUtilsBean == null) {
            beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean2());
            beanUtilsBean.getConvertUtils().register(new IntegerConverter(null), Integer.class);
            beanUtilsBean.getConvertUtils().register(new DoubleConverter(null), Double.class);
            beanUtilsBean.getConvertUtils().register(new LongConverter(null), Long.class);
            beanUtilsBean.getConvertUtils().register(new BooleanConverter(null), Boolean.class);
            beanUtilsBean.getConvertUtils().register(new BigIntegerConverter(null), BigInteger.class);
            beanUtilsBean.getConvertUtils().register(new BigDecimalConverter(null), BigDecimal.class);
            beanUtilsBean.getConvertUtils().register(new ArrayConverter(String[].class, new StringConverter()), String[].class);
            beanUtilsBean.getConvertUtils().register(new CustomStringConverter(), String.class);
        }
        return beanUtilsBean;
    }
}
