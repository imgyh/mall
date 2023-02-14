package com.imgyh.mall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:54:58
 */
@Data
@TableName("undo_log")
public class UndoLogEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Long branchId;
	/**
	 * 
	 */
	private String xid;
	/**
	 * 
	 */
	private String context;
	/**
	 * Longblob 改成了 byte[], 生成代码时类型未转换上
	 *  BLOB (binary large object)二进制大对象，是一个可以存储二进制文件的容器。
	 *  MySQL中BLOB是个类型系列，包括：TinyBlob、Blob、MediumBlob、LongBlob，这几个类型之间的唯一区别是在存储文件的最大大小上不同。
	 * 　　类型         大小(单位：字节)
	 * 　　TinyBlob     最大255
	 * 　　Blob         最大65K
	 * 　　MediumBlob   最大16M
	 * 　　LongBlob     最大4G
	 */
	private byte[] rollbackInfo;
	/**
	 * 
	 */
	private Integer logStatus;
	/**
	 * 
	 */
	private Date logCreated;
	/**
	 * 
	 */
	private Date logModified;
	/**
	 * 
	 */
	private String ext;

}
