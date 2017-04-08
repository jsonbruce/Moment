package com.bukeu.moment.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 基础Model对象，实现可序列化和可克隆
 * 
 * @author Max Xu
 * 
 */
public class BaseModel implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Object clone() {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(this);
			out.close();
			
			ByteArrayInputStream bin = new ByteArrayInputStream(
					bout.toByteArray());
			ObjectInputStream in = new ObjectInputStream(bin);
			Object ret = in.readObject();
			in.close();
			
			return ret;
		} catch (Exception e) {
			return null;
		}
	}
	
}
