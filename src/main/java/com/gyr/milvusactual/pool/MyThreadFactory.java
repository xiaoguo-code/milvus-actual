package com.gyr.milvusactual.pool;

import java.util.concurrent.ThreadFactory;

/**
 * 类描述：线程工厂
 * 
 * @author: sujf
 * @version $Id: meiya-codetemplates.xml,v 1.1 2016/05/10 administrator Exp $
 *
 *          History: 2019年10月24日 下午3:13:54 sujf Created.
 * 
 */
public class MyThreadFactory implements ThreadFactory {

	private String name;

	public MyThreadFactory(String name) {
		super();
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable target) {
		Thread thread = new Thread(target, name);
		return thread;
	}

}
