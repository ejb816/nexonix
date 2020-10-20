package org.wuwei.archon;

import org.wuwei.util.Scriptable.Controller;

/**
 *
 * @author eburke
 */
public abstract class Core  {
	private static Core coreInstance;
	private static Class<?> coreClass;
	
	public static Object core(Object value, boolean force) {
		if (force) {
			if (coreClass == null || value.getClass() == coreClass) {
				coreInstance = new Core(value) {};
				coreClass = value.getClass();
			} else if (value.getClass() != coreClass) {
				throw new ClassCastException();
			}
		} else {
			core(value);
		}
		return coreInstance == null ? null : coreInstance.coreValue;
	}
	
	public static Object core(Object value) {
		if (coreInstance == null) {
			coreInstance = new Core(value) {};
			coreClass = value.getClass();
		}
		return coreInstance == null ? null : coreInstance.coreValue;
	}
	
	public static Object core() {
		return coreInstance == null ? null : coreInstance.coreValue;
	}
	
	private Object coreValue = null;
	private Core(Object value) {
		coreValue = value;
	}
	
	public static interface Deployable {
		public static interface DeployHandler {
			void deploy(Deployable deployed, String error);
		}
		Deployable deploy(Deployable deployable, String options, DeployHandler deployHandler);
	}
	
	public static interface Face extends Deployable {
		Face initialize(String options, Face... faces);
	   	String protocol();
    	Face setProtocol(String name);
 	}
	
	public static void main(String... args) {
	}  
}
