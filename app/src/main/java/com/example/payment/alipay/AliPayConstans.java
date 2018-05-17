package com.example.payment.alipay;


/**
 * 创建人 : skyCracks<br>
 * 创建时间 : 2016-7-18上午10:39:12<br>
 * 版本 :	[v1.0]<br>
 * 类描述 : 支付宝支付所需参数及配置<br>
 */
public class AliPayConstans {
	/** 商户PID */
	public static final String PARTNER = "2088121106562565";
	/** 商户收款账号 */
	public static final String SELLER = "";
	/** 商户私钥，pkcs8格式 */
	public static final String RSA_PRIVATE = "";
	/** 支付宝公钥 */	
	public static final String RSA_PUBLIC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwQZX530IJ9xGJ0m1BKBVLxc70Q3puId6gFXR+7mr593mAz2FgK1JTK7KdyaAulB2qJcPQ6fHiqCfgePlLJsh8rbs2Fwy2xN5g3700Cw4Tl/Fr2s00cAXv3bTzv1MgEv/cUnelaDFSc6Bmzjv22dfOIP9JtSmF5Oeu+R5jyQo4+7mRvi5fmRRhTmrtPBeg9HU8roWXqW4ooiyVDnNRB64MtzgeRdtYQ5YZFvZlL0Vbdl4A65p8ezFi712cGD7ffHI/p0jdX1HyFobu+kEpMH8Jg4XV7oy5lQyav26s+8VLryJ1zWb7wWNiR5Q5DLj4RT0euriEAobIEbZ+lsDXDb8OQIDAQAB";
	/** 支付回调接口,需要服务器端支持 */
	public static final String NOTIFY_URL = "";
}
